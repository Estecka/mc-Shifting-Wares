package fr.estecka.shiftingwares.TradeLayouts;

import java.util.ArrayList;
import java.util.List;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.TradeOffers.Factory;
import fr.estecka.shiftingwares.ShiftingWaresMod;
import fr.estecka.shiftingwares.api.ITradeLayoutProvider;

public class VanillaTradeLayout
implements ITradeLayoutProvider
{
	/**
	 * This is a leftover from 1.20.x, which provided backward compatibility
	 * for versions without that experimental feature. Although the check is
	 * currently useless, it's expected to become useful again whenever that
	 * feature is removed.
	 */
	static public final boolean IS_EXP_TRADE_AVAILABLE;
	static {
		var featureSet = FeatureFlags.FEATURE_MANAGER.getFeatureSet();
		var featureIds = FeatureFlags.FEATURE_MANAGER.toId(featureSet);
		IS_EXP_TRADE_AVAILABLE = featureIds.contains(Identifier.ofVanilla("trade_rebalance"));
	}

	public List<Factory[]>	GetTradeLayout(VillagerEntity villager){
		List<Factory[]> layout = new ArrayList<>();
		RegistryKey<VillagerProfession> job = villager.getVillagerData().profession().getKey().get();
		int jobLevel = villager.getVillagerData().level();

		Int2ObjectMap<Factory[]> jobPool = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(job);
		if (IS_EXP_TRADE_AVAILABLE 
		&& villager.getEntityWorld().getEnabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)
		&& TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE.containsKey(job)
		) {
			jobPool = TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE.get(job);
		}

		if (jobPool == null){
			ShiftingWaresMod.LOGGER.error("No trade pool for job {}.", job);
			return null;
		}

		for (int lvl=VillagerData.MIN_LEVEL; lvl<=jobLevel; ++lvl)
		{
			var pool = jobPool.get(lvl);
			if (pool == null)
				ShiftingWaresMod.LOGGER.error("Missing pool for job {} lvl.{}", job, lvl);
			else for (int i=0; i<2 && i<pool.length; ++i)
				layout.add(pool);
		}

		return layout;
	}

}
