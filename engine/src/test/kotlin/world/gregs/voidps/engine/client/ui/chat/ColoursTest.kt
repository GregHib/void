package world.gregs.voidps.engine.client.ui.chat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ColoursTest {

    @Test
    fun `Wrapping tags`() {
        val input = "The quick <red>red</red> fox jumps over the lazy dog."
        val output = Colours.replaceCustomTags(input)
        assertEquals("The quick <col=ff0000>red</col> fox jumps over the lazy dog.", output)
    }

    @Test
    fun `Nested tags`() {
        val input = "The quick <red>red fox jumps over the <green>green dog</green>.</red>"
        val output = Colours.replaceCustomTags(input)
        assertEquals("The quick <col=ff0000>red fox jumps over the <col=ff00>green dog</col>.</col>", output)
    }

    @Test
    fun `Open ended overlapping tags`() {
        val input = "The quick <red>red fox jumps over the <green>green dog</green>."
        val output = Colours.replaceCustomTags(input)
        assertEquals("The quick <col=ff0000>red fox jumps over the <col=ff00>green dog</col>.", output)
    }

    @Test
    fun `Open ended tags`() {
        val input = "The quick <red>red fox jumps over the <green>green dog."
        val output = Colours.replaceCustomTags(input)
        assertEquals("The quick <col=ff0000>red fox jumps over the <col=ff00>green dog.", output)
    }
}
