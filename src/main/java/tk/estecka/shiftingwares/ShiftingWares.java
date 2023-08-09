package tk.estecka.shiftingwares;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftingWares implements ModInitializer
{
	static public final Logger LOGGER = LoggerFactory.getLogger("Shifting-Wares");
	static public GameRules.Key<BooleanRule> DAILY_RULE;
	static public GameRules.Key<BooleanRule> DEPLETED_RULE;

	@Override
	public void onInitialize() {
		DAILY_RULE    = GameRuleRegistry.register("shiftingWares.dailyReroll",   GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
		DEPLETED_RULE = GameRuleRegistry.register("shiftingWares.depleteReroll", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
	}
}
