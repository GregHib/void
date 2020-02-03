package org.redrune.network.codec.update.message.impl

import org.redrune.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
data class VersionResponseMessage(val responseCode: Int) :
    Message