package content.minigame.fist_of_guthix

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.login.protocol.encode.sendVarcStr

class FogScoreboard : Script {

    init {
        for (option in scoreboardOptions()) {
            objectOperate(option, "fist_of_guthix_lobby_scoreboard") {
                openScoreboard()
            }
        }
    }

    private fun scoreboardOptions(): List<String> = ObjectDefinitions
        .get("fist_of_guthix_lobby_scoreboard")
        .options
        ?.filterNotNull()
        ?.filterNot { it == "Examine" }
        ?.ifEmpty { listOf("Read") }
        ?: listOf("Read")

    private fun Player.openScoreboard() {
        open("fist_of_guthix_scoreboard")
        interfaces.sendText("fist_of_guthix_scoreboard", "title", "Scoreboard")
        interfaces.sendText("fist_of_guthix_scoreboard", "subtitle", "Last fifty games on this world:")
        sendScoreboardLines(
            listOf(
                "No games have taken place yet.",
            ),
        )
    }

    private fun Player.sendScoreboardLines(lines: List<String>) {
        for (index in 0 until SCOREBOARD_LINE_COUNT) {
            client?.sendVarcStr(SCOREBOARD_LINE_START + index, lines.getOrElse(index) { "" })
        }
    }

    companion object {
        private const val SCOREBOARD_LINE_START = 224
        private const val SCOREBOARD_LINE_COUNT = 50
    }
}
