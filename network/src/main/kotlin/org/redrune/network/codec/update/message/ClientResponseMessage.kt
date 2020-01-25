package org.redrune.network.codec.update.message

import org.redrune.network.message.Message
import org.redrune.tools.ReturnCode

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 8:55 p.m.
 */
class ClientResponseMessage(val code: ReturnCode) : Message