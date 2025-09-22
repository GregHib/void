package content.area.fremennik_province.keldagrim

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script

@Script
class ClothesStall {
    init {
        objectOperate("Steal-from", "clothes_stall_keldagrim") {
            player.message("You don't really see anything you'd want to steal from this stall.")
        }
    }
}