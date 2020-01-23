package org.redrune.network.codec

import org.redrune.network.message.Message
import org.redrune.network.message.MessageDecoder
import org.redrune.network.message.MessageEncoder
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * @author Tyluur<contact@kiaira.tech>
 * @since January 22, 2019
 */
object CodecRegistry {

    @JvmStatic
    fun main(args: Array<String>) {
        bindCodec()
    }

    fun bindCodec() {
        /* val encoderClasses = FileFunc.getClasses((ClassInfoList.ClassInfoFilter {
             it.extendsSuperclass(MessageEncoder::class.simpleName)
         }))
         val decoderClasses = FileFunc.getClasses((ClassInfoList.ClassInfoFilter {
             it.extendsSuperclass(MessageDecoder::class.simpleName) && it.hasAnnotation(FrameDefinition::class.simpleName)
         }))
         println(encoderClasses)*/
//        encoderClasses.forEach { bindEncoder(it)}

        run {
            logger.info("Bound ${decoders.filterNotNull().size} decoders and ${encoders.size} encoders")
        }
    }

    /**
     * The map of encoders
     */
    private val decoders = arrayOfNulls<MessageDecoder<*>>(256)

    /**
     * The map of the packete ncoders
     */
    private val encoders = HashMap<KClass<*>, MessageEncoder<*>>()

    private inline fun <reified T : Message> bindEncoder(encoder: MessageEncoder<T>) {
        bindEncoder(T::class, encoder)
    }

    private fun bindDecoder(decoder: MessageDecoder<*>) {
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

    @Suppress("UNCHECKED_CAST")
    fun <T : Message> getEncoder(clazz: KClass<T>): MessageEncoder<T>? {
        return encoders[clazz] as? MessageEncoder<T>
    }

    fun getDecoder(opcode: Int): MessageDecoder<*>? {
        return decoders[opcode]
    }

    private fun <T : Message> bindEncoder(type: KClass<T>, encoder: MessageEncoder<T>) {
        if (encoders.contains(type)) {
            throw IllegalArgumentException("Cannot have duplicate encoders $type $encoder")
        }
        encoders[type] = encoder
    }


    /**
     * The logger for this class
     */
    private val logger = LoggerFactory.getLogger(CodecRegistry::class.java)
}