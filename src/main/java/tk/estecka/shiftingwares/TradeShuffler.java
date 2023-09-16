package tk.estecka.shiftingwares;

import java.util.ArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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
	private final IVillagerEntityDuck duck;
	private final boolean depletedOnly;

	private final VillagerProfession job;
	private final int jobLevel;

	private final Random random;
	private final TradeOfferList offers;
	private final Int2ObjectMap<Factory[]> jobPool;

	public TradeShuffler(VillagerEntity villager, boolean depletedOnly)
	{
		this.villager = villager;
		this.duck = (IVillagerEntityDuck)villager;
		this.depletedOnly = depletedOnly;

		this.offers = villager.getOffers();
		this.job = villager.getVillagerData().getProfession();
		this.jobLevel = villager.getVillagerData().getLevel();
		this.jobPool = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(job);
		this.random = villager.getRandom();
	}

	public void	Reroll(){
		if (jobPool == null){
			ShiftingWares.LOGGER.error("No trade pool for job {}. Villager will not be rerolled.", job);
			return;
		}

		MapTradesCache.FillCacheFromTrades(duck);
		int tradeIndex = 0;
		for (int tradeLvl=VillagerData.MIN_LEVEL; tradeLvl<=jobLevel; ++tradeLvl)
		{
			final int levelSize = (tradeLvl<VillagerData.MAX_LEVEL) ? 2 : 1;
			Factory[] levelPool = jobPool.get(tradeLvl);
			if (levelPool == null) {
				ShiftingWares.LOGGER.error("No trade pool for job {} lvl.{}", job, jobLevel);
				levelPool = new Factory[0];
			}
			else if (levelPool.length < levelSize) {
				ShiftingWares.LOGGER.error("Trade pool smaller than expected for job {} lvl.{}", job, jobLevel);
			}

			final TradeOffer[] rerollMap = new TradeOffer[levelSize];
			for (int n=0; n<levelSize; ++n)
				if (CanReroll(tradeIndex+n))
					rerollMap[n] = ShiftingWares.PLACEHOLDER_TRADE;

			DuplicataAwareReroll(levelPool, rerollMap);

			for (int n=0; n<levelSize; ++n, ++tradeIndex) {
				while (offers.size() <= tradeIndex)
					offers.add(ShiftingWares.PLACEHOLDER_TRADE);
				if (rerollMap[n] != null)
					offers.set(tradeIndex, rerollMap[n]);
			}
		}
		MapTradesCache.FillCacheFromTrades(duck);
	}

	public boolean	CanReroll(int tradeIndex){
		return !depletedOnly 
		    || offers.size() <= tradeIndex
		    || offers.get(tradeIndex).isDisabled()
		    ;
	}

	/**
	 * @param rerollMap	Will attempt to reroll every non-null entry.
	 */
	private TradeOffer[]	DuplicataAwareReroll(Factory[] levelPool, TradeOffer[] rerollMap){
		var randomPool = new ArrayList<Factory>(levelPool.length);
		for (var f : levelPool)
			randomPool.add(f);

		for (int n=0; n<rerollMap.length && !randomPool.isEmpty(); ++n) 
		if  (rerollMap[n] != null)
		{
			int roll = random.nextInt(randomPool.size());
			TradeOffer offer = randomPool.get(roll).create(villager, random);
			randomPool.remove(roll);
			if (offer == null)
				ShiftingWares.LOGGER.warn("Failed to generate a valid offer for {} lvl.{} ({})", job, jobLevel, villager);
			else
				rerollMap[n] = offer;
		}
		return rerollMap;
	}

}
