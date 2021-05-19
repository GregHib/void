package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Protocol.FLOOR_ITEM_ADD

/**
 * @param tile The tile offset from the chunk update send
 * @param id Item id
 * @param amount Item stack size
 */
fun Client.addFloorItem(
    tile: Int,
    id: Int,
    amount: Int
) = send(FLOOR_ITEM_ADD) {
    writeShortLittle(amount)
    writeShortLittle(id)
    writeByte(tile)
}

/**
 * @param tile The tile offset from the chunk update send
 * @param id Item id
 */
fun Client.removeFloorItem(
    tile: Int,
    id: Int
) = send(Protocol.FLOOR_ITEM_REMOVE) {
    writeShortAddLittle(id)
    writeByteSubtract(tile)
}

/**
 * @param tile The tile offset from the chunk update send
 * @param id Item id
 * @param amount Item stack size
 * @param owner Client index if matches client's local index then item won't be displayed
 */
fun Client.revealFloorItem(
    tile: Int,
    id: Int,
    amount: Int,
    owner: Int
) = send(Protocol.FLOOR_ITEM_REVEAL) {
    writeShortLittle(amount)
    writeByte(tile)
    writeShortAdd(id)
    writeShortAdd(owner)
}

/**
 * @param tile The tile offset from the chunk update send
 * @param id Item id
 * @param oldAmount Previous item stack size
 * @param newAmount Updated item stack size
 */
fun Client.updateFloorItem(
    tile: Int,
    id: Int,
    oldAmount: Int,
    newAmount: Int
) = send(Protocol.FLOOR_ITEM_UPDATE) {
    writeByte(tile)
    writeShort(id)
    writeShort(oldAmount)
    writeShort(newAmount)
}