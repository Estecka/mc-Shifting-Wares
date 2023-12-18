package tk.estecka.shiftingwares;

import java.util.Optional;
import net.minecraft.item.ItemStack;

public interface IVillagerEntityDuck 
{
	Optional<ItemStack>	shiftingwares$GetCachedMap(String key);
	void	shiftingwares$AddCachedMap(String key, ItemStack mapItem);
}
