package content.area.karamja

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class Karamja : Script {

    init {

        objectOperate("Search", "snake_vine_full") { (target) ->
            if (quest("jungle_potion") == "unstarted") {
                message("Unfortunately, you find nothing of interest.")
            } else {
                message("You search the vine...")
                animDelay("open_chest_mid")
                if (inventory.add("grimy_snake_weed")) {
                    if (quest("jungle_potion") == "started") {
                        set("jungle_potion", "found_snake_weed")
                    }
                    target.replace("snake_vine_empty", ticks = TimeUnit.SECONDS.toTicks(60))
                    item("grimy_snake_weed", "You find a grimy herb.")
                } else {
                    item("grimy_snake_weed", "You find a grimy herb but you have no place to store it in your inventory.")
                }
            }
        }

        objectOperate("Search", "ardrigal_palm_full") { (target) ->
            if (quest("jungle_potion") == "unstarted" ||
                quest("jungle_potion") == "started" ||
                quest("jungle_potion") == "found_snake_weed"
            ) {
                message("You find nothing of significance.")
            } else {
                if (inventory.add("grimy_ardrigal")) {
                    if (quest("jungle_potion") == "gave_snake_weed") {
                        set("jungle_potion", "found_ardrigal")
                    }
                    target.replace("ardrigal_palm_empty", ticks = TimeUnit.SECONDS.toTicks(60))
                    item("grimy_ardrigal", "You find a herb.")
                } else {
                    item("grimy_ardrigal", "You find a grimy herb but you have no place to store it in your inventory.")
                }
            }
        }

        objectOperate("Search", "sito_soil_full") { (target) ->
            if (quest("jungle_potion") == "unstarted" ||
                quest("jungle_potion") == "started" ||
                quest("jungle_potion") == "found_snake_weed" ||
                quest("jungle_potion") == "gave_snake_weed" ||
                quest("jungle_potion") == "found_ardrigal"
            ) {
                message("You find nothing of significance.")
            } else {
                if (inventory.add("grimy_sito_foil")) {
                    if (quest("jungle_potion") == "gave_ardrigal") {
                        set("jungle_potion", "found_sito_foil")
                    }
                    target.replace("sito_soil_empty", ticks = TimeUnit.SECONDS.toTicks(60))
                    item("grimy_sito_foil", "You find a herb.")
                } else {
                    item("grimy_sito_foil", "You find a grimy herb but you have no place to store it in your inventory.")
                }
            }
        }

        objectOperate("Search", "volencia_moss_rock_full") { (target) ->
            if (quest("jungle_potion") == "unstarted" ||
                quest("jungle_potion") == "started" ||
                quest("jungle_potion") == "found_snake_weed" ||
                quest("jungle_potion") == "gave_snake_weed" ||
                quest("jungle_potion") == "found_ardrigal" ||
                quest("jungle_potion") == "gave_ardrigal" ||
                quest("jungle_potion") == "found_sito_foil"
            ) {
                message("You find nothing of significance.")
            } else {
                if (inventory.add("grimy_volencia_moss")) {
                    if (quest("jungle_potion") == "gave_sito_foil") {
                        set("jungle_potion", "found_volencia_moss")
                    }
                    target.replace("volencia_moss_rock_empty", ticks = TimeUnit.SECONDS.toTicks(60))
                    item("grimy_volencia_moss", "You find a herb.")
                } else {
                    item("grimy_volencia_moss", "You find a grimy herb but you have no place to store it in your inventory.")
                }
            }
        }

        objectOperate("Search", "rogues_purse_cave_full") { (target) ->
            message("You search the wall...")
            anim("human_pick_wall_middle") // maybe needs human_pick_wall_start
            delay(4)
            anim("human_pick_wall_end")
            if (quest("jungle_potion") == "unstarted" ||
                quest("jungle_potion") == "started" ||
                quest("jungle_potion") == "found_snake_weed" ||
                quest("jungle_potion") == "gave_snake_weed" ||
                quest("jungle_potion") == "found_ardrigal" ||
                quest("jungle_potion") == "gave_ardrigal" ||
                quest("jungle_potion") == "found_sito_foil" ||
                quest("jungle_potion") == "gave_sito_foil" ||
                quest("jungle_potion") == "found_volencia_moss"
            ) {
                message("Unfortunately, you find nothing of interest.")
            } else {
                if (inventory.add("grimy_rogues_purse")) {
                    if (quest("jungle_potion") == "gave_volencia_moss") {
                        set("jungle_potion", "found_rogues_purse")
                    }
                    target.replace("rogues_purse_cave_empty", ticks = TimeUnit.SECONDS.toTicks(60))
                    item("grimy_rogues_purse", "You find a herb.")
                } else {
                    item("grimy_rogues_purse", "You find a grimy herb but you have no place to store it in your inventory.")
                }
            }
        }

        objectOperate("Search", "pothole_cave_entrance") { (target) ->
            statement("You search the rocks... You find an entrance into some caves.")
            choice {
                option("Yes, I'll enter the cave.") {
                    statement("You decide to enter the cave. You climb down several steep rock faces into the cavern below.")
                    tele(2828, 9524, 0)
                }
                option("No thanks, Ill give it a miss.") {
                    statement("You decide to stay where you are!")
                }
            }
        }

        objectOperate("Climb", "jp_caverocksout") { (target) ->
            statement("You attempt to climb the rocks back out.")
            tele(2823, 3120, 0)
        }
    }
}
