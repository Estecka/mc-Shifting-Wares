package tk.estecka.shiftingwares;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffers.Factory;



@FunctionalInterface
public interface ITradeLayoutProvider
{
	/**
	 * @return	For each hypothetical slot in the villager's offer listing, this
	 * provides the pool of trades that can go into that slot.
	 * 
	 * This  should take  into account  the villager's  current level,  and only
	 * provide trade slots which the villager has unlocked. The villager's offer
	 * listing  may have its size adjusted  so as to match  that of the returned
	 * array.
	 * 
	 * The same pool can be used  for multiple slots. Rerolls will avoid pulling
	 * duplicate trades  across  the slots  of  a same  group. Pool equality  is
	 * evaluated by identity, i.e by comparing pointers.
	 */
	Factory[][]	GetTradeLayout(VillagerEntity villager);
}
