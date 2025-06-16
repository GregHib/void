package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.CAMERA_MOVE
import world.gregs.voidps.network.login.Protocol.CAMERA_RESET
import world.gregs.voidps.network.login.Protocol.CAMERA_SHAKE
import world.gregs.voidps.network.login.Protocol.CAMERA_TURN
import world.gregs.voidps.network.login.protocol.*

/**
 * @param localX localX
 * @param localY localY
 * @param z
 * @param constantSpeed
 * @param variableSpeed max 100
 */
fun Client.moveCamera(
    localX: Int,
    localY: Int,
    z: Int,
    constantSpeed: Int,
    variableSpeed: Int,
) {
    send(CAMERA_MOVE) {
        writeByteSubtract(constantSpeed)
        writeByteInverse(localY)
        writeByteInverse(localX)
        writeByteInverse(variableSpeed)
        writeShortAddLittle(z)
    }
}

/**
 * @param localX
 * @param localY
 * @param z
 * @param constantSpeed
 * @param variableSpeed max 100
 */
fun Client.turnCamera(
    localX: Int,
    localY: Int,
    z: Int,
    constantSpeed: Int,
    variableSpeed: Int,
) {
    send(CAMERA_TURN) {
        writeByteInverse(localY)
        writeShortAddLittle(z)
        writeByteSubtract(localX)
        writeByte(constantSpeed)
        writeByteSubtract(variableSpeed)
    }
}

/**
 * @param intensity shake intensity
 * @param type shake type
 * @param cycle
 * @param movement Movement intensity
 * @param speed
 */
fun Client.shakeCamera(
    intensity: Int,
    type: Int,
    cycle: Int,
    movement: Int,
    speed: Int,
) {
    send(CAMERA_SHAKE) {
        writeByteSubtract(type)
        writeByteAdd(cycle)
        writeShortAddLittle(speed)
        writeByteSubtract(intensity)
        writeByteSubtract(movement)
    }
}

fun Client.clearCamera() {
    send(CAMERA_RESET) {
    }
}