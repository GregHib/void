package world.gregs.voidps.engine.client.ui.chat

sealed class Colour(val int: Int) {

    val string: String = Integer.toHexString(int)

    operator fun invoke(block: () -> String): String = wrap(block.invoke())

    fun wrap(text: String) = buildString {
        append("<col=")
        append(string)
        append(">")
        append(text)
        append("</col>")
    }

    fun open(text: String) = buildString {
        append("<col=")
        append(string)
        append(">")
        append(text)
    }

    companion object {
        fun bool(boolean: Boolean) = if (boolean) Green else Red
    }
}

object Blue : ChatColour(0x0000ff)
object Orange : Colour(0xff981f)
object Green : Colour(0x00ff00)
object Red : Colour(0xff0000)
object Yellow : Colour(0xffff00)
object Lime : Colour(0x00ff80)
object White : Colour(0xffffff)
object Black : Colour(0x000000)