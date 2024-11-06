package fr.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import fr.estecka.shiftingwares.ShiftingTradeData;

@Mixin(value={
	MerchantEntity.class,
	WanderingTraderEntity.class,
	TradeOffers.TypedWrapperFactory.class,
})
public class TradeOfferFactoryWrapper
{
	@WrapOperation(
		require=1, // 1 *per-class*
		method={
			/* MerchantEntity.fillRecipesFromPool */ "method_19170(Lnet/minecraft/village/TradeOfferList;[Lnet/minecraft/village/TradeOffers$Factory;I)V",
			/* WanderingTraderEntity.fillRecipes  */ "method_7237()V",
			/* TypedWrapperFactory.create         */ "method_7246(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/random/Random;)Lnet/minecraft/village/TradeOffer;",
		},
		at=@At(
			value="INVOKE",
			target="net/minecraft/village/TradeOffers$Factory.create(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/random/Random;)Lnet/minecraft/village/TradeOffer;"
		)
	)
	private TradeOffer InitializeShiftingData(TradeOffers.Factory factory, Entity entity, Random random, Operation<TradeOffer> original){
		TradeOffer offer = original.call(factory, entity, random);
		if (offer != null)
			ShiftingTradeData.FinalizeTrade(offer, factory);
		return offer;
	}
}
