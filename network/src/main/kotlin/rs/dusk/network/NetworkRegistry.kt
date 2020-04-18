package rs.dusk.network

import com.github.michaelbull.logging.InlineLogger
import com.google.common.base.Stopwatch
import rs.dusk.core.tools.function.NetworkUtils
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.service.ServiceCodec
import rs.dusk.network.rs.codec.update.UpdateCodec
import java.util.concurrent.TimeUnit

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since March 31, 2020
 */
class NetworkRegistry {

    private val logger = InlineLogger()

    fun register() {
        val stopwatch = Stopwatch.createStarted()
        NetworkUtils.loadCodecs(
            ServiceCodec,
            UpdateCodec,
            LoginCodec,
            GameCodec
        )
        logger.info { "Took ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms to prepare all codecs" }
    }
}