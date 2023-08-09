package tk.estecka.shiftingwares;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradeOffers.Factory;
import net.minecraft.village.VillagerProfession;

public class TradeShuffler 
{
	private final VillagerEntity villager;
	private final boolean depletedOnly;

	private final VillagerProfession job;
	private final Random random;
	private final TradeOfferList offers;
	private final Int2ObjectMap<Factory[]> jobPool;

	public TradeShuffler(VillagerEntity villager, boolean depletedOnly)
	{
		this.villager = villager;
		this.depletedOnly = depletedOnly;

		this.offers = villager.getOffers();
		this.job = villager.getVillagerData().getProfession();
		this.jobPool = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(job);
		this.random = villager.getRandom();
	}

	public void	Reroll(){
		if (jobPool == null){
			ShiftingWares.LOGGER.error("No trade pool for job: {}", job);
			return;
		}

		for (int jobLevel=0; (2*jobLevel)<offers.size(); jobLevel++){
			int i1 = jobLevel*2;
			int i2 = i1 + 1;

			boolean b1 = !depletedOnly || offers.get(i1).isDisabled();
			boolean b2 = i2<offers.size() && ( !depletedOnly || offers.get(i2).isDisabled() );

			
			Factory[] levelPool = jobPool.get(jobLevel+1);
			if (levelPool == null)
				ShiftingWares.LOGGER.error("No trade pool for job level: {} lvl{}", job, jobLevel);
			else if (levelPool.length < 1)
				ShiftingWares.LOGGER.error("Empty trade pool for job level: {} lvl{}", job, jobLevel);
			else if (b1 && b2)
				DuplicataAwareReroll(i1, i2, levelPool);
			else if (b1)
				SingleReroll(i1, levelPool);
			else if (b2)
				SingleReroll(i2, levelPool);
		}

	}

	private void	SingleReroll(int i, Factory[] levelPool){
		TradeOffers.Factory result = levelPool[random.nextInt(levelPool.length)];
		offers.set(i, result.create(villager, random));
	}

	private void	DuplicataAwareReroll(int i1, int i2, Factory[] levelPool){
		if (levelPool.length >= 2) 
		{
			List<Factory> randomPool = new ArrayList<Factory>(levelPool.length);
			for (var f : levelPool)
				randomPool.add(f);
			
			int roll;
			roll = random.nextInt(randomPool.size());
			offers.set(i1, randomPool.get(roll).create(villager, random));

			randomPool.remove(roll);

			roll = random.nextInt(randomPool.size());
			offers.set(i2, randomPool.get(roll).create(villager, random));
		}
		else 
		{
			ShiftingWares.LOGGER.warn("Trade pool is smaller than the number of trades for this job level: {} lvl{}", job, i1/2);
			offers.set(i1, levelPool[0].create(villager, random));
			offers.set(i2, levelPool[0].create(villager, random));
		}
	}
	
}
