package content.entity.player.bank

import WorldTest
import containsMessage
import interfaceOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.epochSeconds

internal class BankPinTest : WorldTest() {

    private val bankBooth = "36786"

    private fun Player.clickDigit(digit: Int) {
        val layout: List<Int> = get("bank_pin_layout")!!
        interfaceOption("bank_pin", "digit_${layout.indexOf(digit) + 1}", "Enter digit")
    }

    private fun Player.enterPin(pin: String) {
        for (digit in pin) {
            clickDigit(digit.digitToInt())
        }
    }

    @Test
    fun `Set a pin takes effect immediately`() {
        val player = createPlayer(emptyTile)
        val booth = createObject(bankBooth, emptyTile.addY(1))
        player.open("bank_pin_settings")

        player.interfaceOption("bank_pin_settings", "set_pin", "Set a PIN")
        player.interfaceOption("bank_pin_settings", "confirm", "Confirm")
        assertTrue(player.hasOpen("bank_pin"))
        player.enterPin("1739")
        player.enterPin("1739")

        assertEquals("1739", player["bank_pin", ""])
        assertTrue(player["bank_pin_unlocked", false])
        assertTrue(player.hasOpen("bank_pin_settings"))

        player.interfaceOption("bank_pin_settings", "close", "Close")
        player.objectOption(booth, "Use-quickly")
        tick(5)
        assertTrue(player.hasOpen("bank"))
    }

    @Test
    fun `Mismatched confirmation doesn't set a pin`() {
        val player = createPlayer(emptyTile)
        player.open("bank_pin_settings")

        player.interfaceOption("bank_pin_settings", "set_pin", "Set a PIN")
        player.interfaceOption("bank_pin_settings", "confirm", "Confirm")
        player.enterPin("1739")
        player.enterPin("2846")

        assertEquals("", player["bank_pin", ""])
        assertTrue(player.hasOpen("bank_pin_settings"))
    }

    @Test
    fun `Easily guessed pins are rejected`() {
        val player = createPlayer(emptyTile)
        player.open("bank_pin_settings")

        player.interfaceOption("bank_pin_settings", "set_pin", "Set a PIN")
        player.interfaceOption("bank_pin_settings", "confirm", "Confirm")
        player.enterPin("1234")
        player.enterPin("1234")

        assertEquals("", player["bank_pin", ""])
        assertTrue(player.hasOpen("bank_pin_settings"))
    }

    @Test
    fun `Bank requires the pin once it is active`() {
        val player = createPlayer(emptyTile)
        val booth = createObject(bankBooth, emptyTile.addY(1))
        player["bank_pin"] = "1739"

        player.objectOption(booth, "Use-quickly")
        tick(5)
        assertTrue(player.hasOpen("bank_pin"))
        assertFalse(player.hasOpen("bank"))
        player.enterPin("1739")

        assertTrue(player.hasOpen("bank"))
        assertTrue(player["bank_pin_unlocked", false])
        assertTrue(player.containsMessage("You have correctly entered your PIN."))
    }

    @Test
    fun `Pin is only required once per session`() {
        val player = createPlayer(emptyTile)
        val booth = createObject(bankBooth, emptyTile.addY(1))
        player["bank_pin"] = "1739"
        player["bank_pin_unlocked"] = true

        player.objectOption(booth, "Use-quickly")
        tick(5)

        assertTrue(player.hasOpen("bank"))
    }

    @Test
    fun `Changed pin stays pending until the recovery delay passes`() {
        val player = createPlayer(emptyTile)
        player["bank_pin"] = "1739"
        player.open("bank_pin_settings")

        player.interfaceOption("bank_pin_settings", "change_pin", "Change your PIN")
        player.enterPin("1739")
        player.enterPin("2846")
        player.enterPin("2846")

        assertEquals("1739", player["bank_pin", ""])
        assertEquals("2846", player["bank_pin_pending", ""])
        assertTrue(player.hasClock("bank_pin_change", epochSeconds()))
        assertTrue(player.containsMessage("Your PIN change will take effect in 3 days."))
    }

    @Test
    fun `Old pin works while a change is pending`() {
        val player = createPlayer(emptyTile)
        val booth = createObject(bankBooth, emptyTile.addY(1))
        player["bank_pin"] = "1739"
        player["bank_pin_pending"] = "2846"
        player["bank_pin_change"] = epochSeconds() + 100000

        player.objectOption(booth, "Use-quickly")
        tick(5)
        player.enterPin("1739")

        assertTrue(player.hasOpen("bank"))
        assertEquals("1739", player["bank_pin", ""])
    }

    @Test
    fun `Pending pin change takes over after the recovery delay`() {
        val player = createPlayer(emptyTile)
        val booth = createObject(bankBooth, emptyTile.addY(1))
        player["bank_pin"] = "1739"
        player["bank_pin_pending"] = "2846"
        player["bank_pin_change"] = epochSeconds() - 10

        player.objectOption(booth, "Use-quickly")
        tick(5)
        assertEquals("2846", player["bank_pin", ""])
        assertEquals("", player["bank_pin_pending", ""])
        player.enterPin("2846")

        assertTrue(player.hasOpen("bank"))
    }

