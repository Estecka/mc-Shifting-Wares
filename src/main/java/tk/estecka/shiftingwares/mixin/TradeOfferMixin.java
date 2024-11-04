package tk.estecka.shiftingwares.mixin;

import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.village.TradeOffer;
import tk.estecka.shiftingwares.ShiftingTradeData;
import tk.estecka.shiftingwares.duck.ITradeOfferDuck;

@Unique
@Mixin(TradeOffer.class)
public class TradeOfferMixin
implements ITradeOfferDuck
{
	private ShiftingTradeData data = new ShiftingTradeData();

	@Override
	public ShiftingTradeData shiftingwares$GetTradeData(){
		return this.data;
	}

	@Override
	public void shiftingwares$SetTradeData(ShiftingTradeData data){
		this.data = data;
	}

	@ModifyExpressionValue( method="<clinit>", at=@At(value="INVOKE", remap=false, target="com/mojang/serialization/codecs/RecordCodecBuilder.create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;") )
	static private Codec<TradeOffer> ExtendCodec(Codec<TradeOffer> original)
	{
		return RecordCodecBuilder.create(instance -> 
			instance.group(
				MapCodec.assumeMapUnsafe(original).forGetter(Function.identity()),
				ShiftingTradeData.CODEC.fieldOf("shiftingwares:tradeData").orElseGet(()->new ShiftingTradeData()).forGetter(offer -> ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData())
			)
			.apply(instance, (offer,data)->{
				ITradeOfferDuck.Of(offer).shiftingwares$SetTradeData(data);
				return offer;
			})
		);
	}
}
