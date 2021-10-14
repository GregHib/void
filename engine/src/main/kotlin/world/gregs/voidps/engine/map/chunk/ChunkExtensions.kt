package world.gregs.voidps.engine.map.chunk

import world.gregs.voidps.engine.entity.definition.GraphicDefinitions
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.proj.Projectile
import world.gregs.voidps.engine.entity.sound.AreaSound
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.chunk.update.*

fun addFloorItem(floorItem: FloorItem) = FloorItemAddition(floorItem.intId, floorItem.amount, floorItem.tile.offset(), floorItem.owner)

fun removeFloorItem(floorItem: FloorItem) = FloorItemRemoval(floorItem.intId, floorItem.tile.offset(), floorItem.owner)

fun revealFloorItem(floorItem: FloorItem, owner: Int) = FloorItemReveal(floorItem.intId, floorItem.amount, floorItem.tile.offset(), owner, floorItem.owner)

fun updateFloorItem(floorItem: FloorItem, stack: Int, combined: Int) = FloorItemUpdate(floorItem.intId, floorItem.tile.offset(), stack, combined, floorItem.owner)

fun addGraphic(graphic: AreaGraphic) = GraphicAddition(graphic.graphic.id, graphic.tile.offset(), graphic.graphic.height, graphic.graphic.delay, graphic.graphic.rotation, graphic.owner)

fun addObject(gameObject: GameObject) = ObjectAddition(gameObject.intId, gameObject.tile.offset(), gameObject.type, gameObject.rotation, gameObject.owner)

fun animateObject(id: Int, gameObject: GameObject) = ObjectAnimation(id, gameObject.tile.offset(), gameObject.type, gameObject.rotation)

fun removeObject(gameObject: GameObject) = ObjectRemoval(gameObject.tile.offset(), gameObject.type, gameObject.rotation, gameObject.owner)

fun addProjectile(projectile: Projectile) = ProjectileAddition(get<GraphicDefinitions>().getId(projectile.id), projectile.index, projectile.tile.offset(3), projectile.direction.x, projectile.direction.y, projectile.startHeight, projectile.endHeight, projectile.delay, projectile.flightTime, projectile.curve, projectile.offset, projectile.owner)

fun addSound(soundArea: AreaSound) = SoundAddition(soundArea.intId, soundArea.tile.offset(), soundArea.radius, soundArea.repeat, soundArea.delay, soundArea.volume, soundArea.speed, soundArea.midi, soundArea.owner)

fun GameObject.animate(id: Int) = get<ChunkBatches>().update(tile.chunk, animateObject(id, this))