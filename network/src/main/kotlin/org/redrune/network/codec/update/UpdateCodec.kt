package org.redrune.network.codec.update

import org.redrune.network.codec.Codec
import org.redrune.network.codec.update.decoder.ClientEventMessageDecoder
import org.redrune.network.codec.update.decoder.ConnectionEventMessageDecoder
import org.redrune.network.codec.update.decoder.FileRequestMessageDecoder
import org.redrune.network.codec.update.encoder.FileResponseEncoder
import org.redrune.network.codec.update.encoder.VersionResponseEncoder
import org.redrune.network.codec.update.handler.ClientEventMessageHandler
import org.redrune.network.codec.update.handler.ConnectionEventMessageHandler
import org.redrune.network.codec.update.handler.FileRequestMessageHandler
import org.redrune.network.codec.update.message.impl.ClientEventMessage
import org.redrune.network.codec.update.message.impl.ConnectionEventMessage
import org.redrune.network.codec.update.message.impl.FileRequestMessage
import org.redrune.network.codec.update.message.impl.VersionResponseMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
object UpdateCodec : Codec() {

    override fun load() {
        bindDecoder(FileRequestMessageDecoder())
        bindDecoder(ClientEventMessageDecoder())
        bindDecoder(ConnectionEventMessageDecoder())

        bindHandler(ClientEventMessage::class, ClientEventMessageHandler())
        bindHandler(ConnectionEventMessage::class, ConnectionEventMessageHandler())
        bindHandler(FileRequestMessage::class, FileRequestMessageHandler())

        bindEncoder(VersionResponseMessage::class, VersionResponseEncoder())
        bindEncoder(FileRequestMessage::class, FileResponseEncoder())

        report()
    }
}