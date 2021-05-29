package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Protocol.OBJECT_ADD
import world.gregs.voidps.utility.get

fun addObject(gameObject: GameObject): (Player) -> Unit = { player ->
    player.client?.addObject(gameObject.tile.offset(), gameObject.id, gameObject.type, gameObject.rotation)
}

/**
 * @param tile The tile offset from the chunk update send
 * @param id Object id
 * @param type Object type
 * @param rotation Object rotation
 */
fun Client.addObject(
    tile: Int,
    id: Int,
    type: Int,
    rotation: Int
) = send(OBJECT_ADD) {
    writeByteSubtract((type shl 2) or rotation)
    writeShort(id)
    writeByteAdd(tile)
}

/**
 * Show animation of an object for a single client
 * @param tile 30 bit location hash
 * @param animation Animation id
 * @param type Object type
 * @param rotation Object rotation
 */
fun Client.animateObject(
    tile: Int,
    animation: Int,
    type: Int,
    rotation: Int
) = send(Protocol.OBJECT_ANIMATION) {
    writeShortAddLittle(animation)
    writeByteAdd((type shl 2) or rotation)
    writeIntInverseMiddle(tile)
}

/**
 * @param tile The tile offset from the chunk update send
 * @param animation Animation id
 * @param type Object type
 * @param rotation Object rotation
 */
fun Client.animateSpecificObject(
    tile: Int,
    animation: Int,
    type: Int,
    rotation: Int
) = send(Protocol.OBJECT_ANIMATION_SPECIFIC) {
    writeShortLittle(animation)
    writeByteSubtract(tile)
    writeByteInverse((type shl 2) or rotation)
}

fun GameObject.animate(id: Int) = get<ChunkBatcher>()
    .update(tile.chunk) { player -> player.client?.animateSpecificObject(tile.offset(), id, type, rotation) }

/**
 * Preloads a object model
 */
fun Client.preloadObject(
    id: Int,
    modelType: Int
) = send(Protocol.OBJECT_PRE_FETCH) {
    writeShort(id)
    writeByte(modelType)
}

fun removeObject(gameObject: GameObject): (Player) -> Unit = { player -> player.client?.removeObject(gameObject.tile.offset(), gameObject.type, gameObject.rotation) }
/**
 * @param tile The tile offset from the chunk update send
 * @param type Object type
 * @param rotation Object rotation
 */
fun Client.removeObject(
    tile: Int,
    type: Int,
    rotation: Int
) = send(Protocol.OBJECT_REMOVE) {
    writeByteAdd((type shl 2) or rotation)
    writeByte(tile)
}