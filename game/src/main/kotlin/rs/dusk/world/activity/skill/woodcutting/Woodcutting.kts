package rs.dusk.world.activity.skill.woodcutting

import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.action.action
import rs.dusk.engine.entity.character.contain.equipment
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.entity.character.update.visual.setAnimation
import rs.dusk.engine.entity.item.detail.ItemDetails
import rs.dusk.engine.entity.item.detail.contains
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.ObjectOption
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.get
import rs.dusk.utility.inject
import rs.dusk.world.activity.skill.woodcutting.tree.CursedTree
import rs.dusk.world.activity.skill.woodcutting.tree.DungeoneeringTree
import rs.dusk.world.activity.skill.woodcutting.tree.RegularTree
import rs.dusk.world.activity.skill.woodcutting.tree.Tree
import kotlin.random.Random

fun Player.has(skill: Skill, level: Int, message: Boolean): Boolean {
    if (levels.get(skill) < level) {
        if (message) {
            message("You need to have an ${skill.name} level of $level.")
        }
        return false
    }
    return true
}

fun ItemDecoder.get(name: String) = get(get<ItemDetails>().get(name).id)// TODO

val decoder: ItemDecoder by inject()

ObjectOption where { obj.def.name.toLowerCase().contains("tree") && option == "Chop down" } then {
    chopTree(player, obj)
}

fun success(level: Int): Boolean {
    return (2 * level + 20) / 256.0 == 0.0
}

fun chopTree(player: Player, obj: GameObject) {
    player.action {
        try {
            val hatchet = getHatchet(player)
            val tree = getTree(obj)
            while (player.canUse(hatchet, true) && player.canCut(tree, true)) {
                player.setAnimation(hatchet!!.id)
                player.message("You swing your axe at the tree.")
                delay(4)
                val item = decoder.get(tree!!.log.id)
                if (success(1)) {
                    if (player.inventory.add(item.id)) {
                        if (tree.deplete()) {
                            // TODO
                        }
                        player.message("You get some ${item.name}.")
                    } else {
                        player.message("You don't have enough inventory space.")
                        cancel()
                    }
                }
            }
        } finally {
            player.setAnimation(-1)
        }
    }
}

fun Player.canUse(hatchet: Hatchet?, message: Boolean): Boolean {
    if (hatchet == null) {
        if (message) {
            message("You need a hatchet to chop down this tree.")
        }
        return false
    }
    val wc = getLevelRequirement(hatchet)
    if (hatchet == Hatchet.Inferno_Adze && !has(Skill.Woodcutting, 92, message)) {
        return false
    }
    if (!has(Skill.Woodcutting, wc, message)) {
        return false
    }
    return true
}

fun Player.canCut(tree: Tree?, message: Boolean): Boolean {
    if (tree == null) {
        return false
    }
    if (!has(Skill.Woodcutting, tree.level, message)) {
        return false
    }
    return true
}

fun getLevelRequirement(hatchet: Hatchet): Int {
    return when (hatchet) {
        Hatchet.Inferno_Adze -> 61
        Hatchet.Sacred_Clay_Hatchet, Hatchet.Volatile_Clay_Hatchet -> 50
        else -> decoder.get(hatchet.id).getParam(750L, 0)
    }
}

fun getHatchet(player: Player): Hatchet? {
    val list = Hatchet.values().filter { player.canUse(it, false) && (player.inventory.contains(it.id) || player.equipment.contains(it.id)) }
    return list.maxBy { getLevelRequirement(it) }
}

fun Tree.deplete() = Random.nextDouble() <= fellRate

val trees: Array<Tree> = arrayOf(*RegularTree.values(), *DungeoneeringTree.values(), *CursedTree.values())

fun getTree(obj: GameObject): Tree? {
    val tree = obj.def.name.toLowerCase().replace(" ", "_")
    return trees.firstOrNull { tree == it.id  }
}