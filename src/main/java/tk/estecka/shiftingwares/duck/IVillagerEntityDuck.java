package tk.estecka.shiftingwares.duck;

import net.minecraft.entity.passive.VillagerEntity;
import tk.estecka.shiftingwares.MapTradesCache;
import tk.estecka.shiftingwares.api.IHasItemCache;

public interface IVillagerEntityDuck
extends IHasItemCache
{
	static public IVillagerEntityDuck Of(VillagerEntity villager){
		return (IVillagerEntityDuck)villager;
	}

	MapTradesCache shiftingwares$GetItemCache();
}
