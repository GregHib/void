package world.gregs.voidps.engine.client.update.batch

import world.gregs.voidps.engine.data.definition.extra.AnimationDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.encode.chunk.ObjectAnimation

fun animateObject(id: String, gameObject: GameObject) = ObjectAnimation(gameObject.tile.id, get<AnimationDefinitions>().get(id).id, gameObject.shape, gameObject.rotation)

fun GameObject.animate(id: String) = get<ChunkBatchUpdates>().add(tile.chunk, animateObject(id, this))