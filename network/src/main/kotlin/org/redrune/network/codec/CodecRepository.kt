package org.redrune.network.codec

import org.redrune.network.codec.handshake.UpdateMessageDecoder
import org.redrune.network.codec.handshake.UpdateMessageEncoder
import org.redrune.network.codec.handshake.UpdateMessageHandler
import org.redrune.network.message.Message
import org.redrune.network.message.MessageDecoder
import org.redrune.network.message.MessageEncoder
import org.redrune.network.message.MessageHandler
import org.redrune.tools.func.FileFunc
import kotlin.reflect.KClass

/**
 * This class stores the encoders and decoders of the network
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 3:24 p.m.
 */
abstract class CodecRepository {

    internal val decoders = arrayOfNulls<MessageDecoder<*>>(256)

    internal val encoders = HashMap<KClass<*>, MessageEncoder<*>>()

    internal val handlers = HashMap<KClass<*>, MessageHandler<*>>()

    abstract fun initialize()

    inline fun <reified T : MessageDecoder<*>> bindDecoders() {
        val decoders = FileFunc.getChildClassesOf<T>()
        for (clazz in decoders) {
            val decoder = clazz as UpdateMessageDecoder<*>
            bindDecoder(decoder)
            println("bound $decoders")
        }
    }

    inline fun <reified T : MessageHandler<*>> bindHandlers() {
        val handlers = FileFunc.getChildClassesOf<T>()
        for (clazz in handlers) {
            val handler = clazz as UpdateMessageHandler<*>
            val type: KClass<*> = handler.getGenericTypeClass()
            UpdateCodecRepository.bindHandler(type, handler)
        }
    }

    inline fun <reified T : MessageEncoder<*>> bindEncoders() {
        val encoders = FileFunc.getChildClassesOf<T>()
        for (clazz in encoders) {
            val encoder = clazz as UpdateMessageEncoder<*>
            val type: KClass<*> = encoder.getGenericTypeClass()
            UpdateCodecRepository.bindEncoder(type, encoder)
        }
    }

    fun decoder(opcode: Int): MessageDecoder<*>? {
        return decoders[opcode]
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Message> encoder(clazz: KClass<T>): MessageEncoder<T>? {
        return encoders[clazz] as? MessageEncoder<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Message> handler(clazz: KClass<T>): MessageHandler<T>? {
        return handlers[clazz] as? MessageHandler<T>
    }

    fun bindDecoder(decoder: MessageDecoder<*>) {
        if (decoders.contains(decoder)) {
            throw IllegalArgumentException("Cannot have duplicate decoders $decoder")
        }
        decoder.opcodes.forEach { opcode ->
            if (decoders[opcode] != null) {
                throw IllegalArgumentException("Cannot have duplicate decoders $decoder $opcode")
            }
            decoders[opcode] = decoder
        }
    }

    fun bindEncoder(type: KClass<*>, encoder: MessageEncoder<*>) {
        if (encoders.contains(type)) {
            throw IllegalArgumentException("Cannot have duplicate encoders $type $encoder")
        }
        encoders[type] = encoder
    }

    fun bindHandler(type: KClass<*>, encoder: MessageHandler<*>) {
        if (handlers.contains(type)) {
            throw IllegalArgumentException("Cannot have duplicate handlers $type $encoder")
        }
        handlers[type] = encoder
    }


}