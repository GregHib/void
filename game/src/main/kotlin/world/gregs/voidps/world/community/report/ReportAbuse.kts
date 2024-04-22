package world.gregs.voidps.world.community.report

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open

interfaceOption("Report Abuse", "report", "filter_buttons") {
    if (player.hasMenuOpen()) {
        player.message("Please finish what you're doing first.")
        return@interfaceOption
    }
    player.open("report_abuse_select")
}