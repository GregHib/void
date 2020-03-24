package org.redrune.network.codec.game.encode

import org.redrune.core.network.message.Message
import org.redrune.core.network.message.codec.MessageEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class GameMessageEncoder<M : Message> : MessageEncoder<M>()