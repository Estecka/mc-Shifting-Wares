package tk.estecka.shiftingwares;

import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOfferList;

public interface IVillagerEntityDuck 
{
	TradeOfferList	getOffers();

	Optional<ItemStack>	GetCachedMap(String key);
	void	AddCachedMap(String key, ItemStack mapItem);
}
