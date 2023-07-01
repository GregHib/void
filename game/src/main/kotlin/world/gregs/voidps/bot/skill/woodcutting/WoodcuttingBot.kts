package world.gregs.voidps.bot.skill.woodcutting

import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.data.definition.data.Tree
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.world.interact.entity.death.weightedSample

val areas: Areas by inject()
val tasks: TaskManager by inject()

onBot<TimerStop>({ timer == "woodcutting" }) { bot: Bot ->
    bot.resume(timer)
}

on<World, Registered> {
    for (area in areas.getTagged("trees")) {
        val spaces: Int = area["spaces", 1]
        val range: IntRange = area["levels", "1-5"].toIntRange()
        val type = area["trees", emptyList<String>()].firstOrNull()
        val task = Task(
            name = "cut ${(type ?: "tree").plural(2).lowercase()} at ${area.name}",
            block = {
                while (player.levels.getMax(Skill.Woodcutting) < range.last + 1) {
                    cutTrees(area, type)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf(
                { player.levels.getMax(Skill.Woodcutting) in range },
                { hasExactGear(Skill.Woodcutting) || hasCoins(1000) }
            )
        )
        tasks.register(task)
    }
}

suspend fun Bot.cutTrees(map: MapArea, type: String? = null) {
    setupGear(Skill.Woodcutting)
    goToArea(map)
    while (player.inventory.spaces > 0) {
        val trees = getObjects { isAvailableTree(map, it, type) }
            .map { tree -> tree to tile.distanceTo(tree) }
        val tree = weightedSample(trees, invert = true)
        if (tree == null) {
            await("tick")
            if (player.inventory.spaces < 4) {
                break
            }
            continue
        }
        player.instructions.emit(InteractObject(tree.def.id, tree.tile.x, tree.tile.y, 1))
        await("woodcutting")
    }
}

fun Bot.isAvailableTree(map: MapArea, obj: GameObject, type: String?): Boolean {
    if (!map.area.contains(obj.tile)) {
        return false
    }
    if (!obj.def.contains("Chop down")) {
        return false
    }
    if (type != null && !obj.id.contains(type)) {
        return false
    }
    val tree: Tree = obj.def.getOrNull("woodcutting") ?: return false
    return player.has(Skill.Woodcutting, tree.level, false)
}