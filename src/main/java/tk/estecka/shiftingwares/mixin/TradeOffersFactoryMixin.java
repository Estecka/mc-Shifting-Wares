package tk.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.village.TradeOffers;
import tk.estecka.shiftingwares.api.IShiftingTradeFactory;

@Mixin(TradeOffers.Factory.class)
public interface TradeOffersFactoryMixin
extends IShiftingTradeFactory
{}
