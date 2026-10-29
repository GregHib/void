package content.quest.member.tree_gnome_village

import content.entity.player.AdventurersLogs
import content.entity.player.bank.ownsItem
import content.quest.quest
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

/** Dialogue calls these transitions only after the relevant conversation finishes. */
object TreeGnomeVillageProgress {
    const val QUEST = "tree_gnome_village"

    fun start(player: Player): Boolean {
        if (player.quest(QUEST) != "unstarted") {
            return false
        }
        player["tree_gnome_village_trackers"] = 0
        player["tree_gnome_village_coordinate"] = (1..4).random()
        player["tree_gnome_village_montai_met"] = false
        player["tree_gnome_village_briefed"] = false
        player["tree_gnome_village_ceremony"] = 0
        player["tree_gnome_village_commander_met"] = false
        advance(player, "started")
        return true
    }

    fun deliverLogs(player: Player): Boolean {
        if (player.quest(QUEST) != "started" || !player["tree_gnome_village_montai_met", false] || !player.inventory.remove("logs", 6)) {
            return false
        }
        advance(player, "logs_delivered")
        return true
    }

    fun brief(player: Player): Boolean {
        if (player.quest(QUEST) != "logs_delivered") {
            return false
        }
        player["tree_gnome_village_briefed"] = true
        player.refreshQuestJournal()
        return true
    }

    fun recordTracker(player: Player, tracker: Int): Boolean {
        require(tracker in 1..3)
        if (player.quest(QUEST) !in setOf("logs_delivered", "coordinates_obtained") || !player["tree_gnome_village_briefed", false]) {
            return false
        }
        val mask = player["tree_gnome_village_trackers", 0] or (1 shl (tracker - 1))
        player["tree_gnome_village_trackers"] = mask
        if (mask == 7) {
            advance(player, "coordinates_obtained")
        }
        return true
    }

    fun fire(player: Player, coordinate: Int): Boolean {
        if (player.quest(QUEST) != "coordinates_obtained" || player["tree_gnome_village_trackers", 0] != 7) {
            return false
        }
        val expected = player["tree_gnome_village_coordinate", 0]
        if (expected !in 1..4 || coordinate != expected) {
            return false
        }
        advance(player, "stronghold_open")
        return true
    }

    fun recoverOrb(player: Player): Boolean {
        if (player.quest(QUEST) !in setOf("stronghold_open", "orb_recovered") || player.ownsItem("orb_of_protection")) {
            return false
        }
        if (!player.inventory.add("orb_of_protection")) {
            return false
        }
        advance(player, "orb_recovered")
        return true
    }

    fun returnOrb(player: Player): Boolean {
        if (player.quest(QUEST) != "orb_recovered" || !player.inventory.remove("orb_of_protection")) {
            return false
        }
        advance(player, "hunt_warlord")
        return true
    }

    fun defeatWarlord(player: Player): Boolean {
        if (player["tree_gnome_village_ceremony", 0] != 0) {
            return false
        }
        if (player.quest(QUEST) !in setOf("hunt_warlord", "warlord_defeated", "orbs_recovered")) {
            return false
        }
        if (player.ownsItem("orbs_of_protection")) {
            return false
        }
        // Keep the defeated milestone if the inventory is full, so recovery can be retried.
        advance(player, "warlord_defeated")
        return recoverOrbs(player)
    }

    fun recoverOrbs(player: Player): Boolean {
        if (player.quest(QUEST) != "warlord_defeated" || player.ownsItem("orbs_of_protection")) {
            return false
        }
        if (!player.inventory.add("orbs_of_protection")) {
            return false
        }
        advance(player, "orbs_recovered")
        return true
    }

    fun complete(player: Player): Boolean {
        if (player.quest(QUEST) != "orbs_recovered" || player["tree_gnome_village_ceremony", 0] != 2 || !player.inventory.add("gnome_amulet")) {
            return false
        }
        AdventurersLogs.questCompleted(player, QUEST, points = 2)
        advance(player, "completed")
        player["quest_points"] = player["quest_points", 0] + 2
        player.exp(Skill.Attack, 11450.0)
        return true
    }

    fun beginCeremony(player: Player): Boolean {
        if (player.quest(QUEST) != "orbs_recovered") {
            return false
        }
        // A logout or interrupted ceremony can be resumed without handing in the orbs again.
        if (player["tree_gnome_village_ceremony", 0] == 1) {
            return true
        }
        if (player["tree_gnome_village_ceremony", 0] != 0 || !player.inventory.remove("orbs_of_protection")) {
            return false
        }
        player["tree_gnome_village_ceremony"] = 1
        return true
    }

    fun finishCeremony(player: Player) {
        if (player.quest(QUEST) == "orbs_recovered" && player["tree_gnome_village_ceremony", 0] == 1) {
            player["tree_gnome_village_ceremony"] = 2
            player.refreshQuestJournal()
        }
    }

    private fun advance(player: Player, stage: String) {
        player[QUEST] = stage
        player.refreshQuestJournal()
    }
}
