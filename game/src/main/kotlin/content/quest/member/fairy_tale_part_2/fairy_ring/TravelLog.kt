package content.quest.member.fairy_tale_part_2.fairy_ring

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player

class TravelLog : Script {

    init {
        interfaceOption("Re-sort list", "travel_log:re_sort_list") {
            toggle("travel_log_re_sort")
        }

        interfaceOption(id = "travel_log:*") {
            val component = it.component
            setCode(component[0].toString(), component[1].toString(), component[2].toString())
        }

        interfaceOpened("travel_log") { id ->
            val list: List<String> = get("travel_log_locations") ?: return@interfaceOpened
            for (row in Tables.get("fairy_ring_codes").rows()) {
                if (list.contains(row.rowId)) {
                    interfaces.sendText(id, row.rowId, "<br>${row.string("name")}")
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
