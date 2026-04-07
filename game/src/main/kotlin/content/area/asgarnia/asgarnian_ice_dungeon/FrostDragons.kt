package content.area.asgarnia.asgarnian_ice_dungeon

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.queue.softQueue

class FrostDragons : Script {

    init {
        npcAttack("frost_dragon*", "orb") {
            set("orb_protection", true)
            queue.clear("frost_dragon_orb_protection")
            softQueue("frost_dragon_orb_protection", 8) {
                clear("orb_protection")
            }
        }
    }
}
