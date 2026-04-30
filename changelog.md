# v1
## 1.0
### 1.0.0
Initial release
### 1.0.1
- Fixed a crash that would occur after a Cartographer failed to generate a map trade in a world that generates no structure.
### 1.0.2
**This version contains a critical bug and should not be used**
- Force cartographers remember the maps they generated, and always resell it.
### 1.0.3
**This version contains a critical bug and should not be used**
- Introduced placeholder trades. Fixes cartographers having trouble generating high-level trades in worlds with no structures.
### 1.0.4
- Fixes mapping issue affecting v1.0.2 and v1.0.3
### 1.0.5
- The level of each trade slot is evaluated more accurately for every job.

## 1.1
- Added a gamerules that allows cartographers to regenerate maps that have been sold at least once.
- _Regression: Restocks cause a crash if Deplete Reroll is disabled._

# v2
## 2.0
- Made some methods and types available through an API.
- Added an entrypoint to define villager trades from outside mods
## 2.1
### 2.1.0
- Reimplemented Trade-Rebalance support in a backward-compatible way
### 2.1.1
- Fixed a crash that would occur upon restock if Depleted Reroll is disabled.
### 2.1.2 (Branched of v2.2)
- Custom serialization is no longer callable from the render thread.
## 2.2
### 2.2.0
- Updated for MC 1.20.5
- For caching, a map's `item_name` takes priority over the `custom_name` if both are present.
- Placeholder trades are no longer empty. (Cosmetic change only.)
- Added custom logic for upgrading caches from before MC 1.20.5
### 2.2.1
- Updated for MC 1.21
### 2.2.2
- Custom serialization is no longer callable from the render thread.
This boost performances when using Fresh Animations
### 2.2.3
- Updated for MC 1.21.2
- Starting MC 1.21.2, placeholder trades now use `item_model`'s to look empty again.

# v3
## 3.0
- Removed trade cache.
- Trades can now be marked a persistent by their factories. Persistent items are *never* rerolled until they have been sold. Filled map trades are automatically marked as persistent regardless of the factories setting. This prevents mods who implement custom factories from causing unsold maps to be discarded.
- Trades can now be given an identifier by their factories. ShiftingWares will prevent villagers from getting multiple trades coming from the same factory during rerolls. This will prevent the villager's listing from getting clogged up with multiple copies of the same persistent trade after any reroll, and prevent duplicatas of non-persistent from ocurring during depleted rerolls.
- Other mods can provide the relevant data for their factories, by simply implementing the correct methods. No dependency on ShiftingWares is required.
# 3.1
### 3.1.0
- Added a gamerule that prevents rerolls when breaking a workstation.
### 3.1.1
- Updated for MC 1.21.5
### 3.1.2
- Updated for MC 1.21.9
### 3.1.3
- Updated for MC 1.21.11
### 3.1.4
- Fixed map gamerule behaviour being inverted.
