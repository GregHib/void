package world.gregs.voidps.engine.data.file

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.koin.dsl.module
import world.gregs.voidps.engine.data.StorageStrategy
import world.gregs.voidps.engine.entity.character.player.Player
import java.io.File

/**
 * @author GregHib <greg@gregs.world>
 * @since April 03, 2020
 */
class PlayerStorage(private val path: String) : StorageStrategy<Player> {

    val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
    val writer = mapper.writerWithDefaultPrettyPrinter()

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
