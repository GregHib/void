package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.buffer.write.Writer

abstract class VisualEncoder<V : Visual>(val mask: Int) {

    fun encodeVisual(writer: Writer, visual: Visual) {
        encode(writer, visual as V)
    }

    abstract fun encode(writer: Writer, visual: V)

}