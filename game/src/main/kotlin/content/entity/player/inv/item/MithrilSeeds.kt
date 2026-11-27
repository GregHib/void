package content.entity.player.inv.item

import content.entity.obj.door.Door.isDoor
import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random

class MithrilSeeds : Script {
    init {
        itemOption("Plant", "mithril_seeds") { (_, slot) ->
            if (!canPlant()) return@itemOption
            val plantedTile = tile
            anim("plant_mithril_seed")
            delay(2)
            if (tile != plantedTile || !canPlant()) return@itemOption
            val direction = Direction.stepAway.firstOrNull { !blocked(it) } ?: return@itemOption
            if (!inventory.remove(slot, "mithril_seeds", 1)) return@itemOption
            val flower = when (random.nextInt(1001)) {
                0 -> "white_flowers"
                1, 2 -> "black_flowers"
                else -> COMMON_FLOWERS.random(random)
            }
            val obj = GameObjects.add("planted_$flower", plantedTile, ticks = 500)
            message("You plant the seeds and some flowers grow.")
            walkOverDelay(plantedTile.add(direction))
            face(obj)
            choice("What would you like to do with the flowers?") {
                option("Pick the flowers.") {
                    if (GameObjects.getLayer(plantedTile, ObjectLayer.GROUND) != obj || tile.distanceTo(plantedTile) > 1) return@option
                    if (!inventory.add(flower)) {
                        message("You don't have enough inventory space to pick the flowers.")
                        return@option
                    }
                    GameObjects.remove(obj)
                    anim("plant_mithril_seed")
                    message("You pick the flowers.")
                }
                option("Leave the flowers.") { }
            }
        }
    }

    private fun Player.canPlant(): Boolean {
        if (hasClock("movement_delay")) {
            message("You can't plant flowers while you can't move.")
            return false
        }
        if (GameObjects.getLayer(tile, ObjectLayer.GROUND) != null ||
            GameObjects.getLayer(tile, ObjectLayer.GROUND_DECORATION) != null ||
            GameObjects.getLayer(tile, ObjectLayer.WALL)?.def?.isDoor() == true ||
            Direction.stepAway.all { blocked(it) }
        ) {
            message("You can't plant flowers here.")
            return false
        }
        return true
    }

    companion object {
        private val COMMON_FLOWERS = listOf("flowers_pastel", "red_flowers", "blue_flowers", "yellow_flowers", "purple_flowers", "orange_flowers", "flowers_mixed")
    }
}
