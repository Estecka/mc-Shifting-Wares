package tk.estecka.shiftingwares;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradeOffers.Factory;
import tk.estecka.shiftingwares.TradeLayouts.VanillaTradeLayout;
import tk.estecka.shiftingwares.api.IShiftingTradeFactory;
import tk.estecka.shiftingwares.api.ITradeLayoutProvider;
import tk.estecka.shiftingwares.duck.ITradeOfferDuck;
import tk.estecka.shiftingwares.duck.IVillagerEntityDuck;
import net.minecraft.village.VillagerProfession;

public class TradeShuffler 
{
	static public final ITradeLayoutProvider VANILLA_LAYOUT  = new VanillaTradeLayout();

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
		this.tradeCache = IVillagerEntityDuck.Of(villager).shiftingwares$GetItemCache();

		this.tradeLayout = GetTradeLayout(villager);
	}
	
	static public List<TradeOffers.Factory[]> GetTradeLayout(VillagerEntity villager){
		var providers = FabricLoader.getInstance().getEntrypoints("shifting-wares", ITradeLayoutProvider.class);

		for (var p : providers) {
			var layout = p.GetTradeLayout(villager);
			if (layout != null)
				return layout;
		}

		return VANILLA_LAYOUT.GetTradeLayout(villager);
	}

	public void	Reroll(){
		if (tradeLayout == null){
			ShiftingWares.LOGGER.error("Missing layout, villager will not be rerolled: {} ({})", job, villager);
			return;
		}

		tradeCache.FillCacheFromTrades(offers);

		// Fix uninitialized trades
		// TODO: handle map trades differently
		for (TradeOffer offer : offers)
			if (ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData() == null)
				ITradeOfferDuck.Of(offer).shiftingwares$SetTradeData(new ShiftingTradeData());

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
		if (offers.size() <= tradeIndex)
			return true;
		
		TradeOffer offer = offers.get(tradeIndex);
		if (ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData().isPersistent)
			return false;

		return !this.depletedOnly || offer.isDisabled();
	}

	static private List<Factory>[] MutableCopy(List<Factory[]> layout){
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
		Set<Identifier> activeTrades = new HashSet<>();
		boolean missingSome = false;

		for (int i=0; i<offers.size(); ++i)
		if  (!shouldReroll(i)) {
			ShiftingTradeData data = ITradeOfferDuck.Of(offers.get(i)).shiftingwares$GetTradeData();
			if (data.tradeId != null)
				activeTrades.add(data.tradeId);
		}

		for (int i=0; i<offers.size(); ++i) 
		if (shouldReroll(i))
		{
			List<Factory> pool = mutableLayout[i];
			TradeOffer offer = null;

			while (offer == null && !pool.isEmpty()) {
				int roll = random.nextInt(pool.size());
				Factory factory = pool.get(roll);
				pool.remove(roll);

				Identifier tradeId = IShiftingTradeFactory.Of(factory).shiftingwares$GetTradeId();
				if (tradeId == null || !activeTrades.contains(tradeId)){
					offer = factory.create(villager, random);
					if (offer != null){
						ShiftingTradeData.FinalizeTrade(offer, factory);
						if (tradeId != null)
							activeTrades.add(tradeId);
					}
				}
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
