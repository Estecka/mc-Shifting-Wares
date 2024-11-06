package fr.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers.SellMapFactory;
import net.minecraft.world.gen.structure.Structure;
import fr.estecka.shiftingwares.api.IShiftingTradeFactory;

@Mixin(SellMapFactory.class)
public class SellMapFactoryMixin
implements IShiftingTradeFactory
{
	@Shadow @Final private TagKey<Structure> structure;

	@Override
	public boolean shiftingwares$IsItemPersistent(){
		return true;
	}

	@Override
	public Identifier shiftingwares$GetTradeId(){
		return structure.id();
	}
}
