package content.area.morytania.slayer_tower

import content.entity.combat.hit.npcCombatAttack
import content.entity.sound.sound
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.random

val npcs: NPCs by inject()

npcCombatAttack("nechryael") { npc ->
    if (target !is Player) {
        return@npcCombatAttack
    }
    val spawns = npc["death_spawns", 0]
    if (spawns >= 2) {
        return@npcCombatAttack
    }
    if (random.nextInt(4) == 0) { // Unknown rate
        val tile = npc.tile.toCuboid(1).random(npc) ?: return@npcCombatAttack
        // TODO gfx
        val spawn = npcs.add("death_spawn", tile)
        spawn.anim("death_spawn")
        target.sound("death_spawn")
        spawn.mode = Interact(spawn, target, PlayerOption(spawn, target, "Attack"))
        npc.inc("death_spawns")
    }
}
