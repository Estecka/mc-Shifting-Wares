package tk.estecka.shiftingwares.mixin;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import tk.estecka.shiftingwares.IVillagerEntityDuck;
import tk.estecka.shiftingwares.MapTradesCache;
import tk.estecka.shiftingwares.ShiftingWares;
import tk.estecka.shiftingwares.TradeShuffler;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Unique
@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin
implements IVillagerEntityDuck
{
	static private final TradeOfferList EMPTY = new TradeOfferList();

	private final VillagerEntity villager = (VillagerEntity)(Object)this;
	private final Map<String,ItemStack> createdMaps = new HashMap<String,ItemStack>();

	private boolean	IsDailyRerollEnabled()   { return villager.getWorld().getGameRules().get(ShiftingWares.DAILY_RULE   ).get(); }
	private boolean	IsDepleteRerollEnabled() { return villager.getWorld().getGameRules().get(ShiftingWares.DEPLETED_RULE).get(); }

	public Optional<ItemStack>	shiftingwares$GetCachedMap(String key){
		if (this.createdMaps.containsKey(key))
			return Optional.of(this.createdMaps.get(key));
		else
			return Optional.empty();
	}
	public void	shiftingwares$AddCachedMap(String key, ItemStack mapItem){
		Integer neoId=FilledMapItem.getMapId(mapItem);
		if (createdMaps.containsKey(key)){
			Integer oldId=FilledMapItem.getMapId(createdMaps.get(key));
			if (!neoId.equals(oldId))
				ShiftingWares.LOGGER.error("Overwriting a villager's existing map: #{}->#{} @ {}", oldId, neoId, key);
			else if (ItemStack.areEqual(mapItem, createdMaps.get(key)))
				ShiftingWares.LOGGER.warn("Updating a villager's existing map#{} @ {}", neoId, key);
		}
		else
			ShiftingWares.LOGGER.info("New map #{} created by {}", neoId, villager);

		createdMaps.put(key, mapItem);
	}

	/**
	 * Triggered once a day, regardless of whether the villager needs restocks.
	 */
	@Inject( method="restockAndUpdateDemandBonus", at=@At(value="HEAD") )
	private void DailyReroll(CallbackInfo info) {
		if (IsDailyRerollEnabled()){
			ShiftingWares.LOGGER.info("A villager has restocked all their trades.");
			new TradeShuffler(villager, false).Reroll();
		}
		else if (IsDepleteRerollEnabled()){
			ShiftingWares.LOGGER.info("A villager has restocked some trades.");
			new TradeShuffler(villager, true).Reroll();
		}
	}
	/**
	 * This redirects the `for` loop that would normally refill all trades.
	 * Daily refills are never needed  due to all trades being outright replaced
	 * when refills are allowed.
	 */
	@WrapOperation( method="restockAndUpdateDemandBonus", at=@At(value="INVOKE", target="net/minecraft/entity/passive/VillagerEntity.getOffers ()Lnet/minecraft/village/TradeOfferList;") )
	private TradeOfferList DailyRefill(VillagerEntity me, Operation<TradeOfferList> original) {
		if (IsDailyRerollEnabled() || IsDepleteRerollEnabled())
			return EMPTY;
		else
			return original.call();
	}

	/**
	 * Triggered whenever the villager decides to restock due to low stocks.
	 * (Excluding the daily restock.)
	 * This also redirects the `for` loop that would normally refill all trades.
	 */
	@WrapOperation( method="restock", at=@At(value="INVOKE", target="net/minecraft/entity/passive/VillagerEntity.getOffers ()Lnet/minecraft/village/TradeOfferList;") )
	private TradeOfferList RestockReroll(VillagerEntity me, Operation<TradeOfferList> original) {
		if (IsDepleteRerollEnabled()){
			ShiftingWares.LOGGER.info("A villager has restocked some trades.");
			new TradeShuffler(villager, true).Reroll();
			return EMPTY;
		}
		else
			return original.call();
	}

	/**
	 * Prevents restocks from being triggered  by partially used trades, so that
	 * only fully depleted trades may trigger restocks.
	 * This does not prevent partially used trades  from being refilled whenever
	 * a restock does occurs, this only prevents restocks from being wasted.
	 * 
	 * @implNote Placeholder trades can never be "used" so they will never 
	 * trigger restocks despite being "disabled".
	 */
	@WrapOperation( method="needsRestock", at=@At(value="INVOKE", target="net/minecraft/village/TradeOffer.hasBeenUsed ()Z") )
	private boolean RestockDepletedOnly(TradeOffer offer, Operation<Boolean> hasBeenUsed){
		return hasBeenUsed.call(offer) && (offer.isDisabled() || !IsDepleteRerollEnabled());
	}


	@Inject ( method="writeCustomDataToNbt", at=@At("TAIL"))
	void	WriteCachedMapsToNbt(NbtCompound nbt, CallbackInfo info){
		MapTradesCache.FillCacheFromTrades(villager);
		MapTradesCache.WriteMapCacheToNbt(nbt, this.createdMaps);
	}

	@Inject ( method="readCustomDataFromNbt", at=@At("TAIL"))
	void	ReadCachedMapsFromNbt(NbtCompound nbt, CallbackInfo info){
		MapTradesCache.ReadMapCacheFromNbt(nbt, this.createdMaps);
		MapTradesCache.FillCacheFromTrades(villager);
	}

}
