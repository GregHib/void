package content.area.misthalin.varrock.museum

import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest

class MuseumDisplayCases : Script {
    init {
        // vm_timeline_no_display is the empty-case transform for every museum display,
        // so scope both options to the terracotta statue's own base object.
        objectOperate("Study", "vm_timeline_terracotta_statue_multi,vm_timeline_no_display") { (target) ->
            if (target.id != "vm_timeline_terracotta_statue") {
                noInterest()
                return@objectOperate
            }
            studyDisplayCase()
        }
    }

    private fun Player.studyDisplayCase() {
        if (!open("vm_timeline")) {
            return
        }
        anim("vm_display_case_ponder")
        val stolen = get("golem_retrieved_statuette", false)
        sendScript("museum_rotate_display", 0, 5, 0, InterfaceDefinition.pack(534, 50))
        interfaces.sendModel("vm_timeline", "vm_timeline_terracotta_statue_model", if (stolen) 25568 else 25576)
        interfaces.sendText("vm_timeline", "display_num", "30")
        interfaces.sendText(
            "vm_timeline",
            "vm_timeline_text",
            "3rd Age - yr 3000-4000<br><br>This " +
                "statuette was found in an underground temple in the ruined city of Uzer, which was destroyed late " +
                "in the 3rd Age, suddenly, due to causes unknown. It probably represents one of the clay golems " +
                "that the craftsmen of the city built as warriors and servants. The statuette was originally part " +
                "of a mechanism whose purpose is unknown." +
                (if (stolen) "<br><br>Recently this display was stolen and its whereabouts are unknown." else ""),
        )
    }
}
