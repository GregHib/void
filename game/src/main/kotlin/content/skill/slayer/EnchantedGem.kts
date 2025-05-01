package content.skill.slayer

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.ChoiceBuilder
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.inv.inventoryItem
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.SlayerTaskDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.Action
import world.gregs.voidps.engine.queue.strongQueue

val slayerDefinitions: SlayerTaskDefinitions by inject()

playerSpawn { player ->
    player.sendVariable("slayer_count")
    player.sendVariable("slayer_target")
}

inventoryItem("Activate", "enchanted_gem") {
    player.strongQueue("enchanted_gem_activate") {
        val master = player.slayerMaster
        npc<Happy>(master, "Hello there ${player.name}, what can I help you with?")
        choice {
            howAmIDoing()
            whoAreYou()
            whereAreYou()
            anyTips()
            option<Talk>("That's all thanks.")
        }
    }
}

inventoryItem("Kills-left", "enchanted_gem") {
    if (player.slayerTask == "nothing") {
        player.message("") // TODO
    } else {
        player.message("Your current assignment is: ${player.slayerTask.lowercase()}; only ${player.slayerTaskRemaining} more to go.")
    }
}

fun ChoiceBuilder<Action<Player>>.howAmIDoing() {
    option<Quiz>("How am I doing so far?") {
        if (player.slayerTask == "nothing") {
            // TODO
        } else {
            npc<Happy>(player.slayerMaster, "You're currently assigned to kill ${player.slayerTask.toLowerSpaceCase()}; only ${player.slayerTaskRemaining} more to go. Your reward point tally is ${player.slayerPoints}.")
        }
        choice {
            whoAreYou()
            whereAreYou()
            anyTips()
            option<Talk>("That's all thanks.")
        }
    }
}

fun ChoiceBuilder<Action<Player>>.whoAreYou() {
    option<Quiz>("Who are you?") {
        npc<Talk>(player.slayerMaster, "My name's ${player.slayerMaster.toSentenceCase()}, I'm the Slayer Master best able to train you.")
        choice {
            howAmIDoing()
            whereAreYou()
            anyTips()
            option<Talk>("That's all thanks.")
        }
    }
}

fun ChoiceBuilder<Action<Player>>.whereAreYou() {
    option<Quiz>("Where are you?") {
        val location = when (player.slayerMaster) {
            "turael" -> "Burthorpe"
            "duradel" -> "Shilo Village"
            else -> "unknown"
        }
        npc<Quiz>("You'll find me in $location. I'll be here when you need a new task.")
        choice {
            howAmIDoing()
            whoAreYou()
            anyTips()
            option<Talk>("That's all thanks.")
        }
    }
}

fun ChoiceBuilder<Action<Player>>.anyTips() {
    option<Quiz>("Got any tips for me?") {
        val definition = slayerDefinitions.get(player.slayerMaster)[player.slayerTask]!!
        npc<Talk>(player.slayerMaster, definition.tip)
        choice {
            howAmIDoing()
            whoAreYou()
            whereAreYou()
            option<Talk>("That's all thanks.")
        }
    }
}