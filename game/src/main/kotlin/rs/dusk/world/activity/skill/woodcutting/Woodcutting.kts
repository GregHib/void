package rs.dusk.world.activity.skill.woodcutting

import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.action.action
import rs.dusk.engine.entity.character.contain.equipment
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.skill.Level
import rs.dusk.engine.entity.character.player.skill.Level.has
import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.entity.character.update.visual.setAnimation
import rs.dusk.engine.entity.item.detail.ItemDetails
import rs.dusk.engine.entity.item.detail.contains
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.ObjectOption
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.get
import rs.dusk.utility.inject
import rs.dusk.world.activity.skill.woodcutting.tree.CursedTree
import rs.dusk.world.activity.skill.woodcutting.tree.DungeoneeringTree
import rs.dusk.world.activity.skill.woodcutting.tree.RegularTree
import rs.dusk.world.activity.skill.woodcutting.tree.Tree
import rs.dusk.world.interact.entity.obj.replace
import kotlin.random.Random

fun ItemDecoder.get(name: String) = get(get<ItemDetails>().get(name).id)// TODO

val decoder: ItemDecoder by inject()
val objects: Objects by inject()

val trees: Array<Tree> = arrayOf(*RegularTree.values(), *DungeoneeringTree.values(), *CursedTree.values())

ObjectOption where { option == "Chop down" } then {
    chopTree(player, obj)
}

fun success(level: Int, hatchet: Hatchet, tree: Tree): Boolean {
    val lowHatchetChance = hatchet.calculateChance(tree.lowDifference)
    val highHatchetChance = hatchet.calculateChance(tree.highDifference)
    val chance = tree.chance.first + lowHatchetChance..tree.chance.last + highHatchetChance
    return Level.success(level, chance)
}

fun chopTree(player: Player, obj: GameObject) {
    player.action {
        try {
            val hatchet = getHatchet(player)
            val tree = getTree(obj)
            var first = true
            while (player.canUse(hatchet, true) && player.canCut(tree, true)) {
                player.setAnimation("${hatchet!!.id}_chop")
                if (first) {
                    player.message("You swing your axe at the tree.")
                    first = false
                }
                delay(4)
                if (success(player.levels.get(Skill.Woodcutting), hatchet, tree!!)) {
                    val log = tree.log
                    if (log != null) {
                        val item = decoder.get(log.id)
                        if (player.inventory.add(item.id)) {
                            player.message("You get some ${item.name}.")
                        } else {
                            player.message("You don't have enough inventory space.")
                            cancel()
                            break
                        }
                    }

                    if (tree.deplete()) {
                        val stump = getStump(obj.id)
                        if(stump != -1) {
                            obj.replace(stump, ticks = 10)
                            val treeTop = objects[obj.tile.add(-1, -1, 1)].firstOrNull()
                            treeTop?.replace(-1, ticks = 10)
                        }
                        cancel()
                        break
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

fun getTree(obj: GameObject): Tree? {
    val tree = obj.def.name.toLowerCase().replace(" ", "_")
    return trees.firstOrNull { tree == it.id }
}

// YEY half broken hard coded switch statements
fun getStump(tree: Int): Int {
    return when (tree) {
        1292 -> 771
        11866 -> 11864
        2023 -> 3371
        3300, 9354, 9355, 9366, 9387, 9388 -> 9389
        4060 -> 4061
        2289 -> 2310
        1276 -> 4329
        1278 -> 1342
        1277 -> 1343
        1383, 1384 -> 1341
        1284 -> 1350
        1286 -> 1351
        1365 -> 1352
        1282, 1283, 1285 -> 1347
        38782 -> 40351
        38784 -> 40353
        38785 -> 40354
        38787 -> 40356
        61190, 38760 -> 40350
        61191, 38783 -> 40352
        61192, 38786 -> 40355
        61193, 38788 -> 40357
        58109, 58108 -> 58134
        58140 -> 58132
        58135 -> 58131
        1316, 1318, 1319, 1330, 1331, 1332 -> 36510
        1315 -> 4329
        1281 -> 1356
        38731 -> 38741
        38732 -> 38754
        139, 142, 2210, 2372 -> 5554
        38627, 38616, 58006 -> 38725
        1307 -> 7400
        4674, 46277 -> 1343
        51843 -> 54766
        28951 -> 28954
        28952 -> 28955
        28953 -> 28956
        1309 -> 7402
        38755 -> 38759
        1306, 37823 -> 7401
        63176 -> 63179
        70076 -> 70081
        70075 -> 70080
        70074 -> 70079
        70077, 46274 -> 70082
        37821 -> 37822
        69554 -> 69555
        69556 -> 69557
        4135, 19153 -> 4136
        else -> -1
    }
}