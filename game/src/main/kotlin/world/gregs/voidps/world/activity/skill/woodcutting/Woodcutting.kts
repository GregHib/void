package world.gregs.voidps.world.activity.skill.woodcutting

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Level
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.Math
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.skill.woodcutting.tree.RegularTree
import world.gregs.voidps.world.activity.skill.woodcutting.tree.Tree
import kotlin.random.Random

val players: Players by inject()

val minPlayers = 0
val maxPlayers = 2000

on<ObjectOption>({ option == "Chop down" || option == "Chop" }) { player: Player ->
    player.action(ActionType.Woodcutting) {
        try {
            var first = true
            while (true) {
                val tree = Tree.get(obj)
                if (tree == null || !player.has(Skill.Woodcutting, tree.level, true)) {
                    break
                }

                val ivy = tree == RegularTree.Ivy
                if (!ivy && player.inventory.isFull()) {
                    player.message("Your inventory is too full to hold any more logs.")
                    break
                }

                val hatchet = Hatchet.get(player)
                if (!Hatchet.hasRequirements(player, hatchet, true) || hatchet == null) {
                    break
                }

                player.setAnimation("${hatchet.id}_chop${if (ivy) "_ivy" else ""}")
                if (first) {
                    player.message("You swing your hatchet at the ${if (ivy) "ivy" else "tree"}.")
                    first = false
                }
                delay(4)
                if (success(player.levels.get(Skill.Woodcutting), hatchet, tree)) {
                    player.experience.add(Skill.Woodcutting, tree.xp)

                    if (!addLog(player, tree) || deplete(tree, obj)) {
                        break
                    }

                    if (ivy) {
                        player.message("You successfully chop away some ivy.")
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
    val added = player.inventory.add(log.id)
    player.message(if (added) "You get some ${log.id.replace("_", "").toLowerCase()}." else "You don't have enough inventory space.")
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
        Math.interpolate(players.count, delay.last, delay.first, minPlayers, maxPlayers)
    }
}