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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin
implements IVillagerEntityDuck
{
	private final VillagerEntity villager = (VillagerEntity)(Object)this;

	private final Map<String,ItemStack> createdMaps = new HashMap<String,ItemStack>();

	private boolean	IsDailyRerollEnabled()   { return villager.getWorld().getGameRules().get(ShiftingWares.DAILY_RULE).get();    }
	private boolean	IsDepleteRerollEnabled() { return villager.getWorld().getGameRules().get(ShiftingWares.DEPLETED_RULE).get(); }

	static private final TradeOfferList EMPTY = new TradeOfferList();

	public Optional<ItemStack>	GetCachedMap(String key){
		if (this.createdMaps.containsKey(key))
			return Optional.of(this.createdMaps.get(key));
		else
			return Optional.empty();
	}
	public void	AddCachedMap(String key, ItemStack mapItem){
		if (createdMaps.containsKey(key)){
			Integer oldId=FilledMapItem.getMapId(createdMaps.get(key));
			Integer neoId=FilledMapItem.getMapId(mapItem);
			if (!neoId.equals(oldId))
				ShiftingWares.LOGGER.error("Overwriting a villager's existing map: #{}->#{} @ {}", oldId, neoId, key);
			else if (ItemStack.areEqual(mapItem, createdMaps.get(key)))
				ShiftingWares.LOGGER.warn("Updating a villager's existing map#{} @ {}", oldId, key);
		}

		createdMaps.put(key, mapItem);
	}

	public void UpdateCachedMaps(){
		MapTradesCache.FillCacheFromTrades(this.createdMaps, villager.getOffers());
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
	 */
	@Redirect( method="restockAndUpdateDemandBonus", at=@At(value="INVOKE", target="net/minecraft/entity/passive/VillagerEntity.getOffers ()Lnet/minecraft/village/TradeOfferList;") )
	private TradeOfferList DailyRefill(VillagerEntity me) {
		if (IsDailyRerollEnabled())
			return EMPTY;
		else if (IsDepleteRerollEnabled())
			return EMPTY;
		else
			return villager.getOffers();
	}

	/**
	 * Triggered whenever the villager decides to restock due to low stocks. 
	 * (Excluding the daily restock.)
	 * This also redirects the `for` loop that would normally refill all trades.
	 */
	@Redirect( method="restock", at=@At(value="INVOKE", target="net/minecraft/entity/passive/VillagerEntity.getOffers ()Lnet/minecraft/village/TradeOfferList;") )
	private TradeOfferList RestockReroll(VillagerEntity me) {
		if (IsDepleteRerollEnabled()){
			ShiftingWares.LOGGER.info("A villager has restocked some trades.");
			new TradeShuffler(villager, true).Reroll();
			return EMPTY;
		}
		else
			return villager.getOffers();
	}

	/**
	 * Prevents restocks from being triggered by partially used trades, so that
	 * only fully depleted trades may trigger restocks.
	 * This does not prevent partially used trades  from being refilled whenever
	 * a restock occurs,  but this does prevent  restocks from  being needlessly
	 * spent.
	 */
	@Redirect( method="needsRestock", at=@At(value="INVOKE", target="net/minecraft/village/TradeOffer.hasBeenUsed ()Z") )
	private boolean RestockDepletedOnly(TradeOffer offer){
		if (IsDepleteRerollEnabled())
			return offer.isDisabled();
		else
			return offer.hasBeenUsed();
	}


	@Inject ( method="writeCustomDataToNbt", at=@At("TAIL"))
	void	WriteCachedMapsToNbt(NbtCompound nbt, CallbackInfo info){
		this.UpdateCachedMaps();
		MapTradesCache.WriteMapCacheToNbt(nbt, this.createdMaps);
	}

	@Inject ( method="readCustomDataFromNbt", at=@At("TAIL"))
	void	ReadCachedMapsFromNbt(NbtCompound nbt, CallbackInfo info){
		MapTradesCache.ReadMapCacheFromNbt(nbt, this.createdMaps);
		this.UpdateCachedMaps();
	}

}
