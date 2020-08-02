package rs.dusk.engine.client.ui.dialogue

import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import rs.dusk.engine.client.ui.open

internal class ChoiceTest : DialogueIOTest() {

    @TestFactory
    fun `Send choice lines`() = arrayOf(
        """
            One
            Two
        """ to "multi2",
        "One\nTwo\nThree" to "multi3",
        "One\nTwo\nThree\nFour" to "multi4",
        "One\nTwo\nThree\nFour\nFive" to "multi5"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            io.sendChoice(DialogueBuilder(player, text = text))
            verify {
                player.open(expected)
                for((index, line) in text.trimIndent().lines().withIndex()) {
                    manager.sendText(expected, "line${index + 1}", line)
                }
            }
        }
    }

    @TestFactory
    fun `Send multi line choice lines`() = arrayOf(
        """
            One
            Two<br>Three
        """ to "multi2_chat",
        "One\nTwo\nThree<br>Four" to "multi3_chat",
        "One\nTwo<br>Five\nThree\nFour" to "multi4_chat",
        "One\nTwo\nThree\nFour<br>Six\nFive" to "multi5_chat"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            io.sendChoice(DialogueBuilder(player, text = text))
            verify {
                player.open(expected)
                for((index, line) in text.trimIndent().lines().withIndex()) {
                    manager.sendText(expected, "line${index + 1}", line)
                }
            }
        }
    }

    @TestFactory
    fun `Send multi line title choice lines`() = arrayOf(
        """
            One
            Two
        """ to "multi_var2",
        "One\nTwo\nThree" to "multi_var3",
        "One\nTwo\nThree\nFour" to "multi_var4",
        "One\nTwo\nThree\nFour\nFive" to "multi_var5"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            io.sendChoice(DialogueBuilder(player, text = text, title = "First<br>Second"))
            verify {
                player.open(expected)
                for((index, line) in text.trimIndent().lines().withIndex()) {
                    manager.sendText(expected, "line${index + 1}", line)
                }
            }
        }
    }

    @Test
    fun `Sending six or more lines is ignored`() {
        io.sendChoice(DialogueBuilder(player, text = "\nOne\nTwo\nThree\nFour\nFive\nSix"))
        verify(exactly = 0) {
            player.open(any())
        }
    }

    @Test
    fun `Sending less than two lines is ignored`() {
        io.sendChoice(DialogueBuilder(player, text = "One line"))
        verify(exactly = 0) {
            player.open(any())
        }
    }

    @Test
    fun `Send no title`() {
        io.sendChoice(DialogueBuilder(player, text = "Yes\nNo", title = null))
        verify {
            player.open("multi2")
        }
        verify(exactly = 0) {
            manager.sendText("multi2", "title", any())
        }
    }

    @Test
    fun `Choice not sent if interface not opened`() {
        every { player.open("multi2") } returns false
        io.sendChoice(DialogueBuilder(player, text = "Yes\nNo"))
        verify(exactly = 0) {
            manager.sendText("multi2", "line1", "Yes")
            manager.sendText("multi2", "line2", "No")
        }
    }
}