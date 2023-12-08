package tk.estecka.shiftingwares;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import tk.estecka.shiftingwares.TradeLayouts.ITradeLayoutProvider;
import tk.estecka.shiftingwares.TradeLayouts.VanillaTradeLayout;
import tk.estecka.shiftingwares.TradeLayouts.VillagerConfigTradeLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftingWares implements ModInitializer
{
	static public final Logger LOGGER = LoggerFactory.getLogger("Shifting-Wares");
	static public final GameRules.Key<BooleanRule> DAILY_RULE    = GameRuleRegistry.register("shiftingWares.dailyReroll",   GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
	static public final GameRules.Key<BooleanRule> DEPLETED_RULE = GameRuleRegistry.register("shiftingWares.depleteReroll", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
	static public final TradeOffer PLACEHOLDER_TRADE = new TradeOffer(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, 0, 0, 0, 0, 0);

	static public final ITradeLayoutProvider TRADE_LAYOUT_PROVIDER;

	static {
		if (FabricLoader.getInstance().isModLoaded("villagerconfig"))
			TRADE_LAYOUT_PROVIDER = new VillagerConfigTradeLayout();
		else
			TRADE_LAYOUT_PROVIDER = new VanillaTradeLayout();
	}

	@Override
	public void onInitialize() {
	}
}
