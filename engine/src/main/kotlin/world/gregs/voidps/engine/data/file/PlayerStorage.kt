package world.gregs.voidps.engine.data.file

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.koin.dsl.module
import world.gregs.voidps.engine.data.StorageStrategy
import world.gregs.voidps.engine.entity.character.player.Player
import java.io.File

class PlayerStorage(private val path: String) : StorageStrategy<Player> {

    private val mapper = jacksonObjectMapper()
    private val writer = mapper.writerWithDefaultPrettyPrinter()

    private fun path(name: String) = "$path\\$name.json"

    override fun load(name: String): Player? {
        val file = File(path(name))
        if (file.exists()) {
            try {
                return mapper.readValue(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    override fun save(name: String, data: Player) {
        val file = File(path(name))
        if (!file.exists()) {
            file.createNewFile()
        }
        writer.writeValue(file, data)
    }
}

@Suppress("USELESS_CAST")
val jsonPlayerModule = module {
    single {
        PlayerStorage(getProperty("savePath")) as StorageStrategy<Player>
    }
}
