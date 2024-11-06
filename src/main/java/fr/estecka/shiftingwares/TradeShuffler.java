package fr.estecka.shiftingwares;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradeOffers.Factory;
import net.minecraft.village.VillagerProfession;
import fr.estecka.shiftingwares.TradeLayouts.VanillaTradeLayout;
import fr.estecka.shiftingwares.api.IShiftingTradeFactory;
import fr.estecka.shiftingwares.api.ITradeLayoutProvider;
import fr.estecka.shiftingwares.duck.ITradeOfferDuck;

public class TradeShuffler 
{
	static public final ITradeLayoutProvider VANILLA_LAYOUT  = new VanillaTradeLayout();

	private final VillagerEntity villager;
	private final boolean depletedOnly;
	private final boolean keepPersistent;

	private final VillagerProfession job;

	private final Random random;
	private final TradeOfferList offers;
	private final List<Factory[]> tradeLayout;

	public TradeShuffler(VillagerEntity villager, boolean depletedOnly)
	{
		this.villager = villager;
		this.depletedOnly = depletedOnly;

		this.keepPersistent = !villager.getServer().getGameRules().getBoolean(ShiftingWares.MAP_RULE);
		this.offers = villager.getOffers();
		this.job = villager.getVillagerData().getProfession();
		this.random = villager.getRandom();

		this.tradeLayout = GetTradeLayout(villager);
	}
	
	static public List<TradeOffers.Factory[]> GetTradeLayout(VillagerEntity villager){
		var providers = FabricLoader.getInstance().getEntrypoints(ShiftingWares.MODID, ITradeLayoutProvider.class);

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

		// Fix uninitialized trades, and update used trades.
		for (TradeOffer offer : offers){
			ShiftingTradeData data = ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData();
			FixTradeData(data, offer.getSellItem());
			if (offer.hasBeenUsed())
				data.wasNeverUsed = false;
		}

		// Trim superfluous trades
		for (int i=offers.size()-1; tradeLayout.size()<=i; --i)
			if (shouldReroll(i))
				offers.remove(i);

		// Reserve space for new trades
		while(offers.size() < tradeLayout.size())
			offers.add(ShiftingWares.PLACEHOLDER_TRADE);

		DuplicataAwareReroll();
	}

	public boolean	shouldReroll(int tradeIndex){
		if (offers.size() <= tradeIndex)
			return true;
		
		TradeOffer offer = offers.get(tradeIndex);
		ShiftingTradeData data = ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData();
		if (data.isPersistent && (data.wasNeverUsed || keepPersistent))
			return false;

		return !this.depletedOnly || offer.isDisabled();
	}

	/**
	 * Find trades  that *should* be persistent, but were not marked as such  by
	 * their factories. This may happen if:
	 * - ShiftingWares  has been installed  for the first time  on a world where
	 * existing villagers were already selling maps.
	 * - Another mod uses  a custom  map trade factory, but does not communicate
	 * it to ShiftingWares.
	 */
	static private void FixTradeData(ShiftingTradeData data, ItemStack sellItem){
		if (sellItem.contains(DataComponentTypes.MAP_ID)
		&& !data.isPersistent
		){
			data.isPersistent = true;
			ShiftingWares.LOGGER.warn("Forcibly marked a trade offer as persistent due to having a map Id:\n{}", sellItem);
		}
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
						if (tradeId != null){
							activeTrades.add(tradeId);
							ShiftingWares.LOGGER.warn("Added trade: {}", tradeId);
						}
					}
				}
				else
					ShiftingWares.LOGGER.warn("Skipped trade: {}", tradeId);
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
