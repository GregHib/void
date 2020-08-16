package rs.dusk.engine.entity.character.player.effect

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEffect
import rs.dusk.engine.entity.character.update.visual.player.appearance
import rs.dusk.engine.entity.character.update.visual.player.flagAppearance

object Hidden : PlayerEffect("hidden") {

    override fun onStart(player: Player) {
        player.appearance.hidden = true
        player.flagAppearance()
    }

    override fun onFinish(player: Player) {
        player.appearance.hidden = false
        player.flagAppearance()
    }
}