package world.gregs.voidps.world.community.report

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open

interfaceOption("filter_buttons", "report", "Report Abuse") {
    if (player.hasMenuOpen()) {
        player.message("Please close the interface you have open before using .")// TODO
        return@interfaceOption
    }
    player.open("report_abuse_select")
}