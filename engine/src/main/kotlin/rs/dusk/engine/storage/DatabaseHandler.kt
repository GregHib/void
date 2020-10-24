package rs.dusk.engine.storage

import com.github.michaelbull.logging.InlineLogger
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import org.koin.dsl.module
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject
import java.sql.Connection

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since August 12, 2020
 */
class DatabaseHandler {
	
	private val logger = InlineLogger()
	
	/**
	 * The file loader used to pull properties data
	 */
	val loader : FileLoader by inject()
	
	/**
	 * The properties data
	 */
	val properties : Map<String, String> =
		loader.load<Map<String, String>>(getProperty("databasePath")).mapKeys { it.key }
	
	/**
	 * The configuration settings used for the connection
	 */
	private lateinit var config : HikariConfig
	
	/**
	 * The data source for the database
	 */
	private lateinit var dataSource : HikariDataSource
	
	/**
	 * The connection to use
	 */
	var connection : Connection? = null
	
	/**
	 * Loads the configuration from the file containing the data and constructs a new [instance][HikariConfig]
	 */
	private fun loadConfiguration() : HikariConfig {
		config = HikariConfig()
		config.jdbcUrl = properties["jdbcUrl"]
		config.username = properties["username"]
		config.password = properties["password"]
		return config
	}
	
	/**
	 * Construct the hikari database pool
	 */
	private fun construct() : HikariDataSource {
		return HikariDataSource(config)
	}
	
	/**
	 * Starts the database factory
	 */
	fun start() {
		config = loadConfiguration()
		dataSource = construct()
		connection = dataSource.connection
	}
	
	init {
		try {
			start()
			logger.info { "Successfully created the database pool." }
		} catch (e: HikariPool.PoolInitializationException) {
			logger.warn { "Unable to connect to database." }
		}
	}
	
}

val databaseModule = module {
	single(createdAtStart = true) { DatabaseHandler() }
}