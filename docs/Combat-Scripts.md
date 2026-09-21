> [!NOTE]
> Combat scripting is somewhat experimental and is subject to change as more bosses and monsters are added.


While fighting in combat two characters will take it in turns to swing at one another dealing a random amount of damage which varies based on levels and equipment used. Each swing is broken up into several stages and determines the number of ticks delay to wait afterwards until swinging again.

# Anatomy of an attack

* Swing
  * Determine attack style
  * Start attack animation
  * Calculate damage
  * Queue hits
* Attack
  * Grant experience
  * Apply effects on target
  * Target block animations
* Hit
  * Hit mark & graphics
  * Retaliation
  * Deflection

![swing-stages-magic](https://github.com/GregHib/void/assets/5911414/f4f280a4-aac0-4531-8f5b-58e7c65deb9e)



## Swing style

Swing is the first and main stage in a combat cycle, it is where all the calculations are made, hits created and delays for the next turn set.

### Player swing

Players attack style is already determined by their weapon and selected spell before the swing so the swing stage acts as the place to start animations, set the next delay based on the weapons attack speed and calculate hits to send.

Melee and ranged attacks are handled by `weaponSwing`:

```kotlin
weaponSwing("granite_maul*", Priority.LOW) { player ->
    player.anim("granite_maul_attack")
    player.hit(target)
    delay = 7
}

weaponSwing("*chinchompa", style = "range", priority = Priority.LOW) { player ->
    player.anim("chinchompa_short_fuse")
    player.shoot(id = player.ammo, target = target)
    player.hit(target, delay = Hit.throwDelay(player.tile.distanceTo(target)))
    delay = player["attack_speed", 4]
}
```

Magic attacks are handled by `spellSwing`:

```kotlin
spellSwing("magic_dart", Priority.LOW) { player ->
    player.anim("magic_dart")
    player.gfx("magic_dart_cast")
    player.shoot(id = player.spell, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.magicDelay(distance))
    delay = 5
}
```

### NPC swing

Npcs that have multiple styles of attacking also use the swing stage to decide which attack style to use

```kotlin
npcSwing("dark_wizard_water*", Priority.HIGHEST) { npc ->
    npc.spell = if (!random.nextBoolean() && Spell.canDrain(target, "confuse")) "confuse" else "water_strike"
}
```

## Calculations

Hit calculations are separated into two parts [Hit](https://github.com/GregHib/void/blob/a4af3f90fdc33c890935abc9531f903e06b1a6d0/game/src/main/kotlin/world/gregs/voidps/world/interact/entity/combat/hit/Hit.kt#L19) and [Damage](https://github.com/GregHib/void/blob/a4af3f90fdc33c890935abc9531f903e06b1a6d0/game/src/main/kotlin/world/gregs/voidps/world/interact/entity/combat/hit/Damage.kt#L27):

### Hit

Hit calculates a number between 0.0 and 1.0 which represents the chance of successfully dealing damage, 1.0 being 100%, and 0.0 - 0% chance. Hit chance combines the attackers offensive rating against the targets defensive rating, both of which combine the combat levels, equipment and bonuses of the respective characters.

### Damage

Damage calculates the maximum and minimum hit the attacker is able to deal on the target, maximum hits are often fixed for magic spells or combines levels and bonuses in other cases. A number between the min and max hit will then be randomly selected to be used. Damage is only calculated if the Hit roll was successful.

> [!NOTE]
> For further calculation details see the [Combat Lifecycle](combat-lifecycle).

## Attack

Attack stage is applied for each hit before the delay, the damage dealt cannot be modified past this point hense why it is the stage where experience is granted. This stage is often used for block animations and damage effects.

```kotlin
combatAttack { player ->
    if (damage > 40 && player.praying("smite")) {
        target.levels.drain(Skill.Prayer, damage / 40)
    }
}
```

Block animations are handled by `block`:

```kotlin
block("granite_maul*") {
    target.anim("granite_maul_block", delay)
    blocked = true
}
```

```kotlin
npcCombatAttack("king_black_dragon") { npc ->
    when (spell) {
        "toxic" -> npc.poison(target, 80)
        "ice" -> npc.freeze(target, 10)
        "shock" -> {
            target.message("You're shocked and weakened!")
            for (skill in Skill.all) {
                target.levels.drain(skill, 2)
            }
        }
    }
}
```

## Hit

Hit is the final stage when hit marks are shown, hit graphics are displayed and after effects like deflection and vengeance are applied.

```kotlin
combatHit { player ->
    if (player.softTimers.contains("power_of_light")) {
        player.gfx("power_of_light_hit")
    }
}
```

```kotlin
weaponHit("*chinchompa", "range") { character ->
    source as Player
    source.playSound("chinchompa_explode", delay = 40)
    character.gfx("chinchompa_hit")
}
```

```kotlin
specialAttackHit("korasis_sword") { character ->
    character.gfx("disrupt_hit")
}
```
