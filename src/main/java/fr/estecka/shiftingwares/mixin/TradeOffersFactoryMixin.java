package fr.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.village.TradeOffers;
import fr.estecka.shiftingwares.api.IShiftingTradeFactory;

@Mixin(TradeOffers.Factory.class)
public interface TradeOffersFactoryMixin
extends IShiftingTradeFactory
{}
