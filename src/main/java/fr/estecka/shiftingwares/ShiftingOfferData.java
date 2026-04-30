package fr.estecka.shiftingwares;

import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.shiftingwares.duck.ITradeFactoryDuck;
import fr.estecka.shiftingwares.duck.ITradeOfferDuck;

public class ShiftingOfferData
{
	static public final Codec<ShiftingOfferData> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("tradeId").forGetter(data -> Optional.ofNullable(data.tradeId)),
			Codec.BOOL.fieldOf("isPersistent").orElse(false).forGetter(data -> data.isPersistent),
			Codec.BOOL.fieldOf("wasNeverUsed").orElse(true).forGetter(data -> data.wasNeverUsed)
		)
		.apply(instance, ShiftingOfferData::new)
	);

	/**
	 * The mod will never  leave this data null  when possible, but it may still
	 * be on trades that pre-date the mod's installation.
	*/
	public @Nullable Identifier tradeId = null;
	public boolean isPersistent = false;
	public boolean wasNeverUsed = true;

	public ShiftingOfferData(){};

	public ShiftingOfferData(Optional<Identifier> tradeId, boolean isPersistent, boolean wasNeverUsed){
		this.tradeId = tradeId.orElse(null);
		this.isPersistent = isPersistent;
		this.wasNeverUsed = wasNeverUsed;
	};

	static public ShiftingOfferData Of(MerchantOffer offer){
		return ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData();
	}


	/**
	 * Should be called immediately  after a factory  has produced  a new trade,
	 * regardless of context.
	 */
	static public void FinalizeOffer(@Nullable MerchantOffer offer, Identifier factoryId, VillagerTrade factory){
		if (offer == null)
			return;

		ITradeFactoryDuck factoryData = ITradeFactoryDuck.Of(factory);
		ShiftingOfferData offerData = ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData();

		offerData.isPersistent |= factoryData.shiftingwares$IsItemPersistent();
		offerData.tradeId = factoryId;

		if (offerData.isPersistent){
			ShiftingWaresMod.LOGGER.info(
				"Created new persistent trade: {} => {} ({})",
				factoryId,
				offer.getResult().getHoverName().getString(),
				offer.getResult().getItem()
			);
		}
		else
			EnforceAutoPersistence(offer);
	}

	static public void FinalizeOffer(MerchantOffer offer, Holder<VillagerTrade> factory){
		ShiftingOfferData.FinalizeOffer(
			offer,
			factory.unwrapKey().get().identifier(),
			factory.value()
		);
	}


	static public boolean EnforceAutoPersistence(MerchantOffer offer){
		ShiftingOfferData offerData = ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData();
		ItemStack sellItem = offer.getResult();
		if (offerData.isPersistent || !ShouldBePersistent(sellItem))
			return false;

		offerData.isPersistent = true;

		if (offerData.tradeId == null)
			ShiftingWaresMod.LOGGER.info("Marked an old trade as persistent: {} ({})", sellItem.getHoverName().getString(), sellItem.getItem());
		else if (!SilenceWarning(offerData.tradeId))
			ShiftingWaresMod.LOGGER.warn(
				"A trade unexpectedly produced a persistent item, but did not declare it as such: {} => {} ({})",
				offerData.tradeId,
				sellItem.getHoverName().getString(),
				sellItem.getItem()
			);
		else
			ShiftingWaresMod.LOGGER.info(
				"Auto-marked trade as persistent: {} => {} ({})",
				offerData.tradeId,
				sellItem.getHoverName().getString(),
				sellItem.getItem()
			);

		return true;
	}

	static public boolean ShouldBePersistent(ItemStack stack){
		return stack.has(DataComponents.MAP_ID);
	}

	static private final Pattern KNOWN_PERSISTENT = Pattern.compile("^cartographer/[0-9]+/emerald_and_compass_.*_map$");
	static public boolean SilenceWarning(Identifier tradeId){
		return tradeId.getNamespace().equals("minecraft")
		    && KNOWN_PERSISTENT.matcher(tradeId.getPath()).matches()
		    ;
	}
}
