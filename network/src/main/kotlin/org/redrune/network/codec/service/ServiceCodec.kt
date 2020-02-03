package org.redrune.network.codec.service

import org.redrune.network.codec.Codec
import org.redrune.network.codec.service.decoder.LoginServiceMessageDecoder
import org.redrune.network.codec.service.decoder.UpdateServiceMessageDecoder
import org.redrune.network.codec.service.handler.SimpleServiceMessageHandler
import org.redrune.network.codec.service.handler.VersionBoundServiceMessageHandler
import org.redrune.network.codec.service.message.impl.SimpleServiceMessage
import org.redrune.network.codec.service.message.impl.VersionBoundServiceMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
object ServiceCodec : Codec() {

    override fun load() {
        bindDecoder(UpdateServiceMessageDecoder())
        bindDecoder(LoginServiceMessageDecoder())

        bindHandler(VersionBoundServiceMessage::class, VersionBoundServiceMessageHandler())
        bindHandler(SimpleServiceMessage::class, SimpleServiceMessageHandler())

        report()
    }

}