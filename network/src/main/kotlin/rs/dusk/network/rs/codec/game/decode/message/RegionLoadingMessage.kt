package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * When the players client is starting to load a region
 */
object RegionLoadingMessage : MessageCompanion<RegionLoadingMessage>(), Message