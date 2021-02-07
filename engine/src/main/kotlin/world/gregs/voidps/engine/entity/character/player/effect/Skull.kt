package world.gregs.voidps.engine.entity.character.player.effect

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEffect
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.utility.toTicks
import java.util.concurrent.TimeUnit

data class Skull(val minutes: Int, val type: Int = 0) : PlayerEffect("skull") {

    override fun onStart(player: Player) {
        player.appearance.skull = 0
        player.flagAppearance()
        removeSelf(player, TimeUnit.MINUTES.toTicks(minutes.toLong()).toInt())
    }

    override fun onFinish(player: Player) {
        player.appearance.skull = -1
        player.flagAppearance()
    }
}