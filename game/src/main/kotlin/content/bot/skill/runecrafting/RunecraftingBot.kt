package content.bot.skill.runecrafting

import content.bot.*
import content.bot.interact.navigation.await
import content.bot.interact.navigation.awaitInteract
import content.bot.interact.navigation.goToArea
import content.bot.skill.combat.hasExactGear
import content.bot.skill.combat.setupGear
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.client.instruction.InteractObject

@Script
class RunecraftingBot : Api {

    val areas: AreaDefinitions by inject()
    val tasks: TaskManager by inject()

    init {
        worldSpawn {
            for (area in areas.getTagged("altar")) {
                val type: String = area["type"]
                val spaces: Int = area["spaces", 1]
                val range: IntRange = area["levels", "1-5"].toIntRange()
                val task = Task(
                    name = "craft $type runes at ${area.name}",
                    block = {
                        while (levels.getMax(Skill.Runecrafting) < range.last + 1) {
                            bot.craftRunes(area)
                        }
                    },
                    area = area.area,
                    spaces = spaces,
                    requirements = listOf(
                        { levels.getMax(Skill.Runecrafting) in range },
                        { bot.hasExactGear(Skill.Runecrafting) },
                    ),
                )
                tasks.register(task)
            }
        }
    }

    suspend fun Bot.craftRunes(map: AreaDefinition) {
        setupGear(Skill.Runecrafting)
        goToArea(map)
        await("tick")
        val altar = getObjects { isAltar(map, it) }
            .first()
        player.instructions.send(InteractObject(altar.def.id, altar.tile.x, altar.tile.y, 1))
        awaitInteract()
    }

    fun isAltar(map: AreaDefinition, obj: GameObject): Boolean {
        if (!map.area.contains(obj.tile)) {
            return false
        }
        return obj.def.containsOption("Craft-rune")
    }
}
