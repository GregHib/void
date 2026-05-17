package content.area.wilderness.daemonheim

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class DungeonEntrance : Script {
    init {
        objectOperate("Climb-down", "daemonheim_dungeon_entrance") {
            if (!ownsItem("ring_of_kinship")) {
                item("ring_of_kinship", 300, "To join or create a party, you need a ring of kinship. You can get one form the dungeoneering tutor, on the right of the entrance to Daemonheim castle.")
                return@objectOperate
            }
            if (!DungeoneeringParty.inParty(this)) {
                choice("Would you like to start a party?") {
                    option("Yes.") {
                        DungeoneeringParty.setLeader(this)
                    }
                    option("No.") {
                        message("You must be in a party to enter a dungeon.")
                        return@option
                    }
                }
            }

            if (this != dungeonLeader) {
                message("<red_orange>Only the party leader can start a dungeon.")
                return@objectOperate
            }

            message("<red_orange>Not yet implemented.")
            return@objectOperate
//            statement("You have just entered a dungeon. In the starting room, you'll find a smuggler to trade with, and some starting supplies in your inventory and about the room. If you want to leave, there is a ladder that will take you back to the surface. For more information speak to the smuggler.")
//            item("ring_of_kinship", 300, "You have unlocked more features and opportunities within Daemonheim. You can now reach complexity level 2.")

            // You are carrying items which are not permitted in Daemonheim.
            // Deposit all but: Ring of kinship.
            message("")
            message("- Welcome to Daemonheim -")
            message("Floor <purple>1    <black>Complexity <purple>1")
            message("Dungeon Size: <purple>Small")
            message("Party Size:Difficulty <purple>2:2")
            message("<purple>Guide Mode ON")
            message("")
        }
    }
}
