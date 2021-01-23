package world.gregs.voidps.engine.entity.character.player.effect

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEffect
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance

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