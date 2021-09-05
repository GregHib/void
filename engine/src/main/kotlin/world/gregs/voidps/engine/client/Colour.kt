package world.gregs.voidps.engine.client

sealed class Colour(val int: Int) {
    abstract class Chat(int: Int) : Colour(int) {
        object ChatBlue : Chat(0x0000ff)
        object ChatRed : Chat(0x800000)
        object TradePurple : Chat(0x800080)
        object DuelBrown : Chat(0x7e3200)
        object AssistPurple : Chat(0x8824e3)
        object DropGreen : Chat(0x005100)
    }

    object Green : Colour(0x00ff00)
    object Orange : Colour(0xff981f)
    object Red : Colour(0xff0000)
    object Yellow : Colour(0xffff00)
    object Lime : Colour(0x00ff80)
    object White : Colour(0xffffff)

    val string = Integer.toHexString(int)

    operator fun invoke(block: () -> String): String = wrap(block.invoke())

    fun wrap(text: String): String {
        val builder = StringBuilder()
        builder.append("<col=$string>").append(text).append("</col>")
        return builder.toString()
    }

    companion object {
        fun bool(boolean: Boolean) = if (boolean) Green else Red
    }
}
