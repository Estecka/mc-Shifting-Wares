package tk.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers.SellMapFactory;
import tk.estecka.shiftingwares.ISellMapFactoryDuck;
import tk.estecka.shiftingwares.IVillagerEntityDuck;
import tk.estecka.shiftingwares.ShiftingWares;

@Mixin(SellMapFactory.class)
public abstract class SellMapFactoryMixin
implements ISellMapFactoryDuck
{
	@Shadow @Final private int price;
	@Shadow @Final private int maxUses;
	@Shadow @Final private int experience;
	@Shadow @Final private String nameKey;
	@Shadow @Final private MapIcon.Type iconType;

	@Shadow public abstract TradeOffer	create(Entity entity, Random random);

	@Inject( method="create", at=@At("HEAD"), cancellable=true)
	public void	restoreCached(Entity entity, Random random, CallbackInfoReturnable<TradeOffer> info){
		if (!(entity instanceof VillagerEntity))
			return;

		final IVillagerEntityDuck villagerDuck = (IVillagerEntityDuck)entity;
		var cachedMap = villagerDuck.GetCachedMap(this.nameKey);

		if (cachedMap.isPresent()) {
			ItemStack stack = cachedMap.get();
			ShiftingWares.LOGGER.info("Reselling previously available map #{}", FilledMapItem.getMapId(stack));
			info.setReturnValue(new TradeOffer( new ItemStack(Items.EMERALD, this.price), new ItemStack(Items.COMPASS), stack, this.maxUses, this.experience, 0.2f ));
		}
		else {
			ShiftingWares.LOGGER.info("Selling a brand new map: {}.", entity);
		}
	}

	
	@Inject( method="create", at=@At("RETURN"))
	public void	cacheCreated(Entity entity, Random random, CallbackInfoReturnable<TradeOffer> info){
		if (!(entity instanceof VillagerEntity))
			return;

		final var villager = (IVillagerEntityDuck)entity;
		TradeOffer offer = info.getReturnValue();
		if (offer != null)
			villager.AddCachedMap(this.nameKey, info.getReturnValue().getSellItem());
	}
}
