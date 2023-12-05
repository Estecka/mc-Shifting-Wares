package tk.estecka.shiftingwares;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffers.Factory;


public interface IRerollBlueprint
{
	/**
	 * @return	For each hypothetical slot in the villager's offer listing, this
	 * indicates the level of the corresponding trade.
	 * 
	 * This  should take  into account  the villager's  current level,  and only
	 * provide the data for the trades the villager has unlocked. The villager's
	 * offer listing  will have  its size  adjusted  so as to match  that of the
	 * returned array.
	 */
	IntList	GetSlotLevels(VillagerEntity villager);

	/**
	 * @return	Provides the pool of trades  that will be used  to populate each
	 * level.
	 * 
	 * This  should provide  a  big enough  pool  for  every level  returned  by
	 * {@link #GetSlotLevels}. If a pool is missing or is too small, placeholder
	 * empty trades may be used to pad the villager's offer listing.
	 */
	Int2ObjectMap<Factory[]>	GetTradePools(VillagerEntity villager);
}
