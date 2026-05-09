package content.activity.evil_tree

import com.github.michaelbull.logging.InlineLogger
import content.entity.effect.transform
import content.entity.player.dialogue.type.statement
import content.skill.woodcutting.Hatchet
import net.pearx.kasechange.toCamelCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class EvilTree : Script {

    val logger = InlineLogger()

    companion object {
        var place = ""
        var tree: GameObject = GameObject(0)
        var spawnTile: Tile = Tile.EMPTY
        var type: String = "normal"
        val roots = arrayOfNulls<GameObject>(4)
        var health = 100
        var leprechaun = NPC()

        fun isSapling() = when (tree.id) {
            "evil_tree_seedling", "strange_sapling", "evil_tree_sapling_large", "evil_tree_young", "evil_tree_young_large" -> true
            else -> false
        }
    }

    init {
        // TODO spawn roots on players with 2x2 regions

        worldSpawn {
            if (Settings["events.evilTree.enabled", false]) {
                start()
            }
        }

        settingsReload {
            if (Settings["events.evilTree.enabled", false] && spawnTile == Tile.EMPTY) {
                start()
            } else if (!Settings["events.evilTree.enabled", false]) {
                clear()
            }
        }

        // TODO grows self every 1 minute
        objectOperate("Nurture", "evil_tree_seedling,evil_tree_sapling*,evil_tree_young*") { (target) ->
            val level = Tables.intOrNull("evil_tree_type.${type}.farming") ?: return@objectOperate
            if (!has(Skill.Farming, level, " to help this sapling grow")) {
                return@objectOperate
            }
            message("You begin tending to the sapling.", ChatType.Filter)
            nurture(target)
        }

        objectOperate("Inspect", "evil_tree_*") {
            val level = Tables.int("evil_tree_type.${type}.woodcutting")
            statement("This is an Evil${if (type == "normal") "" else " ${type.toCamelCase()}"} tree. A Woodcutting / Firemaking level of at least ${level} is required to interact with this tree and the surrounding roots.")
        }

        objectOperate("Chop", "evil_tree_*") { (target) ->
            nextStage(target)
        }

        objectDespawn("evil_branches_*_spawn") {
            for (root in roots) {
                root ?: continue
                root.replace(root.id.removeSuffix("_spawn"))
            }
        }

        npcSpawn("leprechaun_evil_tree") {
            softTimers.start("evil_tree_timer")
        }

        timerStart("evil_tree_timer") {
            10
        }

        timerTick("evil_tree_timer") {
            when (tree.id) {
                "evil_tree_seedling", "evil_tree_sapling", "evil_tree_sapling_large", "evil_tree_young" + "evil_tree_young_large" -> takeHealth(10)
            }
            // TODO lightning strike every 30 mins
            Timer.CONTINUE
        }
        // TODO light fires
    }

    private fun takeHealth(amount: Int) {
        health -= amount
        if (health <= 0) {
            nextStage(tree)
        }
    }

    fun Player.nurture(target: GameObject) {
        if (tree != target) {
            return
        }
        anim("nurture_sapling")
        weakQueue("nurture_evil_sapling", 3) {
            if (tree != target) {
                return@weakQueue
            }
            exp(Skill.Farming, Tables.int("evil_tree_type.${type}.nurture_xp") / 10.0)
            takeHealth(1)
            nurture(target)
        }
    }

    suspend fun Player.chop(target: GameObject) {
        val hatchet = Hatchet.best(this)
        if (hatchet == null) {
            message("You need a hatchet to chop down this tree.")
            message("You do not have a hatchet which you have the woodcutting level to use.")
            return
        }
        val row = Rows.get("evil_tree_type.${type}")
        while (awaitDialogues()) {
            val level = row.int("woodcutting")
            if (!GameObjects.contains(target) || !has(Skill.Woodcutting, level, message = true)) {
                return
            }
            if (!Hatchet.hasRequirements(this, hatchet, message = true)) {
                break
            }
            val remaining = remaining("action_delay")
            if (remaining < 0) {
                anim("${hatchet.id}_chop")
                start("action_delay", 3)
                pause(3)
            } else if (remaining > 0) {
                pause(remaining)
            }
            if (!GameObjects.contains(target)) {
                break
            }
//            if (success(player.levels.get(Skill.Woodcutting), hatchet, log)) {
            exp(Skill.Woodcutting, row.int("tree_xp") / 10.0)
//            }
            // TODO branch attacks when chopping root
        }
    }

    suspend fun Player.light(target: GameObject) {
        val row = Rows.get("evil_tree_type.${type}")
        var first = true
        while (awaitDialogues()) {
            val level = row.int("woodcutting")
            if (!GameObjects.contains(target) || !has(Skill.Firemaking, level, message = " to set fire to this evil tree")) {
                return
            }
//            message("That part of the tree is already on fire!")
            if (!inventory.contains("tinderbox")) {
                message("You need a tinderbox in order to light a fire.")
                return
            }
            if (!inventory.contains("kindling")) {
                message("You don't have the required items to light this.")
                return
            }
            val remaining = remaining("action_delay")
            if (remaining < 0) {
                if (first) {
                    message("You crouch to light the kindling")
                    first = false
                }
                anim("light_fire")
                start("action_delay", 4)
                pause(4)
            } else if (remaining > 0) {
                pause(remaining)
            }
            if (!GameObjects.contains(target) || health < 0) {
                break
            }
            "You dive out of the way as a new root bursts from the ground."
            "The logs are magically escorted to your bank."
//            if (success(player.levels.get(Skill.Woodcutting), hatchet, log)) {
            inventory.remove("kindling")
            exp(Skill.Firemaking, row.int("burn_xp") / 10.0)
//            }
        }
    }

    private fun nextStage(target: GameObject) {
        tree = when {
            target.id == "evil_tree_seedling" -> {
                health = Tables.int("evil_tree_type.${type}.seed_health")
                leprechaun.say("Whoa!")
                target.replace("evil_tree_sapling", spawnTile.add(1, 1))
            }
            target.id == "evil_tree_sapling" -> {
                health = Tables.int("evil_tree_type.${type}.seed_health")
                leprechaun.say("Whoa!")
                target.replace("evil_tree_sapling_large", spawnTile.add(1, 1))
            }
            target.id == "evil_tree_sapling_large" -> {
                health = Tables.int("evil_tree_type.${type}.seed_health")
                val centre = spawnTile.add(1, 1)
                for (player in Players.at(centre)) {
                    player.walkTo(centre.add(Direction.SOUTH).add(Direction.SOUTH))
                }
                // TODO npcs too
                for (tile in Spiral.spiral(centre, 1)) {
                    for (player in Players.at(tile)) {
                        val dir = player.tile.delta(centre).toDirection()
                        player.walkTo(player.tile.add(dir))
                    }
                }
                leprechaun.say("Whoa!")
                target.replace("evil_tree_young", spawnTile)
            }
            target.id == "evil_tree_young" -> {
                health = Tables.int("evil_tree_type.${type}.seed_health")
                leprechaun.say("Whoa!")
                target.replace("evil_tree_young_large", spawnTile)
            }
            target.id == "evil_tree_young_large" -> {
                health = Tables.int("evil_tree_type.${type}.health")
                spawnEvilTree(target.tile)
                target.replace("evil_tree_${type}_full")
            }
            target.id.endsWith("full") -> target.replace(target.id.replace("_full", "_half"))
            target.id.endsWith("half") -> target.replace(target.id.replace("_half", "_weak"))
            target.id.endsWith("weak") -> target.replace(target.id.replace("_weak", "_stump"))
            target.id.endsWith("stump") -> {
                clearRoots()
                // TODO respawn in 2 hours
                target.replace(target.id.replace("_stump", "_death"))
            }
            else -> return
        }
    }

    private fun spawnEvilTree(target: Tile) {
        var index = 0
        for (branch in Tables.get("evil_branches").rows()) {
            val spawn = branch.obj("spawn")
            val dir = branch.int("dir")
            val tile = target.add(branch.int("deltaX"), branch.int("deltaY"))
            roots[index++] = GameObjects.add(spawn, tile, rotation = dir, ticks = 2)
        }
        leprechaun.transform("leprechaun_panic")
        leprechaun.say("Whoa!")
    }


    fun clear() {
        GameObjects.remove(tree)
        tree = GameObject(0)
        spawnTile = Tile.EMPTY
        type = "normal"
        leprechaun = NPC()
        clearRoots()
    }

    private fun clearRoots() {
        for (root in roots) {
            GameObjects.remove(root ?: continue)
        }
        roots.fill(null)
    }

    fun start() {
        if (spawnTile != Tile.EMPTY) {
            return
        }
        val place = Tables.get("evil_tree_place").rows().random(random)
        EvilTree.place = place.rowId
        if (Settings["world.messages", false]) {
            for (player in Players) {
                val hint = place.string("hint").replaceFirstChar { it.lowercase() }.replace("<br>", " ")
                player.message("${Colours.DARK_RED.toTag()}An evil tree has spawned $hint.")
            }
        }
        spawnTile = place.tileList("tiles").random(random)
        val type = Tables.get("evil_tree_type").rows().random(random)
        logger.info { "Evil tree event has started at: ${place.rowId} (${spawnTile.x}, ${spawnTile.y}) type ${type.rowId}." }
        EvilTree.type = type.rowId
        val centre = spawnTile.add(1, 1)
        tree = GameObjects.add("evil_tree_seedling", centre)
        // Push players out
        for (player in Players.at(centre)) {
            player.walkTo(centre.add(Direction.cardinal.random(random)))
        }
        leprechaun = NPCs.add("leprechaun_evil_tree", spawnTile.add(-1, -1))
    }
}