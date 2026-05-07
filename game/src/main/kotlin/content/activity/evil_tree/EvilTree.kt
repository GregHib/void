package content.activity.evil_tree

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class EvilTree : Script {

    val logger = InlineLogger()

    var tree: GameObject = GameObject(0)
    var spawnTile: Tile = Tile.EMPTY
    var type: String = "normal"
    val roots = arrayOfNulls<GameObject>(4)

    init {
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

        objectOperate("Nurture", "evil_tree_seedling,evil_tree_sapling*,evil_tree_young*") { (target) ->
            nextStage(target)
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

        // TODO controller on leprechaun to do branch attacks
    }

    private fun nextStage(target: GameObject) {
        tree = when {
            target.id == "evil_tree_seedling" -> target.replace("evil_tree_sapling", spawnTile.add(1, 1))
            target.id == "evil_tree_sapling" -> target.replace("evil_tree_sapling_large", spawnTile.add(1, 1))
            target.id == "evil_tree_sapling_large" -> {
                val centre = spawnTile.add(1, 1)
                for (player in Players.at(centre)) {
                    player.walkTo(centre.add(Direction.SOUTH).add(Direction.SOUTH))
                }
                for (tile in Spiral.spiral(centre, 1)) {
                    for (player in Players.at(tile)) {
                        val dir = player.tile.delta(centre).toDirection()
                        player.walkTo(player.tile.add(dir))
                    }
                }
                target.replace("evil_tree_young", spawnTile)
            }
            target.id == "evil_tree_young" -> target.replace("evil_tree_young_large", spawnTile)
            target.id == "evil_tree_young_large" -> {
                var index = 0
                for (branch in Tables.get("evil_branches").rows()) {
                    val spawn = branch.obj("spawn")
                    val dir = branch.int("dir")
                    val tile = target.tile.add(branch.int("deltaX"), branch.int("deltaY"))
                    roots[index++] = GameObjects.add(spawn, tile, rotation = dir, ticks = 2)
                }
                target.replace("evil_tree_${type}_full")
            }
            target.id.endsWith("full") -> target.replace(target.id.replace("_full", "_half"))
            target.id.endsWith("half") -> target.replace(target.id.replace("_half", "_weak"))
            target.id.endsWith("weak") -> target.replace(target.id.replace("_weak", "_stump"))
            target.id.endsWith("stump") -> {
                clearRoots()
                target.replace(target.id.replace("_stump", "_death"))
            }
            else -> return
        }
    }


    fun clear() {
        GameObjects.remove(tree)
        tree = GameObject(0)
        spawnTile = Tile.EMPTY
        type = "normal"
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
        if (Settings["world.messages", false]) {
            for (player in Players) {
                val hint = place.string("hint").replaceFirstChar { it.lowercase() }.replace("<br>", " ")
                player.message("${Colours.DARK_RED.toTag()}An evil tree has spawned $hint.")
            }
        }
        spawnTile = place.tileList("tiles").random(random)
        val type = Tables.get("evil_tree_type").rows().random(random)
        logger.info { "Evil tree event has started at: ${place.rowId} (${spawnTile.x}, ${spawnTile.y}) type ${type.rowId}." }
        this.type = type.rowId
        val centre = spawnTile.add(1, 1)
        tree = GameObjects.add("evil_tree_seedling", centre)
        // Push players out
        for (player in Players.at(centre)) {
            player.walkTo(centre.add(Direction.cardinal.random(random)))
        }


    }
}