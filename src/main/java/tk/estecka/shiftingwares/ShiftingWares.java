package tk.estecka.shiftingwares;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import tk.estecka.shiftingwares.api.ITradeLayoutProvider;
import tk.estecka.shiftingwares.TradeLayouts.VanillaTradeLayout;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftingWares
implements ModInitializer
{
	static public final Logger LOGGER = LoggerFactory.getLogger("Shifting-Wares");

	static public final Identifier TRADE_REBALANCE_FLAGID = new Identifier("minecraft", "trade_rebalance");
	static public final boolean IS_EXP_TRADE_AVAILABLE;

	static public final GameRules.Key<BooleanRule> DAILY_RULE    = GameRuleRegistry.register("shiftingWares.dailyReroll",    GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
	static public final GameRules.Key<BooleanRule> DEPLETED_RULE = GameRuleRegistry.register("shiftingWares.depleteReroll",  GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
	static public final GameRules.Key<BooleanRule> MAP_RULE      = GameRuleRegistry.register("shiftingWares.allowMapReroll", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));

	static public final TradeOffer PLACEHOLDER_TRADE = new TradeOffer(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, 0, 0, 0, 0, 0);

	static public final ITradeLayoutProvider VANILLA_LAYOUT  = new VanillaTradeLayout();

	static {
		var featureSet = FeatureFlags.FEATURE_MANAGER.getFeatureSet();
		var featureIds = FeatureFlags.FEATURE_MANAGER.toId(featureSet);
		IS_EXP_TRADE_AVAILABLE = featureIds.contains(TRADE_REBALANCE_FLAGID);
	}

	static public List<TradeOffers.Factory[]> GetTradeLayout(VillagerEntity villager){
		var providers = FabricLoader.getInstance().getEntrypoints("shifting-wares", ITradeLayoutProvider.class);

		for (var p : providers) {
			var layout = p.GetTradeLayout(villager);
			if (layout != null)
				return layout;
		}

		return VANILLA_LAYOUT.GetTradeLayout(villager);
	}

	@Override
	public void onInitialize() {
		// Static initialization
		LOGGER.warn("Trade rebalance is : {}", IS_EXP_TRADE_AVAILABLE?"available":"unavailable");
	}
}
