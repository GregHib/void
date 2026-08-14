package content.quest.member.ghosts_ahoy

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class ModelShip : Script {
    init {
        itemOnItem("model_ship_silk", "red_dye") { _, _ -> dyeToyBoat("red", 1) }
        itemOnItem("model_ship_silk", "yellow_dye") { _, _ -> dyeToyBoat("yellow", 2) }
        itemOnItem("model_ship_silk", "blue_dye") { _, _ -> dyeToyBoat("blue", 3) }
        itemOnItem("model_ship_silk", "orange_dye") { _, _ -> dyeToyBoat("orange", 4) }
        itemOnItem("model_ship_silk", "green_dye") { _, _ -> dyeToyBoat("green", 5) }
        itemOnItem("model_ship_silk", "purple_dye") { _, _ -> dyeToyBoat("purple", 6) }
    }

    private suspend fun Player.dyeToyBoat(colourName: String, colourIndex: Int) {
        choice("Which part of the flag do you want to dye?") {
            option("Top half") {
                inventory.remove("${colourName}_dye")
                set("ahoy_toy_top", colourIndex)
                item(item = "model_ship_silk", text = "You dye the top of the flag $colourName.")
            }
            option("Bottom half") {
                inventory.remove("${colourName}_dye")
                set("ahoy_toy_bottom", colourIndex)
                item(item = "model_ship_silk", text = "You dye the bottom of the flag $colourName.")
            }
            option("Skull emblem") {
                inventory.remove("${colourName}_dye")
                set("ahoy_toy_skull", colourIndex)
                item(item = "model_ship_silk", text = "You dye the skull emblem $colourName.")
            }
        }
    }
}