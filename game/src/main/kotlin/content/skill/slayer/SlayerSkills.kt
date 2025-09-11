package content.skill.slayer

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script

@Script
class SlayerSkills {

    init {
        interfaceOption("Assignment", "assignment", "slayer_rewards_learn") {
            player.open("slayer_rewards_assignment")
        }

        interfaceOption("Buy", "buy", "slayer_rewards_learn") {
            player.open("slayer_rewards")
        }

        interfaceOpen("slayer_rewards_learn") { player ->
            refreshText(player, id)
        }

        interfaceOption("Buy", "broader_fletching", "slayer_rewards_learn") {
            purchase(player, component, 300, "The secret is yours. You can now flech broad bolts and arrows.")
        }

        interfaceOption("Buy", "aquanites", "slayer_rewards_learn") {
            purchase(player, component, 50, "Kuradel will now assign Aquanites, provided you have the slayer level required.")
        }

        interfaceOption("Buy", "ring_bling", "slayer_rewards_learn") {
            purchase(player, component, 300, "The secret is yours. You can now create rings of slayer by crafting together a gold bar and enchanted gem.")
        }

        interfaceOption("Buy", "killing_blow", "slayer_rewards_learn") {
            purchase(player, component, 400, "The secret is yours. You can now finish off Gargoyles, Rockslugs, Desert Lizards and Zygomites more quickly.")
        }

        interfaceOption("Buy", "malevolent_masquerade", "slayer_rewards_learn") {
            purchase(player, component, 400, "The secret is yours. You can now combine a black mask, face mask, spiny helm, nosepeg and earmuffs into one useful item.")
        }

        interfaceOption("Buy", "strike_wyrms", "slayer_rewards_learn") {
            purchase(player, component, 2000, "The secret is yours. You have learned how kill Ice Strykewyrms in a different way, without the need for a fire cape.")
        }
    }

    fun refreshText(player: Player, id: String) {
        val points = player.slayerPoints
        player.interfaces.sendText(id, "current_points", points.toString())
        player.interfaces.sendColour(id, "current_points", if (points == 0) Colours.RED else Colours.GOLD)
        updateUnlock(player, id, "broader_fletching", 300)
        updateUnlock(player, id, "aquanites", 50)
        updateUnlock(player, id, "ring_bling", 300)
        updateUnlock(player, id, "killing_blow", 400)
        updateUnlock(player, id, "malevolent_masquerade", 400)
        updateUnlock(player, id, "strike_wyrms", 2000)
    }

    fun updateUnlock(player: Player, id: String, key: String, points: Int) {
        val unlocked = player[key, false]
        if (unlocked) {
            player.interfaces.sendText(id, "${key}_text", "Already unlocked.")
            player.interfaces.sendVisibility(id, key, false)
            player.interfaces.sendVisibility(id, "${key}_points", false)
        } else {
            player.interfaces.sendColour(
                id,
                "${key}_points",
                if (player.slayerPoints < points) Colours.RED else Colours.ORANGE,
            )
        }
    }

    fun purchase(player: Player, key: String, points: Int, message: String) {
        if (player[key, false]) {
            return
        }
        if (player.slayerPoints < points) {
            player.message("Sorry. That would cost $points and you only have ${player.slayerPoints} Slayer ${"Point".plural(player.slayerPoints)}.")
        } else {
            player.slayerPoints -= points
            player[key] = true
            refreshText(player, "slayer_rewards_learn")
            player.message(message)
        }
    }

    // player.message("You need a nosepeg, face mask, earmuffs, spiny helmet and an uncharged black\nmask in your inventory in order to construct a fully-enhanced Slayer helmet.")
}
