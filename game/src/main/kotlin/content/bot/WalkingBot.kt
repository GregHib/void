package content.bot

import content.bot.interact.navigation.await
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.network.client.instruction.Walk

class WalkingBot(val tasks: TaskManager) : Script {

    init {
        worldSpawn {
            val task = Task(
                name = "walk randomly",
                block = {
                    while (true) {
                        val tile = tile.toCuboid(10).random()
                        instructions.send(Walk(tile.x, tile.y))
                        bot.await("tick")
                    }
                },
                area = null,
                spaces = Int.MAX_VALUE,
                requirements = emptyList(),
            )
            if (Settings["bots.idle", "nothing"] == "randomWalk") {
                tasks.idle(task)
            }
        }
    }
}
