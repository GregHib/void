package content.area.kandarin.tower_of_life

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class TheGuns : Script {

    init {
        npcSpawn("the_guns") {
            startCount()
        }

        worldSpawn {
            World.queue("the_guns_start") {
                NPCs.findOrNull(Tile(2643, 3226), "the_guns")?.startCount()
            }
        }
    }

    private fun NPC.startCount() {
        if (get("the_guns_started", false)) {
            return
        }
        set("the_guns_started", true)
        set("the_guns_count", 0)
        set("the_guns_limit", random.nextInt(5000, 10001))
        queue("the_guns_count", 3) {
            countTick()
        }
    }

    private fun NPC.countTick() {
        when (get("the_guns_stage", 0)) {
            1 -> {
                say("10012...15?")
                set("the_guns_stage", 2)
            }
            2 -> {
                say("Bah")
                set("the_guns_stage", 3)
            }
            3 -> {
                say("Start again!")
                set("the_guns_count", 0)
                set("the_guns_limit", random.nextInt(5000, 10001))
                set("the_guns_stage", 0)
            }
            else -> {
                val count = get("the_guns_count", 0) + 1
                val limit = get("the_guns_limit", 0)
                set("the_guns_count", count)
                say(count.toString())
                if (count >= limit) {
                    set("the_guns_stage", 1)
                }
            }
        }
        queue("the_guns_count", 3) {
            countTick()
        }
    }
}
