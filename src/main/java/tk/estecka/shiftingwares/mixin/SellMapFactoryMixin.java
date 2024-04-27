package tk.estecka.shiftingwares.mixin;

import java.util.Optional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.village.TradeOffers.SellMapFactory;
import tk.estecka.shiftingwares.IVillagerEntityDuck;
import tk.estecka.shiftingwares.MapTradesCache;
import tk.estecka.shiftingwares.ShiftingWares;

@Mixin(SellMapFactory.class)
public abstract class SellMapFactoryMixin
{
	@Shadow @Final private int price;
	@Shadow @Final private int maxUses;
	@Shadow @Final private int experience;
	@Shadow @Final private String nameKey;

	@Inject( method="create", at=@At("HEAD"), cancellable=true )
	private void	restoreCached(Entity entity, Random random, CallbackInfoReturnable<TradeOffer> info){
		var cachedMap = MapTradesCache.Resell(entity, this.nameKey);

		if (cachedMap.isPresent()){
			ItemStack stack = cachedMap.get();
			ShiftingWares.LOGGER.info("Reselling previously available map #{} @ {}", MapTradesCache.GetRawMapId(stack), this.nameKey);
			info.setReturnValue(new TradeOffer(
				new TradedItem(Items.EMERALD, this.price),
				Optional.of(new TradedItem(Items.COMPASS)),
				stack,
				this.maxUses, this.experience, 0.2f
			));
		}
	}

	
	@Inject( method="create", at=@At("RETURN") )
	private void	cacheCreated(Entity entity, Random random, CallbackInfoReturnable<TradeOffer> info){
		if (!(entity instanceof VillagerEntity))
			return;

		final var villager = (IVillagerEntityDuck)entity;
		TradeOffer offer = info.getReturnValue();
		if (offer != null)
			villager.shiftingwares$GetItemCache().AddCachedItem(this.nameKey, info.getReturnValue().getSellItem());
	}
}
