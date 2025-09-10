package content.area.kandarin.tree_gnome_stronghold

import content.quest.questCompleted
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Tile

@Script
class GliderMap {

    init {
        interfaceOption("Travel", id = "glider_map") {
            val current = player["glider_location", "ta_quir_priw"]
            if (current == component) {
                player.message("You're already there.")
                return@interfaceOption
            }
            if (component == "lemantolly_undri" && !player.questCompleted("one_small_favour")) {
                player.message("You need to have completed One Small Favour quest to travel to here.") // TODO proper message
                return@interfaceOption
            }
            player["gnome_glider_journey"] = "${current}_to_$component"
            player.open("fade_out")
            World.queue("gnome_glider_${player.index}", 3) {
                val target = when (component) {
                    "ta_quir_priw" -> Tile(2465, 3500, 3)
                    "sindarpos" -> Tile(2850, 3497)
                    "lemanto_andra" -> Tile(3325, 3429)
                    "kar_hewo" -> Tile(3285, 3212)
                    "lemantolly_undri" -> Tile(2548, 2970)
                    "gandius" -> Tile(2972, 2965)
                    else -> return@queue
                }
                player.tele(target)
                player.open("fade_in")
                player.clear("gnome_glider_journey")
            }
        }
    }
}
