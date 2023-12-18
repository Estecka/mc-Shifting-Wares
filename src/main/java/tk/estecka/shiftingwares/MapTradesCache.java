package tk.estecka.shiftingwares;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.gen.structure.Structure;

public class MapTradesCache 
{
	static public final String MAPID_CACHE = "shifting-wares:created_maps";
	static public final Map<String, TagKey<Structure>> NAME_TO_STRUCT = new HashMap<>();

	private final Map<String,ItemStack> cachedItems = new HashMap<String,ItemStack>();

	static {
		// NAME_TO_STRUCT's initialization is loosely based on the instantiation
		// of new SellMapFactories.
		new TradeOffers();
	}

	public Optional<ItemStack>	GetCachedMap(String key){
		if (this.cachedItems.containsKey(key))
			return Optional.of(this.cachedItems.get(key));
		else
			return Optional.empty();
	}

	public void	AddCachedMap(String key, ItemStack mapItem){
		Integer neoId=FilledMapItem.getMapId(mapItem);
		if (cachedItems.containsKey(key)){
			Integer oldId=FilledMapItem.getMapId(cachedItems.get(key));
			if (!neoId.equals(oldId))
				ShiftingWares.LOGGER.error("Overwriting an existing map: #{}->#{} @ {}", oldId, neoId, key);
			else if (ItemStack.areEqual(mapItem, cachedItems.get(key)))
				ShiftingWares.LOGGER.warn("Updating a villager's existing map#{} @ {}", neoId, key);
		}
		else
			ShiftingWares.LOGGER.info("New map {}#{}", key, neoId);

		cachedItems.put(key, mapItem);
	}

	/**
	 * Scans the trade list for maps that might not have been cached.
	 * This is  only  really useful  the first time  a villager  is loaded after
	 * installing the mod. Otherwise, it's paranoid safeguard.
	 */
	public void	FillCacheFromTrades(TradeOfferList offers){
		for (int i=0; i<offers.size(); ++i)
		{
			ItemStack sellItem = offers.get(i).getSellItem();
			if (!sellItem.isOf(Items.FILLED_MAP))
				continue;

			if (!sellItem.hasCustomName()){
				ShiftingWares.LOGGER.error("Unable to identify map#{} with no name in slot {}:\n{}", FilledMapItem.getMapId(sellItem), i, sellItem);
				continue;
			}

			TextContent fullName = sellItem.getName().getContent();
			String nameKey;
			if (fullName instanceof TranslatableTextContent)
				nameKey = ((TranslatableTextContent)fullName).getKey();
			else {
				ShiftingWares.LOGGER.error("Map name is not a translatation key: {}", fullName);
				nameKey = sellItem.getName().getString();
			}

			if (NAME_TO_STRUCT.containsKey(nameKey))
				nameKey = NAME_TO_STRUCT.get(nameKey).id().toString();
			else
				ShiftingWares.LOGGER.error("Unable to identify map name: {}", nameKey);

			var oldItem = this.GetCachedMap(nameKey);
			if (oldItem.isEmpty() || !ItemStack.areEqual(sellItem, oldItem.get())){
				ShiftingWares.LOGGER.warn("Caught a map trade that wasn't properly cached: #{} @ {}", FilledMapItem.getMapId(sellItem), nameKey);
				this.AddCachedMap(nameKey, sellItem);
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
