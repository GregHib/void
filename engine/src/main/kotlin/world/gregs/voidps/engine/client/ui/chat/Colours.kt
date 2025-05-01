package world.gregs.voidps.engine.client.ui.chat

import java.util.regex.Pattern

@Suppress("MemberVisibilityCanBePrivate")
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

    const val BLUE: Colour = 0x0000ff
    const val ORANGE: Colour = 0xff981f
    const val GREEN: Colour = 0x00ff00
    const val RED: Colour = 0xff0000
    const val RED_ORANGE: Colour = 0xff3333
    const val YELLOW: Colour = 0xffff00
    const val LIME: Colour = 0x00ff80
    const val GOLD: Colour = 0xd7b011
    const val WHITE: Colour = 0xffffff
    const val BLACK: Colour = 0x000000
    const val NAVY: Colour = 0x000080
    const val MAROON: Colour = 0x800000
    const val PURPLE: Colour = 0x800080 // trade
    const val BROWN: Colour = 0x7e3200 // duel
    const val VIOLET: Colour = 0x8824e3 // assist
    const val DARK_GREEN: Colour = 0x005100 // drops
    const val DARK_RED: Colour = 0x480000 // warning

    private val colourMap = mapOf(
        "blue" to BLUE,
        "orange" to ORANGE,
        "green" to GREEN,
        "red" to RED,
        "red_orange" to RED_ORANGE,
        "yellow" to YELLOW,
        "lime" to LIME,
        "gold" to GOLD,
        "white" to WHITE,
        "black" to BLACK,
        "navy" to NAVY,
        "maroon" to MAROON,
        "purple" to PURPLE,
        "brown" to BROWN,
        "violet" to VIOLET,
        "dark_green" to DARK_GREEN,
        "dark_red" to DARK_RED,
    )

    fun bool(boolean: Boolean) = if (boolean) "green" else "red"
}