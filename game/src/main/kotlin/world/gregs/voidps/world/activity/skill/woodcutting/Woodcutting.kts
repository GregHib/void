package world.gregs.voidps.world.activity.skill.woodcutting

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.data.Tree
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.sound.areaSound

val players: Players by inject()
val definitions: ObjectDefinitions by inject()
val objects: GameObjects by inject()

val minPlayers = 0
val maxPlayers = 2000

on<ObjectOption>({ operate && def.contains("woodcutting") && (option == "Chop down" || option == "Chop") }) { player: Player ->
    val tree: Tree = def.getOrNull("woodcutting") ?: return@on
    val hatchet = getBestHatchet(player)
    if (hatchet == null) {
        player.message("You need a hatchet to chop down this tree.")
        player.message("You do not have a hatchet which you have the woodcutting level to use.")
        return@on
    }
    arriveDelay()
    player.closeDialogue()
    player.softTimers.start("woodcutting")
    val ivy = tree.log.isEmpty()
    var first = true
    while (awaitDialogues()) {
        if (!objects.contains(target)) {
            break
        }

        if (!player.has(Skill.Woodcutting, tree.level, true)) {
            break
        }

        if (!ivy && player.inventory.isFull()) {
            player.message("Your inventory is too full to hold any more logs.")
            break
        }

        if (!hasRequirements(player, hatchet, true)) {
            break
        }
        if (first) {
            player.message("You swing your hatchet at the ${if (ivy) "ivy" else "tree"}.")
            first = false
        }
        val remaining = player.remaining("skill_delay")
        if (remaining < 0) {
            player.setAnimation("${hatchet.id}_chop${if (ivy) "_ivy" else ""}")
            player.start("skill_delay", 3)
            pause(3)
        } else if (remaining > 0) {
            pause(remaining)
        }
        if (success(player.levels.get(Skill.Woodcutting), hatchet, tree)) {
            player.experience.add(Skill.Woodcutting, tree.xp)
            if (!addLog(player, tree) || deplete(tree, target)) {
                break
            }

            if (ivy) {
                player.message("You successfully chop away some ivy.")
            }
        }
        player.stop("skill_delay")
    }
    player.softTimers.stop("woodcutting")
}

val hatchets = listOf(
    Item("inferno_adze"),
    Item("volatile_clay_hatchet"),
    Item("sacred_clay_hatchet"),
    Item("dragon_hatchet"),
    Item("rune_hatchet"),
    Item("adamant_hatchet"),
    Item("mithril_hatchet"),
    Item("black_hatchet"),
    Item("steel_hatchet"),
    Item("iron_hatchet"),
    Item("bronze_hatchet")
)

fun getBestHatchet(player: Player): Item? {
    return hatchets.firstOrNull { hasRequirements(player, it, false) && player.holdsItem(it.id) }
}

fun hasRequirements(player: Player, hatchet: Item, message: Boolean = false): Boolean {
    if (hatchet.id == "inferno_adze" && !player.has(Skill.Firemaking, hatchet.def["fm_level", 1], message)) {
        return false
    }
    return player.hasRequirementsToUse(hatchet, message, setOf(Skill.Firemaking, Skill.Firemaking))
}

fun success(level: Int, hatchet: Item, tree: Tree): Boolean {
    val lowHatchetChance = calculateChance(hatchet, tree.hatchetLowDifference)
    val highHatchetChance = calculateChance(hatchet, tree.hatchetHighDifference)
    val chance = tree.chance.first + lowHatchetChance..tree.chance.last + highHatchetChance
    return Level.success(level, chance)
}

fun calculateChance(hatchet: Item, treeHatchetDifferences: IntRange): Int {
    return (0 until hatchet.def["rank", 0]).sumOf { calculateHatchetChance(it, treeHatchetDifferences) }
}

/**
 * Calculates the chance of success out of 256 given a [hatchet] and the hatchet chances for that tree [treeHatchetDifferences]
 * @param hatchet The index of the hatchet (0..7)
 * @param treeHatchetDifferences The min and max increase chance between each hatchet
 * @return chance of success
 */
fun calculateHatchetChance(hatchet: Int, treeHatchetDifferences: IntRange): Int {
    return if (hatchet % 4 < 2) treeHatchetDifferences.last else treeHatchetDifferences.first
}

fun addLog(player: Player, tree: Tree): Boolean {
    val log = tree.log
    if (log.isEmpty()) {
        return true
    }
    val added = player.inventory.add(log)
    if (added) {
        player.message("You get some ${log.toLowerSpaceCase()}.")
    } else {
        player.inventoryFull()
    }
    return added
}

fun deplete(tree: Tree, obj: GameObject): Boolean {
    val depleted = random.nextDouble() <= tree.depleteRate
    if (!depleted) {
        return false
    }
    val stumpId = "${obj.id}_stump"
    if (definitions.contains(stumpId)) {
        val delay = getRegrowTickDelay(tree)
        objects.replace(obj, stumpId, ticks = delay)
        areaSound("fell_tree", obj.tile)
    }
    return true
}

/**
 * Returns regrow delay based on the type of tree and number of players online
 */
fun getRegrowTickDelay(tree: Tree): Int {
    val delay = tree.respawnDelay
    return if (tree.level == 1) {
        random.nextInt(delay.first, delay.last)// Regular tree's
    } else {
        Interpolation.interpolate(players.size, delay.last, delay.first, minPlayers, maxPlayers)
    }
}