package rs.dusk.engine.data.file

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import rs.dusk.engine.data.StorageStrategy
import rs.dusk.engine.entity.character.player.Player
import java.io.File

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
class PlayerStorage(private val path: String) : StorageStrategy<Player> {

    private fun path(name: String) = "$path\\$name.json"

    override fun load(name: String): Player? {
        val file = File(path(name))
        if (file.exists()) {
            return Json.decodeFromString<Player>(file.readText())
        }
        return null
    }

    override fun save(name: String, data: Player) {
        val file = File(path(name))
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(Json.encodeToString(data))
    }
}

@Suppress("USELESS_CAST")
val jsonPlayerModule = module {
    single {
        PlayerStorage(getProperty("savePath")) as StorageStrategy<Player>
    }
}
