package tk.estecka.shiftingwares;

import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import tk.estecka.shiftingwares.api.IShiftingTradeFactory;
import tk.estecka.shiftingwares.duck.ITradeOfferDuck;

public class ShiftingTradeData
{
	public int timesSold = 0;
	public boolean isPersistent = false;
	public Identifier tradeId = null;

	static public void InitializeTrade(TradeOffer offer, IShiftingTradeFactory factoryData){
		ShiftingTradeData data = new ShiftingTradeData();
		data.isPersistent = factoryData.shiftingwares$IsItemPersistent();
		data.tradeId = factoryData.shiftingwares$GetTradeId();
		data.timesSold = 0;

		ITradeOfferDuck.Of(offer).shiftingwares$SetTradeData(data);
	}
}
