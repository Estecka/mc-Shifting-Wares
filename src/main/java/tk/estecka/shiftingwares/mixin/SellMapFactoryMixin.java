package tk.estecka.shiftingwares.mixin;

import java.util.OptionalInt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers.SellMapFactory;
import tk.estecka.shiftingwares.ISellMapFactoryDuck;
import tk.estecka.shiftingwares.IVillagerEntityDuck;
import tk.estecka.shiftingwares.ShiftingWares;

@Mixin(SellMapFactory.class)
public abstract class SellMapFactoryMixin
implements ISellMapFactoryDuck
{
	@Shadow @Final private int price;
	@Shadow @Final private int maxUses;
	@Shadow @Final private int experience;
	@Shadow @Final private String nameKey;
	@Shadow @Final private MapIcon.Type iconType;

	@Shadow public abstract TradeOffer	create(Entity entity, Random random);


	public TradeOffer	create(VillagerEntity villager, Random random, int tradeIndex){
		IVillagerEntityDuck villagerDuck = (IVillagerEntityDuck)villager;
		OptionalInt cachedId = villagerDuck.GetCachedMapId(tradeIndex);

		if (cachedId.isPresent()){
			ShiftingWares.LOGGER.info("Reselling a previously available map #{} in slot {}", cachedId.getAsInt(), tradeIndex);
			ItemStack stack = new ItemStack(Items.FILLED_MAP);
			IFilledMapItemMixin.callSetMapId(stack, cachedId.getAsInt());
			stack.setCustomName(Text.translatable(this.nameKey));
			if (iconType.hasTintColor())
				stack.getOrCreateSubNbt("display").putInt("MapColor", iconType.getTintColor());
			return new TradeOffer( new ItemStack(Items.EMERALD, this.price), new ItemStack(Items.COMPASS), stack, this.maxUses, this.experience, 0.2f );
		}
		else{
			ShiftingWares.LOGGER.info("Selling a brand new map in slot {}.", tradeIndex);
			return create(villager, random);
		}
	}
}
