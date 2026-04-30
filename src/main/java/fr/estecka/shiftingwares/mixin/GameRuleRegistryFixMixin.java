package fr.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.GameRuleRegistryFix;

@Mixin(GameRuleRegistryFix.class)
public abstract class GameRuleRegistryFixMixin
{
	@Shadow static private Dynamic<?> convertBoolean (Dynamic<?> dynamic) { throw new AssertionError(); }

	@ModifyReturnValue(
		method = "lambda$makeRule$2",
		at = @At("RETURN")
	)
	static private <T> Dynamic<T> RenameGamerules(Dynamic<T> original)
	{
		return original
			.renameField("shiftingWares.dailyReroll",           "shifting-wares:daily_reroll")
			.renameField("shiftingWares.depleteReroll",         "shifting-wares:deplete_reroll")
			.renameField("shiftingWares.allowMapReroll",        "shifting-wares:allow_persistent_reroll")
			.renameField("shiftingWares.workstationProtection", "shifting-wares:workstation_protection")
			;
	}
}
