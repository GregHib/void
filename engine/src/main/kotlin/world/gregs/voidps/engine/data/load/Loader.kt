package world.gregs.voidps.engine.data.load

import world.gregs.voidps.engine.data.ConfigFiles

/**
 * Loads one kind of definition or config from the cache and/or config files.
 * [load] builds a new state without touching any global, [install] publishes it.
 */
interface Loader<T : Any> {
    /** Unique; used as the snapshot file name and reload alias. */
    val name: String

    /** Setting keys whose file lists are inputs, e.g. "definitions.items". Resolved with [ConfigFiles.list]. */
    val files: List<String> get() = emptyList()

    /** Setting keys of single file inputs. Resolved with [ConfigFiles.find]. */
    val file: List<String> get() = emptyList()

    val dependsOn: List<Loader<*>> get() = emptyList()

    /** Only the settings that change this loader's output, e.g. "world.members". */
    val settings: List<String> get() = emptyList()

    /** True if the output depends on the RS cache, so when it was modified is part of the key. */
    val usesCache: Boolean get() = false

    /** Null means never snapshot, for loaders that are cheap enough to always parse. */
    val codec: SnapshotCodec<T>? get() = null

    /** Builds a NEW state. Must not mutate globals or previously installed state. */
    fun load(context: LoadContext): T

    /** Publishes [state]; a plain field swap on the singleton. */
    fun install(state: T)
}
