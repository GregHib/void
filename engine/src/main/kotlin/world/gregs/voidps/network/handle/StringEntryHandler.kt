package world.gregs.voidps.network.handle

import world.gregs.voidps.engine.client.ui.dialogue.StringEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Handler

/**
 * @author GregHib <greg@gregs.world>
 * @since August 04, 2020
 */
class StringEntryHandler : Handler() {

    override fun stringEntered(player: Player, text: String) {
        player.events.emit(StringEntered(text))
    }

}