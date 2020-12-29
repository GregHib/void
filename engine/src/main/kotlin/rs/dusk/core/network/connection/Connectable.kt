package rs.dusk.core.network.connection

/**
 * This class interfaces for objects in a network that can connect or be connected to.
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since May 11, 2020
 */
interface Connectable {
	
	/**
	 * A connectable object will call this when the connection has been dropped
	 */
	fun onConnect()
	
	/**
	 * A connectable object will call this when a connection has been dropped
	 */
	fun onDisconnect()
}