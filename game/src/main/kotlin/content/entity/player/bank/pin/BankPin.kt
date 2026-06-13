package content.entity.player.bank.pin

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.suspend.pauseString
import world.gregs.voidps.engine.timer.epochSeconds
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random

object BankPin {

    fun hasPin(player: Player): Boolean = player.get("bank_pin", "").isNotEmpty()

    fun isPending(player: Player): Boolean = pendingPin(player).isNotEmpty()

    // Safe cast as old saves may hold a boolean from a previous format.
    fun pendingPin(player: Player): String = player.get<Any>("bank_pin_pending") as? String ?: ""

    fun isUnlocked(player: Player): Boolean = !hasPin(player) || player["bank_pin_unlocked", false]

    fun isLockedOut(player: Player): Boolean = player.hasClock("bank_pin_lockout", epochSeconds())

    fun recoveryDays(player: Player): Int = if (player["bank_pin_long_recovery", false]) 7 else 3

    fun pendingDays(player: Player): Int {
        val remaining = player.remaining("bank_pin_change", epochSeconds())
        if (remaining <= 0) {
            return 0
        }
        return ceil(remaining / 86400.0).toInt()
    }

    /**
     * A first pin takes effect immediately; the player clearly knows
     * the pin they just chose so the session stays unlocked too.
     */
    fun setPin(player: Player, pin: String) {
        player["bank_pin"] = pin
        player["bank_pin_unlocked"] = true
    }

    /**
     * A changed pin only takes effect after the recovery delay;
     * the old pin stays active until then.
     */
    fun setPending(player: Player, pin: String) {
        player["bank_pin_pending"] = pin
        player.start("bank_pin_change", TimeUnit.DAYS.toSeconds(recoveryDays(player).toLong()).toInt(), epochSeconds())
    }

    /**
     * A forgotten pin can be removed without knowing it,
     * but only after the recovery delay.
     */
    fun requestForgottenDelete(player: Player) {
        clearPending(player)
        player["bank_pin_delete"] = true
        player.start("bank_pin_change", TimeUnit.DAYS.toSeconds(recoveryDays(player).toLong()).toInt(), epochSeconds())
    }

    fun isDeletePending(player: Player): Boolean = player["bank_pin_delete", false]

    /**
     * Applies a pending pin change or deletion once the recovery delay has passed.
     */
    fun checkPending(player: Player) {
        if ((isPending(player) || isDeletePending(player)) && !player.hasClock("bank_pin_change", epochSeconds())) {
            if (isDeletePending(player)) {
                player["bank_pin"] = ""
                player["bank_pin_unlocked"] = false
            } else {
                player["bank_pin"] = pendingPin(player)
            }
            clearPending(player)
        }
    }

    fun clearPending(player: Player) {
        player["bank_pin_pending"] = ""
        player["bank_pin_delete"] = false
        player.stop("bank_pin_change")
    }

    fun cancelPin(player: Player) {
        player["bank_pin"] = ""
        player["bank_pin_unlocked"] = false
        clearPending(player)
        player.sound("bank_pin_cancel")
    }

    /**
     * Escalating retry cooldown after consecutive incorrect entries.
     */
    fun lockoutSeconds(tries: Int): Int = when {
        tries <= 2 -> 10
        tries == 3 -> 15
        else -> 1000
    }

    /**
     * Pins where every digit is the same or the whole pin is a run
     * of adjacent digits are too easy to guess.
     */
    fun isEasyGuess(pin: String): Boolean {
        if (pin.all { it == pin[0] }) {
            return true
        }
        return (0 until 3).all { abs(pin[it + 1] - pin[it]) == 1 }
    }

    /**
     * Re-shuffles which digit each keypad button maps to and updates the client.
     */
    fun shuffle(player: Player) {
        val digits = (0..9).shuffled()
        player["bank_pin_layout"] = digits
        sendState(player)
    }

