package content.minigame.duel_arena

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.req.hasRequest
import world.gregs.voidps.engine.entity.character.player.req.removeRequest
import world.gregs.voidps.engine.entity.character.player.req.request
import world.gregs.voidps.engine.inv.clear

class DuelRequest : Script {

    init {
        playerOperate("Challenge") { (target) ->
            if (duel != null || tile !in Areas["duel_arena_walkways"]) {
                return@playerOperate
            }
            if (busy(target)) {
                message("The other player is busy.", ChatType.Trade)
                return@playerOperate
            }
            // Accepting a pending challenge skips the type selection
            for (type in TYPES) {
                if (target.hasRequest(this, type)) {
                    send(target, type)
                    return@playerOperate
                }
            }
            set("duel_target", target)
            set("duel_type", FRIENDLY)
            open("duel_request")
        }

        interfaceOption("Select", "duel_request:friendly,duel_request:friendly_box") {
            set("duel_type", FRIENDLY)
        }

        interfaceOption("Select", "duel_request:staked,duel_request:staked_box") {
            set("duel_type", STAKED)
        }

        interfaceOption("Next-Screen", "duel_request:challenge") {
            val target: Player = get("duel_target") ?: return@interfaceOption
            val type = if (get("duel_type", FRIENDLY) == STAKED) "duel_staked" else "duel_friendly"
            closeMenu()
            send(target, type)
        }

        interfaceOption("Close", "duel_request:close") {
            closeMenu()
        }

        interfaceClosed("duel_request") {
            clear("duel_target")
        }
    }

    fun Player.send(target: Player, type: String) {
        if (duel != null || tile !in Areas["duel_arena_walkways"] || target.tile !in Areas["duel_arena_walkways"]) {
            return
        }
        if (busy(target)) {
            message("The other player is busy.", ChatType.Trade)
            return
        }
        val staked = type == "duel_staked"
        message("Sending duel offer...", ChatType.Trade)
        if (!target.hasRequest(this, type)) {
            target.message("wishes to duel with you (${if (staked) "stake" else "friendly"}).", ChatType.ChallengeDuel, name = name)
        }
        request(target, type) { requester, acceptor ->
            start(requester, acceptor, staked)
        }
    }

    fun busy(target: Player): Boolean = target.duel != null || target.menu != null

    fun start(requester: Player, acceptor: Player, staked: Boolean) {
        val duel = Duel(requester, acceptor, staked)
        for (player in duel.players) {
            val opponent = duel.opponent(player)
            for (type in TYPES) {
                player.removeRequest(opponent, type)
            }
            player["duel"] = duel
            openRules(player, duel)
        }
    }

    fun openRules(player: Player, duel: Duel) {
        val opponent = duel.opponent(player)
        player.closeMenu()
        for (rule in DuelRules.all) {
            player.clear("duel_$rule")
        }
        player.stake.clear()
        player.otherStake.clear()
        val screen = duel.screen
        player.interfaces.apply {
            open(screen)
            sendText(screen, "name", opponent.name)
            sendText(screen, "combat_level", opponent.combatLevel.toString())
            sendText(screen, "status", "")
        }
        if (!duel.staked) {
            return
        }
        player.interfaces.apply {
            close("inventory")
            open("duel2_side")
            sendVisibility(screen, "max_stake", false)
            sendVisibility(screen, "max_stake_label", false)
            sendVisibility(screen, "other_max_stake", false)
            sendVisibility(screen, "other_max_stake_label", false)
        }
        player.interfaceOptions.apply {
            send("duel2_side", "offer")
            unlockAll("duel2_side", "offer", 0 until 28)
            unlockAll(screen, "offer", 0 until 28)
            unlockAll(screen, "other_offer", 0 until 28)
        }
    }

    companion object {
        const val FRIENDLY = 1
        const val STAKED = 2
        val TYPES = listOf("duel_friendly", "duel_staked")
    }
}
