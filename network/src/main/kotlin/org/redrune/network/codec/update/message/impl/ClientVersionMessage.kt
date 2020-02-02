package org.redrune.network.codec.update.message.impl

import org.redrune.network.codec.update.message.UpdateMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-01
 */
data class ClientVersionMessage(val majorBuild: Int) :
    UpdateMessage