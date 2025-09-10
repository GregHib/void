package content.quest.member.fairy_tale_part_2.fairy_ring

import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject

@Script
class TravelLog {

    val fairyRing: FairyRingCodes by inject()

    init {
        interfaceOption("Re-sort list", "re_sort_list", "travel_log") {
            player.toggle("travel_log_re_sort")
        }

        interfaceOption(id = "travel_log") {
            player.setCode(component[0].toString(), component[1].toString(), component[2].toString())
        }

        interfaceOpen("travel_log") { player ->
            player.sendVariable("travel_log_re_sort")
            val list: List<String> = player["travel_log_locations"] ?: return@interfaceOpen
            for ((code, def) in fairyRing.codes) {
                if (list.contains(code)) {
                    player.interfaces.sendText(id, def.id.lowercase(), "<br>${def.name}")
                }
            }
        }
    }

    fun Player.setCode(one: String, two: String, three: String) {
        set("fairy_ring_code_1", one)
        set("fairy_ring_code_2", two)
        set("fairy_ring_code_3", three)
    }
}
