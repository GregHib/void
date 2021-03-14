package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.buffer.write.writeByteSubtract
import world.gregs.voidps.buffer.write.writeShortAdd
import world.gregs.voidps.buffer.write.writeShortAddLittle
import world.gregs.voidps.buffer.write.writeShortLittle
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.GameOpcodes
import world.gregs.voidps.network.GameOpcodes.FLOOR_ITEM_ADD

/**
 * @param tile The tile offset from the chunk update send
 * @param id Item id
 * @param amount Item stack size
 */
fun Client.addFloorItem(
    tile: Int,
    id: Int,
    amount: Int
) = send(FLOOR_ITEM_ADD, 5) {
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
) = send(GameOpcodes.FLOOR_ITEM_REMOVE, 3) {
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
) = send(GameOpcodes.FLOOR_ITEM_REVEAL, 7) {
    writeShortLittle(amount)
    writeByte(tile)
    writeShortAdd(id)
    writeShortAdd(owner)
}

/**
 * @author GregHib <greg@gregs.world>
 * @since June 19, 2020
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
) = send(GameOpcodes.FLOOR_ITEM_UPDATE, 7) {
    writeByte(tile)
    writeShort(id)
    writeShort(oldAmount)
    writeShort(newAmount)
}