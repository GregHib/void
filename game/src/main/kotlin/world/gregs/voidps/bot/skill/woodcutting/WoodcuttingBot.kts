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
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.*
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.world.activity.skill.woodcutting.tree.RegularTree
import world.gregs.voidps.world.activity.skill.woodcutting.tree.Tree

val areas: Areas by inject()
val tasks: TaskManager by inject()

on<ActionFinished>({ type == ActionType.Woodcutting }) { bot: Bot ->
    bot.resume("woodcutting")
}

on<World, Startup> {
    for (area in areas.getTagged("trees")) {
        val spaces: Int = area["spaces", 1]
        val range: IntRange = area["levels", "1-5"].toIntRange()
        val type = area["trees", emptyList<String>()].map { RegularTree.valueOf(it.capitalize()) }.firstOrNull()
        val task = Task(
            name = "cut ${(type ?: RegularTree.Tree).name.plural(2).toLowerCase()} at ${area.name}".toTitleCase(),
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

suspend fun Bot.cutTrees(map: MapArea, type: Tree? = null) {
    setupGear(Skill.Woodcutting)
    goToArea(map)
    while (player.inventory.isNotFull()) {
        val trees = player.viewport.objects
            .filter { isAvailableTree(map, it, type) }
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

fun Bot.isAvailableTree(map: MapArea, obj: GameObject, type: Tree?): Boolean {
    if (!map.area.contains(obj.tile)) {
        return false
    }
    if (!obj.def.options.contains("Chop down")) {
        return false
    }
    val tree = Tree.get(obj) ?: return false
    if (type != null && type != tree && type != RegularTree.Oak && tree != RegularTree.Tree) {
        return false
    }
    return player.has(Skill.Woodcutting, tree.level, false)
}