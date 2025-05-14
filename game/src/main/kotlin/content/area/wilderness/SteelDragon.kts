package content.area.wilderness

import content.entity.combat.CombatSwing
import content.entity.combat.hit.hit
import content.entity.combat.npcCombatSwing
import content.entity.proj.shoot
import content.entity.sound.sound
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

val handler: suspend CombatSwing.(NPC) -> Unit = { npc ->
    val withinMelee = CharacterTargetStrategy(npc).reached(target)
    if (withinMelee && random.nextBoolean()) {
        // 🗡️ Melee attack
        npc.anim("steel_dragon_attack")
        npc.hit(target)
    } else if (withinMelee) {
        // 🔥 Close-range dragonfire (mouth blast)
        npc.anim("steel_dragon_hit")
        npc.gfx("dragon_breath_hit")
        npc.hit(target, type = "dragonfire", special = true)
        target.sound("dragon_attack")
    } else {
        // 🔥 Ranged dragonfire (fireball)
        npc.anim("steel_dragon_breath")
        nearestTile(npc, target).shoot("dragon_breath", target)
        npc.hit(target, type = "dragonfire")
        target.sound("dragon_attack")
    }
}
npcCombatSwing("bronze_dragon", handler = handler)
npcCombatSwing("iron_dragon", handler = handler)
npcCombatSwing("steel_dragon", handler = handler)



/**
 * Tile the dragon breath originates from.
 * Mimics OSRS fire pathing logic.
 */
fun nearestTile(source: Character, target: Character): Tile {
    val half = source.size / 2
    val centre = source.tile.add(half, half)
    val direction = target.tile.delta(centre).toDirection()
    return centre.add(direction).add(direction)
}
