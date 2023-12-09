package world.gregs.voidps.bot.skill.runecrafting

import world.gregs.voidps.bot.Bot
import world.gregs.voidps.bot.Task
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.getObjects
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.awaitInteract
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.instruct.InteractObject

val areas: AreaDefinitions by inject()
val tasks: TaskManager by inject()

on<World, Registered> {
    for (area in areas.getTagged("altar")) {
        val type: String = area["type"]
        val spaces: Int = area["spaces", 1]
        val range: IntRange = area["levels", "1-5"].toIntRange()
        val task = Task(
            name = "craft $type runes at ${area.name}",
            block = {
                while (player.levels.getMax(Skill.Runecrafting) < range.last + 1) {
                    craftRunes(area)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf(
                { player.levels.getMax(Skill.Runecrafting) in range },
                { hasExactGear(Skill.Runecrafting) }
            )
        )
        tasks.register(task)
    }
}

suspend fun Bot.craftRunes(map: AreaDefinition) {
    setupGear(Skill.Runecrafting)
    goToArea(map)
    await("tick")
    val altar = getObjects { isAltar(map, it) }
        .first()
    player.instructions.emit(InteractObject(altar.def.id, altar.tile.x, altar.tile.y, 1))
    awaitInteract()
}

fun isAltar(map: AreaDefinition, obj: GameObject): Boolean {
    if (!map.area.contains(obj.tile)) {
        return false
    }
    return obj.def.containsOption("Craft-rune")
}