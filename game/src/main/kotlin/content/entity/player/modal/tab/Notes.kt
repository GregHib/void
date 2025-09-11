package content.entity.player.modal.tab

import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.event.Script

@Script
class Notes {

    init {
        interfaceOpen("notes") { player ->
            player.interfaceOptions.unlockAll(id, "notes", 0..30)
        }
    }
}
