package tk.estecka.shiftingwares.api;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffers.Factory;

public interface ITradeLayoutProvider
{
	/**
	 * @return  For each hypothetical slot in the villager's offer listing, this
	 * provides the pool of trades  that can go into that slot. If this instance
	 * does not know  the layout of  a specific villager, it can return  null in
	 * order to fall back to the vanilla layout.
	 * 
	 * The same  pool  can be  assigned  to  multiple  slots  in order  to avoid
	 * duplicates.  Pool equality  is evaluated  by  identity,  i.e by comparing
	 * pointers.
	 * 
	 * The size of the returned list needs not match  the size of the villager's
	 * current lising;  it should  return  the  intended layout. The  villager's
	 * listing may have its size adjusted so as to match the return value.
	 * 
	 * This  should take  into account  the villager's  current level,  and only
	 * provide trade slots which the villager has unlocked. 
	 * 
	 */
	@Nullable List<@NotNull Factory @NotNull[]>	GetTradeLayout(VillagerEntity villager);
}
