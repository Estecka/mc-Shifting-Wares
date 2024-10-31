package tk.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers.SellMapFactory;
import tk.estecka.shiftingwares.api.IShiftingTradeFactory;

@Mixin(SellMapFactory.class)
public class SellMapFactoryMixin
implements IShiftingTradeFactory
{
	@Shadow @Final private String nameKey;

	@Override
	public boolean shiftingwares$IsItemPersistent(){
		return true;
	}

	@Override
	public Identifier shiftingwares$GetTradeId(){
		return Identifier.ofVanilla(nameKey);
	}
}
