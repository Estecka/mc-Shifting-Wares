package fr.estecka.shiftingwares;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import fr.estecka.shiftingwares.TradeLayouts.VanillaTradeLayout;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftingWaresMod
implements ModInitializer
{
	static public final String MODID = "shifting-wares";
	static public final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public final CustomGameRuleCategory RULE_CATEGORY = new CustomGameRuleCategory(Identifier.of(MODID, "gamerules"), Text.translatable("gamerule.category.shiftingwares").formatted(Formatting.BOLD, Formatting.YELLOW));

	static public final GameRules.Key<BooleanRule> WORKSTATION_RULE = GameRuleRegistry.register("shiftingWares.workstationProtection", RULE_CATEGORY, GameRuleFactory.createBooleanRule(true));
	static public final GameRules.Key<BooleanRule> DAILY_RULE    = GameRuleRegistry.register("shiftingWares.dailyReroll",    RULE_CATEGORY, GameRuleFactory.createBooleanRule(true));
	static public final GameRules.Key<BooleanRule> DEPLETED_RULE = GameRuleRegistry.register("shiftingWares.depleteReroll",  RULE_CATEGORY, GameRuleFactory.createBooleanRule(true));
	static public final GameRules.Key<BooleanRule> MAP_RULE      = GameRuleRegistry.register("shiftingWares.allowMapReroll", RULE_CATEGORY, GameRuleFactory.createBooleanRule(false));

	static public final TradeOffer PLACEHOLDER_TRADE;
	static {
		TradedItem fake_air = new TradedItem(Items.EMERALD).withComponents( builder -> {
			builder.add(DataComponentTypes.ITEM_NAME, Text.literal("Unobtainium"));
			builder.add(DataComponentTypes.TOOLTIP_DISPLAY, new TooltipDisplayComponent(true, ReferenceSortedSets.emptySet()));
			if (Registries.DATA_COMPONENT_TYPE.containsId(Identifier.ofVanilla("item_model"))) // 1.21.0 compatibility
				builder.add(DataComponentTypes.ITEM_MODEL, Identifier.ofVanilla("air"));
			return builder;
		});

		PLACEHOLDER_TRADE = new TradeOffer(
			fake_air,
			fake_air.itemStack(),
			0, 0, 0
		);
	}

	static public boolean GetBoolean(Entity entity, GameRules.Key<BooleanRule> rule){
		return ((ServerWorld)entity.getEntityWorld()).getGameRules().getBoolean(rule);
	}

	@Override
	public void onInitialize() {
		// Static initialization
		LOGGER.info("Trade-Rebalance support is {} for this version of minecraft.", VanillaTradeLayout.IS_EXP_TRADE_AVAILABLE?"enabled":"disabled");
	}
}
