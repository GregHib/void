package content.area.karamja.brimhaven

import content.entity.combat.CombatSwing
import content.entity.combat.hit.hit
import content.entity.combat.npcCombatSwing
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class MetalDragons : Script {

    val handler: suspend CombatSwing.(NPC) -> Unit = { npc ->
        val withinMelee = CharacterTargetStrategy(npc).reached(target)
        if (withinMelee && random.nextBoolean()) {
            // Melee attack
            npc.anim("dragon_attack")
            target.sound("dragon_attack")
            npc.hit(target, offensiveType = "melee")
        } else if (withinMelee) {
            // Close-range dragonfire
            npc.anim("dragon_breath")
            npc.gfx("dragon_breath_shoot")
            target.sound("dragon_breath")
            npc.hit(target, offensiveType = "dragonfire", special = true)
        } else {
            // Ranged dragonfire
            npc.anim("dragon_shoot")
            target.sound("metal_dragon_fireball")
            nearestTile(npc, target).shoot("dragon_breath", target)
            npc.hit(target, offensiveType = "dragonfire")
        }
    }

    init {
        npcCombatSwing("bronze_dragon", handler = handler)

        npcCombatSwing("iron_dragon", handler = handler)

        npcCombatSwing("steel_dragon", handler = handler)
    }

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
}
