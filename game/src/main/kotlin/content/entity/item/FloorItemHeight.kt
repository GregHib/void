package content.entity.item

import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer

/**
 * Height the item is displayed at, in client units (one tile is 512 wide). The client raises item piles
 * by the model height of the object under them (tables, altars), so projectiles and graphics aimed at
 * the item need the same offset. Object definitions declare it with a `height` param; model heights of
 * pre-version-13 models are multiplied by 4 by the client.
 */
fun FloorItem.height(): Int = GameObjects.getLayer(tile, ObjectLayer.GROUND)?.def?.get("height", 0) ?: 0
