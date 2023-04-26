package world.gregs.voidps.engine.client.ui.chat

import java.util.regex.Pattern

object Colours {

    private val tagPattern = Pattern.compile("<(/?[a-zA-Z_]+)>")

    fun replaceCustomTags(input: String): String {
        val matcher = tagPattern.matcher(input)
        val sb = StringBuilder()
        while (matcher.find()) {
            val tag = matcher.group(1)
            if (tag.startsWith("/") && colourMap.containsKey(tag.substring(1))) {
                matcher.appendReplacement(sb, "</col>")
            } else if (colourMap.containsKey(tag)) {
                matcher.appendReplacement(sb, "<col=${Integer.toHexString(colourMap.getValue(tag))}>")
            }
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    const val blue: Colour = 0x0000ff
    const val orange: Colour = 0xff981f
    const val green: Colour = 0x00ff00
    const val red: Colour = 0xff0000
    const val redOrange: Colour = 0xff3333
    const val yellow: Colour = 0xffff00
    const val lime: Colour = 0x00ff80
    const val white: Colour = 0xffffff
    const val black: Colour = 0x000000
    const val navy: Colour = 0x000080
    const val maroon: Colour = 0x800000
    const val purple: Colour = 0x800080 // trade
    const val brown: Colour = 0x7e3200 // duel
    const val violet: Colour = 0x8824e3 // assist
    const val darkGreen: Colour = 0x005100 // drops
    const val darkRed: Colour = 0x480000 // warning

    private val colourMap = mapOf(
        "blue" to blue,
        "orange" to orange,
        "green" to green,
        "red" to red,
        "red_orange" to redOrange,
        "yellow" to yellow,
        "lime" to lime,
        "white" to white,
        "black" to black,
        "navy" to navy,
        "maroon" to maroon,
        "purple" to purple,
        "brown" to brown,
        "violet" to violet,
        "dark_green" to darkGreen,
        "dark_red" to darkRed,
    )

    fun bool(boolean: Boolean) = if (boolean) "green" else "red"
}