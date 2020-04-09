package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import com.google.common.base.Stopwatch
import org.redrune.core.tools.function.NetworkUtils
import org.redrune.network.rs.codec.game.GameCodec
import org.redrune.network.rs.codec.login.LoginCodec
import org.redrune.network.rs.codec.service.ServiceCodec
import org.redrune.network.rs.codec.update.UpdateCodec
import org.redrune.network.social.codec.SocialCodec
import java.util.concurrent.TimeUnit

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since March 31, 2020
 */
class NetworkRegistry {

    private val logger = InlineLogger()

    fun register() {
        NetworkUtils.loadCodecs(ServiceCodec, UpdateCodec, LoginCodec, GameCodec, SocialCodec)
        val stopwatch = Stopwatch.createStarted()
        NetworkUtils.loadCodecs(ServiceCodec, UpdateCodec, LoginCodec, GameCodec)
        logger.info { "Took ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms to prepare all codecs" }
    }
}