package content.bot.skill.woodcutting

import content.bot.*
import content.bot.interact.navigation.await
import content.bot.interact.navigation.goToArea
import content.bot.interact.navigation.resume
import content.bot.skill.combat.hasExactGear
import content.bot.skill.combat.setupGear
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.data.Tree
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.network.client.instruction.InteractObject
import content.entity.death.weightedSample

val areas: AreaDefinitions by inject()
val tasks: TaskManager by inject()

timerStop("woodcutting") { player ->
    if (player.isBot) {
        player.bot.resume(timer)
    }
}

worldSpawn {
    for (area in areas.getTagged("trees")) {
        val spaces: Int = area["spaces", 1]
        val range: IntRange = area["levels", "1-5"].toIntRange()
        val type = area["trees", emptyList<String>()].firstOrNull()
        val task = Task(
            name = "cut ${(type ?: "tree").plural(2).lowercase()} at ${area.name}",
            block = {
                while (levels.getMax(Skill.Woodcutting) < range.last + 1) {
                    bot.cutTrees(area, type)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf(
                { levels.getMax(Skill.Woodcutting) in range },
                { bot.hasExactGear(Skill.Woodcutting) || bot.hasCoins(1000) }
            )
        )
        tasks.register(task)
    }
}

suspend fun Bot.cutTrees(map: AreaDefinition, type: String? = null) {
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
        player.instructions.send(InteractObject(tree.def.id, tree.tile.x, tree.tile.y, 1))
        await("woodcutting")
    }
}

fun Bot.isAvailableTree(map: AreaDefinition, obj: GameObject, type: String?): Boolean {
    if (!map.area.contains(obj.tile)) {
        return false
    }
    if (!obj.def.containsOption("Chop down")) {
        return false
    }
    if (type != null && !obj.id.contains(type)) {
        return false
    }
    val tree: Tree = obj.def.getOrNull("woodcutting") ?: return false
    return player.has(Skill.Woodcutting, tree.level, false)
}