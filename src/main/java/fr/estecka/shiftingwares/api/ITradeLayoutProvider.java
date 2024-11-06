package fr.estecka.shiftingwares.api;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffers.Factory;

/**
 * Must be implemented by mods that want to change how many trades are available
 * to each villager level, or who don't want villagers to use the trades defined
 * in {@link net.minecraft.village.TradeOffers#PROFESSION_TO_LEVELED_TRADE}  nor
 * in {@link net.minecraft.village.TradeOffers#REBALANCED_PROFESSION_TO_LEVELED_TRADE}
 */
public interface ITradeLayoutProvider
{
	/**
	 * @return  For each hypothetical slot in the villager's offer listing, this
	 * provides the pool of trades  that can go into that slot. If this provider
	 * does not know  the layout  of the given  villager, it can return  null in
	 * order to fall back to the vanilla layout.
	 * 
	 * The same  pool  can be  assigned  to  multiple  slots  in order  to avoid
	 * duplicate  offers.  Pool  equality  is  evaluated  by  identity,  i.e  by
	 * comparing pointers.
	 * 
	 * The size of  the returned list  needs not match  the current size  of the
	 * villager's listing; it should return  the intended layout. Shifting-Wares
	 * will try to adjust the villager's listing to match the returned layout.
	 * 
	 * This  should take  into account  the villager's  current level,  and only
	 * provide trade slots which the villager has unlocked.
	 * 
	 */
	@Nullable List<@NotNull Factory @NotNull[]>	GetTradeLayout(VillagerEntity villager);
}
