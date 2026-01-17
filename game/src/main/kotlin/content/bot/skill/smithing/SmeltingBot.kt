package content.bot.skill.smithing

import content.bot.*
import content.bot.interact.navigation.await
import content.bot.interact.navigation.goToArea
import content.bot.interact.navigation.resume
import content.bot.skill.combat.getGear
import content.bot.skill.combat.hasExactGear
import content.bot.skill.combat.setupGear
import content.skill.smithing.oreToBar
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractDialogue

class SmeltingBot(
    val tasks: TaskManager,
    val itemDefinitions: ItemDefinitions,
) : Script {

    init {
        worldSpawn {
            for (area in Areas.tagged("smelting")) {
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
                    requirements = listOf { bot.hasExactGear("smelting", Skill.Smithing) },
                )
                tasks.register(task)
            }
        }

        timerStop("smelting") {
            if (isBot) {
                bot.resume("smelting")
            }
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
                val id: Int = player["skill_creation_item_$i"] ?: continue
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
}
