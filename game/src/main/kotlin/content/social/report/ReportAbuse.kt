package content.social.report

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open

class ReportAbuse : Script {

    init {
        interfaceOption("Report Abuse", "report", "filter_buttons") {
            if (player.hasMenuOpen()) {
                player.message("Please finish what you're doing first.")
                return@interfaceOption
            }
            player.open("report_abuse_select")
        }
    }
}
