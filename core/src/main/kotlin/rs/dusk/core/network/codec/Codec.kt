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
abstract class Codec : CodecComponents() {
	
	/**
	 * The registration of all [components][CodecComponents] of this codec must be done here.
	 *
	 */
	abstract fun register()
	
	/**
	 * Finds a decoder by an [opcode][Int]
	 * @return MessageDecoder<*>?
	 */
	fun decoder(opcode : Int) : MessageDecoder<*>? {
		return decoders[opcode]
	}
	
	/**
	 * This message finders a [handler][MessageHandler] by class
	 * @return MessageHandler<M>?
	 */
	@Suppress("UNCHECKED_CAST")
	fun <M : Message> handler(clazz : KClass<M>) : MessageHandler<M>? {
		return handlers[clazz] as? MessageHandler<M>
	}
	
	/**
	 * Finds an [encoder][MessageEncoder] by class
	 * @return MessageEncoder<M>?
	 */
	@Suppress("UNCHECKED_CAST")
	fun <M : Message> encoder(clazz : KClass<M>) : MessageEncoder<M>? {
		return encoders[clazz] as? MessageEncoder<M>
	}
	
	companion object {
		/**
		 * The attribute in the [channel][Channel] that identifies the [codec][Codec]
		 */
		val CODEC_KEY : AttributeKey<Codec> = AttributeKey.valueOf("codec.key")
	}
}

/**
 * Getting the codec of the channel
 * @receiver Channel
 */
fun Channel.getCodec() : Codec? {
	return attr(CODEC_KEY).get()
}

/**
 * Setting the codec of the channel
 * @receiver Channel
 */
fun Channel.setCodec(codec : Codec) {
	attr(CODEC_KEY).set(codec)
}