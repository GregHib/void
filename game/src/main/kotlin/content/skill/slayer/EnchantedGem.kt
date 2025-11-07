package content.skill.slayer

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.SlayerTaskDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.strongQueue

class EnchantedGem : Script {

    val slayerDefinitions: SlayerTaskDefinitions by inject()

    init {
        playerSpawn {
            sendVariable("slayer_count")
            sendVariable("slayer_target")
        }

        itemOption("Activate", "enchanted_gem") {
            strongQueue("enchanted_gem_activate") {
                val master = slayerMaster
                npc<Happy>(master, "Hello there $name, what can I help you with?")
                choice {
                    howAmIDoing()
                    whoAreYou()
                    whereAreYou()
                    anyTips()
                    option<Talk>("That's all thanks.")
                }
            }
        }

        itemOption("Kills-left", "enchanted_gem") {
            if (slayerTask == "nothing") {
                message("") // TODO
            } else {
                message("Your current assignment is: ${slayerTask.lowercase()}; only $slayerTaskRemaining more to go.")
            }
        }
    }

    fun ChoiceOption.howAmIDoing() {
        option<Quiz>("How am I doing so far?") {
            if (slayerTask == "nothing") {
                // TODO
            } else {
                npc<Happy>(slayerMaster, "You're currently assigned to kill ${slayerTask.toLowerSpaceCase()}; only $slayerTaskRemaining more to go. Your reward point tally is $slayerPoints.")
            }
            choice {
                whoAreYou()
                whereAreYou()
                anyTips()
                option<Talk>("That's all thanks.")
            }
        }
    }

    fun ChoiceOption.whoAreYou() {
        option<Quiz>("Who are you?") {
            npc<Talk>(slayerMaster, "My name's ${slayerMaster.toSentenceCase()}, I'm the Slayer Master best able to train you.")
            choice {
                howAmIDoing()
                whereAreYou()
                anyTips()
                option<Talk>("That's all thanks.")
            }
        }
    }

    fun ChoiceOption.whereAreYou() {
        option<Quiz>("Where are you?") {
            val location = when (slayerMaster) {
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

    fun ChoiceOption.anyTips() {
        option<Quiz>("Got any tips for me?") {
            val definition = slayerDefinitions.get(slayerMaster)[slayerTask]!!
            npc<Talk>(slayerMaster, definition.tip)
            choice {
                howAmIDoing()
                whoAreYou()
                whereAreYou()
                option<Talk>("That's all thanks.")
            }
        }
    }
}
