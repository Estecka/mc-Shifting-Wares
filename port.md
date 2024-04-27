# Minecraft Code Breaking Changes
### 1.19.4
Current master

### 1.20.2
#### Worked Around:
- Need to handle the experimental trade rebalance: build for 1.20.2 and check that the feature's identifier exists before executing related code.

### 1.20.5
#### No Workaround:
- `ItemStack::hasCustomName` and `FilledMapItem::getMapId` were replaced with DataComponents.
- Exploration maps now use an `item_name` instead of a `custom_name`.
- `TradeOffer` now takes the price as a `TradeItem` instead of an `ItemStack`. The second price is also an `Optional`
#### Possible Workaround:
- `ItemStack::writeToNbt` and `readFromNbt` were removed or changed: Use `ItemStack::CODEC` instead.
- Trade offers no longer support selling or buying air: Use some placeholder items instead.
