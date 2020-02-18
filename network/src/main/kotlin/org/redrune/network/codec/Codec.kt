package org.redrune.network.codec

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.message.MessageEncoder
import org.redrune.network.model.message.MessageHandler
import org.redrune.network.model.packet.Packet
import org.redrune.network.model.packet.PacketBuilder
import org.redrune.network.model.packet.PacketReader
import kotlin.reflect.KClass

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
@Suppress("UNCHECKED_CAST")
// TODO impl auto load
abstract class Codec : CodecRepository(), CodecFunction {

    private val logger = InlineLogger()

    override fun bindDecoder(decoder: MessageDecoder): Boolean {
        for (opcode in decoder.opcodes) {
            if (decoders.containsKey(opcode)) {
                logger.info { "Unable to bind pair [opcode=$opcode, decoder=$decoder] - already found a match for the opcode (${decoders[opcode]})" }
                return false
            }
            decoders[opcode] = decoder
        }
        return true
    }

    override fun decoder(opcode: Int): MessageDecoder {
        return decoders[opcode]
            ?: throw IllegalStateException("Unable to identify decoder by [opcode=$opcode], [codec=$this]")
    }

    override fun decode(reader: PacketReader): Message {
        return decoder(reader.opcode).decode(reader)
    }

    override fun getLength(opcode: Int): Int {
        return decoder(opcode).length
    }

    override fun <T : Message> handle(clazz: KClass<*>, ctx: ChannelHandlerContext, msg: T): MessageHandler<T> {
        val handler = handlers[clazz] as? MessageHandler<T>
            ?: throw IllegalStateException("Unable to identify handler by [class=$clazz], [codec=$this]")
        handler.handle(ctx, msg)
        return handler
    }

    override fun <T : Message> encode(clazz: KClass<*>, msg: T, out: PacketBuilder): Packet {
        val encoder = encoders[clazz] as? MessageEncoder<T>
            ?: throw IllegalStateException("Unable to identify encoder by [class=$clazz], [codec=$this]")
        encoder.encode(out, msg)
        return out.toPacket()
    }

    override fun <T : Message> bindHandler(clazz: KClass<T>, handler: MessageHandler<T>): Boolean {
        if (handlers.containsKey(clazz)) {
            logger.info { "Unable to bind pair [opcode=$clazz, handler=$handler] - already found a match for the class" }
            return false
        }
        return handlers.put(clazz, handler) != null
    }

    override fun <T : Message> bindEncoder(clazz: KClass<T>, encoder: MessageEncoder<T>): Boolean {
        if (encoders.containsKey(clazz)) {
            logger.info { "Unable to bind pair [opcode=$clazz, encoder=$encoder] - already found a match for the class" }
            return false
        }
        return encoders.put(clazz, encoder) != null
    }

}