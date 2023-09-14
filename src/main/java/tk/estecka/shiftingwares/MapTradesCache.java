package tk.estecka.shiftingwares;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOfferList;

public class MapTradesCache {
	static public final String MAPID_CACHE = "ownedMaps";

	static public void	FillFromTrades(Map<Integer,Integer> cacheMap, TradeOfferList offers){
		for (int i=0; i<offers.size(); ++i){
			ItemStack mapItem = offers.get(i).getSellItem();
			if (!mapItem.isOf(Items.FILLED_MAP))
				continue;

			Integer mapId = FilledMapItem.getMapId(mapItem);
			if (mapId == null){
				ShiftingWares.LOGGER.error("ItemStack in slot {} has no Map ID: {} ", i, mapItem);
				continue;
			}
			if (cacheMap.containsKey(i) && cacheMap.get(i)!=mapId)
				ShiftingWares.LOGGER.warn("Overwritting previously known map for trade in slot {}: {}=>{}", i, cacheMap.get(i), mapId);
			cacheMap.put(i, mapId);
		}
	}

	static public Map<String,Integer>	ReadMapFromNbt(NbtCompound nbt){
		var r = new HashMap<String,Integer>();
		var nbtmap = nbt.getCompound(MAPID_CACHE);
		if (nbtmap == null)
			return r;
		for (var tag : nbtmap.getKeys())
			r.put(tag, nbtmap.getInt(tag));
		return r;
	}

	static public NbtCompound	WriteMapToNbt(NbtCompound nbt, Map<String,Integer> map){
		NbtCompound nbtmap = new NbtCompound();
		for (var pair : map.entrySet())
			nbtmap.putInt(pair.getKey(), pair.getValue());
		nbt.put(MAPID_CACHE, nbtmap);
		return nbt;
	}
}
