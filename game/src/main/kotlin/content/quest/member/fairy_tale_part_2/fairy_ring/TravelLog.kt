package content.quest.member.fairy_tale_part_2.fairy_ring

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject

class TravelLog : Script {

    val fairyRing: FairyRingCodes by inject()

    init {
        interfaceOption("Re-sort list", "travel_log:re_sort_list") {
            toggle("travel_log_re_sort")
        }

        interfaceOption(id = "travel_log:*") {
            val component = it.component
            setCode(component[0].toString(), component[1].toString(), component[2].toString())
        }

        interfaceOpened("travel_log") { id ->
            sendVariable("travel_log_re_sort")
            val list: List<String> = get("travel_log_locations") ?: return@interfaceOpened
            for ((code, def) in fairyRing.codes) {
                if (list.contains(code)) {
                    interfaces.sendText(id, def.id.lowercase(), "<br>${def.name}")
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
