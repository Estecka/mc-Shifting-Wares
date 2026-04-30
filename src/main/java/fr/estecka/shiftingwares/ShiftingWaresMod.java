package fr.estecka.shiftingwares;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftingWaresMod
implements ModInitializer
{
	static public final String MODID = "shifting-wares";
	static public final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public final GameRuleCategory RULE_CATEGORY = GameRuleCategory.register(Identifier.fromNamespaceAndPath(MODID, "gamerules"));

	static public final GameRule<Boolean> WORKSTATION_RULE = GameRuleBuilder.forBoolean(true) .category(RULE_CATEGORY).buildAndRegister(Identifier.fromNamespaceAndPath(MODID, "workstation_protection"));
	static public final GameRule<Boolean> DAILY_RULE       = GameRuleBuilder.forBoolean(true) .category(RULE_CATEGORY).buildAndRegister(Identifier.fromNamespaceAndPath(MODID, "daily_reroll"));
	static public final GameRule<Boolean> DEPLETED_RULE    = GameRuleBuilder.forBoolean(true) .category(RULE_CATEGORY).buildAndRegister(Identifier.fromNamespaceAndPath(MODID, "deplete_reroll"));
	static public final GameRule<Boolean> PERSISTENT_RULE  = GameRuleBuilder.forBoolean(false).category(RULE_CATEGORY).buildAndRegister(Identifier.fromNamespaceAndPath(MODID, "allow_persistent_reroll"));

	static public boolean GetBoolean(Entity entity, GameRule<Boolean> rule){
		return ((ServerLevel)entity.level()).getGameRules().get(rule);
	}

	@SuppressWarnings("unused")
	@Override
	public void onInitialize() {
		/**
		 * Force classloading and mixin injection to happen (and potentially
		 * fail) ASAP, in order to avoid villager-wiping failures later on.
		 */
		Object thingamajig;
		thingamajig = Villager.class;
		thingamajig = VillagerTrade.class;
		thingamajig = MerchantOffer.class;
	}
}
