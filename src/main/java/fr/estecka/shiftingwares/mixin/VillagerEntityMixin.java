package fr.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fr.estecka.shiftingwares.ShiftingWaresMod;
import fr.estecka.shiftingwares.TradeShuffler;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

@Unique
@Mixin(Villager.class)
public abstract class VillagerEntityMixin
{
	static private final MerchantOffers EMPTY = new MerchantOffers();

	private final Villager villager = (Villager)(Object)this;

	private boolean	IsDailyRerollEnabled()   { return ShiftingWaresMod.GetBoolean(villager, ShiftingWaresMod.DAILY_RULE   ); }
	private boolean	IsDepleteRerollEnabled() { return ShiftingWaresMod.GetBoolean(villager, ShiftingWaresMod.DEPLETED_RULE); }


/******************************************************************************/
/* # Daily Rerolls                                                            */
/******************************************************************************/

	/**
	 * Triggered once a day, regardless of whether the villager needs restocks.
	 */
	@Inject( method="catchUpDemand", at=@At(value="HEAD") )
	private void DailyReroll(CallbackInfo info) {
		if (IsDailyRerollEnabled()){
			ShiftingWaresMod.LOGGER.info("A villager has restocked all their trades.");
			new TradeShuffler(villager, false).Reroll();
		}
		else if (IsDepleteRerollEnabled()){
			ShiftingWaresMod.LOGGER.info("A villager has restocked some trades.");
			new TradeShuffler(villager, true).Reroll();
		}
	}

	/**
	 * This redirects the `for` loop that would normally refill all trades.
	 * Daily refills are never needed  due to all trades being outright replaced
	 * when refills are allowed.
	 */
	@WrapOperation(
		method = "catchUpDemand",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/world/entity/npc/villager/Villager.getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"
		)
	)
	private MerchantOffers DailyRefill(Villager me, Operation<MerchantOffers> original) {
		if (IsDailyRerollEnabled() || IsDepleteRerollEnabled())
			return EMPTY;
		else
			return original.call(me);
	}


/******************************************************************************/
/* # Depleted Rerolls                                                         */
/******************************************************************************/

	/**
	 * Triggered whenever the villager decides to restock due to low stocks.
	 * (Excluding the daily restock.)
	 * This also redirects the `for` loop that would normally refill all trades.
	 */
	@WrapOperation(
		method = "restock",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/world/entity/npc/villager/Villager.getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"
		)
	)
	private MerchantOffers RestockReroll(Villager me, Operation<MerchantOffers> original) {
		if (IsDepleteRerollEnabled()){
			ShiftingWaresMod.LOGGER.info("A villager has restocked some trades.");
			new TradeShuffler(villager, true).Reroll();
			return EMPTY;
		}
		else
			return original.call(me);
	}

	/**
	 * Prevents restocks from being triggered  by partially used trades, so that
	 * only fully depleted trades may trigger restocks.
	 * This does not prevent partially used trades  from being refilled whenever
	 * a restock does occurs, this only prevents restocks from being wasted.
	 * 
	 * @implNote Placeholder  trades  can never  be "used" so  they  will  never
	 * trigger restocks despite being "disabled".
	 */
	@WrapOperation(
		method = "needsToRestock",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/world/item/trading/MerchantOffer.needsRestock()Z"
		)
	)
	private boolean RestockDepletedOnly(MerchantOffer offer, Operation<Boolean> hasBeenUsed){
		return hasBeenUsed.call(offer) && (offer.isOutOfStock() || !IsDepleteRerollEnabled());
	}

}
