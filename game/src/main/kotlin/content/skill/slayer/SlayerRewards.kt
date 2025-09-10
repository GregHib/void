package content.skill.slayer

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.Transaction
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.event.Script
@Script
class SlayerRewards {

    init {
        interfaceOption("Learn", "learn", "slayer_rewards") {
            player.open("slayer_rewards_learn")
        }

        interfaceOption("Assignments", "assignment", "slayer_rewards") {
            player.open("slayer_rewards_assignment")
        }

        interfaceOpen("slayer_rewards") { player ->
            refreshText(player, id)
        }

        interfaceOption("Buy XP", "buy_xp_*", "slayer_rewards") {
            if (player.slayerPoints < 400) {
                player.message("Sorry. That would cost 400 and you only have ${player.slayerPoints} Slayer ${"Point".plural(player.slayerPoints)}.")
            } else if (player.inventory.add("ring_of_slaying_8")) {
                player.slayerPoints -= 400
                player.exp(Skill.Slayer, 10_000.0)
                // TODO message
            }
        }

        interfaceOption("Buy Ring", "buy_ring_*", "slayer_rewards") {
            buy(player, 75, "Here are your ring. Use it wisely.") {
                // TODO proper message
                add("ring_of_slaying_8")
            }
        }

        interfaceOption("Buy Runes", "buy_runes_*", "slayer_rewards") {
            buy(player, 35, "Here are your runes. Use them wisely.") {
                add("death_rune", 250)
                add("mind_rune", 1000)
            }
        }

        interfaceOption("Buy Bolts", "buy_bolts_*", "slayer_rewards") {
            buy(player, 35, "Here are your bolts. Use them wisely.") {
                // TODO proper message
                add("broad_tipped_bolts", 250)
            }
        }

        interfaceOption("Buy Arrows", "buy_arrows_*", "slayer_rewards") {
            buy(player, 35, "Here are your arrows. Use them wisely.") {
                // TODO proper message
                add("broad_arrow", 250)
            }
        }

    }

    fun refreshText(player: Player, id: String) {
        val points = player.slayerPoints
        player.interfaces.sendText(id, "current_points", points.toString())
        player.interfaces.sendColour(id, "current_points", if (points == 0) Colours.RED else Colours.GOLD)
        player.interfaces.sendColour(id, "buy_xp_text", if (points < 400) Colours.RED else Colours.ORANGE)
        player.interfaces.sendColour(id, "buy_xp_points", if (points < 400) Colours.RED else Colours.ORANGE)
        player.interfaces.sendColour(id, "buy_ring_text", if (points < 75) Colours.RED else Colours.ORANGE)
        player.interfaces.sendColour(id, "buy_ring_points", if (points < 75) Colours.RED else Colours.ORANGE)
        val colour = if (points < 35) Colours.RED else Colours.ORANGE
        player.interfaces.sendColour(id, "buy_runes_text", colour)
        player.interfaces.sendColour(id, "buy_runes_points", colour)
        player.interfaces.sendColour(id, "buy_bolts_text", colour)
        player.interfaces.sendColour(id, "buy_bolts_points", colour)
        player.interfaces.sendColour(id, "buy_arrows_text", colour)
        player.interfaces.sendColour(id, "buy_arrows_points", colour)
    }
    
    fun buy(player: Player, points: Int, message: String, transaction: Transaction.() -> Unit) {
        if (player.slayerPoints < points) {
            player.message("Sorry. That would cost $points and you only have ${player.slayerPoints} Slayer ${"Point".plural(player.slayerPoints)}.")
            return
        }
        if (player.inventory.transaction(transaction)) {
            player.slayerPoints -= points
            player.message(message)
        }
    }
}
