package fr.estecka.shiftingwares.duck;

import fr.estecka.shiftingwares.ShiftingOfferData;
import net.minecraft.world.item.trading.MerchantOffer;

public interface ITradeOfferDuck
{
	static public ITradeOfferDuck Of(MerchantOffer offer){
		return (ITradeOfferDuck)offer;
	}

	ShiftingOfferData shiftingwares$GetTradeData();
	void shiftingwares$SetTradeData(ShiftingOfferData data);
}
