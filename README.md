# Shifting Wares

Causes villager to occasionally re-roll their trade offers, making them more flexible and less exploitable.

The most noticeable effects will be felt on any profession able to sell enchanted items; a single of these villager will be able to offer a much greater variety of wares over time.

## Gamerules

There are two gamerules that control when trades can be re-rolled. When creating a new world, those rules are On by default, and found under the "Mobs" category. Disabling all rules effectively nullifies the mod.
- `shiftingWares.dailyReroll`:
	Causes villagers to reroll **all** their offers once per day, the first time they restock at their job station.
- `shiftingWares.depleteReroll`:
	Causes villagers to re-roll any **fully depleted** trade offer, whenever they restock at their job station.
	This also prevents offers from being refilled, if they have a remaining uses.

## Caveats

When re-rolling depleted trades, there is a chance that a villager may end up with the same offer twice. However this can only happen when re-rolling a single trade for a given job level.

The "Demand Bonus" game mechanic is mostly removed, as the demand bonus data is deleted along the offers that are being re-rolled. Any effect it may still have is uncertain.
