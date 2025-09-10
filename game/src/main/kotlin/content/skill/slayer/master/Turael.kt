package content.skill.slayer.master

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import content.skill.slayer.*
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.config.SlayerTaskDefinition
import world.gregs.voidps.engine.data.definition.SlayerTaskDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.event.Script
@Script
class Turael {

    val slayerDefinitions: SlayerTaskDefinitions by inject()
    
    init {
        npcOperate("Talk-to", "turael") {
            if (player.slayerTasks == 0) {
                player<Quiz>("Who are you?")
                npc<Talk>("I'm one of the elite Slayer Masters.")
                choice {
                    option<Talk>("What's a slayer?") {
                        npc<Upset>("Oh dear, what do they teach you in school?")
                        player<Uncertain>("Well... er...")
                        npc<Talk>("I suppose I'll have to educate you then. A slayer is someone who is trained to fight specific creatures. They know these creatures' every weakness and strength. As you can guess it makes killing them a lot easier.")
                        teachMe()
                    }
                    option<Talk>("Never heard of you...") {
                        npc<Talk>("That's because my foe never lives to tell of me. We slayers are a dangerous bunch.")
                        teachMe()
                    }
                }
                return@npcOperate
            }
            npc<Talk>("'Ello, and what are you after then?")
            choice {
                option<Talk>("I need another assignment.") {
                    if (player.combatLevel <= 70) {
                        assignTask()
                        return@option
                    }
                    npc<Talk>("You're actually very strong, are you sure you don't want Chaeldar in Zanaris to assign you a task?")
                    choice {
                        option<Talk>("No that's okay, I'll take a task from you.") {
                            assignTask()
                        }
                        option<Talk>("Oh okay then, I'll go talk to Chaeldar.")
                    }
                }
                option<Quiz>("Have you any rewards for me, or anything to trade?") {
                    player.open("slayer_rewards_learn")
                }
                option<Talk>("I'm here about blessed axes again.", filter = { player.questCompleted("animal_magnetism") })
            }
        }

        npcOperate("Get-task", "turael") {
            assignTask()
        }

        npcOperate("Trade", "turael") {
            if (player.contains("broader_fletching")) {
                player.openShop("slayer_equipment_broads")
            } else {
                player.openShop("slayer_equipment")
            }
        }

        npcOperate("Rewards", "turael") {
            player.open("slayer_rewards_learn")
        }

    }

    suspend fun NPCOption<Player>.assignTask() {
        if (player.slayerTask == "nothing") {
            roll()
            return
        }
        npc<Talk>("You're still hunting ${player.slayerTask.toSentenceCase()}, you have ${player.slayerTaskRemaining} to go.")
        if (player.slayerMaster != "turael") {
            npc<Talk>("Although, it's not an assignment that I'd normally give... I guess I could give you a new assignment, if you'd like.")
            npc<Talk>("If you do get a new one, you will reset your task streak of ${player.slayerStreak}. Is that okay?")
        } else {
            npc<Quiz>("Although, it's a tougher assignment that I'd normally give... I guess I could give you a new assignment, id you'd like.")
        }
        choice {
            option<Talk>("Yes, please.") {
                roll()
            }
            option<Talk>("No, thanks.")
        }
    }
    
    suspend fun NPCOption<Player>.roll() {
        val (definition, amount) = assign(player)
        npc<Happy>("Excellent, you're doing great. Your new task is to kill $amount ${definition.type.toSentenceCase()}.")
        choice {
            option<Quiz>("Got any tips for me?") {
                npc<Talk>(definition.tip)
            }
            option<Happy>("Okay, great!")
        }
    }
    
    suspend fun NPCOption<Player>.teachMe() {
        choice {
            option<Talk>("Wow, can you teach me?") {
                npc<Uncertain>("Hmmm well I'm not so sure...")
                player<Talk>("Pleeeaasssse!")
                npc<Talk>("Oh okay then, you twisted my arm. You'll have to train against specific groups of creatures.")
                player<Quiz>("Okay, what's first?")
                val (definition, amount) = assign(player)
                npc<Talk>("We'll start you off hunting ${definition.type.toSentenceCase()}, you'll need to kill $amount of them.")
                npc<Talk>("You'll also need this enchanted gem, it allows Slayer Masters like myself to contact you and update you on your progress. Don't worry if you lose it, you can buy another from any Slayer Master.")
                player.inventory.add("enchanted_gem")
                choice {
                    option("Got any tips for me?") {
                        npc<Talk>(definition.tip)
                    }
                    option<Talk>("Okay, great!") {
                        npc<Happy>("Good luck! Don't forget to come back when you need a new assignment.")
                    }
                }
            }
            option<Neutral>("Sounds useless to me.") {
                npc<Neutral>("Suit yourself.")
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
