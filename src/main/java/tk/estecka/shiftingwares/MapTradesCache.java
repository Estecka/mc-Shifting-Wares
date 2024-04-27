package tk.estecka.shiftingwares;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Dynamic;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.minecraft.datafixer.fix.ItemStackCustomNameToItemNameFix;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import tk.estecka.shiftingwares.api.PersistentItemCache;

public class MapTradesCache
implements PersistentItemCache
{
	static public final int DATA_FORMAT = 2;
	static public final String FORMAT_KEY  = "shifting-wares:data_format";
	static public final String MAPID_CACHE = "shifting-wares:created_maps";
	static public final String SOLD_ITEMS  = "shifting-wares:sold_items";

	private final Map<String, ItemStack> cachedItems = new HashMap<String,ItemStack>();
	private final Set<String> soldItems = new HashSet<>();

	static public @Nullable Integer GetRawMapId(ItemStack stack){
		MapIdComponent component = stack.get(DataComponentTypes.MAP_ID);
		return (component==null) ? null : component.id();
	}

	/**
	 * @return null if the item needs not or cannot be cached.
	 */
	static public @Nullable String	FindCacheKey(ItemStack item){
		if (!item.isOf(Items.FILLED_MAP))
			return null;

		Text name = item.get(DataComponentTypes.ITEM_NAME);
		if (name == null){
			name = item.get(DataComponentTypes.CUSTOM_NAME);
			if (name != null)
				ShiftingWares.LOGGER.warn("A map was found that uses a CUSTOM_NAME but has no ITEM_NAME: \"{}\"", name);
		}

		if (name == null){
			ShiftingWares.LOGGER.error("Unable to identify map#{} with no name:\n{}", GetRawMapId(item), item);
			return null;
		}

		TextContent fullName = name.getContent();
		if (fullName instanceof TranslatableTextContent translatable)
			return translatable.getKey();
		else {
			ShiftingWares.LOGGER.error("Map#{} name is not a translation key: {} {}", GetRawMapId(item), fullName.getClass(), fullName);
			return item.getName().getString();
		}

	}

	/**
	 * @return Empty if the entity is allowed to forget the item, or does not
	 * remember it. Otherwise, returns the corresponding cached item.
	 */
	static public Optional<ItemStack> Resell(Entity entity, String cacheKey){
		if (entity instanceof IVillagerEntityDuck villager) 
		{
			MapTradesCache cache =  villager.shiftingwares$GetItemCache();
			Optional<ItemStack> cachedMap = cache.GetCachedItem(cacheKey);
			if (cachedMap.isEmpty() || (cache.HasSold(cacheKey) && entity.getWorld().getGameRules().getBoolean(ShiftingWares.MAP_RULE)))
				return Optional.empty();
			else
				return cachedMap;
		}

		return Optional.empty();
	}

	public Optional<ItemStack>	GetCachedItem(String key){
		ItemStack item = this.cachedItems.get(key);
		if (item != null)
			return Optional.of(this.cachedItems.get(key));
		else
			return Optional.empty();
	}

	public void	AddCachedItem(String key, ItemStack mapItem){
		Integer neoId = GetRawMapId(mapItem);
		if (!cachedItems.containsKey(key))
			ShiftingWares.LOGGER.info("New map trade: #{} @ {}", neoId, key);
		else
		{
			Integer oldId = GetRawMapId(cachedItems.get(key));
			if (soldItems.contains(key))
				ShiftingWares.LOGGER.info("New map trade #{}->#{} @ {}", oldId, neoId, key);
			else if (Objects.equals(neoId, oldId))
				ShiftingWares.LOGGER.warn("Updating existing map trade #{} @ {}", neoId, key);
			else
				ShiftingWares.LOGGER.error("Overwriting existing map trade: #{}->#{} @ {}", oldId, neoId, key);
		}

		cachedItems.put(key, mapItem);
		soldItems.remove(key);
	}

	public boolean HasSold(String key){
		return soldItems.contains(key);
	}

	/**
	 * 1. Scans the trade list for maps that might not have been cached.
	 * This is  only  really useful  the first time  a villager  is loaded after
	 * installing the mod. The rest of the time, it's paranoid safeguard.
	 * 
	 * 2. Detects maps  that  have been  sold  at least once, and marks them  as
	 * potentially re-rollable.
	 */
	public void	FillCacheFromTrades(TradeOfferList offers){
		for (TradeOffer offer : offers) 
		{
			ItemStack sellItem = offer.getSellItem();
			String cacheKey = FindCacheKey(sellItem);
			if (cacheKey == null)
				continue;

			if (offer.hasBeenUsed()) {
				this.soldItems.add(cacheKey);
				ShiftingWares.LOGGER.info("Marked map as sold: #{} @ {}", GetRawMapId(sellItem), cacheKey);
			}

			var oldItem = this.GetCachedItem(cacheKey);
			if (oldItem.isEmpty() || !ItemStack.areEqual(sellItem, oldItem.get())){
				ShiftingWares.LOGGER.warn("Caught a map trade that wasn't properly cached: #{} @ {}", GetRawMapId(sellItem), cacheKey);
				this.AddCachedItem(cacheKey, sellItem);
			}
		}
	}


/******************************************************************************/
/* # Serialization                                                            */
/******************************************************************************/

	// 1.20.4 -> 1.20.5 upgrade
	static public Dynamic<?> ComponentizeLegacyItem(Dynamic<?> dynamic){
		var optStackData = ItemStackComponentizationFix.StackData.fromDynamic(dynamic);
		if (optStackData.isEmpty())
			return dynamic;

		var stackData = optStackData.get();
		ItemStackComponentizationFix.fixStack(optStackData.get(), dynamic);
		dynamic = stackData.finalize();
	
		var components = dynamic.get("components").result();
		if (components.isPresent()){
			dynamic = dynamic.set("components", ItemStackCustomNameToItemNameFix.fixExplorerMaps(components.get()));
		}

		return dynamic;
	}


	public void	ReadMapCacheFromNbt(NbtCompound nbt){
		NbtCompound nbtcache = nbt.getCompound(MAPID_CACHE);
		NbtList nbtsold = nbt.getList(SOLD_ITEMS, NbtElement.STRING_TYPE);
		int format = nbt.getInt(FORMAT_KEY);

		if (nbtcache != null)
		for (String key : nbtcache.getKeys()){
			Dynamic<?> dynamic = new Dynamic<>(NbtOps.INSTANCE, nbtcache.getCompound(key));
			if (format < 2)
				dynamic = ComponentizeLegacyItem(dynamic);

			ItemStack.CODEC.parse(dynamic)
				.resultOrPartial(err -> ShiftingWares.LOGGER.error("Unabled to decode cached item! @{}\n", key, err))
				.ifPresent( item -> this.cachedItems.put(key, item))
				;
		}

		if (nbtsold != null)
		for (int i=0; i<nbtsold.size(); ++i){
			String key = nbtsold.getString(i);
			this.soldItems.add(key);
		}
	}

	public NbtCompound	WriteMapCacheToNbt(NbtCompound nbt){
		NbtCompound nbtcache = new NbtCompound();
		NbtList nbtsold = new NbtList();

		for (var pair : this.cachedItems.entrySet()){
			ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, pair.getValue())
				.resultOrPartial(err -> ShiftingWares.LOGGER.error("Unable to encode cached item! @{}\n{}", pair.getKey(), err))
				.ifPresent(nbtItem -> nbtcache.put(pair.getKey(), nbtItem))
				;
		}
		for (String key : this.soldItems)
			nbtsold.add(NbtString.of(key));

		nbt.putInt(FORMAT_KEY, DATA_FORMAT);
		if (!nbtcache.isEmpty()) nbt.put(MAPID_CACHE, nbtcache);
		if (!nbtsold.isEmpty() ) nbt.put(SOLD_ITEMS, nbtsold);

		return nbt;
	}

}
