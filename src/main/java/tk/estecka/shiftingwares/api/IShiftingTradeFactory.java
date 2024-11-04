package tk.estecka.shiftingwares.api;

import org.jetbrains.annotations.Nullable;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;

/**
 * Injected into {@link net.minecraft.village.TradeOffers.Factory}. Modded trade
 * factories may override these methods to gain more control on how their trades
 * should be rerolled.
 */
public interface IShiftingTradeFactory
{
	static public IShiftingTradeFactory Of(TradeOffers.Factory factory){
		return (IShiftingTradeFactory)factory;
	}

	/**
	 * Prevents the trades  from being be rerolled, until its item has been sold
	 * at least once. This is, for example, relevant to  Exploration Maps, whose
	 * mere creation leaves a permanent impact on the world.
	 */
	public default boolean shiftingwares$IsItemPersistent(){
		return false;
	}

	/**
	 * This will be used  to associate  a trade offer  to its factory, and avoid
	 * trade duplicata when rerolling. This should be set for persistent trades,
	 * else  the villager's offers  may get clogged up  whith repeated copies of
	 * persistent  trades. Using this on  non-persistent trades  will also  help
	 * preventing duplicatas from being generated during depleted-rerolls.
	 */
	public default @Nullable Identifier shiftingwares$GetTradeId(){
		return null;
	}

}
