package tk.estecka.shiftingwares;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.gen.structure.Structure;

public class MapTradesCache 
{
	static public final String MAPID_CACHE = "shifting-wares:created_maps";
	static public final String SOLD_ITEMS  = "shifting-wares:sold_items";

	static public final Map<String, TagKey<Structure>> NAME_TO_STRUCT = new HashMap<>();

	private final Map<String, ItemStack> cachedItems = new HashMap<String,ItemStack>();
	private final Set<String> soldItems = new HashSet<>();

	static {
		// NAME_TO_STRUCT's initialization is loosely based on the instantiation
		// of new SellMapFactories.
		new TradeOffers();
	}

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
		if (fullName instanceof TranslatableTextContent)
			key = ((TranslatableTextContent)fullName).getKey();
		else {
			ShiftingWares.LOGGER.error("Map name is not a translatation key: {}", fullName);
			key = item.getName().getString();
		}

		if (NAME_TO_STRUCT.containsKey(key))
			key = NAME_TO_STRUCT.get(key).id().toString();
		else
			ShiftingWares.LOGGER.error("Unable to identify map name: {}", key);

		return key;
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
			ShiftingWares.LOGGER.info("New map trade: #{} @	{}", neoId, key);
		else
		{
			Integer oldId=FilledMapItem.getMapId(cachedItems.get(key));
			if (soldItems.contains(key))
				ShiftingWares.LOGGER.info("New map trade #{}->#{} @ {}", oldId, neoId, key);
			else if (!neoId.equals(oldId))
				ShiftingWares.LOGGER.error("Overwriting existing map trade: #{}->#{} @ {}", oldId, neoId, key);
			else
				ShiftingWares.LOGGER.warn("Updating existing map trade #{} @ {}", neoId, key);
		}

		cachedItems.put(key, mapItem);
		soldItems.remove(key);
	}

	public boolean CanForgetCache(String key){
		return soldItems.contains(key) || !cachedItems.containsKey(key);
	}

	/**
	 * 1. Scans the trade list for maps that might not have been cached.
	 * This is  only  really useful  the first time  a villager  is loaded after
	 * installing the mod. The rest of the time, it's paranoid safeguard.
	 * 
	 * 2. Detects maps  that  have been  sold  at least once, and marks them  as
	 * potentiallyy re-rollable.
	 */
	public void	FillCacheFromTrades(TradeOfferList offers){
		for (TradeOffer offer : offers) 
		{
			ItemStack sellItem = offer.getSellItem();
			String cacheKey = FindCacheKey(sellItem);

			if (offer.hasBeenUsed()) {
				this.soldItems.add(cacheKey);
			}
			else {
				var oldItem = this.GetCachedMap(cacheKey);
				if (oldItem.isEmpty() || !ItemStack.areEqual(sellItem, oldItem.get())){
					ShiftingWares.LOGGER.warn("Caught a map trade that wasn't properly cached: #{} @ {}", FilledMapItem.getMapId(sellItem), cacheKey);
					this.AddCachedMap(cacheKey, sellItem);
				}
			}
		}
	}

	public void	ReadMapCacheFromNbt(NbtCompound nbt){
		NbtCompound nbtmap = nbt.getCompound(MAPID_CACHE);
		if (nbtmap == null)
			return;

		for (String key : nbtmap.getKeys()){
			ItemStack item = ItemStack.fromNbt(nbtmap.getCompound(key));
			if (NAME_TO_STRUCT.containsKey(key)){
				ShiftingWares.LOGGER.info("Converted an old cached map ({})", key);
				key = NAME_TO_STRUCT.get(key).id().toString();
			}
			this.cachedItems.put(key, item);
		}
	}

	public NbtCompound	WriteMapCacheToNbt(NbtCompound nbt){
		NbtCompound nbtmap = new NbtCompound();
		for (var pair : this.cachedItems.entrySet())
			nbtmap.put(pair.getKey(), pair.getValue().writeNbt(new NbtCompound()));

		nbt.put(MAPID_CACHE, nbtmap);
		return nbt;
	}
}
