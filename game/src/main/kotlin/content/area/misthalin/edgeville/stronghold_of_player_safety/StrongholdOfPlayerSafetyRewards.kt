package content.area.misthalin.edgeville.stronghold_of_player_safety

import content.entity.player.bank.bank
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.skillLamp
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class StrongholdOfPlayerSafetyRewards : Script {

    init {
        playerSpawn {
            sendVariable("stronghold_of_player_safety_chest")
        }

        objectOperate("Open", "stronghold_of_player_safety_treasure_chest_closed") { (target) ->
            if (inventory.isFull()) {
                // https://youtu.be/eSZY9zdBAwg?si=UlMwKzgDA51IZ7TF&t=203
                statement("You don't have any room in your inventory for the treasure.")
                message("You don't have any room in your inventory for the treasure.")
                return@objectOperate
            }
            target.replace("stronghold_of_player_safety_treasure_chest_opened")
            set("stronghold_of_player_safety_chest", true)
            inventory.add("coins", 10000)
            inventory.add("safety_gloves")
            inventory.add("antique_lamp_misthalin_training_centre_of_excellence")
            inventory.add("antique_lamp_misthalin_training_centre_of_excellence_2")
            set("unlocked_emote_safety_first", true)
            item("safety_gloves", 400, "You open the chest to find a large pile of gold, along with a pair of safety gloves and two antique lamps. Also in the chest is the secret of the 'Safety First' emote.")
        }

        itemOption("Rub", "antique_lamp_misthalin_training_centre_of_excellence*") { (item, slot) ->
            val skill = skillLamp()
            if (inventory.remove(slot, item.id)) {
                exp(skill, 500.0)
                statement("<blue>Your wish has been granted!<br><black>You have been awarded 500 ${skill.name} experience!")
            }
        }
        objectOperate("Search", "stronghold_of_player_safety_treasure_chest_opened") {
            if (get("unlocked_emote_safety_first", false)) {
                if (bank.contains("safety_gloves") || (inventory.contains("safety_gloves") || (holdsItem("safety_gloves")))) {
                    statement("The chest appears to be empty.")
                    message("The chest is already open and empty.")
                } else {
                    if (inventory.isFull()) {
                        message("There are some gloves in the chest, but you have no space for them in your inventory.")
                        return@objectOperate
                    }
                    inventory.add("safety_gloves")
                    message("You find another pair of gloves in the chest.")
                }
            }
        }
    }
}
