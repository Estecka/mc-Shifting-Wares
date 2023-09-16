package tk.estecka.shiftingwares;

import java.util.Optional;
import net.minecraft.item.ItemStack;

public interface IVillagerEntityDuck 
{
	Optional<ItemStack>	GetCachedMap(String key);
	void	AddCachedMap(String key, ItemStack mapItem);
}
