import world.gregs.voidps.bot.Task
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.getGear
import world.gregs.voidps.bot.skill.combat.getSuitableItem
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.move.awaitWalk
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractInterfaceItem

val areas: Areas by inject()
val tasks: TaskManager by inject()
val objects: Objects by inject()

on<ActionFinished>({ type == ActionType.FireMaking }) { bot: Bot ->
    bot.resume("firemaking")
}

on<World, Startup> {
    for (area in areas.getTagged("fire_making")) {
        val spaces: Int = area["spaces", 1]
        val task = Task(
            name = "make fires at ${area.name}".replace("_", " "),
            block = {
                val gear = getGear(Skill.Firemaking) ?: return@Task
                val lighter = getSuitableItem(gear.inventory.first())
                val logs = getSuitableItem(gear.inventory.last())
                while (player.levels.getMax(Skill.Firemaking) < gear.levels.last + 1) {
                    light(area, lighter, logs)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf { hasExactGear(Skill.Firemaking) }
        )
        tasks.register(task)
    }
}

suspend fun Bot.light(map: MapArea, lighter: Item, logs: Item) {
    setupGear(Skill.Firemaking, buy = false)
    goToArea(map)
    val lighterIndex = player.inventory.indexOf(lighter.id)
    while (player.inventory.contains(logs.id)) {
        if (objects.getType(player.tile, 10) != null) {
            val spot = player.tile
                .toCuboid(1)
                .firstOrNull { objects.getType(it, 10) == null }
            if (spot == null) {
                await("tick")
                if (player.inventory.spaces < 4) {
                    break
                }
                continue
            }
            player.awaitWalk(spot, cancelAction = true)
            await("tick")
        }
        val logIndex = player.inventory.indexOf(logs.id)
        if (logIndex == -1) {
            break
        }
        player.instructions.emit(InteractInterfaceItem(lighter.def.id, logs.def.id, lighterIndex, logIndex, 149, 0, 149, 0))
        await("firemaking")
    }
}