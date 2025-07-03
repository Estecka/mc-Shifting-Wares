package fr.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.random.Random;

@Mixin(Entity.class)
public interface IEntityAccessor
{
	@Mutable @Accessor void setRandom(Random random);
}
