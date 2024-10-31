package tk.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.village.TradeOffer;
import tk.estecka.shiftingwares.ShiftingTradeData;
import tk.estecka.shiftingwares.duck.ITradeOfferDuck;

@Unique
@Mixin(TradeOffer.class)
public class TradeOfferMixin
implements ITradeOfferDuck
{
	private ShiftingTradeData data = null;

	@Override
	public ShiftingTradeData shiftingwares$GetTradeData(){
		return this.data;
	}

	@Override
	public void shiftingwares$SetTradeData(ShiftingTradeData data){
		this.data = data;
	}
}
