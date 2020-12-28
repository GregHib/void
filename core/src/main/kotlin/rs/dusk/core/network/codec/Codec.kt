package rs.dusk.core.network.codec

import io.netty.channel.Channel
import io.netty.util.AttributeKey
import rs.dusk.core.network.codec.Codec.Companion.CODEC_KEY
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.message.Message
import kotlin.reflect.KClass

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class Codec {

    val decoders = HashMap<Int, MessageDecoder<*>>()
    val handlers = HashMap<KClass<*>, MessageHandler<*>>()
    val encoders = HashMap<KClass<*>, MessageEncoder<*>>()

    abstract fun register()

    fun registerDecoder(opcode: Int, decoder: MessageDecoder<*>) {
        if (decoders[opcode] != null) {
            throw IllegalArgumentException("Cannot have duplicate decoders $decoder $opcode")
        }
        decoders[opcode] = decoder
    }

    inline fun <reified T : Message> registerEncoder(encoder: MessageEncoder<T>) {
        if (encoders.contains(T::class)) {
            throw IllegalArgumentException("Cannot have duplicate encoders ${T::class} $encoder")
        }
        encoders[T::class] = encoder
    }

    inline fun <reified T : Message> registerHandler(handler: MessageHandler<T>) {
        if (handlers.contains(T::class)) {
            throw IllegalArgumentException("Cannot have duplicate handlers ${T::class} $handler")
        }
        handlers[T::class] = handler
    }

    fun getDecoder(opcode: Int): MessageDecoder<*>? {
        return decoders[opcode]
    }

    /**
     * This message finders a [handler][MessageHandler] by class
     * @return MessageHandler<M>?
     */
    @Suppress("UNCHECKED_CAST")
    fun <M : Message> getHandler(clazz: KClass<M>): MessageHandler<M>? {
        return handlers[clazz] as? MessageHandler<M>
    }

    /**
     * Finds an [encoder][MessageEncoder] by class
     * @return MessageEncoder<M>?
     */
    @Suppress("UNCHECKED_CAST")
    fun <M : Message> getEncoder(clazz: KClass<M>): MessageEncoder<M>? {
        return encoders[clazz] as? MessageEncoder<M>
    }

    companion object {
        /**
         * The attribute in the [channel][Channel] that identifies the [codec][Codec]
         */
        val CODEC_KEY: AttributeKey<Codec> = AttributeKey.valueOf("codec.key")
    }
}

/**
 * Getting the codec of the channel
 * @receiver Channel
 */
fun Channel.getCodec(): Codec? {
    return attr(CODEC_KEY).get()
}

/**
 * Setting the codec of the channel
 * @receiver Channel
 */
fun Channel.setCodec(codec: Codec) {
    attr(CODEC_KEY).set(codec)
}