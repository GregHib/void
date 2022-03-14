package world.gregs.voidps.engine.client.update.batch

import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.offset
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.proj.Projectile
import world.gregs.voidps.engine.entity.sound.AreaSound
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.chunk.update.*

fun addFloorItem(floorItem: FloorItem) = FloorItemAddition(floorItem.def.id, floorItem.amount, floorItem.tile.offset(), floorItem.owner)

fun removeFloorItem(floorItem: FloorItem) = FloorItemRemoval(floorItem.def.id, floorItem.tile.offset(), floorItem.owner)

fun revealFloorItem(floorItem: FloorItem, owner: Int) = FloorItemReveal(floorItem.def.id, floorItem.amount, floorItem.tile.offset(), owner, floorItem.owner)

fun updateFloorItem(floorItem: FloorItem, stack: Int, combined: Int) = FloorItemUpdate(floorItem.def.id, floorItem.tile.offset(), stack, combined, floorItem.owner)

fun addGraphic(graphic: AreaGraphic) = GraphicAddition(graphic.graphic.id, graphic.tile.offset(), graphic.graphic.height, graphic.graphic.delay, graphic.graphic.rotation, graphic.owner)

fun addObject(gameObject: GameObject) = ObjectAddition(gameObject.def.id, gameObject.tile.offset(), gameObject.type, gameObject.rotation, gameObject.owner)

fun animateObject(id: String, gameObject: GameObject) = ObjectAnimation(get<AnimationDefinitions>().get(id).id, gameObject.tile.offset(), gameObject.type, gameObject.rotation)

fun removeObject(gameObject: GameObject) = ObjectRemoval(gameObject.tile.offset(), gameObject.type, gameObject.rotation, gameObject.owner)

fun addProjectile(projectile: Projectile) = ProjectileAddition(projectile.def.id, projectile.index, projectile.tile.offset(3), projectile.direction.x, projectile.direction.y, projectile.startHeight, projectile.endHeight, projectile.delay, projectile.flightTime, projectile.curve, projectile.offset, projectile.owner)

fun addSound(soundArea: AreaSound) = SoundAddition(soundArea.def.id, soundArea.tile.offset(), soundArea.radius, soundArea.repeat, soundArea.delay, soundArea.volume, soundArea.speed, soundArea.midi, soundArea.owner)

fun GameObject.animate(id: String) = get<ChunkBatches>().update(tile.chunk, animateObject(id, this))