package content.minigame.duel_arena

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.inventory

class DuelRulesScreen : Script {

    init {
        val toggles = SCREENS.flatMap { screen ->
            DuelRules.general.flatMap { listOf("$screen:$it", "$screen:${it}_box") } + DuelRules.equipment.keys.map { "$screen:$it" }
        }
        interfaceOption("Ok", toggles.joinToString(",")) {
            val duel = duel ?: return@interfaceOption
            if (duel.stage != DuelStage.Rules) {
                return@interfaceOption
            }
            toggle(duel, it.interfaceComponent.substringAfter(":").removeSuffix("_box"))
        }

        interfaceOption("Accept", "stake:accept,duel_confirm:accept") {
            val duel = duel ?: return@interfaceOption
            if (duel.stage != DuelStage.Rules || !canAccept(duel)) {
                return@interfaceOption
            }
            accept(duel, this, duel.screen)
            if (duel.accepted.size == 2) {
                DuelConfirm.open(duel)
            }
        }

        interfaceOption("Decline", "stake:decline,duel_confirm:decline,stake_confirm:decline,duel_rules_confirm:decline") {
            val duel = duel ?: return@interfaceOption
            DuelEnd.decline(duel, this)
        }

        interfaceOption("Close", "stake:close,duel_confirm:close,stake_confirm:close,duel_rules_confirm:close") {
            val duel = duel ?: return@interfaceOption
            DuelEnd.decline(duel, this)
        }

        // Walking away or any other close during the setup stages declines the duel.
        // Moving between screens also closes the previous one, so only the current stage's screen counts.
        interfaceClosed("stake,duel_confirm,stake_confirm,duel_rules_confirm") { id ->
            val duel = duel ?: return@interfaceClosed
            val current = when (duel.stage) {
                DuelStage.Rules -> duel.screen
                DuelStage.Confirm -> duel.confirmScreen
                else -> return@interfaceClosed
            }
            if (id == current) {
                DuelEnd.decline(duel, this)
            }
        }
    }

    fun Player.toggle(duel: Duel, rule: String) {
        val opponent = duel.opponent(this)
        if (duel.hasRule(rule)) {
            duel.rules.remove(rule)
            sync(duel, rule)
            resetAccept(duel)
            return
        }
        if (rule in DuelRules.styles && DuelRules.styles.count { duel.hasRule(it) } == 2) {
            message("You can't have No Ranged, No Melee AND No Magic, how would you fight?")
            return
        }
        if (rule == "no_movement" && duel.hasRule("obstacles")) {
            duel.rules.remove("obstacles")
            sync(duel, "obstacles")
            message("You can't have No Movement in an area with obstacles.")
            opponent.message("You can't have No Movement in an area with obstacles.")
        }
        if (rule == "obstacles" && duel.hasRule("no_movement")) {
            duel.rules.remove("no_movement")
            sync(duel, "no_movement")
            message("You can't have obstacles if you want No Movement.")
            opponent.message("You can't have obstacles if you want No Movement.")
        }
        if (rule == "no_weapon" || rule == "no_shield") {
            message("Beware: You won't be able to use two-handed weapons such as bows.")
            opponent.message("Beware: You won't be able to use two-handed weapons such as bows.")
        }
        duel.rules.add(rule)
        sync(duel, rule)
        resetAccept(duel)
    }

    /**
     * Both players are checked so either side sees why the duel can't start
     */
    fun canAccept(duel: Duel): Boolean {
        for (player in duel.players) {
            val opponent = duel.opponent(player)
            if (duel.hasRule("fun_weapons") && !DuelRules.hasFunWeapon(player)) {
                player.message("Fun Weapons is selected but you don't have a 'fun weapon'.")
                opponent.message("Fun Weapons is selected but your opponent does not have a 'fun weapon'.")
                return false
            }
            var needed = DuelRules.removedEquipment(player, duel).size
            if (duel.staked) {
                needed += player.stake.count + opponent.stake.count
            }
            if (needed > player.inventory.spaces) {
                player.message("You do not have enough space for the items removed and/or the stake.")
                opponent.message("Your opponent does not have enough space for the items removed and/or the stake.")
                return false
            }
        }
        return true
    }

    companion object {
        val SCREENS = listOf("stake", "duel_confirm")

        fun sync(duel: Duel, rule: String) {
            for (player in duel.players) {
                player["duel_$rule"] = duel.hasRule(rule)
            }
        }

        fun accept(duel: Duel, player: Player, screen: String) {
            val opponent = duel.opponent(player)
            duel.accepted.add(player.name)
            player.interfaces.sendText(screen, "status", "Waiting for other player...")
            opponent.interfaces.sendText(screen, "status", "Other player has accepted.")
        }

        fun resetAccept(duel: Duel) {
            if (duel.accepted.isEmpty()) {
                return
            }
            duel.accepted.clear()
            for (player in duel.players) {
                player.interfaces.sendText(duel.screen, "status", "")
            }
        }
    }
}
