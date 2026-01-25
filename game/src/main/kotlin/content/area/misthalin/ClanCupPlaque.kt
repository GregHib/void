package content.area.misthalin

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open

class ClanCupPlaque : Script {

    init {
        // https://www.youtube.com/watch?v=K-Ptq7ZxeWI
        objectOperate("Read", "*clan_cup_plaque*") {
            open("clan_cup_interface")
            interfaces.sendText("clan_cup_interface", "current_winners_button", "Current")
            interfaces.sendText("clan_cup_interface", "main_text", "Current Winners")
            interfaces.sendText("clan_cup_interface", "sub_text", "The victorious winners of the 2010 Jagex Clan Cup<br><br>Combat - Runescape Dinasty<br>Skilling - Divination<br>Combined - BasedIn2Minutes")
        }
        interfaceOption("Current-winners", "clan_cup_interface:current_winners_button") {
            interfaces.sendText("clan_cup_interface", "current_winners", "Current")
            interfaces.sendText("clan_cup_interface", "main_text", "Current Winners")
            interfaces.sendText("clan_cup_interface", "sub_text", "The victorious winners of the 2010 Jagex Clan Cup<br><br>Combat - Runescape Dinasty<br>Skilling - Divination<br>Combined - Basedin2minutes")
        }
        interfaceOption("Combat-winners", "clan_cup_interface:combat_winners_button") {
            interfaces.sendText("clan_cup_interface", "combat_winners", "Combat")
            interfaces.sendText("clan_cup_interface", "main_text", "Combat winners")
            interfaces.sendText("clan_cup_interface", "sub_text", "2010 - Runescape Dinasty<br>2009 - The Titans")
        }
        interfaceOption("Skilling-winners", "clan_cup_interface:skilling_winners_button") {
            interfaces.sendText("clan_cup_interface", "skilling_winners", "Skilling")
            interfaces.sendText("clan_cup_interface", "main_text", "Skilling Winners")
            interfaces.sendText("clan_cup_interface", "sub_text", "2010 - Divination<br> 2009 - Divination")
        }
        interfaceOption("Combined-winners", "clan_cup_interface:combined_winners_button") {
            interfaces.sendText("clan_cup_interface", "combined_winners", "Combined")
            interfaces.sendText("clan_cup_interface", "main_text", "Combined Winners")
            interfaces.sendText("clan_cup_interface", "sub_text", "2010 - BasedIn2Minutes<br> 2009 - Wicked Fury")
        }
    }
}
