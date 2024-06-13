package tk.estecka.shiftingwares;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import tk.estecka.shiftingwares.TradeLayouts.VanillaTradeLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftingWares
implements ModInitializer
{
	static public final Logger LOGGER = LoggerFactory.getLogger("Shifting-Wares");

	static public final CustomGameRuleCategory RULE_CATTEGORY = new CustomGameRuleCategory(Identifier.of("shifting-wares","gamerules"), Text.translatable("gamerule.category.shiftingwares").formatted(Formatting.BOLD, Formatting.YELLOW));

	static public final GameRules.Key<BooleanRule> DAILY_RULE    = GameRuleRegistry.register("shiftingWares.dailyReroll",    RULE_CATTEGORY, GameRuleFactory.createBooleanRule(true));
	static public final GameRules.Key<BooleanRule> DEPLETED_RULE = GameRuleRegistry.register("shiftingWares.depleteReroll",  RULE_CATTEGORY, GameRuleFactory.createBooleanRule(true));
	static public final GameRules.Key<BooleanRule> MAP_RULE      = GameRuleRegistry.register("shiftingWares.allowMapReroll", RULE_CATTEGORY, GameRuleFactory.createBooleanRule(false));

	static public final TradeOffer PLACEHOLDER_TRADE;
	
	static {
		TradedItem fake_void = new TradedItem(Items.EMERALD)
			.withComponents( builder -> builder.add(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE) )
			;

		PLACEHOLDER_TRADE = new TradeOffer(
			fake_void,
			fake_void.itemStack(),
			0, 0, 0
		);
	}

	@Override
	public void onInitialize() {
		// Static initialization
		LOGGER.info("Trade-Rebalance support is {} for this version of minecraft.", VanillaTradeLayout.IS_EXP_TRADE_AVAILABLE?"enabled":"disabled");
	}
}
