package tk.estecka.shiftingwares;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
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

		for (int jobLevel=0, i=0; i<offers.size(); ++jobLevel)
		{
			if (jobPool.size() <= jobLevel){
				ShiftingWares.LOGGER.error("Not enough trade pools to fully reroll {} ({}) after index {}", villager, job, i);
				break;
			}

			boolean[] shouldReroll = new boolean[2];
			shouldReroll[0] = !depletedOnly || offers.get(i).isDisabled();
			shouldReroll[1] = i+1<offers.size() && ( !depletedOnly || offers.get(i+1).isDisabled() );
			int amount = 0;
			for (int n=0; n<2; ++n)
				amount += shouldReroll[n] ? 1 : 0;
			if (amount <= 0){
				i += 2;
				continue;
			}

			Factory[] levelPool = jobPool.get(jobLevel+1);
			if (levelPool == null) {
				ShiftingWares.LOGGER.error("No trade pool for job level: {} lvl{}", job, jobLevel);
				continue;
			}
			else if (levelPool.length < 1) {
				ShiftingWares.LOGGER.error("Empty trade pool for job level: {} lvl{}", job, jobLevel);
				continue;
			}

			var newOffers = DuplicataAwareReroll(levelPool, amount, jobLevel);
			for (int n=0; n<2; ++n) {
				if (shouldReroll[n] && !newOffers.isEmpty()){
					offers.set(i, newOffers.get(0));
					newOffers.remove(0);
					++i;
				}
				else if (!shouldReroll[n])
					++i;
			}
		}
	}

	@Nullable
	private TradeOffer	SingleReroll(Factory[] levelPool){
		TradeOffers.Factory result = levelPool[random.nextInt(levelPool.length)];
		return result.create(villager, random);
	}

	/**
	 * May generate less than the requested amount, to try accounting for 
	 * cartographers generating less trades in worlds with no mappable 
	 * structure.
	 */
	private List<TradeOffer>	DuplicataAwareReroll(Factory[] levelPool, int amount, int jobLevel){
		var result = new ArrayList<TradeOffer>(2);
		var randomPool = new ArrayList<Factory>(levelPool.length);
		for (var f : levelPool)
			randomPool.add(f);

		for (int i=0; i<amount; ++i) {
			if (randomPool.isEmpty()){
				ShiftingWares.LOGGER.error("Trade pool is smaller than the number of trades for this job level: {} lvl{}", job, jobLevel);
				break;
			}
			else {
				int roll = random.nextInt(randomPool.size());
				TradeOffer offer = randomPool.get(roll).create(villager, random);
				randomPool.remove(roll);
				if (offer != null)
					result.add(offer);
				else
					ShiftingWares.LOGGER.warn("Failed to generate a valid offer for {} ({}) lvl{}", villager, job, jobLevel);
			}
		}
		return result;
	}

}
