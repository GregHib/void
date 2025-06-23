package world.gregs.voidps.engine.client.ui.chat

typealias Colour = Int

fun Colour.toTag() = "<col=${Integer.toHexString(this)}>"
