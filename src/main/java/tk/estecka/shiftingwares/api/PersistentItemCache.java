package tk.estecka.shiftingwares.api;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import tk.estecka.shiftingwares.MapTradesCache;

public interface PersistentItemCache
{
	/**
	 * @param cacheKey	The key that identifies the item within the cache.
	 * @return Empty if the entity is allowed to forget the item, or does not
	 * remember it. Otherwise, returns the corresponding cached item.
	 */
	static public Optional<ItemStack>	Resell(Entity entity, String cacheKey){
		return MapTradesCache.Resell(entity, cacheKey);
	}

	/**
	 * This overwrite any existing item, and clears the "sold" flag of the item.
	 */
	void	AddCachedItem(String key, @NotNull ItemStack mapItem);
	Optional<@NotNull ItemStack>	GetCachedItem(String key);
}
