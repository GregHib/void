package content.quest.member.myreque.nature_spirit

import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class DruidPouch : Script {
    init {
        itemOption("Fill", "druid_pouch,druid_pouch_2") {
            fillPouch()
        }
    }

    private fun Player.fillPouch() {
        val fungi = inventory.count("mort_myre_fungus")
        val pear = inventory.count("mort_myre_pear")
        val stem = inventory.count("mort_myre_stem")
        if (fungi + pear + stem < 3) {
            message("You need at least 3 of nature's harvests to add to your druid pouch.")
            return
        }
        if (!inventory.contains("druid_pouch_2")) {
            inventory.remove("druid_pouch", 1)
        }
        var added = 0
        var charges = 0
        while (inventory.contains("mort_myre_pear") && added < 3) {
            inventory.remove("mort_myre_pear", 1)
            inventory.add("druid_pouch_2", 3)
            charges += 3
            added++
        }
        while (inventory.contains("mort_myre_stem") && added < 3) {
            inventory.remove("mort_myre_stem", 1)
            inventory.add("druid_pouch_2", 2)
            charges += 2
            added++
        }
        while (added < 3 && inventory.contains("mort_myre_fungus")) {
            inventory.remove("mort_myre_fungus", 1)
            inventory.add("druid_pouch_2", 1)
            charges += 1
            added++
        }
        if (questStage("nature_spirit") in 75..85) {
            set("nature_spirit", "ghast_3")
        }
        message("You add $charges nature's ${if (charges == 1) "harvest" else "harvests"} to your druid pouch.")
    }
}
