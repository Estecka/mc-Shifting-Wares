package fr.estecka.shiftingwares.mixin;

import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.shiftingwares.duck.ITradeFactoryDuck;
import net.minecraft.world.item.trading.VillagerTrade;


@Unique
@Mixin(VillagerTrade.class)
public class TradeFactoryMixin
implements ITradeFactoryDuck
{
	private boolean shiftingwares$isPersistent = false;

	static private TradeFactoryMixin mixin(VillagerTrade trade){
		return (TradeFactoryMixin)(Object)trade;
	}

	public boolean shiftingwares$IsItemPersistent(){
		return this.shiftingwares$isPersistent;
	}

	@ModifyExpressionValue(
		method = "<clinit>",
		at = @At(
			value = "INVOKE",
			target = "com/mojang/serialization/codecs/RecordCodecBuilder.create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
		)
	)
	static private Codec<VillagerTrade> ExtendCodec(Codec<VillagerTrade> original)
	{
		return RecordCodecBuilder.create(instance -> 
			instance.group(
				MapCodec.assumeMapUnsafe(original).forGetter(Function.identity()),
				Codec.BOOL.fieldOf("shiftingwares:isPersistent").orElse(false).forGetter(factory -> mixin(factory).shiftingwares$isPersistent)
			)
			.apply(instance, (factory,isPersistent)->{
				mixin(factory).shiftingwares$isPersistent = isPersistent;
				return factory;
			})
		);
	}

}
