package tk.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers.SellMapFactory;
import net.minecraft.world.gen.structure.Structure;
import tk.estecka.shiftingwares.IVillagerEntityDuck;
import tk.estecka.shiftingwares.MapTradesCache;
import tk.estecka.shiftingwares.ShiftingWares;

@Mixin(SellMapFactory.class)
public abstract class SellMapFactoryMixin
{
	@Shadow @Final private int price;
	@Shadow @Final private int maxUses;
	@Shadow @Final private int experience;
	@Shadow @Final private String nameKey;
	@Shadow @Final private TagKey<Structure> structure;

	@Inject( method="<init>", at=@At("HEAD"))
	private void	CreateNameAssociation(int price, TagKey<Structure> structure, String nameKey, MapIcon.Type iconType, int maxUses, int experience)
	{
		if (MapTradesCache.NAME_TO_STRUCT.containsKey(nameKey))
			ShiftingWares.LOGGER.warn("Duplicate map name association: \"{}\"", nameKey);
		MapTradesCache.NAME_TO_STRUCT.put(nameKey, structure);
	}

	@Inject( method="create", at=@At("HEAD"), cancellable=true )
	private void	restoreCached(Entity entity, Random random, CallbackInfoReturnable<TradeOffer> info){
		if (!(entity instanceof VillagerEntity))
			return;

		final var villagerDuck = (IVillagerEntityDuck)entity;
		var cachedMap = villagerDuck.GetCachedMap(this.structure.id().toString());
		if (cachedMap.isPresent()) {
			ItemStack stack = cachedMap.get();
			ShiftingWares.LOGGER.info("Reselling previously available map #{}", FilledMapItem.getMapId(stack));
			info.setReturnValue(new TradeOffer( new ItemStack(Items.EMERALD, this.price), new ItemStack(Items.COMPASS), stack, this.maxUses, this.experience, 0.2f ));
		}
	}

	
	@Inject( method="create", at=@At("RETURN") )
	private void	cacheCreated(Entity entity, Random random, CallbackInfoReturnable<TradeOffer> info){
		if (!(entity instanceof VillagerEntity))
			return;

		final var villager = (IVillagerEntityDuck)entity;
		TradeOffer offer = info.getReturnValue();
		if (offer != null)
			villager.AddCachedMap(this.structure.id().toString(), info.getReturnValue().getSellItem());
	}
}
