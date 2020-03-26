package org.redrune.network.rs.codec.game.handle

import org.redrune.core.network.codec.message.MessageHandler
import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class GameMessageHandler<M : Message> : MessageHandler<M>()