package tk.estecka.shiftingwares;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
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

	private final Random random;
	private final TradeOfferList offers;
	private final List<Factory[]> tradeLayout;
	private final MapTradesCache tradeCache;

	public TradeShuffler(VillagerEntity villager, boolean depletedOnly)
	{
		this.villager = villager;
		this.depletedOnly = depletedOnly;

		this.offers = villager.getOffers();
		this.job = villager.getVillagerData().getProfession();
		this.random = villager.getRandom();
		this.tradeCache = ((IVillagerEntityDuck)villager).shiftingwares$GetTradeCache();

		this.tradeLayout = ShiftingWares.TRADE_LAYOUT_PROVIDER.GetTradeLayout(villager);
	}

	public void	Reroll(){
		if (tradeLayout == null){
			ShiftingWares.LOGGER.error("Missing layout, villager will not be rerolled: {} ({})", job, villager);
			return;
		}

		tradeCache.FillCacheFromTrades(offers);

		// Trim superfluous trades
		for (int i=offers.size()-1; tradeLayout.size()<=i; --i)
			if (shouldReroll(i))
				offers.remove(i);

		// Reserve space for new trades
		while(offers.size() < tradeLayout.size())
			offers.add(ShiftingWares.PLACEHOLDER_TRADE);

		DuplicataAwareReroll();

		tradeCache.FillCacheFromTrades(offers);
	}

	public boolean	shouldReroll(int tradeIndex){
		return !depletedOnly 
		    || offers.size() <= tradeIndex
		    || offers.get(tradeIndex).isDisabled()
		    ;
	}

	List<Factory>[] MutableCopy(List<Factory[]> layout){
		IdentityHashMap<Factory[], ArrayList<Factory>> mutablePools = new IdentityHashMap<>();
		mutablePools.put(null, new ArrayList<>(0));

		for (var pool : layout)
		if (!mutablePools.containsKey(pool))
		{
			var mpool = new ArrayList<Factory>(pool.length);
			for (var f : pool)
				mpool.add(f);
			mutablePools.put(pool, mpool);
		}

		@SuppressWarnings("unchecked")
		List<Factory>[] workspace = new List[layout.size()];
		for (int i=0; i<workspace.length; ++i)
			workspace[i] = mutablePools.get(layout.get(i));

		return workspace;
	}

	private void	DuplicataAwareReroll(){
		List<Factory>[] mutableLayout = MutableCopy(this.tradeLayout);
		boolean missingSome = false;

		for (int i=0; i<offers.size(); ++i) 
		if (shouldReroll(i))
		{
			var pool = mutableLayout[i];
			TradeOffer offer = null;

			while (offer == null && !pool.isEmpty()) {
				int roll = random.nextInt(pool.size());
				offer = pool.get(roll).create(villager, random);
				pool.remove(roll);
			}
			if (offer == null){
				offer = ShiftingWares.PLACEHOLDER_TRADE;
				missingSome = true;
			}

			offers.set(i, offer);
		}

		if (missingSome)
			ShiftingWares.LOGGER.warn("Failed to generate some trade offers for job {} ({})", job, villager);
	}

}
