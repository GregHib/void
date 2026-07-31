package content.area.asgarnia.rimmington

import world.gregs.voidps.engine.Script

class HettysHouse : Script {
    init {
        objectOperate("Open", "trapdoor_hettys_basement_closed") {
            set("hettys_trapdoor_opened", true)
        }

        objectOperate("Close", "trapdoor_hettys_basement_opened") {
            set("hettys_trapdoor_opened", false)
        }
    }
}