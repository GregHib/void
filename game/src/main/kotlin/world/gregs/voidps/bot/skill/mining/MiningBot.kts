import world.gregs.voidps.bot.Task
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.hasCoins
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.data.MiningRock
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.engine.utility.toIntRange
import world.gregs.voidps.engine.utility.weightedSample
import world.gregs.voidps.network.instruct.InteractObject

val areas: Areas by inject()
val tasks: TaskManager by inject()

on<ActionFinished>({ type == ActionType.Mining }) { bot: Bot ->
    bot.resume("mining")
}

on<World, Startup> {
    for (area in areas.getTagged("mine")) {
        val spaces: Int = area["spaces", 1]
        val type = area["rocks", emptyList<String>()].firstOrNull() ?: continue
        val range: IntRange = area["levels", "1-5"].toIntRange()
        val task = Task(
            name = "mine ${type.plural(2).lowercase()} at ${area.name}".replace("_", " "),
            block = {
                while (player.levels.getMax(Skill.Mining) < range.last + 1) {
                    mineRocks(area, type)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf(
                { player.levels.getMax(Skill.Mining) in range },
                { hasExactGear(Skill.Woodcutting) || hasCoins(1000) }
            )
        )
        tasks.register(task)
    }
}

suspend fun Bot.mineRocks(map: MapArea, type: String) {
    setupGear(Skill.Mining)
    goToArea(map)
    while (player.inventory.isNotFull()) {
        val rocks = player.viewport.objects
            .filter { isAvailableRock(map, it, type) }
            .map { rock -> rock to tile.distanceTo(rock) }
        val rock = weightedSample(rocks, invert = true)
        if (rock == null) {
            await("tick")
            if (player.inventory.spaces < 4) {
                break
            }
            continue
        }
        player.instructions.emit(InteractObject(rock.def.id, rock.tile.x, rock.tile.y, 1))
        await("mining")
    }
}

fun Bot.isAvailableRock(map: MapArea, obj: GameObject, type: String): Boolean {
    if (!map.area.contains(obj.tile)) {
        return false
    }
    if (!obj.def.options.contains("Mine")) {
        return false
    }
    if (!obj.id.contains(type)) {
        return false
    }
    val rock = obj.def.getOrNull("mining") as? MiningRock ?: return false
    return player.has(Skill.Mining, rock.level, false)
}