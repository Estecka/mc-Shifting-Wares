# Shifting Wares

Causes villager to re-roll their trade offers on their own accord, up to three times a day.
This effect will be most noticeable on professions who can sell enchanted or coloured items; a single one of these villagers will be able to offer a much greater variety of wares over time.

This aims to reduce the benefits of exploitative playstyle, while making serendipitous playstyles more viable. Villagers with perfect trades will not keep those trades forever, and villagers with bad trades will get better eventually. Even though a single villager can potentially provide everything you need, having multiple villagers of the same profession is still useful to increase the odds of getting a specific trade.

Because all trades eventually expire, villagers become much easier to replace if they die, so you don't need to care as much about their safety. You can let them roam around freely with little risk to your economy.

## Reroll Triggers
There are two gamerules that control when trades can be re-rolled. Both are enabled by default.
Disabling all rules effectively disables the mod.
- `shifting-wares:daily_reroll` :
	Causes villagers to re-roll **all** their offers once per day, the first time they restock at their job station.
- `shifting-wares:deplete_reroll` :
	Causes villagers to re-roll any **fully depleted** trade offer, whenever they restock at their job station.
	This also prevents offers from being refilled, if they have a remaining uses.


## Exploration Maps and Persitent trades
Minecraft permanently saves all created maps, and lock their structures from appearing on other exploration maps. To prevent daily rerolls from throwing away endless amounts of unsold maps, these trades are treated as persistent.

Persistent trades can only be rerolled they have been used at least once, and if the gamerule `shifting-wares:allow_persistent_reroll` is enabled. This gamerule is disabled by default.

Datapack makes can mark any trade as persistent by adding this field to the trade:
```json
{
	"shiftingwares:isPersistent": true,
	"wants": "...",
	"gives": "...",
	"...": "..."
}
```

## Workstation protection
Breaking and replacing a villager's workstation no longer forces the villager to reroll its trades. The initial trades are generated deterministically, using the villager's UUID as the seed.

This is controlled by the gamerule `shifting-wares:workstation_protection`.


## Miscellani technical details
- If a villager is unable to generate a trade for a slot, it will be replaced with an empty trade. In vanilla, this should only ever happen to cartographers, who are unable to generate explorer maps in worlds with no structures.  
These empty paddings are required to ensure trades are rerolled with ones of equivalent level, because a trade's position in the list is the only indication to its level.
Placeholder trades will never take the place of a valid trade; they will only show up if all other options are exhausted.

- As a side effect of this mod, the "Demand Bonus" mechanic has its effect greatly diminished, because the demand bonus data is deleted along with the offers that are rerolled.
