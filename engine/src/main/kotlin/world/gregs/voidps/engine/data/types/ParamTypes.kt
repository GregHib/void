package world.gregs.voidps.engine.data.types

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.type.Params
import world.gregs.voidps.cache.type.Type
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.param.Parameters
import java.io.File

interface ParamTypes<T> : DefinitionTypes<T> where T : Type, T : Params {
    var ids: MutableMap<String, Int>

    fun get(id: String) = get(ids.getValue(id))

    fun getOrNull(id: String) = getOrNull(ids.getValue(id))

    fun Parameters<T>.read(files: ConfigFiles, fileExtension: String, maxSize: Int = 1_000_000, maxString: Int = 100) {
        validate()
        // Params
        val file = File("${Settings["storage.caching.path"]}${fileExtension.replace(".toml", "_params.bin")}")
        val active = Settings["storage.caching.active", false]
        if (file.exists() && !files.cacheUpdate && active && !files.needsUpdate(fileExtension)) {
            read(file, types)
            return
        }
        readConfig(files.list(fileExtension), this, maxString)
        if (active) {
            write(file, types, maxSize)
            files.update(fileExtension)
        }
    }

    fun readConfig(paths: List<String>, params: Parameters<T>, maxStringSize: Int) {
        val ids = Object2IntOpenHashMap<String>()
        ids.defaultReturnValue(-1)
        for (path in paths) {
            Config.fileReader(path, maxStringSize) {
                while (nextSection()) {
                    val section = section()
                    val params = params.read(this)
                    val id = params.remove(Parameters.ID) as? Int ?: throw IllegalArgumentException("Missing id for $section ${exception()}")
                    val clone = params.remove(Parameters.CLONE) as? String
                    if (clone != null) {
                        val cloneId = ids.getInt(clone)
                        require(cloneId >= 0) { "Unable to find clone with id '$clone' for $section ${exception()}" }
                        val cloneParams = types[cloneId].params
                        if (cloneParams != null) {
                            params.putAll(cloneParams)
                        }
                    }
                    val type = types[id]
                    ids.put(section, id)
                    type.stringId = section
                    type.set(params)
                }
            }
        }
        this.ids = ids
    }

}
