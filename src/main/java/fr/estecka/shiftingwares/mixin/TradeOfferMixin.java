package fr.estecka.shiftingwares.mixin;

import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.village.TradeOffer;
import fr.estecka.shiftingwares.ShiftingTradeData;
import fr.estecka.shiftingwares.ShiftingWaresMod;
import fr.estecka.shiftingwares.duck.ITradeOfferDuck;

@Unique
@Mixin(TradeOffer.class)
public class TradeOfferMixin
implements ITradeOfferDuck
{
	static private final Codec<Optional<ShiftingTradeData>> DATA_CODEC = ShiftingTradeData.CODEC.optionalFieldOf("shiftingwares:tradeData").codec();

	private ShiftingTradeData data = new ShiftingTradeData();

	@Override
	public ShiftingTradeData shiftingwares$GetTradeData(){
		return this.data;
	}

	@Override
	public void shiftingwares$SetTradeData(ShiftingTradeData data){
		this.data = data;
	}

	@Inject( method="use", at=@At("TAIL") )
	private void use(CallbackInfo ci){
		this.data.wasNeverUsed = false;
	}

	@Inject( method="<init>", at=@At("TAIL") )
	private void DataFromfromNbt(NbtCompound nbt, CallbackInfo ci){
		var result = DATA_CODEC.decode(NbtOps.INSTANCE, nbt);
		var error = result.error();
		if (error.isPresent())
			ShiftingWaresMod.LOGGER.error("Couldn't decode trade offer Shifting-Data: {}", error);
		else
			this.data = result.get().left().get().getFirst().orElseGet(ShiftingTradeData::new);
	}

	@ModifyReturnValue( method="toNbt", at=@At("TAIL") )
	private NbtCompound DataToNbt(NbtCompound nbt){
		var result = DATA_CODEC.encode(Optional.of(this.data), NbtOps.INSTANCE, nbt);
		var error = result.error();
		if (error.isPresent())
			ShiftingWaresMod.LOGGER.error("Couldn't encode trade offer Shifting-Data: {}", error);
		else
			nbt = (NbtCompound)result.get().left().get();

		return nbt;
	}
}
