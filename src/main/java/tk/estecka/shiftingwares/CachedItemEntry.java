package tk.estecka.shiftingwares;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class CachedItemEntry 
extends ItemEntry
{
	static public final Identifier ID = new Identifier("shiftingwares:cached_item");
	static public final LootPoolEntryType TYPE;
	static public final Codec<CachedItemEntry> CODEC;

	private final String cacheKey;

	static {
		CODEC = RecordCodecBuilder.create(
			instance -> instance
			.group(
				Codecs.NON_EMPTY_STRING.fieldOf("cache_key").forGetter(CachedItemEntry::getCacheKey),
				Registries.ITEM.createEntryCodec().fieldOf("name").forGetter(itemEntry -> itemEntry.item)
			)
			.and(LeafEntry.method_53290(instance))
			.apply(instance, CachedItemEntry::new)
		);

		TYPE = new LootPoolEntryType(CODEC);
	}

	static public void	Register(){
		Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, ID, TYPE);
	}

	public CachedItemEntry(String cacheKey, RegistryEntry<Item> item, int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions){
		super(item, weight, quality, conditions, functions);
		this.cacheKey = cacheKey;
	}

	public String getCacheKey() { return this.cacheKey; }

	@Override 
	public LootPoolEntryType getType(){
		return TYPE;
	}

	@Override
	public boolean	expand(LootContext context, Consumer<LootChoice> consumer){
		Optional<ItemStack> item;

		if(this.test(context)
		&& context.hasParameter(LootContextParameters.THIS_ENTITY)
		&& context.get(LootContextParameters.THIS_ENTITY) instanceof IVillagerEntityDuck villager
		&& (item = villager.GetCachedMap(this.cacheKey)).isPresent())
		{
			consumer.accept(new Choice(){
				@Override public void generateLoot(Consumer<ItemStack> consumer, LootContext ctx){
					consumer.accept(item.get());
				}
			});
			return true;
		}
		else {
			return super.expand(context, consumer);
		}
	}

}
