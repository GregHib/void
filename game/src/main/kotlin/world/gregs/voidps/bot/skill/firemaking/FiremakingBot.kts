package world.gregs.voidps.bot.skill.firemaking

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.getGear
import world.gregs.voidps.bot.skill.combat.getSuitableItem
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.network.client.instruction.InteractInterfaceItem

val areas: AreaDefinitions by inject()
val tasks: TaskManager by inject()
val objects: GameObjects by inject()

timerStop("firemaking") { player ->
    if (player.isBot) {
        player.bot.resume(timer)
    }
}

worldSpawn {
    for (area in areas.getTagged("fire_making")) {
        val spaces: Int = area["spaces", 1]
        val task = Task(
            name = "make fires at ${area.name}".toLowerSpaceCase(),
            block = {
                val gear = bot.getGear(Skill.Firemaking) ?: return@Task
                val lighter = bot.getSuitableItem(gear.inventory.first())
                val logs = bot.getSuitableItem(gear.inventory.last())
                while (levels.getMax(Skill.Firemaking) < gear.levels.last + 1) {
                    bot.light(area, lighter, logs)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf { bot.hasExactGear(Skill.Firemaking) }
        )
        tasks.register(task)
    }
}

suspend fun Bot.light(map: AreaDefinition, lighter: Item, logs: Item) {
    setupGear(Skill.Firemaking, buy = false)
    goToArea(map)
    val lighterIndex = player.inventory.indexOf(lighter.id)
    while (player.inventory.contains(logs.id)) {
        if (objects.getLayer(player.tile, ObjectLayer.GROUND) != null) {
            val spot = player.tile
                .toCuboid(1)
                .firstOrNull { objects.getLayer(it, ObjectLayer.GROUND) == null }
            if (spot == null) {
                await("tick")
                if (player.inventory.spaces < 4) {
                    break
                }
                continue
            }
            player.queue.clearWeak()
            player.walkTo(spot)
            await("tick")
        }
        val logIndex = player.inventory.indexOf(logs.id)
        if (logIndex == -1) {
            break
        }
        player.instructions.send(InteractInterfaceItem(lighter.def.id, logs.def.id, lighterIndex, logIndex, 149, 0, 149, 0))
        await("firemaking")
    }
}