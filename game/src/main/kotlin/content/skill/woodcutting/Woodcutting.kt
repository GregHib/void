package content.skill.woodcutting

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.data.Tree
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.type.random

class Woodcutting : Script {

    val players: Players by inject()
    val definitions: ObjectDefinitions by inject()
    val objects: GameObjects by inject()
    val floorItems: FloorItems by inject()
    val drops: DropTables by inject()

    val minPlayers = 0
    val maxPlayers = 2000

    init {
        objectOperate("Chop", handler = ::chopDown)
        objectOperate("Chop down", handler = ::chopDown)
        objectOperate("Chop-down", handler = ::chopDown)
    }

    suspend fun chopDown(player: Player, interact: PlayerOnObjectInteract) {
        val target = interact.target
        val tree: Tree = target.def.getOrNull("woodcutting") ?: return
        val hatchet = Hatchet.best(player)
        if (hatchet == null) {
            player.message("You need a hatchet to chop down this tree.")
            player.message("You do not have a hatchet which you have the woodcutting level to use.")
            return
        }
        player.closeDialogue()
        player.softTimers.start("woodcutting")
        val ivy = tree.log.isEmpty()
        var first = true
        while (player.awaitDialogues()) {
            if (!objects.contains(target) || !player.has(Skill.Woodcutting, tree.level, true)) {
                break
            }

            if (!ivy && player.inventory.isFull()) {
                player.message("Your inventory is too full to hold any more logs.")
                break
            }

            if (!Hatchet.hasRequirements(player, hatchet, true)) {
                break
            }
            if (first) {
                player.message("You swing your hatchet at the ${if (ivy) "ivy" else "tree"}.")
                first = false
            }
            val remaining = player.remaining("action_delay")
            if (remaining < 0) {
                player.anim("${hatchet.id}_chop${if (ivy) "_ivy" else ""}")
                player.start("action_delay", 3)
                player.pause(3)
            } else if (remaining > 0) {
                player.pause(remaining)
            }
            if (!objects.contains(target)) {
                break
            }
            if (success(player.levels.get(Skill.Woodcutting), hatchet, tree)) {
                player.experience.add(Skill.Woodcutting, tree.xp)
                tryDropNest(player, ivy)
                if (!addLog(player, tree) || deplete(tree, target)) {
                    break
                }
                if (ivy) {
                    player.message("You successfully chop away some ivy.")
                }
            }
            player.stop("action_delay")
        }
        player.softTimers.stop("woodcutting")
    }

    fun tryDropNest(player: Player, ivy: Boolean) {
        val dropChance = 254
        if (random.nextInt(dropChance) != 0) return
        val table = drops.get("birds_nest_table") ?: return

        val hasRabbitFoot = player.equipment.contains("strung_rabbit_foot")
        val totalWeight = if (hasRabbitFoot) 95 else 100

        val drop = table.role(totalWeight).firstOrNull() ?: return

        val source = if (ivy) "ivy" else "tree"
        player.message("<red>A bird's nest falls out of the $source!")
        areaSound("bird_chirp", player.tile)

        val dropTile = player.tile.toCuboid(1).random(player) ?: player.tile
        floorItems.add(tile = dropTile, id = drop.id, amount = drop.amount?.start ?: 1, disappearTicks = 50)
    }

    fun success(level: Int, hatchet: Item, tree: Tree): Boolean {
        val lowHatchetChance = calculateChance(hatchet, tree.hatchetLowDifference)
        val highHatchetChance = calculateChance(hatchet, tree.hatchetHighDifference)
        val chance = tree.chance.first + lowHatchetChance..tree.chance.last + highHatchetChance
        return Level.success(level, chance)
    }

    fun calculateChance(hatchet: Item, treeHatchetDifferences: IntRange): Int = (0 until hatchet.def["rank", 0]).sumOf { calculateHatchetChance(it, treeHatchetDifferences) }

    /**
     * Calculates the chance of success out of 256 given a [hatchet] and the hatchet chances for that tree [treeHatchetDifferences]
     * @param hatchet The index of the hatchet (0..7)
     * @param treeHatchetDifferences The min and max increase chance between each hatchet
     * @return chance of success
     */
    fun calculateHatchetChance(hatchet: Int, treeHatchetDifferences: IntRange): Int = if (hatchet % 4 < 2) treeHatchetDifferences.last else treeHatchetDifferences.first

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
            random.nextInt(delay.first, delay.last) // Regular tree's
        } else {
            Interpolation.interpolate(players.size, delay.last, delay.first, minPlayers, maxPlayers)
        }
    }
}
