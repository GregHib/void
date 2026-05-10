package content.area.wilderness.daemonheim

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class DungeonEntrance : Script {
    init {
        objectOperate("Climb-down", "daemonheim_dungeon_entrance") {
            item("ring_of_kinship", 400, "To join or create a party, you need a ring of kinship. You can get one form the dungeoneering tutor, on the right of the entrance to Daemonheim castle.")
            if (!DungeoneeringParty.inParty(this)) {
                choice("Would you like to start a party?") {
                    option("Yes.") {
                        DungeoneeringParty.start(this)
                    }
                    option("No.") {
                        message("You must be in a party to enter a dungeon.")
                    }
                }
            }
            // You are carrying items which are not permitted in Daemonheim.
            // Deposit all but: Ring of kinship.
        }
    }
}