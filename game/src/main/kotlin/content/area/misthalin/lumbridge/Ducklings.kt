package content.area.misthalin.lumbridge

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random

class Ducklings : Script {

    init {
        npcSpawn("ducklings", ::followParent)
        npcTimerTick("follow_parent", ::follow)
        npcDeath("duck*swim") {
            val ducklings: NPC = get("ducklings") ?: return@npcDeath
            ducklings.say("Eek!")
            followParent(ducklings)
        }
    }

    fun isDuck(it: NPC) = it.id.startsWith("duck") && it.id.endsWith("swim")

    fun followParent(npc: NPC) {
        npc.softTimers.start("follow_parent")
    }

    fun follow(npc: NPC): Int {
        if (npc.mode != EmptyMode && npc.mode !is Wander) {
            return Timer.CONTINUE
        }
        val parent = findParent(npc) ?: return Timer.CANCEL
        npc.mode = Follow(npc, parent)
        parent["ducklings"] = npc
        if (random.nextInt(300) < 1) {
            parent.say("Quack?")
            npc.softQueue("quack", 1) {
                npc.say(if (random.nextBoolean()) "Cheep Cheep!" else "Eep!")
            }
        }
        return Timer.CANCEL
    }

    fun findParent(npc: NPC): NPC? {
        for (dir in Direction.cardinal) {
            return NPCs[npc.tile.add(dir.delta)].firstOrNull { isDuck(it) && !it.contains("ducklings") } ?: continue
        }
        return null
    }
}
