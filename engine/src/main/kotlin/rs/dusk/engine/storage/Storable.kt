package rs.dusk.engine.storage

/**
 * This class provides the storage functionality for all data that will be saved in a sql database
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since August 12, 2020
 */
interface Storable {
	
	fun save()
	
	fun load()
	
	
}