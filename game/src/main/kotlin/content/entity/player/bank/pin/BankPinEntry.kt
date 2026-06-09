package content.entity.player.bank.pin

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.suspend.Suspension

class BankPinEntry : Script {

    init {
        interfaceOpened("bank_pin") {
            if (BankPin.isDeletePending(this)) {
                val days = BankPin.pendingDays(this)
                interfaces.sendText("bank_pin", "deletion", "YOUR PIN WILL BE DELETED IN $days DAY${if (days == 1) "" else "S"}")
            } else {
                interfaces.sendText("bank_pin", "deletion", "")
            }
            set("bank_pin_entered", "")
            BankPin.shuffle(this)
        }

        interfaceOption("I don't know it", "bank_pin:forgot") {
            (suspension as? Suspension.StringEntry)?.resume("forgot")
        }

        continueDialogue("bank_pin:forgot") {
            (suspension as? Suspension.StringEntry)?.resume("forgot")
        }

        interfaceOption("Enter digit", "bank_pin:digit_*") {
            val layout: List<Int> = get("bank_pin_layout") ?: return@interfaceOption
            val button = it.component.removePrefix("digit_").toInt()
            val entered = get("bank_pin_entered", "") + layout[button - 1]
            set("bank_pin_entered", entered)
            if (entered.length == 4) {
                (suspension as? Suspension.StringEntry)?.resume(entered)
            } else {
                sound("bank_pin_beep")
                BankPin.shuffle(this)
            }
        }

        interfaceOption("Exit", "bank_pin:exit") {
            close("bank_pin")
        }

        interfaceClosed("bank_pin") {
            (suspension as? Suspension.StringEntry)?.resume("")
        }
    }
}
