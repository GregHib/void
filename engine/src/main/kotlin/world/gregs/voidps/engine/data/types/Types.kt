package world.gregs.voidps.engine.data.types

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.decoder.DefinitionCodec
import world.gregs.voidps.cache.type.Params
import world.gregs.voidps.cache.type.Type
import world.gregs.voidps.cache.type.codec.TypeCodec
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import java.io.File
import kotlin.collections.set
import kotlin.text.replace

abstract class Types<T, D: Definition> where T: Type, T: Params {
    protected var types: Array<T> = create(0)
    protected var ids = Object2IntOpenHashMap<String>().also { it.defaultReturnValue(-1) }

    fun get(id: Int) = types[id]

    fun getOrNull(id: Int) = types.getOrNull(id)

    abstract val extension: String
    abstract val definitionCodec: DefinitionCodec<D>
    abstract val typeCodec: TypeCodec<T>
    val size = 1_000_000

    abstract fun create(size: Int, array: Array<D>): Array<T>

    abstract fun create(size: Int): Array<T>

    open fun load(cache: Cache, files: ConfigFiles) {
        val extension = Settings[extension]
        val file = File("${Settings["storage.caching.path"]}${extension.replace(".toml", ".bin")}")
        // Definitions
        if (!file.exists() || files.cacheUpdate || !Settings["storage.caching.active", false]) {
            val defs = definitionCodec.load(cache)
            types = create(defs.size, defs)
            typeCodec.write(file, types, size)
        } else {
            types = typeCodec.read(file)
        }
        // Ids
        for (type in types) {
            ids[type.stringId] = type.id
        }
    }

    @TestOnly
    fun set(type: T) {
        types[type.id] = type
        ids[type.stringId] = type.id
    }

    fun clear() {
        types = create(0)
        ids.clear()
    }
}