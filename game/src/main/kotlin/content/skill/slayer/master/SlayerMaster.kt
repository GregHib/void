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
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class SlayerMaster : Script {

    init {
        npcOperate("Talk-to", "turael,mazchna,vannaka,chaeldar,sumona,duradel,kuradal") { (target) ->
            if (target.id == "turael" && slayerTasks == 0) {
                player<Quiz>("Who are you?")
                npc<Neutral>("I'm one of the elite Slayer Masters.")
                choice {
                    option<Neutral>("What's a slayer?") {
                        npc<Sad>("Oh dear, what do they teach you in school?")
                        player<Confused>("Well... er...")
                        npc<Neutral>("I suppose I'll have to educate you then. A slayer is someone who is trained to fight specific creatures. They know these creatures' every weakness and strength. As you can guess it makes killing them a lot easier.")
                        teachMe(target.id)
                    }
                    option<Neutral>("Never heard of you...") {
                        npc<Neutral>("That's because my foe never lives to tell of me. We slayers are a dangerous bunch.")
                        teachMe(target.id)
                    }
                }
                return@npcOperate
            }
            npc<Neutral>("'Ello, and what are you after then?")
            choice {
                option<Neutral>("I need another assignment.") {
                    val nextCombat = when (target.id) {
                        "turael" -> 50
                        "mazchna" -> 75
                        "vannaka" -> 90
                        "chaeldar" -> 100
                        "sumona" -> 120
                        else -> 128
                    }
                    if (combatLevel <= nextCombat) {
                        assignTask(target.id)
                        return@option
                    }
                    val next = when (nextCombat) {
                        50 -> "Mazchna in Canifis"
                        75 -> "Chaeldar in Zanaris"
                        90 -> "Sumona in Pollnivneach"
                        else -> "Duradel in Shilo Village"
                    }
                    npc<Neutral>("You're actually very strong, are you sure you don't want $next to assign you a task?")
                    choice {
                        option<Neutral>("No that's okay, I'll take a task from you.") {
                            assignTask(target.id)
                        }
                        option<Neutral>("Oh okay then, I'll go talk to ${next.substringBefore(" in")}.")
                    }
                }
                option<Quiz>("Have you any rewards for me, or anything to trade?") {
                    open("slayer_rewards_learn")
                }
                if (target.id == "turael" && questCompleted("animal_magnetism")) {
                    option<Neutral>("I'm here about blessed axes again.")
                }
                option<Neutral>("Er...nothing... ")
            }
        }

        npcOperate("Get-task", "turael,mazchna,vannaka,chaeldar,sumona,duradel,kuradal") { (target) ->
            assignTask(target.id)
        }

        npcOperate("Trade", "turael,mazchna,vannaka,chaeldar,sumona,duradel,kuradal") {
            if (contains("broader_fletching")) {
                openShop("slayer_equipment_broads")
            } else {
                openShop("slayer_equipment")
            }
        }

        npcOperate("Rewards", "turael,mazchna,vannaka,chaeldar,sumona,duradel,kuradal") {
            open("slayer_rewards_learn")
        }
    }

    suspend fun Player.assignTask(master: String) {
        if (slayerTask == "nothing") {
            roll(master)
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
                roll(master)
            }
            option<Neutral>("No, thanks.")
        }
    }

    suspend fun Player.roll(master: String) {
        val (npc, amount) = assignTask(this, master)
        val type = EnumDefinitions.string("slayer_tasks_categories", npc)
        npc<Happy>("Excellent, you're doing great. Your new task is to kill $amount ${type.toSentenceCase()}.")
        choice {
            option<Quiz>("Got any tips for me?") {
                val tip = EnumDefinitions.string("slayer_task_tips", npc)
                npc<Neutral>(tip)
            }
            option<Happy>("Okay, great!")
        }
    }

    suspend fun Player.teachMe(master: String) {
        choice {
            option<Neutral>("Wow, can you teach me?") {
                npc<Confused>("Hmmm well I'm not so sure...")
                player<Neutral>("Pleeeaasssse!")
                npc<Neutral>("Oh okay then, you twisted my arm. You'll have to train against specific groups of creatures.")
                player<Quiz>("Okay, what's first?")
                val (npc, amount) = assignTask(this, master)
                val type = EnumDefinitions.string("slayer_tasks_categories", npc)
                npc<Neutral>("We'll start you off hunting ${type.toSentenceCase()}, you'll need to kill $amount of them.")
                npc<Neutral>("You'll also need this enchanted gem, it allows Slayer Masters like myself to contact you and update you on your progress. Don't worry if you lose it, you can buy another from any Slayer Master.")
                inventory.add("enchanted_gem")
                choice {
                    option("Got any tips for me?") {
                        val tip = EnumDefinitions.string("slayer_task_tips", npc)
                        npc<Neutral>(tip)
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

}
