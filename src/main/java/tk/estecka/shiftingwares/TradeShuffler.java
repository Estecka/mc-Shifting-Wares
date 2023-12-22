package tk.estecka.shiftingwares;

import java.util.ArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
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
	private final MapTradesCache tradeCache;

	public TradeShuffler(VillagerEntity villager, boolean depletedOnly)
	{
		this.villager = villager;
		this.depletedOnly = depletedOnly;

		this.offers = villager.getOffers();
		this.job = villager.getVillagerData().getProfession();
		this.jobLevel = villager.getVillagerData().getLevel();
		this.random = villager.getRandom();
		this.tradeCache = ((IVillagerEntityDuck)villager).shiftingwares$GetTradeCache();

		this.jobPool = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(job);
	}

	public void	Reroll(){
		if (jobPool == null){
			ShiftingWares.LOGGER.error("No trade pool for job {}. Villager will not be rerolled.", job);
			return;
		}

		tradeCache.FillCacheFromTrades(offers);

		IntList slotLevels = new IntArrayList();
		for (int lvl=VillagerData.MIN_LEVEL; lvl<=this.jobLevel; ++lvl)
		if (jobPool.containsKey(lvl)){
			var pool = jobPool.get(lvl);
			if (pool == null)
				ShiftingWares.LOGGER.error("Missing pool for job {} lvl.{}", job, jobLevel);
			else for (int i=0; i<2 && i<pool.length; ++i)
				slotLevels.add(lvl);
		}

		for (int i=offers.size()-1; slotLevels.size()<=i; --i)
			if (shouldReroll(i))
				offers.remove(i);
		while(offers.size() < slotLevels.size())
			offers.add(ShiftingWares.PLACEHOLDER_TRADE);

		for (int tradeLvl=VillagerData.MIN_LEVEL; tradeLvl<=jobLevel; ++tradeLvl)
			DuplicataAwareReroll(tradeLvl, slotLevels);

		tradeCache.FillCacheFromTrades(offers);
	}

	public boolean	shouldReroll(int tradeIndex){
		return !depletedOnly 
		    || offers.size() <= tradeIndex
		    || offers.get(tradeIndex).isDisabled()
		    ;
	}

	private void	DuplicataAwareReroll(int tradeLvl, IntList slotLevels){
		Factory[] levelPool = jobPool.get(tradeLvl);
		boolean missingSome = false;

		var randomPool = new ArrayList<Factory>(levelPool.length);
		for (var f : levelPool)
			randomPool.add(f);

		for (int i=0; i<offers.size(); ++i) 
		if (tradeLvl==slotLevels.getInt(i) && shouldReroll(i)) {
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
