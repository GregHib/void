import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.move.move
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.utility.inject
import kotlin.random.Random

val collisions: Collisions by inject()
val minRespawnTick = 280
val maxRespawnTick = 530

on<Registered>({ it.id.startsWith("fishing_spot") }) { npc: NPC ->
    val area: Area = npc.getOrNull("area") ?: return@on
    move(npc, area)
}

fun move(npc: NPC, area: Area) {
    npc.queue(Random.nextInt(minRespawnTick, maxRespawnTick)) {
        area.random(collisions, npc)?.let { tile ->
            npc.move(tile)
        }
        move(npc, area)
    }
}