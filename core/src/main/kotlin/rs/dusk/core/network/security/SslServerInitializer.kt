package rs.dusk.core.network.security

import io.netty.channel.Channel
import io.netty.handler.ssl.IdentityCipherSuiteFilter
import io.netty.handler.ssl.SslContextBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since March 18, 2020
 */
class SslServerInitializer(private val config: SslConfig) {

    private val sslContext = SslContextBuilder.forServer(config.certificationFile, config.keyFile).apply {
        trustManager((if (config.trustCertificationFile.exists()) config.trustCertificationFile else null))
        keyManager(config.certificationFile, config.keyFile)
        ciphers(null, IdentityCipherSuiteFilter.INSTANCE)
        sessionCacheSize(0)
        sessionTimeout(0)
    }.build()

    fun addSslHandler(ch: Channel) {
        ch.pipeline().addLast("ssl.handler", sslContext.newHandler(ch.alloc()))
    }

}