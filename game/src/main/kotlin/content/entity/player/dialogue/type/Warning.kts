package content.entity.player.dialogue.type

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.suspend.StringSuspension

interfaceOption("Yes", "yes", "warning_*") {
    (player.dialogueSuspension as StringSuspension).resume("yes")
}

interfaceOption("No", "no", "warning_*") {
    (player.dialogueSuspension as StringSuspension).resume("no")
}

interfaceOption("Off/On", "dont_ask", "warning_*") {
    val count = player[id, 0]
    if (count == 6) {
        player[id] = 7
    } else {
        player[id] = 6
    }
}