# Minecraft Code Breaking Changes
### 1.19.4
Current master

### 1.20.2
#### Backward compatibility workaround:
- Need to handle the experimental trade rebalance: build for 1.20.2 and check that the feature's identifier exists before executing related code.

### 1.20.5
- `ItemStack::hasCustomName` and `FilledMapItem::getMapId` were replaced with DataComponents.
- Exploration maps now use an `item_name` instead of a `custom_name`. Map caches from older versions are not automatically upgraded by minecraft.
- `TradeOffer` now takes the price as a `TradeItem` instead of an `ItemStack`. The second price is also an `Optional`
#### Backward compatibility workaround:
- `ItemStack::writeToNbt` and `readFromNbt` were removed or changed: Use `ItemStack::CODEC` instead.
- Trade offers no longer support selling or buying air: Use some placeholder items instead.

### 1.21.0
- `getRandom()` was moved from `LivingEntity` to its parent class `Entity`. No code change required, but needs recompilation.

### 1.21.2
#### Backward compatibility workaround:
- `World.getGamerules()` was moved to `ServerWorld`. Call it from `MinecraftServer` instead.

### 1.21.5
- Villager Data now hold registry entries instead of straight up data.
- "Hide Tooltip" component was replaced with "Tooltip display".
- Wandering Trader no longer needs special treatment to have its trade ids set. (Trade Rebalance merged.)

### 1.21.9
- `Entity.getEntityWorld()` was moved to a superinterface.
- `Entity.getWorld()` was removed.
- `Entity.getServer()` was removed.

### 1.21.11
- Gamerules must now be registered via regular registries.
- `TradeOffer::create` now takes a `ServerWorld` as parameters.
- `VillagerEntity::fillRecipesFromPool` now takes a ServerWorld as parameter.
