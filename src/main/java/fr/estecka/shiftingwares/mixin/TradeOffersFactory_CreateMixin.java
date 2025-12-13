package fr.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import fr.estecka.shiftingwares.ShiftingTradeData;

@Mixin(value={
	MerchantEntity.class,
	TradeOffers.TypedWrapperFactory.class,
})
public class TradeOffersFactory_CreateMixin
{
	@WrapOperation(
		require = 1, // 1 *per-class*
		remap = false,
		method = {
			"method_19170", "fillRecipesFromPool", // MerchantEntity
			"method_7246",  "create", // TypedWrapperFactory
		},
		at = @At(
			value = "INVOKE",
			remap = true,
			target = "net/minecraft/village/TradeOffers$Factory.create(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/random/Random;)Lnet/minecraft/village/TradeOffer;"
		)
	)
	private TradeOffer InitializeShiftingData(TradeOffers.Factory factory, ServerWorld world, Entity entity, Random random, Operation<TradeOffer> original){
		TradeOffer offer = original.call(factory, world, entity, random);
		ShiftingTradeData.FinalizeTrade(offer, factory);
		return offer;
	}
}
