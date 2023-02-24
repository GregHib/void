import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.world.interact.entity.death.Death
import kotlin.random.Random

val npcs: NPCs by inject()

on<Registered>({ it.id == "ducklings" }) { npc: NPC ->
    followParent(npc)
}

fun isDuck(it: NPC) = it.id.startsWith("duck") && it.id.endsWith("swim")

on<Death>({ isDuck(it) }) { npc: NPC ->
    val ducklings: NPC = npc.getOrNull("ducklings") ?: return@on
    ducklings.forceChat = "Eek!"
    followParent(ducklings)
}

on<TimerStart>({ timer == "follow_parent" }) { _: NPC ->
    interval = 0
}

on<TimerTick>({ timer == "follow_parent" && it.mode == EmptyMode || it.mode is Wander }) { npc: NPC ->
    val parent = findParent(npc) ?: return@on
    npc.mode = Follow(npc, parent)
    parent["ducklings"] = npc
    if (Random.nextInt(300) < 1) {
        parent.forceChat = "Quack?"
        npc.softQueue("quack", 1) {
            npc.forceChat = if (Random.nextBoolean()) "Cheep Cheep!" else "Eep!"
        }
    }
    cancel()
}

fun followParent(npc: NPC) {
    npc.softTimers.start("follow_parent")
}

fun findParent(npc: NPC): NPC? {
    for (dir in Direction.cardinal) {
        return npcs[npc.tile.add(dir.delta)].firstOrNull { isDuck(it) && !it.contains("ducklings") } ?: continue
    }
    return null
}
