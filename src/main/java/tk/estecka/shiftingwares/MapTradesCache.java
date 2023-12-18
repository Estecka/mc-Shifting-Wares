package tk.estecka.shiftingwares;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.passive.VillagerEntity;
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

	static {
		new TradeOffers(); // NAME_TO_STRUCT init
	}

	static public void	FillCacheFromTrades(VillagerEntity villager){
		IVillagerEntityDuck villagerMixin = (IVillagerEntityDuck)villager;
		TradeOfferList offers = villager.getOffers();
		for (int i=0; i<offers.size(); ++i)
		{
			ItemStack sellItem = offers.get(i).getSellItem();
			if (!sellItem.isOf(Items.FILLED_MAP))
				continue;

			if (!sellItem.hasCustomName()){
				ShiftingWares.LOGGER.error("Unable to identify map#{} with no name in slot {} of {}\n{}", FilledMapItem.getMapId(sellItem), i, villager, sellItem);
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

			var oldItem = villagerMixin.shiftingwares$GetCachedMap(nameKey);
			if (oldItem.isEmpty() || !ItemStack.areEqual(sellItem, oldItem.get())){
				ShiftingWares.LOGGER.warn("Caught a map trade that wasn't properly cached: #{} @ {}", FilledMapItem.getMapId(sellItem), villagerMixin);
				villagerMixin.shiftingwares$AddCachedMap(nameKey, sellItem);
			}
		}
	}

	static public Map<String,ItemStack>	ReadMapCacheFromNbt(NbtCompound nbt, Map<String, ItemStack> map){
		NbtCompound nbtmap = nbt.getCompound(MAPID_CACHE);
		if (nbtmap == null)
			return map;

		for (String key : nbtmap.getKeys()){
			if (NAME_TO_STRUCT.containsKey(key)){
				ShiftingWares.LOGGER.info("Converted an old cached map ({})", key);
				key = NAME_TO_STRUCT.get(key).id().toString();
			}
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
