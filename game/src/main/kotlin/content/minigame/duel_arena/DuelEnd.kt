package content.minigame.duel_arena

import com.github.michaelbull.logging.InlineLogger
import content.entity.combat.attackers
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import content.minigame.duel_arena.DuelArena.Companion.returnItems
import content.skill.prayer.getActivePrayerVarKey
import content.skill.summoning.dismissFamiliar
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearHinted
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.req.removeRequest
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.moveAll
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.Tile

class DuelEnd : Script {

    enum class Result {
        Death,
        Forfeit,
        Disconnect,
    }

    init {
        playerDeath {
            val duel = duel
            if (duel == null || !duel.active) {
                // Covers both players dying on the same tick: the arena is always a safe death
                if (Areas.tagged("duel_arena").any { tile in it.area }) {
                    it.dropItems = false
                    it.teleport = hospital(this)
                }
                return@playerDeath
            }
            it.dropItems = false
            it.teleport = hospital(this)
            finish(duel, winner = duel.opponent(this), loser = this, Result.Death)
        }

        playerDespawn {
            val duel = duel ?: return@playerDespawn
            when (duel.stage) {
                DuelStage.Rules, DuelStage.Confirm -> decline(duel, this)
                DuelStage.Countdown, DuelStage.Fighting -> finish(duel, winner = duel.opponent(this), loser = this, Result.Disconnect)
                DuelStage.Finished -> {}
            }
        }

        interfaceOption("Claim", "stake_victory:claim,duel_victory:claim") {
            closeMenu()
        }

        interfaceOption("Close", "stake_victory:close,duel_victory:close") {
            closeMenu()
        }

        interfaceClosed("stake_victory") {
            returnItems(winnings)
            DuelArena.save(this)
        }
    }

    companion object {
        private val logger = InlineLogger()
        private const val DEATH_TICKS = 5
        private val hospitalTile = Tile(3369, 3270)

        fun hospital(player: Player): Tile = Areas["duel_arena_hospital"].random(player) ?: hospitalTile

        fun finish(duel: Duel, winner: Player, loser: Player, result: Result) {
            if (duel.stage == DuelStage.Finished) {
                return
            }
            duel.stage = DuelStage.Finished
            for (player in duel.players) {
                cleanup(player)
            }
            if (duel.staked) {
                transferStakes(winner, loser)
            }
            DuelScoreboard.add(winner, loser)
            AuditLog.event(winner, "won_duel", loser, result.name)
            loser.message("Oh dear, it seems you have lost to ${winner.name}.")
            winner.message("Congratulations! You easily defeated ${loser.name}.")
            when (result) {
                // PlayerDeath heals and teleports the loser once the death animation ends
                Result.Death -> World.queue("duel_${duel.id}_victory", DEATH_TICKS) {
                    victory(winner, loser, duel)
                }
                Result.Forfeit, Result.Disconnect -> {
                    restore(loser)
                    loser.tele(hospital(loser))
                    victory(winner, loser, duel)
                }
            }
        }

        fun decline(duel: Duel, player: Player) {
            if (duel.stage == DuelStage.Finished) {
                return
            }
            duel.stage = DuelStage.Finished
            val opponent = duel.opponent(player)
            for (it in duel.players) {
                val other = duel.opponent(it)
                it.clear("duel")
                for (type in DuelRequest.TYPES) {
                    it.removeRequest(other, type)
                }
                for (rule in DuelRules.all) {
                    it.clear("duel_$rule")
                }
                it.closeMenu()
                it.interfaces.close("duel2_side")
                it.interfaces.open("inventory")
                it.otherStake.clear()
                it.returnItems(it.stake)
            }
            if (duel.staked) {
                DuelArena.save(player, opponent)
            }
            opponent.message("Other player declined ${if (duel.staked) "stake and " else ""}duel options.", ChatType.Trade)
        }

        private fun cleanup(player: Player) {
            player.clear("duel")
            player.clear("in_pvp")
            player.stop("movement_delay")
            player.stop("under_attack")
            player.clear("no_food_message")
            player.clear("no_drinks_message")
            player.clear("no_prayer_message")
            player.clear("no_summoning_message")
            player.clear("no_movement_message")
            player.clear("blocked_equip_slots")
            player.clear("blocked_equip_message")
            for (rule in DuelRules.all) {
                player.clear("duel_$rule")
            }
            player.attackers.clear()
            player.clearHinted()
            player.options.remove("Attack")
            player.options.set(DuelArena.CHALLENGE_SLOT, "Challenge")
            player.dismissFamiliar()
            if (player.mode != EmptyMode) {
                player.mode = EmptyMode
            }
        }

        private fun restore(player: Player) {
            player.levels.clear()
            player.runEnergy = MAX_RUN_ENERGY
            player.clear(player.getActivePrayerVarKey())
        }

        private fun transferStakes(winner: Player, loser: Player) {
            val lost = loser.stake.items.filter { it.isNotEmpty() }
            AuditLog.event(loser, "lost_stake", winner, *lost.toTypedArray())
            // Two transactions: one target can't be linked from two sources at once
            for (stake in listOf(loser.stake, winner.stake)) {
                if (stake.moveAll(winner.winnings)) {
                    continue
                }
                logger.warn { "Duel stake transfer failed ${winner.name} ${loser.name} ${stake.items.toList()}" }
                winner.returnItems(stake)
            }
            for (player in listOf(winner, loser)) {
                player.otherStake.clear()
            }
            DuelArena.save(winner, loser)
        }

        private fun victory(winner: Player, loser: Player, duel: Duel) {
            if (winner["logged_out", false]) {
                return
            }
            restore(winner)
            winner.tele(hospital(winner))
            winner.jingle("duel_arena_victory")
            val screen = if (duel.staked) "stake_victory" else "duel_victory"
            // The name component is filled from this string by its load script
            winner["duel_victory_name"] = loser.name
            winner.open(screen)
            winner.interfaces.sendText(screen, "combat_level", loser.combatLevel.toString())
            if (!duel.staked) {
                return
            }
            winner.interfaceOptions.send(screen, "winnings")
            winner.interfaceOptions.unlockAll(screen, "winnings", 0 until 28)
            winner.sendInventory("duelwinnings")
        }
    }
}
