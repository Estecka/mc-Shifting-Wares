package tk.estecka.shiftingwares;

import java.util.OptionalInt;

public interface IVillagerEntityDuck 
{
	void	UpdateCachedMaps();
	OptionalInt	GetCachedMapId(int tradeIndex);
}
