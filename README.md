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

## Map trades
Maps are never forgotten by the game, and lock their structures from appearing on other maps.
To prevent daily rerolls from throwing away endless amounts of unsold maps, those trades are handled very differently.

By default, cartographers will permanently remember each map they sell, and offer it again it the next time the map trade comes up.
The gamerule `shiftingWares.allowMapReroll` (disabled by default) will allow them to forget a map, after it has been sold at least once.


## Technical details
- This mod runs under the assumption that villagers have at most 2 trades per level. It will always try to enforce this layout when rerolling.  

- If a villager is unable to generate all registered trades for a level, it will be replaced with an empty trade. With vanilla trades, this should only ever happen to cartographers, who are unable to generate explorer maps in worlds with no structures.  
These paddings are required to ensure trades are rerolled with one of equivalent level; a trade's position in the list is the only indication to its level.
Placeholder trades will never take the place of a valid trade; they will only show up if all other options are exhausted.

- The "Demand Bonus" game mechanic is mostly removed, because the demand bonus data is deleted along with the offers that are rerolled. Any effect it may still have is uncertain.

- Depleted rerolls have a chance to yield duplicate trades.
