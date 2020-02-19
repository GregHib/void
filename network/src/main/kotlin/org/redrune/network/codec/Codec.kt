package org.redrune.network.codec

import com.github.michaelbull.logging.InlineLogger
import org.redrune.network.message.Message
import org.redrune.network.message.codec.MessageDecoder
import org.redrune.network.message.codec.MessageEncoder
import org.redrune.network.message.codec.MessageHandler
import kotlin.reflect.KClass

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@Suppress("UNCHECKED_CAST")
abstract class Codec : CodecRepository(),
    CodecFunction {

    private val logger = InlineLogger()

    /**
     * The registration of the codec components must be explicitly done here, due to the requirement of type specification
     */
    abstract fun register()

    fun report() {
        logger.info { "${this.javaClass.simpleName} information - [decoders=${decoders.size}, handlers=${handlers.size}, encoders=${encoders.size}]" }
    }

    override fun decoder(opcode: Int): MessageDecoder<*>? {
        return decoders[opcode]
    }

    override fun <M : Message> handler(clazz: KClass<M>): MessageHandler<M>? {
        return handlers[clazz] as? MessageHandler<M>
    }

    override fun <M : Message> encoder(clazz: KClass<M>): MessageEncoder<M>? {
        return encoders[clazz] as? MessageEncoder<M>
    }

}