package org.redrune.network.codec

import com.github.michaelbull.logging.InlineLogger
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.message.MessageEncoder
import org.redrune.network.model.message.MessageHandler
import kotlin.reflect.KClass

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
abstract class CodecRepository {

    private val logger = InlineLogger()

    /**
     * The loading of the repository is done here
     */
    abstract fun load()

    /**
     * The map of decoders, which are of type D, and are specified by the opcode of the message they are handling
     */
    protected val decoders = HashMap<Int, MessageDecoder>()

    /**
     * The map of handlers, which are specified by the class they are handling (a subclass of [Message])
     */
    protected val handlers = HashMap<KClass<*>, MessageHandler<*>>()

    /**
     * The map of message encoders, which are specified by the class they are handling (a subclass of [Message])
     */
    protected val encoders = HashMap<KClass<*>, MessageEncoder<*>>()

    /**
     * Binds the decoder by the opcode
     * @return Boolean Bind succession
     */
    abstract fun bindDecoder(decoder: MessageDecoder): Boolean

    /**
     * Binds a handler to the class it is handling
     * @return Boolean Bind succession
     */
    abstract fun <T : Message> bindHandler(clazz: KClass<T>, handler: MessageHandler<T>): Boolean

    /**
     * Binds an encoder to the class it is encoding
     * @return Boolean Bind succession
     */
    abstract fun <T : Message> bindEncoder(clazz: KClass<T>, encoder: MessageEncoder<T>): Boolean

    /**
     * Gets the decoder by an opcode
     */
    abstract fun decoder(opcode: Int): MessageDecoder

    fun report() {
        logger.info { "${this.javaClass.simpleName} loaded ${decoders.size} decoders, ${handlers.size} handlers, and ${encoders.size} encoders" }
    }
}