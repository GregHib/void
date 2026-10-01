As of Void 2.5.0 npc combat scripts are primarily done using `.combat.toml` config files which follow the following format:

```toml
# Header
[combat_id]
attack_speed = 4                 # Number of ticks between attacks
retreat_range = 8                # Range from spawn tile npc can retreat within see: https://oldschool.runescape.wiki/w/Retreat
defend_anim = "defend_anim_id"   # Animation when hit by an enemy
defend_sound = "defend_sound_id" # Sound played to enemy when hit
death_anim = "death_anim_id"     # Animation played when killed
death_sound = "death_sound_id"   # Sound played when killed

# Attack types
[combat_id.attack_name]
# Selection
chance = 1                # Weight given to this attack when selecting between multiple attack styles (default 1)
range = 1                 # Tiles from target when this attack can be used
approach = false          # Whether melee attack has to be within range to use or the npc should move closer
condition = "custom_name" # Custom conditions for using this attack (see: npcCondition)

# Execution
say = "Urg!"                     # Force the npc to say something when attacking
anim = "attack_anim_id"          # Attack animation id
gfx = "attack_gfx_id"            # Attack graphics id
sound = "attack_sound_id"        # Attack sound id (typically only for area sounds)
projectile_origin = "tile"       # Where the projectile should start (entity, tile, centre)
projectile = "projectile_gfx_id" # Projectile gfx to shoot at target

## Target
target_anim = "anim_id"                      # Animation for the target to play (typically blank as defend anims handled separately)
target_gfx = "gfx_id"                        # Graphic to play on the target
target_sound = "attack_sound_id"             # Sound to play the target (usually used over "sound")
target_hit = { offense = "slash", max = 10 } # Damage roll applied to target
multi_target_area = "boss_dungeon"           # Area to select multi-targets from (single target if blank)

## On Impact
impact_anim = "anim_id"                     # Animation to play once hit
impact_gfx = "gfx_id"                       # Graphic to play once hit
impact_sound = "sound_id"                   # Sound to play on target once hit
miss_gfx = "gfx_id"                         # Graphic to play on hit if no damage dealt
miss_sound = "sound_id"                     # Sound to play on hit if no damage dealt
impact_regardless = false                   # Whether to switch between impact_/miss_ on hit or always use impact_gfx/sounds
impact_drain = { id = "skill", amount = 1 } # Targets skill(s) to drain on impact
impact_freeze = 10                          # Ticks to freeze target for
impact_poison = 10                          # Damage to poison target with
impact_message = "You have been frozen!"    # Message to send target on impact
```

> See [CombatDefinition](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/data/config/CombatDefinition.kt) for more detailed documentation.

```toml
[npc_id]
combat_def = "combat_id"
max_hit_crush = 100 # Max hit's can be overridden
```

## Multiples
Lots of fields can have more than one value e.g. hits, drains, sounds, gfxs, projectiles. 

```toml
sounds = [
    { id = "sound_id" },
    { id = "sound_id_2" },
] 
```

## Sounds

```toml
sound = { id = "sound_id", radius = 5 }
```

## Graphics

```toml
gfx = { id = "gfx_id", area = true }
```

## Mixed type hits
Hits can also mix their offensive and defensive types. 

For example [Magical-Ranged](https://oldschool.runescape.wiki/w/Magical_ranged) hits roll accuracy against magic but can be blocked via protect from missiles and show as a ranged hitsplat. 

```toml
hit = { offense = "range", defence = "magic", min = 100, max = 150 } 
```