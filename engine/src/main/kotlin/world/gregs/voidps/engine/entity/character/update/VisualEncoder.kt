package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.buffer.write.Writer

abstract class VisualEncoder<V : Visual>(val mask: Int, val initial: Boolean = false) {

    abstract fun encode(writer: Writer, visual: V)

}