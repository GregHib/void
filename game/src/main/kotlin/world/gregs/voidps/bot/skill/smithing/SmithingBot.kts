package world.gregs.voidps.bot.skill.smithing

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.getGear
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Smithing
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.TimerStop

val interfaceDefinitions: InterfaceDefinitions by inject()
val itemDefinitions: ItemDefinitions by inject()
val areas: AreaDefinitions by inject()
val tasks: TaskManager by inject()

onBot<TimerStop>({ timer == "smithing" }) { bot: Bot ->
    bot.resume(timer)
}

worldSpawn {
    for (area in areas.getTagged("smithing")) {
        val spaces: Int = area["spaces", 1]
        val task = Task(
            name = "smith on anvil at ${area.name}".toLowerSpaceCase(),
            block = {
                val gear = getGear(Skill.Smithing) ?: return@Task
                val types: List<String> = gear.getOrNull("types") ?: return@Task
                while (player.levels.getMax(Skill.Smithing) < gear.levels.last + 1) {
                    smith(area, types, gear)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf { hasExactGear(Skill.Smithing) }
        )
        tasks.register(task)
    }
}

suspend fun Bot.smith(map: AreaDefinition, types: List<String>, set: GearDefinition) {
    setupGear(set, buy = false)
    goToArea(map)
    val anvil = getObject { isAnvil(map, it) }
    if (anvil == null) {
        await("tick")
        return
    }
    val bar = player.inventory.items.first { it.id.endsWith("_bar") }
    val type = types.filter { player.has(Skill.Smithing, itemDefinitions.get(bar.id.replace("_bar", "_$it")).getOrNull<Smithing>("smithing")?.level ?: Int.MAX_VALUE) }.random()
    await("tick")
    while (player.inventory.contains(bar.id)) {
        itemOnObject(bar, anvil)
        await("tick")
        while (player.mode is Interact) {
            await("tick")
        }
        // Make All - item
        val component = interfaceDefinitions.getComponent("smithing", "${type}_all")!!
        val bars = component.getOrNull("bars") ?: 1
        if (!player.inventory.contains(bar.id, bars)) {
            break
        }
        clickInterface(300, InterfaceDefinition.componentId(component.id))
        await("smithing")
    }
}

fun isAnvil(map: AreaDefinition, obj: GameObject): Boolean {
    if (!map.area.contains(obj.tile)) {
        return false
    }
    return obj.id.startsWith("anvil")
}