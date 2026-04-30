package fr.estecka.shiftingwares;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;


public record SlotRerollSource(
	TradeSet tradeSet,
	LootContext lootContext,
	Map<Identifier,VillagerTrade> elligibleTrades
){
/******************************************************************************/
/* # Construction                                                             */
/******************************************************************************/
	static public SlotRerollSource FromSet(Villager villager, int level)
	{
		var setKey = villager.getVillagerData().profession().value().getTrades(level);
		TradeSet tradeSet = villager.registryAccess()
			.lookupOrThrow(Registries.TRADE_SET)
			.getOptional(setKey)
			.get()
			;

		Map<Identifier,VillagerTrade> tradePool = new HashMap<>();
		for (Holder<VillagerTrade> tradeEntry : tradeSet.getTrades())
			tradePool.put(tradeEntry.unwrapKey().get().identifier(), tradeEntry.value());

		LootContext lootContext = CreateLootContext(villager, tradeSet);
		return new SlotRerollSource(tradeSet, lootContext, tradePool);
	}

	static public List<SlotRerollSource> CreateTradeLayout(Villager villager)
	{
		final List<SlotRerollSource> layout = new ArrayList<>();
		final VillagerProfession profession = villager.getVillagerData().profession().value();
		final Identifier professionId = villager.getVillagerData().profession().unwrapKey().get().identifier();
		final int villagerLevel = villager.getVillagerData().level();

		Int2ObjectMap<SlotRerollSource> leveledSources = new Int2ObjectOpenHashMap<>();
		for (int lvl : profession.tradeSetsByLevel().keySet())
			if (lvl <= villagerLevel)
				leveledSources.put(lvl, SlotRerollSource.FromSet(villager, lvl));

		for (int lvl=VillagerData.MIN_VILLAGER_LEVEL; lvl<=villagerLevel; ++lvl)
		if  (!leveledSources.containsKey(lvl))
			ShiftingWaresMod.LOGGER.warn("No trade set for {} lvl {}", professionId, lvl);
		else
		{
			SlotRerollSource source = leveledSources.get(lvl);
			int setSize = source.tradeSet.calculateNumberOfTrades(source.lootContext);

			// Prevent unnecessary placeholder trades.
			if (!source.AllowsDuplicates())
				setSize = Math.min(setSize, source.elligibleTrades.size());

			for (int i=0; i<setSize; ++i)
				layout.add(source);
		}

		return layout;
	}

	static public LootContext CreateLootContext(Villager villager, TradeSet tradeSet)
	{
		return new LootContext.Builder(
			new LootParams.Builder((ServerLevel)villager.level())
				.withParameter(LootContextParams.ORIGIN, villager.position())
				.withParameter(LootContextParams.THIS_ENTITY, villager)
				.withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
				.create(LootContextParamSets.VILLAGER_TRADE)
			)
			.create(tradeSet.randomSequence());
	}


/******************************************************************************/
/* # Rerolls                                                                  */
/******************************************************************************/

	public boolean AllowsDuplicates(){
		return this.tradeSet.allowDuplicates();
	}

	public void DisqualifyFactory(Identifier tradeId){
		this.elligibleTrades.remove(tradeId);
	}

	public void DeduplicateOffer(MerchantOffer offer){
		ShiftingOfferData offerData =  ShiftingOfferData.Of(offer);
		if (offerData.tradeId != null && !this.AllowsDuplicates())
			this.DisqualifyFactory(offerData.tradeId);
	}

	public Optional<MerchantOffer> GetNextOffer(){
		Identifier tradeId = null;
		VillagerTrade factory = null;
		MerchantOffer offer = null;

		while (offer == null && !elligibleTrades.isEmpty()) {
			Identifier[] idPool = elligibleTrades.keySet().toArray(Identifier[]::new);
			int roll = lootContext.getRandom().nextInt(idPool.length);

			tradeId = idPool[roll];
			factory = elligibleTrades.get(tradeId);
			offer = factory.getOffer(lootContext);

			// Prevent endless rerolling of failing trades.
			if (offer == null)
				DisqualifyFactory(tradeId);
		}

		if (offer != null){
			ShiftingOfferData.FinalizeOffer(offer, tradeId, factory);
			DeduplicateOffer(offer);
		}

		return Optional.ofNullable(offer);
	}
}
