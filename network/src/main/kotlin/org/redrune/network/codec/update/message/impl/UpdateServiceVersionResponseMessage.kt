package org.redrune.network.codec.update.message.impl

import org.redrune.network.model.message.Message

/**
 * This class contains the data for the response message when the update server protocol is requested
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
data class UpdateServiceVersionResponseMessage(val responseCode: Int) :
    Message