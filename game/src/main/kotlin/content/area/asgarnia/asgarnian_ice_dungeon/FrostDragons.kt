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
        npcCondition("frost_magic") { get("frost_style", "magic") == "magic" }

        npcCondition("frost_range") { get("frost_style", "range") == "range" }

        npcAttack("frost_dragon", "ice_arrows") {
            set("frost_style", "range")
        }

        npcAttack("frost_dragon", "magic") {
            set("frost_style", "magic")
        }

        npcDeath("frost_dragon*") {
            clear("frost_style")
        }
    }
}