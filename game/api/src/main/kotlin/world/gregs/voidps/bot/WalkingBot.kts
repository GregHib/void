package world.gregs.voidps.bot

import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.client.instruction.Walk

val tasks: TaskManager by inject()

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
        requirements = emptyList()
    )
    if (Settings["bots.idle", "nothing"] == "randomWalk") {
        tasks.idle(task)
    }
}