package fr.estecka.shiftingwares;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import fr.estecka.shiftingwares.api.IShiftingTradeFactory;
import fr.estecka.shiftingwares.duck.ITradeOfferDuck;

public class ShiftingTradeData
{
	static public final Codec<ShiftingTradeData> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("tradeId").forGetter(data -> Optional.ofNullable(data.tradeId)),
			Codec.BOOL.fieldOf("isPersistent").orElse(false).forGetter(data -> data.isPersistent),
			Codec.BOOL.fieldOf("wasNeverUsed").orElse(true).forGetter(data -> data.wasNeverUsed)
		)
		.apply(instance, ShiftingTradeData::new)
	);

	public @Nullable Identifier tradeId = null;
	public boolean isPersistent = false;
	public boolean wasNeverUsed = true;

	public ShiftingTradeData(){};

	public ShiftingTradeData(Optional<Identifier> tradeId, boolean isPersistent, boolean wasNeverUsed){
		this.tradeId = tradeId.orElse(null);
		this.isPersistent = isPersistent;
		this.wasNeverUsed = wasNeverUsed;
	};


	/**
	 * Should be called immediately  after a factory  has produced  a new trade.
	 * It's possible  for factories to wrap  other factories; persistence should 
	 * be preserved down the line.
	 * Here, trade ids are being  preserved as much as possible, but this has no
	 * use currently, since SW will not be able to identify the inner factories.
	 */
	static public void FinalizeTrade(TradeOffer offer, TradeOffers.Factory factory){
		IShiftingTradeFactory factoryData = IShiftingTradeFactory.Of(factory);
		ShiftingTradeData offerData = ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData();

		offerData.isPersistent |= factoryData.shiftingwares$IsItemPersistent();

		Identifier id = factoryData.shiftingwares$GetTradeId();
		if (id != null)
			offerData.tradeId = id;

		ITradeOfferDuck.Of(offer).shiftingwares$SetTradeData(offerData);
	}
}
