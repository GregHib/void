package world.gregs.voidps.engine.client.update.batch

import world.gregs.voidps.engine.data.definition.extra.AnimationDefinitions
import world.gregs.voidps.engine.entity.obj.GameMapObject
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.encode.chunk.ObjectAnimation

fun animateObject(id: String, gameObject: GameMapObject) = ObjectAnimation(gameObject.tile.id, get<AnimationDefinitions>().get(id).id, gameObject.type, gameObject.rotation)

fun GameMapObject.animate(id: String) = get<ChunkBatchUpdates>().add(tile.chunk, animateObject(id, this))