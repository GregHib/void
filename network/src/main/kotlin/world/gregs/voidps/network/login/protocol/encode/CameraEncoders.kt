package world.gregs.voidps.network.login.protocol.encode

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
        writeByteSubtract(localY)
        writeByteSubtract(constantSpeed)
        writeByteInverse(localX)
        writeByteInverse(variableSpeed)
        writeShortAdd(z)
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
        writeByte(localX)
        writeByteSubtract(localY)
        writeByte(constantSpeed)
        writeShortLittle(z)
        writeByteInverse(variableSpeed)
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
        writeByteAdd(intensity)
        writeByteInverse(type)
        writeShortAdd(cycle)
        writeByteSubtract(movement)
        writeByteInverse(speed)
    }
}

fun Client.clearCamera() {
    send(CAMERA_RESET) {
    }
}
