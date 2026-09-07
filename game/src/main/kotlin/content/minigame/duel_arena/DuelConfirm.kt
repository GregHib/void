package content.minigame.duel_arena

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.Inventory

class DuelConfirm : Script {

    init {
        interfaceOption("Accept", "stake_confirm:accept,duel_rules_confirm:accept") {
            val duel = duel ?: return@interfaceOption
            if (duel.stage != DuelStage.Confirm || !DuelRulesScreen.canAccept(duel)) {
                return@interfaceOption
            }
            DuelRulesScreen.accept(duel, this, duel.confirmScreen)
            if (duel.accepted.size == 2) {
                DuelFight.start(duel)
            }
        }
    }

    companion object {
        private const val BEFORE_LINES = 5
        private const val DURING_LINES = 11

        fun open(duel: Duel) {
            duel.stage = DuelStage.Confirm
            duel.accepted.clear()
            for (player in duel.players) {
                show(player, duel)
            }
        }

        private fun show(player: Player, duel: Duel) {
            val screen = duel.confirmScreen
            player.interfaces.remove(duel.screen)
            if (duel.staked) {
                player.interfaces.close("duel2_side")
                player.interfaces.open("inventory")
            }
            player.interfaces.open(screen)
            val before = before(duel)
            val during = during(duel)
            for (index in 0 until BEFORE_LINES) {
                player.interfaces.sendText(screen, "before_$index", before.getOrElse(index) { "" })
            }
            for (index in 0 until DURING_LINES) {
                player.interfaces.sendText(screen, "during_$index", during.getOrElse(index) { "" })
            }
            if (duel.staked) {
                player.interfaces.sendText(screen, "stake", itemsText(player.stake))
                player.interfaces.sendText(screen, "other_stake", itemsText(player.otherStake))
            }
            player.interfaces.sendText(screen, "status", "")
        }

        fun before(duel: Duel): List<String> {
            val lines = mutableListOf<String>()
            if (DuelRules.hasEquipmentRule(duel)) {
                lines.add("Some worn items will be taken off.")
            }
            if (duel.hasRule("no_drinks") || duel.hasRule("no_food")) {
                lines.add("Boosted stats will be restored.")
            }
            if (duel.hasRule("no_prayer")) {
                lines.add("Existing prayers will be stopped.")
            }
            if (lines.isEmpty()) {
                lines.add("Nothing will be changed.")
            }
            return lines
        }

        fun during(duel: Duel): List<String> {
            val lines = mutableListOf<String>()
            if (duel.hasRule("no_weapon") || duel.hasRule("no_shield")) {
                lines.add("You can't use 2H weapons such as bows.")
            }
            for (rule in DuelRules.general) {
                if (duel.hasRule(rule)) {
                    lines.add(DuelRules.info(rule))
                }
            }
            if (lines.isEmpty()) {
                lines.add("You will fight using normal combat.")
            }
            return lines
        }

        fun itemsText(inventory: Inventory): String {
            if (inventory.isEmpty()) {
                return "Absolutely nothing!"
            }
            return buildString {
                for (item in inventory.items) {
                    if (item.isEmpty()) {
                        continue
                    }
                    append(Colours.ORANGE.toTag())
                    append(item.def.name)
                    if (item.amount > 1) {
                        append(Colours.WHITE.toTag())
                        append(" x ")
                        append(Colours.YELLOW.toTag())
                        append(item.amount.toLong().toDigitGroupString())
                    }
                    append("<br>")
                }
            }
        }
    }
}
