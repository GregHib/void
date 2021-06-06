import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Area
import kotlin.random.Random

val minRespawnTick = 280
val maxRespawnTick = 530

on<Registered>({ it.name.startsWith("fishing_spot") }) { npc: NPC ->
    val area: Area = npc["area"]
    npc.action(ActionType.Movement) {
        while (isActive) {
            delay(Random.nextInt(minRespawnTick, maxRespawnTick))
            area.random(npc.movement.traversal)?.let { tile ->
                npc.movement.delta = tile.delta(npc.tile)
            }
        }
    }
}