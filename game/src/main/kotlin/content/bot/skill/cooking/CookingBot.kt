package content.bot.skill.cooking

import content.bot.*
import content.bot.interact.navigation.await
import content.bot.interact.navigation.goToArea
import content.bot.interact.navigation.resume
import content.bot.skill.combat.getGear
import content.bot.skill.combat.getSuitableItem
import content.bot.skill.combat.hasExactGear
import content.bot.skill.combat.setupGear
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractDialogue
import world.gregs.voidps.network.client.instruction.InteractInterfaceObject

@Script
class CookingBot : Api {

    val areas: AreaDefinitions by inject()
    val tasks: TaskManager by inject()

    init {
        worldSpawn {
            for (area in areas.getTagged("cooking")) {
                val spaces: Int = area["spaces", 1]
                val type: String = area.getOrNull("type") ?: ""
                val task = Task(
                    name = "cook on ${type.plural(2)} at ${area.name}".toLowerSpaceCase(),
                    block = {
                        val gear = bot.getGear(Skill.Cooking) ?: return@Task
                        val item = bot.getSuitableItem(gear.inventory.first())
                        while (levels.getMax(Skill.Cooking) < gear.levels.last + 1) {
                            bot.cook(area, item, gear)
                        }
                    },
                    area = area.area,
                    spaces = spaces,
                    requirements = listOf { bot.hasExactGear(Skill.Cooking) },
                )
                tasks.register(task)
            }
        }

        timerStop("cooking") {
            if (isBot) {
                bot.resume("cooking")
            }
        }
    }

    suspend fun Bot.cook(map: AreaDefinition, rawItem: Item, set: GearDefinition) {
        setupGear(set, buy = false)
        goToArea(map)
        if (player.inventory.contains(rawItem.id)) {
            val range = getObject { isRange(map, it) }
            if (range == null) {
                await("tick")
                return
            }
            // Use item on range
            player.instructions.send(InteractInterfaceObject(range.def.id, range.tile.x, range.tile.y, 149, 0, rawItem.def.id, player.inventory.indexOf(rawItem.id)))
            await("tick")
            await("tick")
            if (rawItem.id == "raw_beef") {
                player.instructions.send(InteractDialogue(228, 3, -1))
                await("tick")
            }
            // Select all
            clickInterface(916, 8, 0)
            await("tick")
            // First option
            player.instructions.send(InteractDialogue(905, 14, -1))
        }
        var count = 0
        while (player.inventory.contains(rawItem.id)) {
            await("cooking")
            if (count++ > 28) {
                break
            }
        }
    }

    fun isRange(map: AreaDefinition, obj: GameObject): Boolean {
        if (!map.area.contains(obj.tile)) {
            return false
        }
        return obj.id.startsWith("cooking_range")
    }
}
