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
        p1Alt3(constantSpeed)
        p1Alt2(localY)
        p1Alt2(localX)
        p1Alt2(variableSpeed)
        p2Alt3(z)
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
        p1Alt2(localY)
        p2Alt3(z)
        p1Alt3(localX)
        writeByte(constantSpeed)
        p1Alt3(variableSpeed)
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
        p1Alt3(type)
        p1Alt1(cycle)
        p2Alt3(speed)
        p1Alt3(intensity)
        p1Alt3(movement)
    }
}

fun Client.clearCamera() {
    send(CAMERA_RESET) {
    }
}