package content.area.fremennik_province.keldagrim

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class ClothesStall : Script {
    init {
        objectOperate("Steal-from", "clothes_stall_keldagrim") {
            message("You don't really see anything you'd want to steal from this stall.")
        }
    }
}
