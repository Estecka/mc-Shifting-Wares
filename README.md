# Shifting Wares

Causes villager to occasionally re-roll their trade offers, making them more flexible and less exploitable.

The most noticeable effects will be felt on any profession able to sell enchanted items; a single of these villager will be able to offer a much greater variety of wares over time.

## Gamerules

There are two gamerules that control when trades can be re-rolled. When creating a new world, both rules are On by default, and found under the "Mobs" category.  
Disabling all rules effectively disables the mod.
- `shiftingWares.dailyReroll`:
	Causes villagers to reroll **all** their offers once per day, the first time they restock at their job station.
- `shiftingWares.depleteReroll`:
	Causes villagers to re-roll any **fully depleted** trade offer, whenever they restock at their job station.
	This also prevents offers from being refilled, if they have a remaining uses.

## Caveats

- When re-rolling depleted trades, there is a chance that a villager may end up with the same offer twice. However this can only happen when re-rolling a single trade for a given job level.

- The "Demand Bonus" game mechanic is mostly removed, as the demand bonus data is deleted along the offers that are being re-rolled. Any effect it may still have is uncertain.

## Known issues

- **When a cartographer re-rolls depleted trades in a world set to generate with no structure**, some rerolls may yield trades one level lower than they should.
In particular, it is impossible for them to yield a Master level trade, unless they reroll at least two trades at once.  
**This issue is not destructive**; affected trades will _not_ degenerate endlessly, but simply bounce back and forth between the intended level and the previous level. Daily reroll is guaranteed to yield appropriate trades, and erase any oddities.

- **When a cartographer yields a new map trade**, a new map ID will be created. It will be saved to the disk whether you buy it or not, and lead to a new structure farther away than the previous one.  
As a result, cartographers may end up somewhat broken when using daily-rerolls, and send you unexpectedly far away.
