package content.entity.player.bank.pin

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound

class BankPinSettings : Script {

    val buttons = listOf("set_pin", "recovery_new", "change_pin", "delete_pin", "recovery", "cancel_pending")

    init {
        interfaceOpened("bank_pin_settings") {
            BankPin.checkPending(this)
            refresh(this, defaultMessages(this))
            sound("bank_pin_pending")
        }

        interfaceOption("Set a PIN", "bank_pin_settings:set_pin") {
            confirm(
                this,
                "set",
                "Do you really wish to set a PIN on your bank account?",
                "Yes, I really want a Bank PIN. I will never forget it!",
                "No, I might forget it!",
            )
        }

        interfaceOption("Change your recovery delay", "bank_pin_settings:recovery*") {
            toggle("bank_pin_long_recovery")
            val days = BankPin.recoveryDays(this)
            refresh(
                this,
                listOf(
                    "Your recovery delay has",
                    "now been set to $days days.",
                    "",
                    "You would have to wait this",
                    "long to delete your PIN if",
                    "you forgot it. But you",
                    if (BankPin.hasPin(this)) "do have one." else "haven't got one...",
                ),
            )
        }

        interfaceOption("Change your PIN", "bank_pin_settings:change_pin") {
            if (BankPin.isLockedOut(this)) {
                close("bank_pin_settings")
                lockoutStatement()
                return@interfaceOption
            }
            val current = requestPin("Please enter your PIN using the buttons below.")
            if (current == "forgot") {
                close("bank_pin")
                forgottenPin()
                return@interfaceOption
            }
            if (current.length != 4) {
                return@interfaceOption
            }
            if (current != get("bank_pin", "")) {
                close("bank_pin")
                wrongPin()
                return@interfaceOption
            }
            set("bank_pin_unlocked", true)
            set("bank_pin_tries", 0)
            val new = choosePin() ?: return@interfaceOption
            close("bank_pin")
            if (new == get("bank_pin", "")) {
                message("Your current PIN matches your new one.")
                reopenSettings(this, defaultMessages(this))
            } else {
                BankPin.setPending(this, new)
                message("Your PIN change will take effect in ${BankPin.recoveryDays(this)} days.")
                reopenSettings(this, pendingMessages(this))
            }
        }

        interfaceOption("Delete your PIN", "bank_pin_settings:delete_pin") {
            confirm(
                this,
                "delete",
                "Do you really wish to delete your Bank PIN?",
                "Yes, I don't need a PIN anymore.",
                "No thanks, I'd rather keep the extra security.",
            )
        }

        interfaceOption("Cancel your PIN", "bank_pin_settings:cancel_pending") {
            val deletion = BankPin.isDeletePending(this)
            BankPin.clearPending(this)
            sound("bank_pin_cancel")
            if (deletion) {
                refresh(this, listOf("The PIN deletion has been", "cancelled.", "", "Your PIN remains active."))
            } else {
                refresh(this, listOf("The PIN change has been", "cancelled.", "", "Your old PIN remains", "active."))
            }
        }

        interfaceOption("Confirm", "bank_pin_settings:confirm") {
            when (get("bank_pin_confirm", "")) {
                "set" -> {
                    val pin = choosePin() ?: return@interfaceOption
                    close("bank_pin")
                    BankPin.setPin(this, pin)
                    reopenSettings(
                        this,
                        listOf("A PIN has been set on", "your bank account.", "", "You will be asked for it", "next time you visit the", "bank."),
                    )
                }
                "delete" -> {
                    if (!get("bank_pin_unlocked", false)) {
                        if (BankPin.isLockedOut(this)) {
                            close("bank_pin_settings")
                            lockoutStatement()
                            return@interfaceOption
                        }
                        val current = requestPin("Please enter your PIN using the buttons below.")
                        if (current == "forgot") {
                            close("bank_pin")
                            forgottenPin()
                            return@interfaceOption
                        }
                        if (current.length != 4) {
                            return@interfaceOption
                        }
                        close("bank_pin")
                        if (current != get("bank_pin", "")) {
                            wrongPin()
                            return@interfaceOption
                        }
                    }
                    BankPin.cancelPin(this)
                    reopenSettings(
                        this,
                        listOf("Your Bank PIN has now been", "deleted.", "", "This means that there is no", "PIN protection on your bank", "account."),
                    )
                }
            }
        }

        interfaceOption("Cancel", "bank_pin_settings:cancel") {
            refresh(this, listOf("No changes made."))
        }

        interfaceOption("Close", "bank_pin_settings:close") {
            close("bank_pin_settings")
        }
    }

