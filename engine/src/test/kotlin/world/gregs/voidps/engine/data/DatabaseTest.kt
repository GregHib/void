package world.gregs.voidps.engine.data

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import com.github.michaelbull.logging.InlineLogger
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import world.gregs.voidps.engine.data.sql.DatabaseStorage

/**
 * Starts a postgres database via docker if available or an in-memory emulation if not
 */
interface DatabaseTest {

    @BeforeEach
    fun setup() {
        transaction {
            SchemaUtils.create(*DatabaseStorage.tables, inBatch = true)
        }
    }

    @AfterEach
    fun tidy() {
        transaction {
            SchemaUtils.drop(*DatabaseStorage.tables, inBatch = true)
        }
    }

    companion object {
        private val logger = InlineLogger()
        private var postgres: PostgreSQLContainer<*>? = null
        private var db: EmbeddedPostgres? = null

        private const val DEFAULT_PORT = 5432
        private const val TEST_PORT = 6543

        @BeforeAll
        @JvmStatic
        fun startContainers() {
            try {
                val postgres = PostgreSQLContainer("postgres:16.2-alpine3.19")
                    .withDatabaseName("test")
                    .withUsername("root")
                    .withPassword("password")
                    .withExposedPorts(DEFAULT_PORT)
                    .withCreateContainerCmdModifier {
                        it.withHostConfig(
                            HostConfig().apply {
                                withPortBindings(PortBinding(Ports.Binding.bindPort(TEST_PORT), ExposedPort(DEFAULT_PORT)))
                            },
                        )
                    }
                postgres.start()
                Database.registerJdbcDriver(
                    prefix = "jdbc:tc",
                    driverClassName = "org.testcontainers.jdbc.ContainerDatabaseDriver",
                    dialect = "testcontainers",
                )
                DatabaseStorage.connect(
                    username = "root",
                    password = "password",
                    driver = "org.testcontainers.jdbc.ContainerDatabaseDriver",
                    url = "jdbc:tc:postgresql://localhost:$TEST_PORT/test?reWriteBatchedInserts=true",
                    poolSize = 2,
                )
                this.postgres = postgres
            } catch (e: IllegalStateException) {
                logger.info { "Docker not found; defaulting to in-memory database." }
                db = EmbeddedPostgres.builder()
                    .setPort(TEST_PORT)
                    .start()
                DatabaseStorage.connect(
                    username = "postgres",
                    password = "password",
                    driver = "org.postgresql.Driver",
                    url = "jdbc:postgresql://localhost:$TEST_PORT/",
                    poolSize = 2,
                )
            }
        }

        @AfterAll
        @JvmStatic
        fun stopContainers() {
            db?.close()
            postgres?.stop()
        }
    }
}
