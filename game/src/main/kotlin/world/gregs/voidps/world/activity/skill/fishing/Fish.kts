import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.move.move
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.utility.inject
import kotlin.random.Random

val collisions: Collisions by inject()
val minRespawnTick = 280
val maxRespawnTick = 530

on<Registered>({ it.id.startsWith("fishing_spot") }) { npc: NPC ->
    val area: Area = npc.getOrNull("area") ?: return@on
    npc.action(ActionType.Movement) {
        while (isActive) {
            delay(Random.nextInt(minRespawnTick, maxRespawnTick))
            area.random(collisions, npc)?.let { tile ->
                npc.move(tile)
            }
        }
    }
}