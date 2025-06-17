package world.gregs.voidps.network.login.protocol.visual

import world.gregs.voidps.buffer.write.Writer

abstract class VisualEncoder<V : Visuals>(
    val mask: Int,
    val initial: Boolean = false,
) {

    /**
     * @param index The index of the observing client
     */
    open fun encode(writer: Writer, visuals: V, index: Int) {
        encode(writer, visuals)
    }

    abstract fun encode(writer: Writer, visuals: V)
}
