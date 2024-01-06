package tk.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ExplorationMapLootFunction;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.gen.structure.Structure;
import tk.estecka.shiftingwares.IVillagerEntityDuck;
import tk.estecka.shiftingwares.MapTradesCache;
import tk.estecka.shiftingwares.ShiftingWares;

@Mixin(ExplorationMapLootFunction.class)
public abstract class ExplorationMapLootFunctionMixin
{
	@Shadow @Final private TagKey<Structure> destination;

	@Inject( method="process", at=@At("HEAD"), cancellable=true )
	private void	RestoreCachedItem(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> info){
		Entity entity = context.get(LootContextParameters.THIS_ENTITY);
		if(!(entity instanceof IVillagerEntityDuck))
			return;

		String cacheKey = MapTradesCache.GetCacheKey(this.destination);
		if (cacheKey == null){
			cacheKey = destination.id().toString();
			ShiftingWares.LOGGER.error("No known cache key for structure: {}", this.destination, cacheKey);
			MapTradesCache.RegisterStructure(destination, cacheKey);
		}

		var cachedMap = MapTradesCache.Resell(entity, cacheKey);
		if (cachedMap.isPresent()){
			stack = cachedMap.get();
			ShiftingWares.LOGGER.info("Reselling previously available map #{} @ {}", FilledMapItem.getMapId(stack), cacheKey);
			info.setReturnValue(stack);
		}
	}

}
