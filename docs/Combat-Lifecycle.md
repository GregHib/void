To help understand when combat modifiers are applied to calculations and how they all work together to calculate a hits accuracy and damage.

## Cheatsheet

| Method | Use | Example |
|---|---|---|
| Hit.effectiveLevel / Damage.effectiveLevel | Override the "effective" level of the attacker or target in the accuracy rating calculation. | Turmoil/leech prayers, magic defence, stance, void set bonus |
| Hit.rating | Modify the offensive or defensive rating of the attacker or target used in the accuracy calculation. | Special attack accuracy increases, Slayer helm task accuracy bonus |
| Hit.chance | Modify the successful hit chance. | Seercull special is guaranteed to hit. |
| Damage.maximum | Calculate the base maximum hit. | Magic dart +100 effective magic level, anti dragon shields and protect magic reducing dragonfire  |
| Damage.modify | Modify the rolled maximum hit. | Special attack damage increases, protection prayers decrease, passive effects like berserker necklace or castle wars bracelet |


## Lifecycle
![flowchart of combat stages and events](https://i.imgur.com/DDfgmxm.png)