package world.gregs.voidps.engine.map.file

import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.engine.utility.get
import java.io.File

class Maps {
    private val decoder = MapDecoder(get(), get<Xteas>())

    fun load(compress: Boolean, path: String) {
        val file = File(path)
        if (!compress || !file.exists()) {
            load()
            if (compress) {
                compress(file)
            }
        } else {
            extract(file)
        }
    }

    fun extract(file: File) {
        MapExtract(file, get(), get()).run()
    }

    fun load() {
        MapLoader(decoder, get(), get()).run()
    }

    fun compress(file: File) {
        MapCompress(file, get(), decoder).run()
    }
}