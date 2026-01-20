package world.gregs.voidps.engine.data.types

import world.gregs.config.Config
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.type.Params
import world.gregs.voidps.cache.type.Type
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.param.Parameters
import java.io.File

abstract class ParamTypes<T, D: Definition> : Types<T, D>() where T: Type, T: Params {
    abstract val params: Parameters<T>
    open val maxStringSize = 100

    override fun load(cache: Cache, files: ConfigFiles) {
        params.validate()
        super.load(cache, files)
        // Params
        val extension = Settings[extension]
        val file = File("${Settings["storage.caching.path"]}${extension.replace(".toml", "_params.bin")}")
        val active = Settings["storage.caching.active", false]
        if (!file.exists() || files.cacheUpdate || !active || files.needsUpdate(extension)) {
            readConfig(files.list(extension), types)
            if (active) {
                params.write(file, types, size)
                files.update(extension)
            }
        } else {
            params.read(file, types)
        }
    }

    open fun readConfig(paths: List<String>, types: Array<T>) {
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
    }
}
