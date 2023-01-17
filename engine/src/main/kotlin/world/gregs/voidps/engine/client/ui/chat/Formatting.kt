package world.gregs.voidps.engine.client.ui.chat

sealed class Formatting(val string: String) {

    operator fun invoke(block: () -> String): String = wrap(block.invoke())

    fun wrap(text: String) = buildString {
        append("<")
        append(string)
        append(">")
        append(text)
        append("</col>")
    }

    fun open(text: String) = buildString {
        append("<")
        append(string)
        append(">")
        append(text)
    }
}

object Strike : Formatting("str")