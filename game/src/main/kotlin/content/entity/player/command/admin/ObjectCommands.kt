package content.entity.player.command.admin

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.event.Script
@Script
class ObjectCommands {

    val objects: GameObjects by inject()
    
    init {
        adminCommand("get", "get all objects under the player") {
            objects[player.tile].forEach {
                player.message(it.toString(), ChatType.Console)
            }
        }

    }

}
