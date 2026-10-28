package world.gregs.voidps.engine.data.load

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings

class LoadContext(
    val files: ConfigFiles,
    val cache: Cache?,
    private val dependencies: Map<Loader<*>, Any> = emptyMap(),
    /** The snapshot key of the loader being run, which changes whenever anything it reads does. Null if unknown. */
    val key: Long? = null,
) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(loader: Loader<T>): T = dependencies[loader] as? T ?: throw IllegalStateException("Loader '${loader.name}' is not a declared dependency.")

    /** The input files of [loader] */
    fun paths(loader: Loader<*>): List<String> = loader.files.flatMap { files.list(Settings[it]) } + loader.file.map { files.find(Settings[it]) }

    fun list(key: String): List<String> = files.list(Settings[key])

    fun find(key: String): String = files.find(Settings[key])

    fun requireCache(): Cache = cache ?: throw IllegalStateException("A cache is required.")
}
