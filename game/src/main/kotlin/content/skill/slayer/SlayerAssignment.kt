package content.skill.slayer

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class SlayerAssignment : Script {

    init {
        interfaceOption("Learn", "slayer_rewards_assignment:learn") {
            open("slayer_rewards_learn")
        }

        interfaceOption("Buy", "slayer_rewards_assignment:buy") {
            open("slayer_rewards")
        }

        interfaceOpen("slayer_rewards_assignment") { id ->
            refresh(this, id)
        }

        interfaceOption(id = "slayer_rewards_assignment:reassign_*") {
            if (!it.option.startsWith("Reassign")) {
                return@interfaceOption
            }
            if (slayerPoints < 30) {
                message("Sorry. That would cost 30 and you only have $slayerPoints Slayer ${"Point".plural(slayerPoints)}.")
                return@interfaceOption
            }
            if (slayerTask == "nothing") {
                message("You need a task in order to skip it.") // TODO proper message
                return@interfaceOption
            }
            slayerPoints -= 30
            slayerTask = "nothing"
            //    npc<Happy>(player["slayer_npc", ""], "") TODO proper message and save npc id on interface open
        }

        interfaceOption(id = "slayer_rewards_assignment:block_*") {
            if (!it.option.startsWith("Permanently")) {
                return@interfaceOption
            }
            if (slayerPoints < 100) {
                message("Sorry. That would cost 100 and you only have $slayerPoints Slayer ${"Point".plural(slayerPoints)}.")
                return@interfaceOption
            }
            if (slayerTask == "nothing") {
                message("You need a task in order to block it.") // TODO proper message
                return@interfaceOption
            }
            var blocked = false
            for (i in 0 until 5) {
                if (!contains("blocked_task_$i")) {
                    set("blocked_task_$i", slayerTask)
                    slayerTask = "nothing"
                    slayerPoints -= 100
                    blocked = true
                    break
                }
            }

            if (!blocked) {
                message("You don't have any free block slots.") // TODO proper message
            }
        }
    }

    fun refresh(player: Player, id: String) {
        val points = player.slayerPoints
        player.interfaces.sendText(id, "current_points", points.toString())
        player.interfaces.sendColour(id, "current_points", if (points == 0) Colours.RED else Colours.GOLD)
        var hasBlockedSlot = false
        for (i in 0 until 5) {
            if (!player.contains("blocked_task_$i")) {
                hasBlockedSlot = true
            }
            player.interfaces.sendText(id, "text_$i", player["blocked_task_$i", "nothing"].toSentenceCase())
        }
        val assignment = player["slayer_assignment", ""]
        if (assignment.isEmpty()) {
            player.interfaces.sendText(id, "reassign_text", "You must have an assignment to use this.")
            player.interfaces.sendText(id, "block_text", "You must have an assignment to use this.")
        } else {
            player.interfaces.sendText(id, "reassign_text", "Cancel task of $assignment.")
            player.interfaces.sendText(id, "block_text", "Never assign $assignment again.")
        }
        player.interfaces.sendColour(id, "reassign_text", if (points < 30) Colours.RED else Colours.ORANGE)
        player.interfaces.sendColour(id, "reassign_points", if (points < 30) Colours.RED else Colours.ORANGE)
        player.interfaces.sendColour(id, "block_text", if (points < 100 || !hasBlockedSlot) Colours.RED else Colours.ORANGE)
        player.interfaces.sendColour(id, "block_points", if (points < 100 || !hasBlockedSlot) Colours.RED else Colours.ORANGE)
    }
}
