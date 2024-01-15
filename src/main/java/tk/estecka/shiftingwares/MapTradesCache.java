package tk.estecka.shiftingwares;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

public class MapTradesCache 
{
	static public final int DATA_FORMAT = 1;
	static public final String FORMAT_KEY  = "shifting-wares:data_format";
	static public final String MAPID_CACHE = "shifting-wares:created_maps";
	static public final String SOLD_ITEMS  = "shifting-wares:sold_items";

	private final Map<String, ItemStack> cachedItems = new HashMap<String,ItemStack>();
	private final Set<String> soldItems = new HashSet<>();

	/**
	 * @return null if the item needs not or cannot be cached.
	 */
	@Nullable
	static public String	FindCacheKey(ItemStack item){
		if (!item.isOf(Items.FILLED_MAP))
			return null;

		if (!item.hasCustomName()){
			ShiftingWares.LOGGER.error("Unable to identify map#{} with no name:\n{}", FilledMapItem.getMapId(item), item);
			return null;
		}

		TextContent fullName = item.getName().getContent();
		String key;
		if (fullName instanceof TranslatableTextContent translatable)
			key = translatable.getKey();
		else {
			ShiftingWares.LOGGER.error("Map#{} name is not a translation key: {} {}", FilledMapItem.getMapId(item), fullName.getClass(), fullName);
			key = item.getName().getString();
		}

		return key;
	}

	/**
	 * @return Empty if the entity is allowed to forget the item, or does not
	 * remember it. Otherwise, returns the corresponding cached item.
	 */
	static public Optional<ItemStack> Resell(Entity entity, String cacheKey){
		if (entity instanceof IVillagerEntityDuck villager) 
		{
			MapTradesCache cache =  villager.shiftingwares$GetTradeCache();
			Optional<ItemStack> cachedMap = cache.GetCachedMap(cacheKey);
			if (cachedMap.isEmpty() || (cache.HasSold(cacheKey) && entity.getWorld().getGameRules().getBoolean(ShiftingWares.MAP_RULE)))
				return Optional.empty();
			else
				return cachedMap;
		}

		return Optional.empty();
	}

	public Optional<ItemStack>	GetCachedMap(String key){
		if (this.cachedItems.containsKey(key))
			return Optional.of(this.cachedItems.get(key));
		else
			return Optional.empty();
	}

	public void	AddCachedMap(String key, ItemStack mapItem){
		Integer neoId=FilledMapItem.getMapId(mapItem);
		if (!cachedItems.containsKey(key))
			ShiftingWares.LOGGER.info("New map trade: #{} @ {}", neoId, key);
		else
		{
			Integer oldId=FilledMapItem.getMapId(cachedItems.get(key));
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
				ShiftingWares.LOGGER.info("Marked map as sold: #{} @ {}", FilledMapItem.getMapId(sellItem), cacheKey);
			}

			var oldItem = this.GetCachedMap(cacheKey);
			if (oldItem.isEmpty() || !ItemStack.areEqual(sellItem, oldItem.get())){
				ShiftingWares.LOGGER.warn("Caught a map trade that wasn't properly cached: #{} @ {}", FilledMapItem.getMapId(sellItem), cacheKey);
				this.AddCachedMap(cacheKey, sellItem);
			}
		}
	}


/******************************************************************************/
/* # Serialization                                                            */
/******************************************************************************/

	public void	ReadMapCacheFromNbt(NbtCompound nbt){
		NbtCompound nbtcache = nbt.getCompound(MAPID_CACHE);
		NbtList nbtsold = nbt.getList(SOLD_ITEMS, NbtElement.STRING_TYPE);

		if (nbtcache != null)
		for (String key : nbtcache.getKeys()){
			ItemStack item = ItemStack.fromNbt(nbtcache.getCompound(key));
			this.cachedItems.put(key, item);
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

		for (var pair : this.cachedItems.entrySet())
			nbtcache.put(pair.getKey(), pair.getValue().writeNbt(new NbtCompound()));
		for (String key : this.soldItems)
			nbtsold.add(NbtString.of(key));

		nbt.putInt(FORMAT_KEY, DATA_FORMAT);
		if (!nbtcache.isEmpty()) nbt.put(MAPID_CACHE, nbtcache);
		if (!nbtsold.isEmpty() ) nbt.put(SOLD_ITEMS, nbtsold);

		return nbt;
	}

}
