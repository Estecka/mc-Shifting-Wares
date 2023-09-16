package tk.estecka.shiftingwares;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftingWares implements ModInitializer
{
	static public final Logger LOGGER = LoggerFactory.getLogger("Shifting-Wares");
	static public final GameRules.Key<BooleanRule> DAILY_RULE;
	static public final GameRules.Key<BooleanRule> DEPLETED_RULE;
	static public final TradeOffer PLACEHOLDER_TRADE;

	static {
		ItemStack NoItem = new ItemStack(Items.BARRIER, 1);
		PLACEHOLDER_TRADE = new TradeOffer(NoItem, ItemStack.EMPTY, NoItem, 0, 1, 0, 0, 0);

		DAILY_RULE    = GameRuleRegistry.register("shiftingWares.dailyReroll",   GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
		DEPLETED_RULE = GameRuleRegistry.register("shiftingWares.depleteReroll", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
	}

	@Override
	public void onInitialize() {
	}
}
