package tk.estecka.shiftingwares.api;

import org.jetbrains.annotations.Nullable;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import tk.estecka.shiftingwares.ShiftingTradeData;

/**
 * Injected into {@link net.minecraft.village.TradeOffers.Factory}. Modded trade
 * factories may override these methods to gain more control on how their trades
 * should be rerolled. Such factories MUST apply {@link #FinalizeTrade} to their
 * output in order for this to take effect.
 */
public interface IShiftingTradeFactory
{
	static public IShiftingTradeFactory Of(TradeOffers.Factory factory){
		return (IShiftingTradeFactory)factory;
	}

	static public void FinalizeTrade(TradeOffer offer, IShiftingTradeFactory factory){
		ShiftingTradeData.InitializeTrade(offer, factory);
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
	 * else  the villager's offers  may get clogged up  whith multiple copies of
	 * the same item, that won't easily go away.
	 * Different  factories  can use  the same  identifier, in  which  case  the
	 * villager can only have an offer. Using this on non-persistent trades will
	 * also prevent them from generating the duplicatas that often arise during
	 * depleted-rerolls.
	 */
	public default @Nullable Identifier shiftingwares$GetTradeId(){
		return null;
	}

}
