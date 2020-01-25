package org.redrune.network.codec.update

import org.redrune.network.message.Message
import org.redrune.network.message.MessageDecoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 9:11 p.m.
 */
abstract class UpdateMessageDecoder<T : Message>(length: Int, vararg opcodes: Int) :
    MessageDecoder<T>(length, *opcodes)