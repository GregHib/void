package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.entity.character.player.PlayerEvent

/**
 * @author GregHib <greg@gregs.world>
 * @since May 01, 2020
 */
data class Command(val prefix: String, val content: String) : PlayerEvent