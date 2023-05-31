package world.gregs.voidps.engine.client.update.batch

import world.gregs.voidps.engine.data.definition.extra.AnimationDefinitions
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.offset
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.encode.chunk.*

fun addFloorItem(floorItem: FloorItem) = FloorItemAddition(floorItem.def.id, floorItem.amount, floorItem.tile.offset(), floorItem.owner)

fun removeFloorItem(floorItem: FloorItem) = FloorItemRemoval(floorItem.def.id, floorItem.tile.offset(), floorItem.owner)

fun revealFloorItem(floorItem: FloorItem, owner: Int) = FloorItemReveal(floorItem.def.id, floorItem.amount, floorItem.tile.offset(), owner, floorItem.owner)

fun updateFloorItem(floorItem: FloorItem, stack: Int, combined: Int) = FloorItemUpdate(floorItem.def.id, floorItem.tile.offset(), stack, combined, floorItem.owner)

fun addObject(gameObject: GameObject) = ObjectAddition(gameObject.def.id, gameObject.tile.offset(), gameObject.type, gameObject.rotation, gameObject.owner)

fun animateObject(id: String, gameObject: GameObject) = ObjectAnimation(get<AnimationDefinitions>().get(id).id, gameObject.tile.offset(), gameObject.type, gameObject.rotation)

fun removeObject(gameObject: GameObject) = ObjectRemoval(gameObject.tile.offset(), gameObject.type, gameObject.rotation, gameObject.owner)

fun GameObject.animate(id: String) = get<ChunkBatchUpdates>().add(tile.chunk, animateObject(id, this))