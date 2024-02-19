package world.gregs.voidps.world.interact.entity.npc.move

import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.npcTimerStart
import world.gregs.voidps.engine.timer.npcTimerTick
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.death.npcDeath

val npcs: NPCs by inject()

npcSpawn("ducklings") { npc ->
    followParent(npc)
}

fun isDuck(it: NPC) = it.id.startsWith("duck") && it.id.endsWith("swim")

npcDeath("duck*swim") { npc: NPC ->
    val ducklings: NPC = npc["ducklings"] ?: return@npcDeath
    ducklings.forceChat = "Eek!"
    followParent(ducklings)
}

npcTimerStart("follow_parent") { _: NPC ->
    interval = 0
}

npcTimerTick("follow_parent") { npc: NPC ->
    if (npc.mode != EmptyMode && npc.mode !is Wander) {
        return@npcTimerTick
    }
    val parent = findParent(npc) ?: return@npcTimerTick
    npc.mode = Follow(npc, parent)
    parent["ducklings"] = npc
    if (random.nextInt(300) < 1) {
        parent.forceChat = "Quack?"
        npc.softQueue("quack", 1) {
            npc.forceChat = if (random.nextBoolean()) "Cheep Cheep!" else "Eep!"
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
