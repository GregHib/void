package org.redrune.network.codec.game.decode

import org.redrune.core.network.model.message.Message
import org.redrune.core.network.model.message.codec.MessageDecoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class GameMessageDecoder<M : Message> : MessageDecoder<M>()