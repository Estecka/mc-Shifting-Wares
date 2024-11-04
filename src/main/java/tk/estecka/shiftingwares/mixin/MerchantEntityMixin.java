package tk.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import tk.estecka.shiftingwares.ShiftingTradeData;

@Mixin(MerchantEntity.class)
public class MerchantEntityMixin
{
	@WrapOperation( method="fillRecipesFromPool", at=@At(value="INVOKE", target="net/minecraft/village/TradeOffers/Factory.create()Lnet/minecraft/village/TradeOffer;") )
	private TradeOffer InitializeShiftingFata(TradeOffers.Factory factory, Entity _1, Random _2, Operation<TradeOffer> original){
		TradeOffer offer = original.call(factory, _1, _2);
		if (offer != null)
			ShiftingTradeData.FinalizeTrade(offer, factory);
		return offer;
	}
}
