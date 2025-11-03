package content.social.report

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.open

class ReportAbuse : Script {

    init {
        interfaceOption("Report Abuse", "filter_buttons:report") {
            if (hasMenuOpen()) {
                message("Please finish what you're doing first.")
                return@interfaceOption
            }
            open("report_abuse_select")
        }
    }
}
