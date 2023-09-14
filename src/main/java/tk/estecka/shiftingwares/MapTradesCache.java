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

	static public void	FillCacheFromTrades(Map<String,ItemStack> cacheMap, TradeOfferList offers){
		for (int i=0; i<offers.size(); ++i){
			ItemStack mapItem = offers.get(i).getSellItem();
			if (!mapItem.isOf(Items.FILLED_MAP))
				continue;

			if (!mapItem.hasCustomName()){
				ShiftingWares.LOGGER.error("Unable to identify map#{} with no name (slot {})", FilledMapItem.getMapId(mapItem), i);
				continue;
			}

			TextContent fullName = mapItem.getName().getContent();
			String nameKey;
			if (fullName instanceof TranslatableTextContent)
				nameKey = ((TranslatableTextContent)fullName).getKey();
			else {
				ShiftingWares.LOGGER.warn("Map name is not a translatation key: {}", fullName);
				nameKey = mapItem.getName().getString();
			}

			if (cacheMap.containsKey(nameKey)){
				Integer oldId=FilledMapItem.getMapId(cacheMap.get(nameKey));
				Integer neoId=FilledMapItem.getMapId(mapItem);
				if (!neoId.equals(oldId))
					ShiftingWares.LOGGER.error("Overwriting a villager's existing map: {}->{} @ {}", oldId, neoId, nameKey);
				else if (!ItemStack.areEqual(mapItem, cacheMap.get(nameKey)))
					ShiftingWares.LOGGER.warn("Updating a villager's existing map#{} in slot", oldId, i);
			}

			cacheMap.put(nameKey, mapItem);
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
