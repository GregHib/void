package content.area.asgarnia.port_sarim

import content.entity.obj.ship.boatTravel
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class Seaman : Script {

    init {
        npcOperate("Talk-to", "seaman_lorris*,captain_tobias*,seaman_thresnor*") {
            npc<Quiz>("Do you want to go on a trip to Karamja?")
            npc<Neutral>("The trip will cost you 30 coins.")
            choice {
                option<Happy>("Yes please.") {
                    if (!inventory.remove("coins", 30)) {
                        player<Sad>("Oh dear, I don't seem to have enough money.")
                        return@option
                    }
                    travel("You pay 30 coins and board the ship.")
                }
                if (charmed) {
                    option("Or I could pay you nothing at all...") {
                        player<Shifty>("Or I could pay you nothing at all...")
                        npc<Quiz>("Mmmm, nothing at all, you say...")
                        npc<Happy>("Yes, why not - jump aboard then.")
                        travel("You board the ship.")
                    }
                }
                option<Neutral>("No, thank you.")
            }
        }

        npcOperate("Pay-fare", "seaman_lorris*,captain_tobias*,seaman_thresnor*") {
            if (!questCompleted("pirates_treasure")) {
                return@npcOperate message("You may only use the Pay-fare option after completing Pirate's Treasure.")
            }
            if (!inventory.remove("coins", 30)) {
                message("You do not have enough money for that.")
                return@npcOperate
            }
            travel("You pay 30 coins and board the ship.")
        }
    }

    private suspend fun Player.travel(boarding: String) {
        message(boarding)
        boatTravel("port_sarim_to_karamja", 7, Tile(2956, 3143, 1))
        statement("The ship arrives at Karamja.")
    }

    private val Player.charmed: Boolean
        get() = equipped(EquipSlot.Ring).id == "ring_of_charos_a"
}

// TODO dragon slayer dialog
// I'd rather go to Crandor Isle.
// No I need to stay alive, I have a wife and family to support.

// TODO diary cost
