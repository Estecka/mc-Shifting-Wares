package tk.estecka.shiftingwares.TradeLayouts;

import java.util.ArrayList;
import java.util.List;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.TradeOffers.Factory;
import tk.estecka.shiftingwares.ShiftingWares;
import tk.estecka.shiftingwares.api.ITradeLayoutProvider;

public class VanillaTradeLayout
implements ITradeLayoutProvider
{
	static public final Identifier TRADE_REBALANCE_FLAGID = Identifier.of("minecraft", "trade_rebalance");
	static public final boolean IS_EXP_TRADE_AVAILABLE;

	static {
		var featureSet = FeatureFlags.FEATURE_MANAGER.getFeatureSet();
		var featureIds = FeatureFlags.FEATURE_MANAGER.toId(featureSet);
		IS_EXP_TRADE_AVAILABLE = featureIds.contains(TRADE_REBALANCE_FLAGID);
	}

	public List<Factory[]>	GetTradeLayout(VillagerEntity villager){
		List<Factory[]> layout = new ArrayList<>();
		VillagerProfession job = villager.getVillagerData().getProfession();
		int jobLevel = villager.getVillagerData().getLevel();

		Int2ObjectMap<Factory[]> jobPool = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(job);
		if (IS_EXP_TRADE_AVAILABLE 
		&& villager.getWorld().getEnabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)
		&& TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE.containsKey(job)
		) {
			jobPool = TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE.get(job);
		}

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

		return layout;
	}

}
