package tk.estecka.shiftingwares.api;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffers.Factory;

/**
 * @deprecated Use {@link fr.estecka.shiftingwares.api.ITradeLayoutProvider} instead.
 */
@Deprecated(forRemoval=true)
public interface ITradeLayoutProvider
extends fr.estecka.shiftingwares.api.ITradeLayoutProvider
{
	@Nullable List<@NotNull Factory @NotNull[]>	GetTradeLayout(VillagerEntity villager);
}
