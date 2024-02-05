package world.gregs.voidps.world.community.report

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

interfaceOption({ id == "filter_buttons" && component == "report" && option == "Report Abuse" }) { player: Player ->
    if (player.hasMenuOpen()) {
        player.message("Please close the interface you have open before using .")// TODO
        return@interfaceOption
    }
    player.open("report_abuse_select")
}