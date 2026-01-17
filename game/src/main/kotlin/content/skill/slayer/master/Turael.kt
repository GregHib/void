package content.skill.slayer.master

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import content.skill.slayer.*
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.config.SlayerTaskDefinition
import world.gregs.voidps.engine.data.definition.SlayerTaskDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class Turael(val slayerDefinitions: SlayerTaskDefinitions) : Script {

    init {
        npcOperate("Talk-to", "turael") {
            if (slayerTasks == 0) {
                player<Quiz>("Who are you?")
                npc<Neutral>("I'm one of the elite Slayer Masters.")
                choice {
                    option<Neutral>("What's a slayer?") {
                        npc<Sad>("Oh dear, what do they teach you in school?")
                        player<Confused>("Well... er...")
                        npc<Neutral>("I suppose I'll have to educate you then. A slayer is someone who is trained to fight specific creatures. They know these creatures' every weakness and strength. As you can guess it makes killing them a lot easier.")
                        teachMe()
                    }
                    option<Neutral>("Never heard of you...") {
                        npc<Neutral>("That's because my foe never lives to tell of me. We slayers are a dangerous bunch.")
                        teachMe()
                    }
                }
                return@npcOperate
            }
            npc<Neutral>("'Ello, and what are you after then?")
            choice {
                option<Neutral>("I need another assignment.") {
                    if (combatLevel <= 70) {
                        assignTask()
                        return@option
                    }
                    npc<Neutral>("You're actually very strong, are you sure you don't want Chaeldar in Zanaris to assign you a task?")
                    choice {
                        option<Neutral>("No that's okay, I'll take a task from you.") {
                            assignTask()
                        }
                        option<Neutral>("Oh okay then, I'll go talk to Chaeldar.")
                    }
                }
                option<Quiz>("Have you any rewards for me, or anything to trade?") {
                    open("slayer_rewards_learn")
                }
                if (questCompleted("animal_magnetism")) {
                    option<Neutral>("I'm here about blessed axes again.")
                }
            }
        }

        npcOperate("Get-task", "turael") {
            assignTask()
        }

        npcOperate("Trade", "turael") {
            if (contains("broader_fletching")) {
                openShop("slayer_equipment_broads")
            } else {
                openShop("slayer_equipment")
            }
        }

        npcOperate("Rewards", "turael") {
            open("slayer_rewards_learn")
        }
    }

    suspend fun Player.assignTask() {
        if (slayerTask == "nothing") {
            roll()
            return
        }
        npc<Neutral>("You're still hunting ${slayerTask.toSentenceCase()}, you have $slayerTaskRemaining to go.")
        if (slayerMaster != "turael") {
            npc<Neutral>("Although, it's not an assignment that I'd normally give... I guess I could give you a new assignment, if you'd like.")
            npc<Neutral>("If you do get a new one, you will reset your task streak of $slayerStreak. Is that okay?")
        } else {
            npc<Quiz>("Although, it's a tougher assignment that I'd normally give... I guess I could give you a new assignment, id you'd like.")
        }
        choice {
            option<Neutral>("Yes, please.") {
                roll()
            }
            option<Neutral>("No, thanks.")
        }
    }

    suspend fun Player.roll() {
        val (definition, amount) = assign(this)
        npc<Happy>("Excellent, you're doing great. Your new task is to kill $amount ${definition.type.toSentenceCase()}.")
        choice {
            option<Quiz>("Got any tips for me?") {
                npc<Neutral>(definition.tip)
            }
            option<Happy>("Okay, great!")
        }
    }

    suspend fun Player.teachMe() {
        choice {
            option<Neutral>("Wow, can you teach me?") {
                npc<Confused>("Hmmm well I'm not so sure...")
                player<Neutral>("Pleeeaasssse!")
                npc<Neutral>("Oh okay then, you twisted my arm. You'll have to train against specific groups of creatures.")
                player<Quiz>("Okay, what's first?")
                val (definition, amount) = assign(this)
                npc<Neutral>("We'll start you off hunting ${definition.type.toSentenceCase()}, you'll need to kill $amount of them.")
                npc<Neutral>("You'll also need this enchanted gem, it allows Slayer Masters like myself to contact you and update you on your progress. Don't worry if you lose it, you can buy another from any Slayer Master.")
                inventory.add("enchanted_gem")
                choice {
                    option("Got any tips for me?") {
                        npc<Neutral>(definition.tip)
                    }
                    option<Neutral>("Okay, great!") {
                        npc<Happy>("Good luck! Don't forget to come back when you need a new assignment.")
                    }
                }
            }
            option<Idle>("Sounds useless to me.") {
                npc<Idle>("Suit yourself.")
            }
        }
    }

    fun assign(player: Player): Pair<SlayerTaskDefinition, Int> {
        val definitions = slayerDefinitions.get("turael")
        val definition = rollTask(player, definitions)
        val amount = definition.amount.random()
        player.slayerTasks++
        player.slayerMaster = "turael"
        player.slayerTask = definition.type
        player.slayerTaskRemaining = amount
        return Pair(definition, amount)
    }
}
