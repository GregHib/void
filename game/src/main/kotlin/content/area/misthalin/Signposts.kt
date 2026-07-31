package content.area.misthalin

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables

class Signposts : Script {

    init {
        objectOperate("Read", "direction_signpost_*") { (target) ->
            val row = Tables.get("signposts").rows().firstOrNull { it.tile("tile") == target.tile } ?: return@objectOperate
            open("signpost_directions")
            sendScript("camera_unlock", 0, 1)
            interfaces.sendText("signpost_directions", "north", row.string("north_text"))
            interfaces.sendText("signpost_directions", "east", row.string("east_text"))
            interfaces.sendText("signpost_directions", "south", row.string("south_text"))
            interfaces.sendText("signpost_directions", "west", row.string("west_text"))
        }

        interfaceClosed("signpost_directions") {
            clearCamera()
        }
    }
}
