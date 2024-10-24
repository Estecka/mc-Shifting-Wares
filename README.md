# Shifting Wares

Causes villager to occasionally re-roll their trade offers, making them more flexible and less exploitable.

The most noticeable effects will be felt on any profession able to sell enchanted items; a single of these villager will be able to offer a much greater variety of wares over time.

## Triggers

There are two gamerules that control when trades can be re-rolled. Both are enabled by default.  
Disabling all rules effectively disables the mod.
- `shiftingWares.dailyReroll`:
	Causes villagers to re-roll **all** their offers once per day, the first time they restock at their job station.
- `shiftingWares.depleteReroll`:
	Causes villagers to re-roll any **fully depleted** trade offer, whenever they restock at their job station.
	This also prevents offers from being refilled, if they have a remaining uses.

## Exploration map trades
Minecraft permanently saves any create map, and lock their structures from appearing on other exploration maps.
To prevent daily rerolls from throwing away endless amounts of unsold maps, those trades are handled differently.

Cartographers will remember each map they sell, and offer it again it the next time the same map trade comes up.
The gamerule `shiftingWares.allowMapReroll` (disabled by default) will allow them to forget a map, after it has been sold at least once.

The map's item name, (or preferably, its translation key), is used to tell apart different types of maps.
Since 1.20.5, the `item_name` is preferred over the `custom_name`.


## Technical details
- If a villager is unable to generate all registered trades for a level, it will be replaced with an empty trade. With vanilla trades, this should only ever happen to cartographers, who are unable to generate explorer maps in worlds with no structures.  
These paddings are required to ensure trades are rerolled with one of equivalent level; a trade's position in the list is the only indication to its level.
Placeholder trades will never take the place of a valid trade; they will only show up if all other options are exhausted.

- The "Demand Bonus" game mechanic is mostly removed, because the demand bonus data is deleted along with the offers that are rerolled. Any effect it may still have is uncertain.

- Depleted rerolls have a chance to yield duplicate trades.

## For developpers
By default, shifting-Wares assumes 2 trades per level, and pulls its trade pools from the same place as Vanilla.

If you have a mod that changes any of that, Shifting-Wares has an API you can use to specify the trade pools and layout that should be used instead.
