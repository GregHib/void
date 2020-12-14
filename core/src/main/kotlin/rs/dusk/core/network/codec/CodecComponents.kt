package rs.dusk.core.network.codec

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.packet.PacketMetaData
import kotlin.reflect.KClass

/**
 * The [codec][Codec] is composed of [decoders][MessageDecoder], [handlers][MessageHandler], and [encoders][MessageEncoder].
 * This class provides the storage and binding of said components.
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
open class CodecComponents {
	
	private val logger = InlineLogger()
	
	/**
	 * The map of decoders, which are of type D, and are specified by the opcode of the message they are handling
	 */
	val decoders = HashMap<Int, MessageDecoder<*>>()
	
	/**
	 * The map of handlers, which are specified by the class they are handling (a subclass of [Message])
	 */
	val handlers = HashMap<KClass<*>, MessageHandler<*>>()
	
	/**
	 * The map of message encoders, which are specified by the class they are handling (a subclass of [Message])
	 */
	val encoders = HashMap<KClass<*>, MessageEncoder<*>>()
	
	inline fun <reified T : MessageDecoder<*>> bindDecoders() {
		val decoders = ReflectionUtils.findSubclasses<T>()
		for (decoder in decoders) {
			if (!decoder.javaClass.isAnnotationPresent(PacketMetaData::class.java)) {
				throw IllegalStateException("Unable to register decoder ${decoder.javaClass.name}, no meta data defined!")
			}
			val metaData = decoder.javaClass.getDeclaredAnnotation(PacketMetaData::class.java)
			decoder.opcodes = metaData.opcodes
			decoder.length = metaData.length
			bindDecoder(decoder)
		}
	}
	
	inline fun <reified T : MessageHandler<*>> bindHandlers() {
		val handlers = ReflectionUtils.findSubclasses<T>()
		for (handler in handlers) {
			val type : KClass<*> = handler.getGenericTypeClass()
			bindHandler(type, handler)
		}
	}
	
	inline fun <reified T : MessageEncoder<*>> bindEncoders() {
		val encoders = ReflectionUtils.findSubclasses<T>()
		for (encoder in encoders) {
			val type : KClass<*> = encoder.getGenericTypeClass()
			bindEncoder(type, encoder)
		}
	}
	
	fun bindDecoder(decoder : MessageDecoder<*>) {
		decoder.opcodes?.forEach { opcode ->
			if (decoders[opcode] != null) {
				throw IllegalArgumentException("Cannot have duplicate decoders $decoder $opcode")
			}
			decoders[opcode] = decoder
		}
	}
	
	fun bindEncoder(type : KClass<*>, encoder : MessageEncoder<*>) {
		if (encoders.contains(type)) {
			throw IllegalArgumentException("Cannot have duplicate encoders $type $encoder")
		}
		encoders[type] = encoder
	}
	
	fun bindHandler(type : KClass<*>, encoder : MessageHandler<*>) {
		if (handlers.contains(type)) {
			throw IllegalArgumentException("Cannot have duplicate handlers $type $encoder")
		}
		handlers[type] = encoder
	}
	
	fun generateStatistics() : String {
		return "${javaClass.simpleName}[${decoders.size}, ${handlers.size}, ${encoders.size}]"
	}
	
}