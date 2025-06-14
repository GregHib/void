package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual

/**
 * @param id graphic id
 * @param delay delay to start graphic 30 = 1 tick
 * @param height 0..255 start height off the ground
 * @param rotation 0..7
 */
data class Graphic(
    var id: Int = -1,
    var delay: Int = 0,
    var height: Int = 0,
    var rotation: Int = 0,
    var forceRefresh: Boolean = false,
    var slot: Int = 0,
) : Visual {

    val packedDelayHeight: Int
        get() = (delay and 0xffff) or (height shl 16)
    val packedRotationRefresh: Int
        get() = (rotation and 0x7) or (slot shl 3) or ((if (forceRefresh) 1 else 0) shl 7)

    override fun needsReset(): Boolean = id != -1

    override fun reset() {
        id = -1
        delay = 0
        height = 0
        rotation = 0
        forceRefresh = false
    }
}
