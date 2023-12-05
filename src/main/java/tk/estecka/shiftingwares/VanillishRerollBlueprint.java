package tk.estecka.shiftingwares;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.TradeOffers.Factory;;

public class VanillishRerollBlueprint
implements IRerollBlueprint
{
	public IntList	GetSlotLevels(VillagerEntity villager){
		IntList slotLevels = new IntArrayList();
		Int2ObjectMap<Factory[]> jobPool = this.GetTradePools(villager);
		VillagerProfession job = villager.getVillagerData().getProfession();
		int jobLevel = villager.getVillagerData().getLevel();

		if (jobPool == null){
			ShiftingWares.LOGGER.error("No trade pool for job {}.", job);
			return null;
		}

		for (int lvl=VillagerData.MIN_LEVEL; lvl<=jobLevel; ++lvl)
		if (jobPool.containsKey(lvl))
		{
			var pool = jobPool.get(lvl);
			if (pool == null)
				ShiftingWares.LOGGER.error("Missing pool for job {} lvl.{}", job, jobLevel);
			else for (int i=0; i<2 && i<pool.length; ++i)
				slotLevels.add(lvl);
		}

		return slotLevels;
	}

	public Int2ObjectMap<Factory[]> GetTradePools(VillagerEntity villager){
		return TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(villager.getVillagerData().getProfession());
	}
}
