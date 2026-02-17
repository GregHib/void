package content.entity.obj

import content.entity.player.bank.bank
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.*

class Mill : Script {

    init {
        playerSpawn {
            sendVariable("flour_bin")
        }

        objectOperate("Operate", "hopper_controls") { (target) ->
            if (get("flour_bin", 0) == 30) {
                message("The flour bin downstairs is full, I should empty it first.")
                return@objectOperate
            }
            anim("pull_hopper_controls")
            sound("lever")
            delay()
            target.replace("hopper_controls_pulled", ticks = 2)
            if (get("hopper_bin", 0) != 1) {
                message("You operate the empty hopper. Nothing interesting happens.")
                return@objectOperate
            }
            set("hopper_bin", 0)
            inc("flour_bin")
            if (get("flour_bin", 0) == 30) {
                message("The flour bin downstairs is now full.")
            } else {
                message("You operate the hopper. The grain slides down the chute.")
            }
        }

        itemOnObjectOperate("grain", "hopper") {
            if (quest("cooks_assistant") != "started") {
                anim("fill_hopper")
                inventory.remove("grain")
                set("hopper_bin", 1)
                message("You put the grain in the hopper. You should now pull the lever nearby to operate the hopper.")
                return@itemOnObjectOperate
            }
            if (get("cooks_assistant_talked_to_millie", 0) == 0) {
                player<Neutral>("Hmm. I should probably ask that lady downstairs how I can make extra fine flour.")
                return@itemOnObjectOperate
            }
            if (carriesItem("extra_fine_flour")) {
                message("It'd be best to take the extra fine flour you already have to the cook first.")
                return@itemOnObjectOperate
            }
            if (bank.contains("extra_fine_flour")) {
                message("It'd be best to take the extra fine flour you already have in your bank to the cook first.")
                return@itemOnObjectOperate
            }
            if (get("hopper_bin", 0) == 1) {
                message("There is already grain in the hopper.")
            } else {
                anim("fill_hopper")
                inventory.remove("grain")
                set("hopper_bin", 1)
                message("You put the grain in the hopper. You should now pull the lever nearby to operate the hopper.")
            }
        }

        objectOperate("Take-flour", "flour_bin") {
            if (!carriesItem("empty_pot")) {
                message("You need an empty pot to hold the flour in.")
                return@objectOperate
            }
            if (quest("cooks_assistant") == "started" && get("cooks_assistant_talked_to_millie", 0) == 1) {
                inventory.remove("empty_pot")
                if (carriesItem("extra_fine_flour") || bank.contains("extra_fine_flour")) {
                    inventory.add("pot_of_flour")
                    message("You fill a pot with flour from the bin.")
                } else {
                    inventory.add("extra_fine_flour")
                    message("You fill a pot with the extra fine flour from the bin.")
                }
                dec("flour_bin")
            } else {
                inventory.replace("empty_pot", "pot_of_flour")
                dec("flour_bin")
                message("You fill a pot with flour from the bin.")
            }
        }
    }
}
