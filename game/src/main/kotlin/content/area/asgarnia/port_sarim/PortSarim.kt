package content.area.asgarnia.port_sarim

import content.entity.player.inv.item.take.canTake
import content.entity.player.inv.item.take.taken
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.type.Tile

@Script
class PortSarim {

    init {
        canTake("white_apron_port_sarim") { player ->
            if (player.holdsItem("white_apron")) {
                player.message("You already have one of those.")
                cancel()
            }
            item = "white_apron"
        }

        taken("white_apron_port_sarim") { player ->
            player.anim("take")
            player.message("You take an apron. It feels freshly starched and smells of laundry.")
        }
    }
}
