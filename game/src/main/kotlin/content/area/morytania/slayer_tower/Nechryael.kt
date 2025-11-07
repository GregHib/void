package content.area.morytania.slayer_tower

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class Nechryael : Script {

    val npcs: NPCs by inject()
    val players: Players by inject()

    init {
        npcCombatAttack("nechryael") { (target) ->
            if (target !is Player) {
                return@npcCombatAttack
            }
            val spawns = target["death_spawns", 0]
            if (spawns >= 2) {
                return@npcCombatAttack
            }
            if (random.nextInt(5) == 0) { // Unknown rate
                val tile = tile.toCuboid(1).random(this) ?: return@npcCombatAttack
                // TODO gfx
                val spawn = npcs.add("death_spawn", tile)
                val name = target.name
                spawn.softQueue("despawn", TimeUnit.SECONDS.toTicks(60)) {
                    npcs.remove(spawn)
                    players.get(name)?.dec("death_spawns")
                }
                spawn.anim("death_spawn")
                spawn.interactPlayer(target, "Attack")
                target.sound("death_spawn")
                target.inc("death_spawns")
            }
        }
    }
}
