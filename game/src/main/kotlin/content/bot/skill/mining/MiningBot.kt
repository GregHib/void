package content.bot.skill.mining

import content.bot.*
import content.bot.interact.navigation.await
import content.bot.interact.navigation.goToArea
import content.bot.interact.navigation.resume
import content.bot.skill.combat.hasExactGear
import content.bot.skill.combat.setupGear
import content.entity.death.weightedSample
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractObject

@Script
class MiningBot : Api {

    val areas: AreaDefinitions by inject()
    val tasks: TaskManager by inject()

    init {
        worldSpawn {
            for (area in areas.getTagged("mine")) {
                val spaces: Int = area["spaces", 1]
                val type = area["rocks", emptyList<String>()].firstOrNull() ?: continue
                val range: IntRange = area["levels", "1-5"].toIntRange()
                val task = Task(
                    name = "mine ${type.plural(2)} at ${area.name}".toLowerSpaceCase(),
                    block = {
                        while (levels.getMax(Skill.Mining) < range.last + 1) {
                            bot.mineRocks(area, type)
                        }
                    },
                    area = area.area,
                    spaces = spaces,
                    requirements = listOf(
                        { levels.getMax(Skill.Mining) in range },
                        { bot.hasExactGear(Skill.Woodcutting) || bot.hasCoins(1000) },
                    ),
                )
                tasks.register(task)
            }
        }

        timerStop("mining") {
            if (isBot) {
                bot.resume("mining")
            }
        }
    }

    suspend fun Bot.mineRocks(map: AreaDefinition, type: String) {
        setupGear(Skill.Mining)
        goToArea(map)
        while (player.inventory.spaces > 0) {
            val rocks = getObjects { isAvailableRock(map, it, type) }
                .map { rock -> rock to tile.distanceTo(rock) }
            val rock = weightedSample(rocks, invert = true)
            if (rock == null) {
                await("tick")
                if (player.inventory.spaces < 4) {
                    break
                }
                continue
            }
            player.instructions.send(InteractObject(rock.def.id, rock.tile.x, rock.tile.y, 1))
            await("mining")
        }
    }

    fun Bot.isAvailableRock(map: AreaDefinition, obj: GameObject, type: String): Boolean {
        if (!map.area.contains(obj.tile)) {
            return false
        }
        if (!obj.def.containsOption("Mine")) {
            return false
        }
        if (!obj.id.contains(type)) {
            return false
        }
        val rock: Rock = obj.def.getOrNull("mining") ?: return false
        return player.has(Skill.Mining, rock.level, false)
    }
}
