package fr.estecka.shiftingwares;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;


public class TradeShuffler
{
	static public final MerchantOffer PLACEHOLDER_TRADE;
	static {
		ItemCost fake_air = new ItemCost(Items.EMERALD).withComponents( builder -> {
			builder.expect(DataComponents.ITEM_NAME, Component.literal("Unobtainium"));
			builder.expect(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
			builder.expect(DataComponents.ITEM_MODEL, Identifier.withDefaultNamespace("air"));
			return builder;
		});

		PLACEHOLDER_TRADE = new MerchantOffer(
			fake_air,
			fake_air.itemStack(),
			0, 0, 0
		);
	}

	private final boolean depletedOnly;
	private final boolean keepPersistent;

	private final MerchantOffers offers;
	private final List<SlotRerollSource> tradeLayout;

	public TradeShuffler(Villager villager, boolean depletedOnly)
	{
		this.depletedOnly = depletedOnly;

		this.keepPersistent = !ShiftingWaresMod.GetBoolean(villager, ShiftingWaresMod.PERSISTENT_RULE);
		this.offers = villager.getOffers();

		this.tradeLayout = SlotRerollSource.CreateTradeLayout(villager);
	}

	public void	Reroll(){
		// Update or initialize trade data
		for (MerchantOffer offer : offers){
			ShiftingOfferData.EnforceAutoPersistence(offer);

			ShiftingOfferData data = ShiftingOfferData.Of(offer);
			if (offer.needsRestock())
				data.wasNeverUsed = false;
		}

		// Trim superfluous trades
		for (int i=offers.size()-1; tradeLayout.size()<=i; --i)
			if (ShouldReroll(i))
				offers.remove(i);

		// Reserve space for new trades
		while (offers.size() < tradeLayout.size())
			offers.add(PLACEHOLDER_TRADE);

		// Prevent duplication of non-rerolled trades.
		for (int i=0; i<offers.size(); ++i)
			if (!ShouldReroll(i))
				tradeLayout.get(i).DeduplicateOffer(offers.get(i));

		// Roll new trades and add them to the listing
		for (int i=0; i<offers.size(); ++i) 
		if  (ShouldReroll(i))
		{
			SlotRerollSource slotReroll = tradeLayout.get(i);
			MerchantOffer offer = slotReroll.GetNextOffer().orElse(PLACEHOLDER_TRADE);
			offers.set(i, offer);
		}
	}

	public boolean ShouldReroll(int slotIndex){
		if (offers.size() <= slotIndex)
			return true;
		
		MerchantOffer offer = offers.get(slotIndex);
		ShiftingOfferData data = ShiftingOfferData.Of(offer);
		if (data.isPersistent && (data.wasNeverUsed || keepPersistent))
			return false;

		return !this.depletedOnly || offer.isOutOfStock();
	}

}
