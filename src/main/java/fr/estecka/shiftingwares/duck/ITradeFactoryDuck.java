package fr.estecka.shiftingwares.duck;

import net.minecraft.world.item.trading.VillagerTrade;

public interface ITradeFactoryDuck
{
	static public ITradeFactoryDuck Of(VillagerTrade factory){
		return (ITradeFactoryDuck)factory;
	}

	public boolean shiftingwares$IsItemPersistent();
}
