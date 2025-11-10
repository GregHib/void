package content.area.kandarin.tree_gnome_stronghold

import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile

class GliderMap : Script {

    init {
        interfaceOption("Travel", id = "glider_map:*") {
            val current = get("glider_location", "ta_quir_priw")
            if (current == it.component) {
                message("You're already there.")
                return@interfaceOption
            }
            if (it.component == "lemantolly_undri" && !questCompleted("one_small_favour")) {
                return@interfaceOption
            }
            set("gnome_glider_journey", "${current}_to_${it.component}")
            open("fade_out")
            World.queue("gnome_glider_$index", 3) {
                val target = when (it.component) {
                    "ta_quir_priw" -> Tile(2465, 3500, 3)
                    "sindarpos" -> Tile(2850, 3497)
                    "lemanto_andra" -> Tile(3325, 3429)
                    "kar_hewo" -> Tile(3285, 3212)
                    "lemantolly_undri" -> Tile(2548, 2970)
                    "gandius" -> Tile(2972, 2965)
                    else -> return@queue
                }
                tele(target)
                open("fade_in")
                clear("gnome_glider_journey")
            }
        }

        interfaceOpened("glider_map") {
            interfaces.sendVisibility("glider_map", "lemantolly_undri", questCompleted("one_small_favour"))
        }
    }
}
