package tk.estecka.shiftingwares.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;

@Mixin(FilledMapItem.class)
public interface IFilledMapItemMixin 
{
	@Invoker static void	callSetMapId(ItemStack mapStack, int mapId)
	{ throw new AssertionError(); }
}
