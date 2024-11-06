# Shifting Wares

Causes villager to occasionally re-roll their trade offers, up to three times a day.
This effect is the most noticeable on professions that can sell enchanted items; a single one of these villagers will be able to offer a much greater variety of wares over time.

This aims to reduce the benefits of exploitative playstyle, while increasing the viability of more serendipitous playstyles.

While it's still possible to get the trade you want by repeatedly breaking a villager's workstation, the benefits of doing so become *very* short lived. Waiting for a natural reroll is probably a better use of your time.

Because trades no longer make the identity of a villager, they become much easier to replace if they die, so you don't need to care as much about their safety. You can let them roam around freely with little risk.

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

By default, map trades will never be rerolled. They may only be rerolled if gamerule `shiftingWares.allowMapReroll` is enabled, and if the trade has been used at least once.

## Technical details
- If a villager is unable to generate all registered trades for a level, it will be replaced with an empty trade. With vanilla trades, this should only ever happen to cartographers, who are unable to generate explorer maps in worlds with no structures.  
These paddings are required to ensure trades are rerolled with one of equivalent level; a trade's position in the list is the only indication to its level.
Placeholder trades will never take the place of a valid trade; they will only show up if all other options are exhausted.

- The "Demand Bonus" game mechanic is mostly removed, because the demand bonus data is deleted along with the offers that are rerolled. Any effect it may still have is uncertain.

- Depleted rerolls have a chance to yield duplicate trades.

## For developpers
By default, shifting-Wares assumes 2 trades per level, and pulls its trade pools from the same place as Vanilla. Mods can override this by using the `shifting-wares` entry-point, and implementing [`ITradeLayoutProvider`](./src/main/java/fr/estecka/shiftingwares/api/ITradeLayoutProvider.java).

If your mod contains custom implementation of map trade factories, or other similar items, you can communicate extra data to Shifting-Wares by implementing the same methods defined in [`IShiftingTradeFactory`](./src/main/java/fr/estecka/shiftingwares/api/IShiftingTradeFactory.java).
No dependency on Shifting-Wares is required, you only need to provide methods with the appropriate prototypes in your factories.
