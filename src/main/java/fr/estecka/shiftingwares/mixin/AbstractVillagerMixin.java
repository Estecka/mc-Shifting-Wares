package fr.estecka.shiftingwares.mixin;

import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fr.estecka.shiftingwares.ShiftingOfferData;
import fr.estecka.shiftingwares.ShiftingWaresMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;


@Mixin(AbstractVillager.class)
public class AbstractVillagerMixin
{
/******************************************************************************/
/* # Initialize Offer Data                                                    */
/******************************************************************************/

	@WrapOperation(
		method = {
			"addOffersFromItemListings",
			"addOffersFromItemListingsWithoutDuplicates",
		},
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/world/item/trading/VillagerTrade.getOffer(Lnet/minecraft/world/level/storage/loot/LootContext;)Lnet/minecraft/world/item/trading/MerchantOffer;"
		)
	)
	static private MerchantOffer AddShiftingData(VillagerTrade factory, LootContext lootContext, Operation<MerchantOffer> original, @Local Holder<VillagerTrade> holder){
		MerchantOffer offer = original.call(factory, lootContext);
		ShiftingOfferData.FinalizeOffer(offer, holder);
		return offer;
	}


/******************************************************************************/
/* # Workstation Protection                                                   */
/******************************************************************************/

	/**
	 * Overwrites the LootContext's random source, which is used for everything:
	 * picking the amount of trades, picking the trades, and baking the trades.
	 * Setting a seed is enough to override the random sequence ID.
	 */
	@WrapOperation(
		method = {
			"addOffersFromTradeSet",
		},
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/world/level/storage/loot/LootContext$Builder.create(Ljava/util/Optional;)Lnet/minecraft/world/level/storage/loot/LootContext;"
		)
	)
	private LootContext SetDeterministicRandom(LootContext.Builder builder, Optional<Identifier> randomSequence, Operation<LootContext> original){
		if (((Object)this) instanceof Villager villager
		&& ShiftingWaresMod.GetBoolean(villager, ShiftingWaresMod.WORKSTATION_RULE)
		)
		{
			int seed = villager.getUUID().hashCode() + villager.getVillagerData().level();
			builder = builder.withOptionalRandomSeed(seed);
		}

		return original.call(builder, randomSequence);
	}
}
