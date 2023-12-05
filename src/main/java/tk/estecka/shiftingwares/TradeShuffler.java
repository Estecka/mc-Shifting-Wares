package tk.estecka.shiftingwares;

import java.util.ArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers.Factory;
import net.minecraft.village.VillagerProfession;

public class TradeShuffler 
{
	private final VillagerEntity villager;
	private final boolean depletedOnly;

	private final VillagerProfession job;
	private final int jobLevel;

	private final Random random;
	private final TradeOfferList offers;
	private final Int2ObjectMap<Factory[]> jobPool;
	private final IntList slotLevels;

	public TradeShuffler(VillagerEntity villager, IRerollBlueprint layout, boolean depletedOnly)
	{
		this.villager = villager;
		this.depletedOnly = depletedOnly;

		this.offers = villager.getOffers();
		this.job = villager.getVillagerData().getProfession();
		this.jobLevel = villager.getVillagerData().getLevel();
		this.random = villager.getRandom();

		this.slotLevels = layout.GetSlotLevels(villager);
		this.jobPool = layout.GetTradePools(villager);
	}

	public void	Reroll(){
		if (jobPool == null || slotLevels == null){
			ShiftingWares.LOGGER.error("Missing layout, villager will not be rerolled: {} ({})", job, villager);
			return;
		}

		MapTradesCache.FillCacheFromTrades(villager);

		// Trim extraneous trades
		for (int i=offers.size()-1; slotLevels.size()<=i; --i)
			if (shouldReroll(i))
				offers.remove(i);

		// Reserve space for new trades
		while(offers.size() < slotLevels.size())
			offers.add(ShiftingWares.PLACEHOLDER_TRADE);

		IntSet checklist = new IntArraySet();
		for (int lvl : slotLevels)
		if (!checklist.contains(lvl)) {
			checklist.add(lvl);
			DuplicataAwareReroll(lvl);
		}

		MapTradesCache.FillCacheFromTrades(villager);
	}

	public boolean	shouldReroll(int tradeIndex){
		return !depletedOnly 
		    || offers.size() <= tradeIndex
		    || offers.get(tradeIndex).isDisabled()
		    ;
	}

	/**
	 * Rerolls all slots  that use the same trade pool, without ever reusing the
	 * same factory twice.
	 */
	private void	DuplicataAwareReroll(int tradeLvl){
		Factory[] levelPool = jobPool.get(tradeLvl);
		boolean missingSome = false;

		if (levelPool == null){
			ShiftingWares.LOGGER.error("A trade pool was declared, but could not be found: {} lvl.{} @ {}", job, tradeLvl, villager);
			return;
		}

		var randomPool = new ArrayList<Factory>(levelPool.length);
		for (var f : levelPool)
			randomPool.add(f);

		for (int i=0; i<offers.size(); ++i) 
		if (tradeLvl == slotLevels.getInt(i) && shouldReroll(i))
		{
			TradeOffer offer = null;
			while (offer == null && !randomPool.isEmpty()) {
				int roll = random.nextInt(randomPool.size());
				offer = randomPool.get(roll).create(villager, random);
				randomPool.remove(roll);
			}
			if (offer == null){
				offer = ShiftingWares.PLACEHOLDER_TRADE;
				missingSome = true;
			}

			offers.set(i, offer);
		}

		if (missingSome)
			ShiftingWares.LOGGER.warn("Failed to generate some trade offers for {} lvl.{} ({})", job, tradeLvl, villager);
	}

}
