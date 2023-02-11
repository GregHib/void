import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.event.Death
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.softTimer
import world.gregs.voidps.engine.utility.inject
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

fun followParent(npc: NPC) {
    npc.softTimer(ticks = 1, loop = true) {
        if (npc.mode == EmptyMode || npc.mode is Wander) {
            val parent = findParent(npc) ?: return@softTimer
            npc.mode = Follow(npc, parent)
            parent["ducklings"] = npc
            if (Random.nextInt(300) < 1) {
                parent.forceChat = "Quack?"
                npc.softTimer(1) {
                    npc.forceChat = if (Random.nextBoolean()) "Cheep Cheep!" else "Eep!"
                }
            }
            cancel()
        }
    }
}

fun findParent(npc: NPC): NPC? {
    for (dir in Direction.cardinal) {
        return npcs[npc.tile.add(dir.delta)].firstOrNull { isDuck(it) && !it.contains("ducklings") } ?: continue
    }
    return null
}
