package tk.estecka.shiftingwares.TradeLayouts;

import java.util.ArrayList;
import java.util.List;
import me.drex.villagerconfig.VillagerConfig;
import me.drex.villagerconfig.data.TradeTable;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.TradeOffers.Factory;
import tk.estecka.shiftingwares.ShiftingWares;

public class VillagerConfigTradeLayout
implements ITradeLayoutProvider
{
	public List<Factory[]>	GetTradeLayout(VillagerEntity villager){
		List<Factory[]> layout = new ArrayList<>();
		VillagerProfession job = villager.getVillagerData().getProfession();
		int jobLevel = villager.getVillagerData().getLevel();
		TradeTable table = VillagerConfig.TRADE_MANAGER.getTrade(Registries.VILLAGER_PROFESSION.getId(job));

		if (table == null){
			ShiftingWares.LOGGER.error("No trade table for job {}.", job);
			return null;
		}

		for (int lvl=VillagerData.MIN_LEVEL; lvl<=jobLevel; ++lvl)
		for (Factory offer : table.getTradeOffers(villager, lvl)) {
			layout.add(new Factory[]{ offer });
		}

		return layout;
	}

}
