package world.gregs.void.engine.entity.character.player.effect

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEffect
import world.gregs.void.engine.entity.character.update.visual.player.appearance
import world.gregs.void.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.void.utility.toTicks
import java.util.concurrent.TimeUnit

data class Skull(val minutes: Int, val type: Int = 0) : PlayerEffect("skull") {

    override fun onStart(player: Player) {
        player.appearance.skull = 0
        player.flagAppearance()
        removeSelf(player, TimeUnit.MINUTES.toTicks(minutes.toLong()))
    }

    override fun onFinish(player: Player) {
        player.appearance.skull = -1
        player.flagAppearance()
    }
}