    /**
     * Choose a new pin and confirm it. Returns null and reopens the settings
     * with the reason when the pin is rejected or the keypad is exited.
     */
    suspend fun Player.choosePin(): String? {
        val first = requestPin("Please choose a new FOUR DIGIT PIN using the buttons below.", new = true)
        if (first.length != 4) {
            return null
        }
        val second = requestPin("Now please enter that number again!", new = true)
        if (second.length != 4) {
            return null
        }
        if (BankPin.isEasyGuess(first)) {
            close("bank_pin")
            reopenSettings(this, listOf("That number wouldn't be very", "hard to guess. Please try", "something different!"))
            return null
        }
        if (first != second) {
            close("bank_pin")
            reopenSettings(
                this,
                listOf("Those numbers did not", "match.", "", "Your PIN has not been set;", "please try again if you wish", "to set a new PIN."),
            )
            return null
        }
        return first
    }

    fun reopenSettings(player: Player, messages: List<String>) {
        player.open("bank_pin_settings")
        refresh(player, messages)
    }

    fun refresh(player: Player, messages: List<String>) {
        val id = "bank_pin_settings"
        player.set("bank_pin_confirm", "")
        player.interfaces.sendText(id, "message_header", "Messages")
        player.interfaces.sendText(id, "hint", "Use the buttons below to change your PIN settings")
        player.interfaces.sendText(id, "delay", "${BankPin.recoveryDays(player)} days")
        for (i in 1..13) {
            player.interfaces.sendText(id, "message_$i", messages.getOrNull(i - 1) ?: "")
        }
        val active = BankPin.hasPin(player)
        val pending = BankPin.isPending(player) || BankPin.isDeletePending(player)
        val status = when {
            pending -> "PIN coming soon"
            active -> "You have a PIN"
            else -> "NO PIN set"
        }
        player.interfaces.sendText(id, "status", status)
        for (i in 1..3) {
            player.interfaces.sendVisibility(id, "extra_$i", false)
        }
        player.interfaces.sendVisibility(id, "confirm_overlay", false)
        player.interfaces.sendVisibility(id, "confirm", false)
        player.interfaces.sendVisibility(id, "cancel", false)
        player.interfaces.sendVisibility(id, "set_pin", !active)
        player.interfaces.sendVisibility(id, "recovery_new", !active)
        player.interfaces.sendVisibility(id, "change_pin", active && !pending)
        player.interfaces.sendVisibility(id, "delete_pin", active && !pending)
        player.interfaces.sendVisibility(id, "recovery", active && !pending)
        player.interfaces.sendVisibility(id, "cancel_pending", pending)
    }

    fun confirm(player: Player, action: String, text: String, yes: String, no: String) {
        val id = "bank_pin_settings"
        player.set("bank_pin_confirm", action)
        for (button in buttons) {
            player.interfaces.sendVisibility(id, button, false)
        }
        player.interfaces.sendVisibility(id, "confirm_overlay", true)
        player.interfaces.sendVisibility(id, "confirm", true)
        player.interfaces.sendVisibility(id, "cancel", true)
        player.interfaces.sendText(id, "confirm_text", text)
        player.interfaces.sendText(id, "confirm", yes)
        player.interfaces.sendText(id, "cancel", no)
        player.sound("bank_pin_pending")
    }

    fun defaultMessages(player: Player): List<String> = if (BankPin.isDeletePending(player)) {
        deleteMessages(player)
    } else if (BankPin.isPending(player)) {
        pendingMessages(player)
    } else {
        listOf(
            "Customers are reminded that",
            "they should NEVER tell",
            "anyone their Bank PINs or",
            "passwords, nor should they",
            "ever enter their PINs on any",
            "website form.",
            "",
            "Have you read the PIN guide",
            "on the website?",
        )
    }

    fun pendingMessages(player: Player): List<String> {
        val days = BankPin.pendingDays(player)
        return listOf(
            "You have requested that",
            "your PIN be changed. This",
            "will take effect in another",
            "$days day${if (days == 1) "" else "s"}.",
            "",
            "Your old PIN stays active",
            "until then. To cancel, use",
            "the button on the left.",
        )
    }

    fun deleteMessages(player: Player): List<String> {
        val days = BankPin.pendingDays(player)
        return listOf(
            "You have requested that",
            "your PIN be deleted. This",
            "will take effect in another",
            "$days day${if (days == 1) "" else "s"}.",
            "",
            "Your PIN stays active until",
            "then. To cancel, use the",
            "button on the left.",
        )
    }
}
