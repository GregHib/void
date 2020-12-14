package rs.dusk.core.network.codec.message

import io.netty.channel.ChannelHandler
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.message.Message
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@ChannelHandler.Sharable
abstract class MessageEncoder<M : Message> {
	
	/**
	 * Encodes a message into a packet builder
	 */
	abstract fun encode(builder : PacketWriter, msg : M) : Any
	
	@Suppress("UNCHECKED_CAST")
	fun getGenericTypeClass() : KClass<M> {
		return try {
			val className =
				(javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0].typeName
			val clazz = Class.forName(className).kotlin
			clazz as KClass<M>
		} catch (e : Exception) {
			throw IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ", e)
		}
	}
}