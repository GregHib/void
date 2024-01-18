package world.gregs.voidps.js5

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.HybridCache
import java.io.File
import java.math.BigInteger
import kotlin.concurrent.thread

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val logger = InlineLogger()
        val start = System.currentTimeMillis()
        logger.info { "Start up..." }
        val file = File("./file-server.properties")
        if (!file.exists()) {
            logger.error { "Unable to find server properties file." }
            return
        }

        var revision = 0
        var port = 0
        var threads = 0
        var acknowledgeId = 3
        var statusId = 0
        lateinit var cachePath: String
        lateinit var modulus: BigInteger
        lateinit var exponent: BigInteger
        var prefetchKeys: IntArray = intArrayOf()
        file.forEachLine { line ->
            val (key, value) = line.split("=")
            when (key) {
                "revision" -> revision = value.toInt()
                "port" -> port = value.toInt()
                "threads" -> threads = value.toInt()
                "acknowledgeId" -> acknowledgeId = value.toInt()
                "statusId" -> statusId = value.toInt()
                "cachePath" -> cachePath = value
                "rsaModulus" -> modulus = BigInteger(value, 16)
                "rsaPrivate" -> exponent = BigInteger(value, 16)
                "prefetchKeys" -> prefetchKeys = value.split(",").map { it.toInt() }.toIntArray()
            }
        }

        println("JS5 Modulus: $modulus")
        println("JS5 Exponent: $exponent")
        logger.info { "Settings loaded." }

        val cache = HybridCache(cachePath, exponent, modulus)
        println("Generate: " + cache.indices().size)
        val versionTable = cache.versionTable(exponent = exponent, modulus = modulus)
        logger.info { "Version table generated: ${versionTable.contentToString()}" }

        if (prefetchKeys.isEmpty()) {
            prefetchKeys = generatePrefetchKeys(cache)
            logger.info { "Prefetch keys generated: ${prefetchKeys.contentToString()}" }
        }
        logger.info { "Cache loaded." }

        val fileServer = FileServer(cache, versionTable)
        val network = Network(fileServer, prefetchKeys, revision, acknowledgeId, statusId)
        logger.info { "Loading complete [${System.currentTimeMillis() - start}ms]" }
        val runtime = Runtime.getRuntime()
        runtime.addShutdownHook(thread(start = false) { network.stop() })
        network.start(port, threads)
    }
}