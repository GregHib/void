package content.skill.slayer

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.queue.queue

class EnchantedGem : Script {

    init {
        playerSpawn {
            sendVariable("slayer_count")
            sendVariable("slayer_target")
        }

        itemOption("Activate", "enchanted_gem") {
            queue("enchanted_gem_activate") {
                val master = slayerMaster
                npc<Happy>(master, "Hello there ${this@itemOption.name}, what can I help you with?")
                choice {
                    howAmIDoing()
                    whoAreYou()
                    whereAreYou()
                    anyTips()
                    option<Neutral>("That's all thanks.")
                }
            }
        }

        itemOption("Kills-left", "enchanted_gem,ring_of_slaying_*") {
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
                option<Neutral>("That's all thanks.")
            }
        }
    }

    fun ChoiceOption.whoAreYou() {
        option<Quiz>("Who are you?") {
            npc<Neutral>(slayerMaster, "My name is ${slayerMaster.toSentenceCase()}, I'm a Slayer Master.")
            choice {
                howAmIDoing()
                whereAreYou()
                anyTips()
                option<Neutral>("That's all thanks.")
            }
        }
    }

    fun ChoiceOption.whereAreYou() {
        option<Quiz>("Where are you?") {
            when (slayerMaster) {
                "turael" -> npc<Neutral>("You'll find me in Burthorpe. I'll be here when you need a new task.")
                "vannaka" -> npc<Neutral>("You'll find me in Edgeville. I'll be here when you need a new task.")
                "duradel" -> npc<Neutral>("You'll find me in Shilo Village. I'll be here when you need a new task.")
                "mazchna" -> npc<Neutral>("You'll find me in Canifis. I'll be here when you need a new task.")
                "chaeldar" -> npc<Neutral>("You'll find me in Zanaris. I'll be here when you need a new task.")
                "sumona" -> npc<Neutral>("You'll find me in Pollnivneach. I'll be here when you need a new task.")
                "kuradal" -> npc<Neutral>("You'll find me in the ancient cavern, near the Dragonkin Forge. I'll be here when you need a new task.")
            }
            choice {
                howAmIDoing()
                whoAreYou()
                anyTips()
                option<Neutral>("That's all thanks.")
            }
        }
    }

    fun ChoiceOption.anyTips() {
        option<Quiz>("Got any tips for me?") {
            var npc = -1
            for ((key, value) in EnumDefinitions.get("${slayerMaster}_tasks").map ?: return@option) {
                if (value == slayerTask) {
                    npc = key
                    break
                }
            }
            val tip = EnumDefinitions.stringOrNull("slayer_task_tips", npc) ?: return@option
            npc<Neutral>(slayerMaster, tip)
            choice {
                howAmIDoing()
                whoAreYou()
                whereAreYou()
                option<Neutral>("That's all thanks.")
            }
        }
    }
}
