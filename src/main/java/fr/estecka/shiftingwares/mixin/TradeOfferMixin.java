package fr.estecka.shiftingwares.mixin;

import java.util.function.Function;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.shiftingwares.ShiftingOfferData;
import fr.estecka.shiftingwares.duck.ITradeOfferDuck;

@Unique
@Mixin(MerchantOffer.class)
public class TradeOfferMixin
implements ITradeOfferDuck
{
	private ShiftingOfferData data = new ShiftingOfferData();

	@Override
	public ShiftingOfferData shiftingwares$GetTradeData(){
		return this.data;
	}

	@Override
	public void shiftingwares$SetTradeData(ShiftingOfferData data){
		this.data = data;
	}

	@ModifyExpressionValue(
		method = "<clinit>",
		at = @At(
			value = "INVOKE",
			target = "com/mojang/serialization/codecs/RecordCodecBuilder.create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
		)
	)
	static private Codec<MerchantOffer> ExtendCodec(Codec<MerchantOffer> original)
	{
		return RecordCodecBuilder.create(instance -> 
			instance.group(
				MapCodec.assumeMapUnsafe(original).forGetter(Function.identity()),
				ShiftingOfferData.CODEC.fieldOf("shiftingwares:tradeData").orElseGet(()->new ShiftingOfferData()).forGetter(offer -> ITradeOfferDuck.Of(offer).shiftingwares$GetTradeData())
			)
			.apply(instance, (offer,data)->{
				ITradeOfferDuck.Of(offer).shiftingwares$SetTradeData(data);
				return offer;
			})
		);
	}

	@Inject( method="increaseUses", at=@At("TAIL") )
	private void use(CallbackInfo ci){
		this.data.wasNeverUsed = false;
	}
}
