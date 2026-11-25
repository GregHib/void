package content.quest.member.tree_gnome_village

import content.entity.combat.Combat
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.timer.Timer

class TreeGnomeVillageWarlord : Script {
    init {
        npcSpawn("khazard_warlord_underground_pass") {
            clear("warlord_challenger")
            clear("warlord_blocked_ticks")
            clear("warlord_blocked_target")
            softTimers.start("tree_gnome_village_warlord_patience")
        }
        huntPlayer("khazard_warlord_underground_pass", "tree_gnome_village_warlord") { player ->
            if (get("warlord_challenger", -1) == player.index && player.quest("tree_gnome_village") in setOf("hunt_warlord", "warlord_defeated")) {
                Combat.combat(this, player)
            }
        }
        npcTimerStart("tree_gnome_village_warlord_patience") { 1 }
        npcTimerTick("tree_gnome_village_warlord_patience") {
            checkPatience()
            Timer.CONTINUE
        }
    }

    private fun NPC.checkPatience() {
        val movement = mode as? CombatMovement
        val player = movement?.target as? Player ?: Players.indexed(get("warlord_challenger", -1))
        val encounter = player != null && player.quest("tree_gnome_village") in setOf("hunt_warlord", "warlord_defeated")
        if (dead || player == null || player.dead || player.tile.level != tile.level || (!encounter && movement == null) || hasClock("under_attack") || (movement ?: CombatMovement(this, player)).inAttackRange()) {
            clear("warlord_blocked_ticks")
            clear("warlord_blocked_target")
            return
        }
        if (get("warlord_blocked_target", -1) != player.index) {
            set("warlord_blocked_target", player.index)
            set("warlord_blocked_ticks", 0)
        }
        val ticks = get("warlord_blocked_ticks", 0) + 1
        // Approximately thirty seconds, retaining the prior encounter timing.
        // Complain once per blocked episode, rather than repeatedly while still stuck.
        set("warlord_blocked_ticks", ticks.coerceAtMost(51))
        if (ticks == 50) say("Bah, enough of you!")
    }
}
