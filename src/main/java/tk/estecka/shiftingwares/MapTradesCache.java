package tk.estecka.shiftingwares;

import java.util.Map;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.village.TradeOfferList;

public class MapTradesCache 
{
	static public final String MAPID_CACHE = "shifting-wares:created_maps";

	static public void	FillCacheFromTrades(IVillagerEntityDuck villager){
		TradeOfferList offers = villager.getOffers();
		for (int i=0; i<offers.size(); ++i){
			ItemStack sellItem = offers.get(i).getSellItem();
			if (!sellItem.isOf(Items.FILLED_MAP))
				continue;

			if (!sellItem.hasCustomName()){
				ShiftingWares.LOGGER.error("Unable to identify map#{} with no name (slot {})", FilledMapItem.getMapId(sellItem), i);
				continue;
			}

			TextContent fullName = sellItem.getName().getContent();
			String nameKey;
			if (fullName instanceof TranslatableTextContent)
				nameKey = ((TranslatableTextContent)fullName).getKey();
			else {
				ShiftingWares.LOGGER.warn("Map name is not a translatation key: {}", fullName);
				nameKey = sellItem.getName().getString();
			}

			var oldItem = villager.GetCachedMap(nameKey);
			if (oldItem.isPresent() && !ItemStack.areEqual(sellItem, oldItem.get())){
				ShiftingWares.LOGGER.warn("Caught a map trade that wasn't properly cached: #{} @ {}", FilledMapItem.getMapId(sellItem), villager);
				villager.AddCachedMap(nameKey, sellItem);
			}
		}
	}

	static public Map<String,ItemStack>	ReadMapCacheFromNbt(NbtCompound nbt, Map<String, ItemStack> map){
		var nbtmap = nbt.getCompound(MAPID_CACHE);
		if (nbtmap == null)
			return map;

		for (String key : nbtmap.getKeys()){
			map.put(key, ItemStack.fromNbt(nbtmap.getCompound(key)));
		}
		return map;
	}

	static public NbtCompound	WriteMapCacheToNbt(NbtCompound nbt, Map<String, ItemStack> map){
		NbtCompound nbtmap = new NbtCompound();
		for (var pair : map.entrySet())
			nbtmap.put(pair.getKey(), pair.getValue().writeNbt(new NbtCompound()));
		nbt.put(MAPID_CACHE, nbtmap);
		return nbt;
	}
}
