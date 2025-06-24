package content.area.morytania.slayer_tower

import content.entity.combat.hit.npcCombatAttack
import content.entity.sound.sound
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

val npcs: NPCs by inject()
val players: Players by inject()

npcCombatAttack("nechryael") { npc ->
    if (target !is Player) {
        return@npcCombatAttack
    }
    val spawns = target["death_spawns", 0]
    if (spawns >= 2) {
        return@npcCombatAttack
    }
    if (random.nextInt(10) == 0) { // Unknown rate
        val tile = npc.tile.toCuboid(1).random(npc) ?: return@npcCombatAttack
        // TODO gfx
        val spawn = npcs.add("death_spawn", tile)
        val name = target.name
        spawn.softQueue("despawn", TimeUnit.SECONDS.toTicks(60)) {
            npcs.remove(spawn)
            players.get(name)?.dec("death_spawns")
        }
        spawn.anim("death_spawn")
        spawn.mode = Interact(spawn, target, PlayerOption(spawn, target, "Attack"))
        target.sound("death_spawn")
        target.inc("death_spawns")
    }
}
