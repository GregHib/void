package world.gregs.voidps.network.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Handler

/**
 * @author GregHib <greg@gregs.world>
 * @since May 05, 2020
 */
class RegionLoadedHandler : Handler() {

    val logger = InlineLogger()

    override fun regionLoaded(player: Player) {
        player.viewport.loaded = true
    }

}