    /**
     * The client renders the keypad digits and entry progress from two packed varps.
     */
    fun sendState(player: Player) {
        val digits: List<Int> = player["bank_pin_layout"] ?: return
        val stage = player.get("bank_pin_entered", "").length
        var packed = 0
        for (i in 0 until 8) {
            packed = packed or (digits[i] shl (i * 4))
        }
        player["bank_pin_digits"] = packed
        player["bank_pin_shuffle"] = digits[8] or (digits[9] shl 4) or (stage shl 26)
        // Scatter the ten digit cells; nine roam a 3x3 grid, cell_4 sits beside the exit button.
        for (i in 0 until 9) {
            val cell = if (i > 2) i + 2 else i + 1
            val x = 37 + (i % 3) * 95 + Random.nextInt(2, 45)
            val y = 157 + (i / 3) * 70 - Random.nextInt(3, 48)
            player.interfaces.sendPosition("bank_pin", "cell_$cell", x, y)
        }
        player.interfaces.sendPosition("bank_pin", "cell_4", 308 + Random.nextInt(2, 45), 155 - Random.nextInt(3, 45))
    }
}

suspend fun Player.openBank() {
    if (verifyBankAccess()) {
        open("bank")
    }
}

suspend fun Player.openCollection() {
    if (verifyBankAccess()) {
        open("collection_box")
    }
}

/**
 * True if no pin is set or it has already been entered this session.
 */
suspend fun Player.verifyBankAccess(): Boolean {
    BankPin.checkPending(this)
    if (BankPin.isUnlocked(this)) {
        return true
    }
    if (BankPin.isLockedOut(this)) {
        lockoutStatement()
        return false
    }
    val pin = requestPin("Please enter your PIN using the buttons below.")
    close("bank_pin")
    if (pin == "forgot") {
        forgottenPin()
        return false
    }
    if (pin.length != 4) {
        return false
    }
    if (pin != get("bank_pin", "")) {
        wrongPin()
        return false
    }
    set("bank_pin_unlocked", true)
    set("bank_pin_tries", 0)
    sound("bank_pin_success")
    message("You have correctly entered your PIN.")
    return true
}

/**
 * Opens the keypad (or reuses it for the next round) and suspends until
 * four digits are entered. Returns "" if the player exits instead.
 */
suspend fun Player.requestPin(instruction: String, new: Boolean = false): String {
    if (interfaces.contains("bank_pin")) {
        set("bank_pin_entered", "")
        BankPin.shuffle(this)
    } else {
        open("bank_pin")
    }
    interfaces.sendVisibility("bank_pin", "forgot", !new)
    if (!new) {
        interfaces.sendText("bank_pin", "forgot", "I don't know it.")
        interfaceOptions.unlockAll("bank_pin", "forgot")
    }
    interfaces.sendText("bank_pin", "instruction", instruction)
    return pauseString()
}

/**
 * Schedules the pin for deletion after the recovery delay so the
 * account isn't permanently locked when the pin is forgotten.
 */
suspend fun Player.forgottenPin() {
    if (!BankPin.isDeletePending(this)) {
        BankPin.requestForgottenDelete(this)
    }
    val days = BankPin.pendingDays(this)
    statement("Your PIN will be deleted in $days day${if (days == 1) "" else "s"}. If you remember it, you can cancel the deletion from your Bank PIN settings.")
}

suspend fun Player.wrongPin() {
    sound("bank_pin_wrong")
    message("The PIN you entered is incorrect.")
    val tries = get("bank_pin_tries", 0) + 1
    set("bank_pin_tries", tries)
    if (tries >= 2) {
        start("bank_pin_lockout", BankPin.lockoutSeconds(tries), epochSeconds())
        lockoutStatement()
    }
}

suspend fun Player.lockoutStatement() {
    val seconds = remaining("bank_pin_lockout", epochSeconds())
    if (seconds <= 60) {
        statement("You will be able to access your bank in less than 1 minute.")
    } else {
        statement("You will be able to access your bank pin in ${ceil(seconds / 60.0).toInt()} minutes.")
    }
}
