package fr.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.GameRuleRegistryFix;

@Mixin(GameRuleRegistryFix.class)
public abstract class GameRuleRegistryFixMixin
{
	@Shadow static private Dynamic<?> isTrue (Dynamic<?> dynamic) { throw new AssertionError(); }

	@ModifyReturnValue(
		method = "method_76071",
		at = @At("RETURN")
	)
	static private <T> Dynamic<T> RenameGamerules(Dynamic<T> original)
	{
		return original
			.renameAndFixField("shiftingWares.dailyReroll",           "shifting-wares:daily_reroll",           GameRuleRegistryFixMixin::isTrue)
			.renameAndFixField("shiftingWares.depleteReroll",         "shifting-wares:deplete_reroll",         GameRuleRegistryFixMixin::isTrue)
			.renameAndFixField("shiftingWares.allowMapReroll",        "shifting-wares:allow_map_reroll",       GameRuleRegistryFixMixin::isTrue)
			.renameAndFixField("shiftingWares.workstationProtection", "shifting-wares:workstation_protection", GameRuleRegistryFixMixin::isTrue)
			;
	}
}
