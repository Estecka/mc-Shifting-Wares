package tk.estecka.shiftingwares;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import tk.estecka.shiftingwares.api.IShiftingTradeFactory;
import tk.estecka.shiftingwares.duck.ITradeOfferDuck;

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

	public Identifier tradeId = null;
	public boolean isPersistent = false;
	public boolean wasNeverUsed = true;

	public ShiftingTradeData(){};

	public ShiftingTradeData(Optional<Identifier> tradeId, boolean isPersistent, boolean wasNeverUsed){
		this.tradeId = tradeId.orElse(null);
		this.isPersistent = isPersistent;
		this.wasNeverUsed = wasNeverUsed;
	};


	/**
	 * Should be called immediately after a factory has produced a new trade.
	 * TODO: villager level-up
	 */
	static public void FinalizeTrade(TradeOffer offer, TradeOffers.Factory factory){
		IShiftingTradeFactory factoryData = IShiftingTradeFactory.Of(factory);
		ShiftingTradeData data = new ShiftingTradeData();
		data.isPersistent = factoryData.shiftingwares$IsItemPersistent();
		data.tradeId = factoryData.shiftingwares$GetTradeId();

		ITradeOfferDuck.Of(offer).shiftingwares$SetTradeData(data);
	}
}
