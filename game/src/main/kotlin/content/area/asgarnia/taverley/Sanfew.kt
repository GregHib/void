package content.area.asgarnia.taverley

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Sanfew : Script {

    val enchantedMeat = listOf(
        Item("enchanted_beef"),
        Item("enchanted_rat_meat"),
        Item("enchanted_bear_meat"),
        Item("enchanted_chicken"),
    )

    init {
        npcOperate("Talk-to", "sanfew") {
            when (quest("druidic_ritual")) {
                "unstarted" -> {
                    npc<Quiz>("What can I do for you young 'un?")
                    choice {
                        option("I've heard you druids might be able to teach me herblore.") {
                            player<Quiz>("So... I've heard you druids might be able to teach me herblore...")
                            npc<Idle>("Herblore eh? You're probably best off talking to Kaqemeex about that; he's the best herblore teacher we currently have. I believe at the moment he's at out stone circle just North of here.")
                            player<Happy>("Thanks.")
                        }
                        option<Confused>("Actually, I don't need to speak to you.") {
                            npc<Idle>("Well, we all make mistakes sometimes.")
                        }
                    }
                }
                "started" -> started()
                "cauldron" -> cauldron()
                else -> eadgarsRuse()
            }
        }
    }

    suspend fun Player.started() {
        npc<Quiz>("What can I do for you young 'un?")
        choice {
            option("I've been sent to help purify the Varrock stone circle.") {
                player<Idle>("I've been sent to assist you with the ritual to purify the Varrockian stone circle.")
                npc<Idle>("Well, what I'm struggling with right now is the meats needed for the potion to honour Guthix. I need the raw meat of four different animals for it, but not just any old meats will do.")
                npc<Idle>("Each meat has to be dipped individually into the Cauldron of Thunder for it to work correctly.")
                set("druidic_ritual", "cauldron")
                choice {
                    option<Quiz>("Where can I find this cauldron?") {
                        npc<Idle>("It is located somewhere in the mysterious underground halls which are located somewhere in the woods just South of here. They are too dangerous for me to go myself however.")
                    }
                    option<Idle>("Ok, I'll do that then.") {
                        npc<Happy>("Well thank you very much!")
                    }
                }
            }
            option<Confused>("Actually, I don't need to speak to you.") {
                npc<Idle>("Well, we all make mistakes sometimes.")
            }
        }
    }

    suspend fun Player.cauldron() {
        npc<Quiz>("Did you bring me the required ingredients for the potion?")
        if (!inventory.contains(enchantedMeat)) {
            noMeat()
            return
        }
        player<Happy>("Yes, I have all four now!")
        npc<Happy>("Well hand 'em over then lad!")
        npc<Happy>("Thank you so much adventurer! These meats will allow our potion to honour Guthix to be completed, and bring one step closer to reclaiming our stone circle!")
        inventory.remove(enchantedMeat)
        set("druidic_ritual", "kaqemeex")
        npc<Idle>("Now go and talk to Kaqemeex and he will introduce you to the wonderful world of herblore and potion making!")
    }

    suspend fun Player.noMeat() {
        player<Disheartened>("No, not yet...")
        npc<Idle>("Well let me know when you do young 'un.")
        choice {
            option<Quiz>("What was I meant to be doing again?") {
                npc<Idle>("Trouble with your memory eh young 'un? I need the raw meats of four different animals that have been dipped into the Cauldron of Thunder so I can make my potion to honour Guthix.")
                player<Idle>("Ooooh yeah, I remember.")
                choice {
                    option<Quiz>("Where can I find this cauldron?") {
                        npc<Idle>("It is located somewhere in the mysterious underground halls which are located somewhere in the woods just South of here. They are too dangerous for me to go myself however.")
                    }
                    option<Idle>("Ok, I'll do that then.") {
                        npc<Happy>("Well thank you very much!")
                    }
                }
            }
            option<Idle>("I'll get on with it.") {
                npc<Happy>("Good, good.")
            }
        }
    }

    suspend fun Player.eadgarsRuse() {
        npc<Quiz>("What can I do for you young 'un?")
        choice {
            option<Idle>("Have you any more work for me, to help reclaim the circle?") {
                npc<Idle>("Not just yet, young 'un.")
                statement("You do not meet all of the requirements to start <br> the Eadgar's Ruse quest.")
            }
            option<Confused>("Actually, I don't need to speak to you.") {
                npc<Idle>("Well, we all make mistakes sometimes.")
            }
        }
    }
}
