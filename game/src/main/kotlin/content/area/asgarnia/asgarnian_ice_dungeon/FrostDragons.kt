package content.area.asgarnia.asgarnian_ice_dungeon

import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.type.Delta

class FrostDragons : Script {

    init {
        npcAttack("frost_dragon*", "orb") {
            start("orb_protection", 8)
            start("movement_delay", 8)
            val list = listOf(Delta(1, 0), Delta(0, 1), Delta(0, 2), Delta(1, 3), Delta(2, 3), Delta(3, 2), Delta(3, 1), Delta(2, 0), Delta(1, 0))
            var step = 20
            var delay = 0
            for (loop in 0 until 3) {
                for (i in 0 until list.lastIndex) {
                    tile.add(list[i]).shoot(
                        "frost_dragon_orb",
                        tile.add(list[i + 1]),
                        delay = delay,
                        flightTime = step,
                        height = 0,
                        endHeight = 0,
                        width = 0,
                    )
                    delay += step
                }
            }
        }

        npcCondition("frost_magic") { get("frost_style", "magic") == "magic" }

        npcCondition("frost_range") { get("frost_style", "range") == "range" }

        npcCondition("no_frost_orb") { hasClock("orb_protection") }

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
