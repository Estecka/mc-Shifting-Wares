package fr.estecka.shiftingwares;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
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

	private final RegistryEntry<VillagerProfession> job;

	private final Random random;
	private final TradeOfferList offers;
	private final List<Factory[]> tradeLayout;

	public TradeShuffler(VillagerEntity villager, boolean depletedOnly)
	{
		this.villager = villager;
		this.depletedOnly = depletedOnly;

		this.keepPersistent = !ShiftingWaresMod.GetBoolean(villager, ShiftingWaresMod.MAP_RULE);
		this.offers = villager.getOffers();
		this.job = villager.getVillagerData().profession();
		this.random = villager.getRandom();

		this.tradeLayout = GetTradeLayout(villager);
	}
	
	static public List<TradeOffers.Factory[]> GetTradeLayout(VillagerEntity villager){
		var providers = FabricLoader.getInstance().getEntrypoints(ShiftingWaresMod.MODID, ITradeLayoutProvider.class);

		for (var p : providers) {
			var layout = p.GetTradeLayout(villager);
			if (layout != null)
				return layout;
		}

		return VANILLA_LAYOUT.GetTradeLayout(villager);
	}

	public void	Reroll(){
		if (tradeLayout == null){
			ShiftingWaresMod.LOGGER.error("Missing layout, villager will not be rerolled: {} ({})", job, villager);
			return;
		}

		// Update or initialize trade data
		for (TradeOffer offer : offers){
			ShiftingTradeData data = ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData();

			ItemStack sellItem = offer.getSellItem();
			if (!data.isPersistent && ShiftingTradeData.ShouldBePersistent(sellItem))
				ShiftingWaresMod.LOGGER.warn("Caught an older unitialized persistent trade: {} ({})", sellItem.getName().getString(), sellItem.getItem());

			if (offer.hasBeenUsed())
				data.wasNeverUsed = false;
		}

		// Trim superfluous trades
		for (int i=offers.size()-1; tradeLayout.size()<=i; --i)
			if (shouldReroll(i))
				offers.remove(i);

		// Reserve space for new trades
		while(offers.size() < tradeLayout.size())
			offers.add(ShiftingWaresMod.PLACEHOLDER_TRADE);

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
		// boolean missingSome = false;

		for (int i=0; i<offers.size(); ++i)
		if  (!shouldReroll(i)) {
			ShiftingTradeData data = ITradeOfferDuck.Of(offers.get(i)).shiftingwares$GetTradeData();
			if (data.tradeId != null)
				activeTrades.add(data.tradeId);
		}

		for (int i=0; i<offers.size(); ++i) 
		if (shouldReroll(i))
		{
			Factory factory = null;
			TradeOffer offer = null;
			List<Factory> pool = mutableLayout[i];
			pool.removeIf( f -> {
				Identifier tradeId = IShiftingTradeFactory.Of(f).shiftingwares$GetTradeId();
				return tradeId != null && activeTrades.contains(tradeId);
			});

			while (offer == null && !pool.isEmpty()) {
				int roll = random.nextInt(pool.size());
				factory = pool.get(roll);
				offer = factory.create(villager, random);
				pool.remove(roll);
			}

			if (offer == null){
				offer = ShiftingWaresMod.PLACEHOLDER_TRADE;
				// missingSome = true;
			}
			else {
				ShiftingTradeData.FinalizeTrade(offer, factory);
				ShiftingTradeData data = ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData();
				if (data.tradeId != null)
					activeTrades.add(data.tradeId);
			}

			offers.set(i, offer);
		}

		// if (missingSome)
		// 	ShiftingWares.LOGGER.warn("Failed to generate some trade offers for job {} ({})", job, villager);
	}

}
