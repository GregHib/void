package rs.dusk.world.activity.skill.woodcutting

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.skill.Level
import rs.dusk.engine.entity.character.player.skill.Level.has
import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.entity.character.update.visual.setAnimation
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.ObjectOption
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.area.area
import rs.dusk.utility.Maths
import rs.dusk.utility.inject
import rs.dusk.world.activity.skill.woodcutting.tree.Tree
import rs.dusk.world.interact.entity.obj.remove
import rs.dusk.world.interact.entity.obj.replace
import kotlin.random.Random

val definitions: ItemDefinitions by inject()
val objects: Objects by inject()
val players: Players by inject()

val minPlayers = 0
val maxPlayers = 2000

ObjectOption where { option == "Chop down" || option == "Chop" } then {
    player.action(ActionType.Woodcutting) {
        try {
            var first = true
            while (true) {
                val tree = Tree.get(obj)
                if (tree == null || !player.has(Skill.Woodcutting, tree.level, true)) {
                    break
                }
                val hatchet = Hatchet.get(player)
                if (!Hatchet.hasRequirements(player, hatchet, true) || hatchet == null) {
                    break
                }
                player.setAnimation("${hatchet.id}_chop")
                if (first) {
                    player.message("You swing your hatchet at the tree.")
                    first = false
                }
                delay(4)
                if (success(player.levels.get(Skill.Woodcutting), hatchet, tree)) {
                    player.experience.add(Skill.Woodcutting, tree.xp)

                    if (!addLog(player, tree) || deplete(tree, obj)) {
                        break
                    }
                }
            }
        } finally {
            player.setAnimation(-1)
        }
    }
}

fun success(level: Int, hatchet: Hatchet, tree: Tree): Boolean {
    val lowHatchetChance = hatchet.calculateChance(tree.lowDifference)
    val highHatchetChance = hatchet.calculateChance(tree.highDifference)
    val chance = tree.chance.first + lowHatchetChance..tree.chance.last + highHatchetChance
    return Level.success(level, chance)
}

fun addLog(player: Player, tree: Tree): Boolean {
    val log = tree.log ?: return true
    val item = definitions.get(log.id)
    val added = player.inventory.add(item.id)
    player.message(if (added) "You get some ${item.name.toLowerCase()}." else "You don't have enough inventory space.")
    return added
}

fun deplete(tree: Tree, obj: GameObject): Boolean {
    val depleted = Random.nextDouble() <= tree.depleteRate
    if (!depleted) {
        return false
    }
    val stumpId = obj.def["stump", -1]
    if (stumpId != -1) {
        val delay = getRegrowTickDelay(tree)
        obj.replace(stumpId, ticks = delay)
        removeCanopy(obj, delay)
    }
    return true
}

/**
 * Returns regrow delay based on the type of tree and number of players online
 */
fun getRegrowTickDelay(tree: Tree): Int {
    val delay = tree.respawnDelay
    return if (tree.level == 1) {
        Random.nextInt(delay.first, delay.last)// Regular tree's
    } else {
        Maths.interpolate(players.count, delay.last, delay.first, minPlayers, maxPlayers)
    }
}

/**
 * Removes the tree canopy (if exists) on the tile above
 */
fun removeCanopy(obj: GameObject, delay: Int) {
    val canopyTile = obj.tile.add(plane = 1)
    val chunks = canopyTile.chunk.area(1)
    chunks.flatMap { objects[it] }
        .firstOrNull { it.tile.within(canopyTile, 1) }
        ?.remove(ticks = delay)
}