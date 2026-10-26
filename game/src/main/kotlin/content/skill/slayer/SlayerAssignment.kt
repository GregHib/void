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

        interfaceOpened("slayer_rewards_assignment") { id ->
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
                message("You need a task in order to skip it.")
                return@interfaceOption
            }
            slayerPoints -= 30
            slayerTask = "nothing"
            slayerTaskRemaining = 0
            message("<green>Your assignment has now been cleared. You may freely choose another. $slayerPoints Slayer ${"point".plural(slayerPoints)} remaining.")
            refresh(this, "slayer_rewards_assignment")
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
                message("You need a task in order to block it.")
                return@interfaceOption
            }
            val slot = (0 until BLOCKED_TASK_SLOTS).firstOrNull { !contains("blocked_task_$it") }
            if (slot == null) {
                message("You don't have any free block slots.")
                return@interfaceOption
            }
            // One task can be blocked for every 50 quest points
            val required = (slot + 1) * 50
            if (get("quest_points", 0) < required) {
                message("<red>Sorry, you'll need at least $required Quest Points to buy this ability.")
                return@interfaceOption
            }
            set("blocked_task_$slot", slayerTask)
            slayerTask = "nothing"
            slayerTaskRemaining = 0
            slayerPoints -= 100
            refresh(this, "slayer_rewards_assignment")
        }

        interfaceOption("Cancel Removal", "slayer_rewards_assignment:cancel_*") {
            val slot = it.component.removePrefix("cancel_").toInt() - 1
            val task: String = get("blocked_task_$slot") ?: return@interfaceOption
            clear("blocked_task_$slot")
            message("<green>Removed ${task.toSentenceCase()} from my ignore list.")
            refresh(this, "slayer_rewards_assignment")
        }
    }

    fun refresh(player: Player, id: String) {
        val points = player.slayerPoints
        player.interfaces.sendText(id, "current_points", points.toString())
        player.interfaces.sendColour(id, "current_points", if (points == 0) Colours.RED else Colours.GOLD)
        var hasBlockedSlot = false
        for (i in 0 until BLOCKED_TASK_SLOTS) {
            if (!player.contains("blocked_task_$i")) {
                hasBlockedSlot = true
            }
            player.interfaces.sendText(id, "text_$i", player["blocked_task_$i", "nothing"].toSentenceCase())
        }
        val assignment = player.slayerTask
        if (assignment == "nothing") {
            player.interfaces.sendText(id, "reassign_text", "You must have an assignment to use this.")
            player.interfaces.sendText(id, "block_text", "You must have an assignment to use this.")
        } else {
            player.interfaces.sendText(id, "reassign_text", "Cancel task of ${assignment.toSentenceCase()}.")
            player.interfaces.sendText(id, "block_text", "Never assign ${assignment.toSentenceCase()} again.")
        }
        player.interfaces.sendColour(id, "reassign_text", if (points < 30) Colours.RED else Colours.ORANGE)
        player.interfaces.sendColour(id, "reassign_points", if (points < 30) Colours.RED else Colours.ORANGE)
        player.interfaces.sendColour(id, "block_text", if (points < 100 || !hasBlockedSlot) Colours.RED else Colours.ORANGE)
        player.interfaces.sendColour(id, "block_points", if (points < 100 || !hasBlockedSlot) Colours.RED else Colours.ORANGE)
    }
}
