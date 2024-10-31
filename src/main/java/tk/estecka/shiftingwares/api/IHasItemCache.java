package tk.estecka.shiftingwares.api;

/**
 * Implemented by VillagerEntity
 * 
 * This is the old way of preventing trade factories from generating never-sold
 * maps. If possible use {@link IShiftingTradeFactory} instead.
 */
public interface IHasItemCache
{
	PersistentItemCache	shiftingwares$GetItemCache();
}
