package content.entity.item

import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer

/**
 * Height the item is displayed at: the client raises item piles by the model height of the object
 * under them (tables, altars), so projectiles and graphics aimed at the item need the same offset.
 * Object definitions declare it with a `height` param in packet units (model units / 4).
 */
fun FloorItem.height(): Int = GameObjects.getLayer(tile, ObjectLayer.GROUND)?.def?.get("height", 0) ?: 0
