package fr.estecka.shiftingwares;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import fr.estecka.shiftingwares.TradeLayouts.VanillaTradeLayout;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftingWaresMod
implements ModInitializer
{
	static public final String MODID = "shifting-wares";
	static public final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public final GameRuleCategory RULE_CATEGORY = GameRuleCategory.register(Identifier.of(MODID, "gamerules"));

	static public final GameRule<Boolean> WORKSTATION_RULE = GameRuleBuilder.forBoolean(true) .category(RULE_CATEGORY).buildAndRegister(Identifier.of(MODID, "workstation_protection"));
	static public final GameRule<Boolean> DAILY_RULE       = GameRuleBuilder.forBoolean(true) .category(RULE_CATEGORY).buildAndRegister(Identifier.of(MODID, "daily_reroll"));
	static public final GameRule<Boolean> DEPLETED_RULE    = GameRuleBuilder.forBoolean(true) .category(RULE_CATEGORY).buildAndRegister(Identifier.of(MODID, "deplete_reroll"));
	static public final GameRule<Boolean> MAP_RULE         = GameRuleBuilder.forBoolean(false).category(RULE_CATEGORY).buildAndRegister(Identifier.of(MODID, "allow_map_reroll"));

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

	static public boolean GetBoolean(Entity entity, GameRule<Boolean> rule){
		return ((ServerWorld)entity.getEntityWorld()).getGameRules().getValue(rule);
	}

	@Override
	public void onInitialize() {
		// Static initialization
		LOGGER.info("Trade-Rebalance support is {} for this version of minecraft.", VanillaTradeLayout.IS_EXP_TRADE_AVAILABLE?"enabled":"disabled");
	}
}
