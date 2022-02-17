import world.gregs.voidps.bot.Task
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.Walk

val tasks: TaskManager by inject()

on<World, Startup> {
    val task = Task(
        name = "walk randomly",
        block = {
            while (true) {
                val tile = player.tile.toCuboid(10).random()
//                clickInterface(271, 8, 0, itemSlot = 19)
                player.instructions.emit(Walk(tile.x, tile.y))
                await("tick")
            }
        },
        area = null,
        spaces = Int.MAX_VALUE,
        requirements = emptyList()
    )
//    tasks.register(task, true)
}