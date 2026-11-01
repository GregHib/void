package content.minigame.great_orb_project

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile

data class GreatOrbProjectTeam(
    var altar: Int = -1,
    val green: MutableList<String> = mutableListOf(),
    val yellow: MutableList<String> = mutableListOf(),
    var yellowWins: Int = 0,
    var greenWins: Int = 0,
    var yellowScore: Int = 0,
    var greenScore: Int = 0,
    var disableWands: Boolean = false,
) {
    fun teleAll(area: Area, tile: Tile) {
        tele(green, true, area, tile)
        tele(yellow, false, area, tile)
    }

    private fun tele(list: List<String>, green: Boolean, area: Area, tile: Tile) {
        forEach(list, green) {
            if (tile in area) {
                return@forEach
            }
            message("You are pulled through the portal.") // https://youtu.be/On1sAphh1Bo?t=83
            tele(tile)
        }
    }

    fun forEach(block: Player.(Boolean) -> Unit) {
        forEach(green, true, block)
        forEach(yellow, false, block)
    }

    private fun forEach(list: List<String>, green: Boolean, block: Player.(green: Boolean) -> Unit) {
        for (name in list) {
            val player = Players.findByAccount(name) ?: continue
            block.invoke(player, green)
        }
    }

    fun ready() = green.size >= 2 && yellow.size >= 2 && green.size - yellow.size <= 1

    fun clear() {
        altar = -1
        green.clear()
        yellow.clear()
        yellowWins = 0
        greenWins = 0
        yellowScore = 0
        greenScore = 0
        disableWands = false
    }
}