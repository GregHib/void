package rs.dusk.network

import com.github.michaelbull.logging.InlineLogger
import com.google.common.base.Stopwatch
import org.koin.dsl.module
import rs.dusk.core.network.codec.CodecRepository
import rs.dusk.utility.inject
import java.util.concurrent.TimeUnit

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since March 31, 2020
 */
class NetworkRegistry {

	private val logger = InlineLogger()
	val repository: CodecRepository by inject()

	fun register() {
		val stopwatch = Stopwatch.createStarted()
		repository.registerAll()
		logger.info { "Took ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms to prepare all codecs" }
	}
}

val codecRepositoryModule = module {
	single { CodecRepository() }
}