package tk.estecka.shiftingwares.mixin;

import java.util.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerType;
import net.minecraft.village.TradeOffers.TypedWrapperFactory;
import tk.estecka.shiftingwares.api.IShiftingTradeFactory;

@Mixin(TypedWrapperFactory.class)
public class WrapperFactoryMixin
implements IShiftingTradeFactory
{
	@Shadow private @Final Map<VillagerType, TradeOffers.Factory> typeToFactory;

	/**
	 * The returned  ids  may be  "wrong"  for the  trades  that are  eventually
	 * produced, but this works for the purpose of avoiding duplicatas since the
	 * trade offers only needs to be associated with the outer-most factory.
	 */
	@Override
	public Identifier shiftingwares$GetTradeId(){
		for (var factory : this.typeToFactory.values()){
			Identifier id = IShiftingTradeFactory.Of(factory).shiftingwares$GetTradeId();
			if (id != null)
				return id;
		}

		return null;
	}
}
