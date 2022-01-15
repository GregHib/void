package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.character.update.visual.player.previousName
import world.gregs.voidps.engine.entity.definition.config.AccountDefinition
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty
import java.io.File

/**
 * Stores data about player accounts whether they're online or offline
 */
class AccountDefinitions {

    private val definitions = mutableMapOf<String, AccountDefinition>()

    fun add(player: Player) {
        definitions[player.name] = AccountDefinition(player.name, player.previousName)
    }

    fun update(newName: String, previousDisplayName: String) {
        val definition = definitions[previousDisplayName] ?: return
        definitions[newName] = definition
        definition.displayName = newName
        definition.previousName = previousDisplayName
    }

    fun get(key: String) = definitions[key]

    fun getValue(key: String) = definitions.getValue(key)

    fun load(storage: FileStorage = get(), path: String = getProperty("savePath")): AccountDefinitions {
        timedLoad("account") {
            for (save in File(path).listFiles() ?: return@timedLoad 0) {
                val player = storage.load<Player>(save.path)
                add(player)
            }
            definitions.size
        }
        return this
    }

}