    @Test
    fun `Cancel a pending pin change keeps the old pin`() {
        val player = createPlayer(emptyTile)
        player["bank_pin"] = "1739"
        player["bank_pin_pending"] = "2846"
        player["bank_pin_change"] = epochSeconds() + 100000
        player.open("bank_pin_settings")

        player.interfaceOption("bank_pin_settings", "cancel_pending", "Cancel your PIN")

        assertEquals("1739", player["bank_pin", ""])
        assertEquals("", player["bank_pin_pending", ""])
    }

    @Test
    fun `Wrong entries lock the bank out after two tries`() {
        val player = createPlayer(emptyTile)
        val booth = createObject(bankBooth, emptyTile.addY(1))
        player["bank_pin"] = "1739"

        player.objectOption(booth, "Use-quickly")
        tick(5)
        player.enterPin("0000")
        assertFalse(player.hasOpen("bank_pin"))
        assertTrue(player.containsMessage("The PIN you entered is incorrect."))
        assertEquals(1, player["bank_pin_tries", 0])

        player.objectOption(booth, "Use-quickly")
        tick(5)
        player.enterPin("0000")
        assertEquals(2, player["bank_pin_tries", 0])
        assertTrue(player.hasClock("bank_pin_lockout", epochSeconds()))

        player.objectOption(booth, "Use-quickly")
        tick(5)
        assertFalse(player.hasOpen("bank_pin"))
        assertFalse(player.hasOpen("bank"))
    }

    @Test
    fun `Delete pin when already unlocked`() {
        val player = createPlayer(emptyTile)
        player["bank_pin"] = "1739"
        player["bank_pin_unlocked"] = true
        player.open("bank_pin_settings")

        player.interfaceOption("bank_pin_settings", "delete_pin", "Delete your PIN")
        player.interfaceOption("bank_pin_settings", "confirm", "Confirm")

        assertEquals("", player["bank_pin", ""])
    }

    @Test
    fun `Delete pin requires the pin when locked`() {
        val player = createPlayer(emptyTile)
        player["bank_pin"] = "1739"
        player.open("bank_pin_settings")

        player.interfaceOption("bank_pin_settings", "delete_pin", "Delete your PIN")
        player.interfaceOption("bank_pin_settings", "confirm", "Confirm")
        assertTrue(player.hasOpen("bank_pin"))
        player.enterPin("1739")

        assertEquals("", player["bank_pin", ""])
    }

    @Test
    fun `Forgotten pin schedules deletion after the recovery delay`() {
        val player = createPlayer(emptyTile)
        val booth = createObject(bankBooth, emptyTile.addY(1))
        player["bank_pin"] = "1739"

        player.objectOption(booth, "Use-quickly")
        tick(5)
        assertTrue(player.hasOpen("bank_pin"))
        player.interfaceOption("bank_pin", "forgot", "I don't know it")

        assertFalse(player.hasOpen("bank_pin"))
        assertFalse(player.hasOpen("bank"))
        assertEquals("1739", player["bank_pin", ""])
        assertTrue(player["bank_pin_delete", false])
        assertTrue(player.hasClock("bank_pin_change", epochSeconds()))
    }

    @Test
    fun `Forgotten pin is deleted once the recovery delay passes`() {
        val player = createPlayer(emptyTile)
        val booth = createObject(bankBooth, emptyTile.addY(1))
        player["bank_pin"] = "1739"
        player["bank_pin_delete"] = true
        player["bank_pin_change"] = epochSeconds() - 10

        player.objectOption(booth, "Use-quickly")
        tick(5)

        assertEquals("", player["bank_pin", ""])
        assertFalse(player["bank_pin_delete", false])
        assertTrue(player.hasOpen("bank"))
    }

    @Test
    fun `Cancel a pending deletion keeps the pin`() {
        val player = createPlayer(emptyTile)
        player["bank_pin"] = "1739"
        player["bank_pin_delete"] = true
        player["bank_pin_change"] = epochSeconds() + 100000
        player.open("bank_pin_settings")

        player.interfaceOption("bank_pin_settings", "cancel_pending", "Cancel your PIN")

        assertEquals("1739", player["bank_pin", ""])
        assertFalse(player["bank_pin_delete", false])
    }

    @Test
    fun `Recovery delay toggles between three and seven days`() {
        val player = createPlayer(emptyTile)
        player.open("bank_pin_settings")

        player.interfaceOption("bank_pin_settings", "recovery_new", "Change your recovery delay")
        assertTrue(player["bank_pin_long_recovery", false])

        player.interfaceOption("bank_pin_settings", "recovery_new", "Change your recovery delay")
        assertFalse(player["bank_pin_long_recovery", false])
    }
}
