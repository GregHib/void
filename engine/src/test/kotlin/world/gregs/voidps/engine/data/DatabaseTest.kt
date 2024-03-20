package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer

/**
 * Starts a postgres database via docker if available or an in-memory emulation if not
 */
interface DatabaseTest {

    companion object {
        private val logger = InlineLogger()
        private var postgres: PostgreSQLContainer<*>? = null

        @BeforeAll
        @JvmStatic
        fun startContainers() {
            try {
                val postgres = PostgreSQLContainer("postgres:alpine3.19")
                postgres.start()
                Database.registerJdbcDriver(
                    prefix = "jdbc:tc",
                    driverClassName = "org.testcontainers.jdbc.ContainerDatabaseDriver",
                    dialect = "testcontainers"
                )
                Database.connect(
                    url = "jdbc:tc://localhost:5432/",
                    user = "postgres",
                    password = "password"
                )
                this.postgres = postgres
            } catch (e: IllegalStateException) {
                logger.info { "Docker not found; defaulting to in-memory database." }
                val dataSource = JdbcDataSource().apply {
                    setURL("jdbc:h2:mem:testdb;MODE=PostgreSQL")
                    user = "root"
                    password = "password"
                }
                Database.connect(dataSource)
            }
        }

        @AfterAll
        @JvmStatic
        fun stopContainers() {
            postgres?.stop()
        }
    }
}