package tk.estecka.shiftingwares;

import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import tk.estecka.shiftingwares.api.IShiftingTradeFactory;
import tk.estecka.shiftingwares.duck.ITradeOfferDuck;

public class ShiftingTradeData
{
	public int timesSold = 0;
	public boolean isPersistent = false;
	public Identifier tradeId = null;

	/**
	 * Should be called immediately after a factory has produced a new trade.
	 * TODO: villager level-up
	 */
	static public void FinalizeTrade(TradeOffer offer, TradeOffers.Factory factory){
		IShiftingTradeFactory factoryData = IShiftingTradeFactory.Of(factory);
		ShiftingTradeData data = new ShiftingTradeData();
		data.isPersistent = factoryData.shiftingwares$IsItemPersistent();
		data.tradeId = factoryData.shiftingwares$GetTradeId();
		data.timesSold = 0;

		ITradeOfferDuck.Of(offer).shiftingwares$SetTradeData(data);
	}
}
