package rs.dusk.core.network.connection.event

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 02, 2020
 */
enum class ChannelEventType {
	
	/**
	 * When a channel registers to a connection, an event of this type is invoked
	 */
    REGISTER,
	
	/**
	 * When a channel is de-registered from a connection, an event of this type is invoked
	 */
	DEREGISTER,
	
	/**
	 * When a channel connection is made active, an event of this type is invoked
	 */
	ACTIVE,
	
	/**
	 * When a channel connection is made inactive, an event of this type is invoked.
	 */
	INACTIVE,
	
	/**
	 * When an exception is thrown as a result of an operation in a netty thread, an event of this type is invoked
	 */
	EXCEPTION
}