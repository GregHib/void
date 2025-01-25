package world.gregs.voidps.bot.skill.smithing

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.getGear
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.network.client.instruction.InteractDialogue
import content.skill.smithing.oreToBar

val areas: AreaDefinitions by inject()
val tasks: TaskManager by inject()
val itemDefinitions: ItemDefinitions by inject()

timerStop("smelting") { player ->
    if (player.isBot) {
        player.bot.resume(timer)
    }
}

worldSpawn {
    for (area in areas.getTagged("smelting")) {
        val spaces: Int = area["spaces", 1]
        val task = Task(
            name = "smelt bars at ${area.name}".toLowerSpaceCase(),
            block = {
                val gear = bot.getGear("smelting", Skill.Smithing) ?: return@Task
                while (levels.getMax(Skill.Smithing) < gear.levels.last + 1) {
                    bot.smelt(area, gear)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf { bot.hasExactGear("smelting", Skill.Smithing) }
        )
        tasks.register(task)
    }
}

suspend fun Bot.smelt(map: AreaDefinition, set: GearDefinition) {
    setupGear(set, buy = false)
    goToArea(map)
    val furnace = getObject { isFurnace(map, it) }
    if (furnace == null) {
        await("tick")
        return
    }
    val ore = player.inventory.items.first { it.id.endsWith("_ore") }
    var bar = oreToBar(ore.id)
    if (bar == "iron_bar" && player.inventory.contains("coal")) {
        bar = "steel_bar"
    }
    val barId = itemDefinitions.get(bar).id
    await("tick")
    while (player.inventory.contains(ore.id)) {
        itemOnObject(ore, furnace)
        await("tick")
        while (player.mode is Interact) {
            await("tick")
        }
        // Select All
        clickInterface(916, 8)
        // Make All
        var index = 0
        for (i in 0 until 10) {
            val id: Int = player["skill_creation_item_${i}"] ?: continue
            if (id == barId) {
                index = i
                break
            }
        }
        player.instructions.send(InteractDialogue(905, 14 + index, -1))
        await("smelting")
    }
}

fun isFurnace(map: AreaDefinition, obj: GameObject): Boolean {
    if (!map.area.contains(obj.tile)) {
        return false
    }
    return obj.id.startsWith("furnace")
}