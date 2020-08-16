package rs.dusk.engine.entity.character.player.effect

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEffect
import rs.dusk.engine.entity.character.update.visual.player.appearance
import rs.dusk.engine.entity.character.update.visual.player.flagAppearance

data class Skull(val minutes: Int, val type: Int = 0) : PlayerEffect("skull") {

    override fun onStart(player: Player) {
        player.appearance.skull = 0
        player.flagAppearance()
        removeSelf(player, minutesToTicks(minutes))
    }

    override fun onFinish(player: Player) {
        player.appearance.skull = -1
        player.flagAppearance()
    }

    companion object {
        fun minutesToTicks(minutes: Int) = (minutes * 60000) / 600
    }
}