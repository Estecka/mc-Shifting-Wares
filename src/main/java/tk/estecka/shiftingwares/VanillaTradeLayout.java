package tk.estecka.shiftingwares;

import java.util.ArrayList;
import java.util.List;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.TradeOffers.Factory;

public class VanillaTradeLayout
implements ITradeLayoutProvider
{
	public Factory[][]	GetTradeLayout(VillagerEntity villager){
		List<Factory[]> layout = new ArrayList<>();
		VillagerProfession job = villager.getVillagerData().getProfession();
		int jobLevel = villager.getVillagerData().getLevel();
		Int2ObjectMap<Factory[]> jobPool = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(job);

		if (jobPool == null){
			ShiftingWares.LOGGER.error("No trade pool for job {}.", job);
			return null;
		}

		for (int lvl=VillagerData.MIN_LEVEL; lvl<=jobLevel; ++lvl)
		{
			var pool = jobPool.get(lvl);
			if (pool == null)
				ShiftingWares.LOGGER.error("Missing pool for job {} lvl.{}", job, lvl);
			else for (int i=0; i<2 && i<pool.length; ++i)
				layout.add(pool);
		}

		return layout.toArray(new Factory[0][]);
	}

}
