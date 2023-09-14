package tk.estecka.shiftingwares;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;

public interface ISellMapFactoryDuck {
	public TradeOffer	create(VillagerEntity villager, Random random, int tradeIndex);
}
