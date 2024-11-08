package fr.estecka.shiftingwares.duck;

import net.minecraft.village.TradeOffer;
import fr.estecka.shiftingwares.ShiftingTradeData;

public interface ITradeOfferDuck
{
	static public ITradeOfferDuck Of(TradeOffer offer){
		return (ITradeOfferDuck)offer;
	}

	ShiftingTradeData shiftingwares$GetTradeData();
	void shiftingwares$SetTradeData(ShiftingTradeData data);
}
