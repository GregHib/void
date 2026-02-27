package content.area.misthalin.lumbridge.church

import content.area.misthalin.draynor_village.wise_old_man.OldMansMessage
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import content.quest.refreshQuestJournal
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class FatherAereck(val drops: DropTables) : Script {

    init {
        npcOperate("Talk-to", "father_aereck") {
            wiseOldManLetter()
            when (quest("the_restless_ghost")) {
                "unstarted" -> {
                    npc<Happy>("Welcome to the church of holy Saradomin.")
                    choice {
                        whosSaradomin()
                        nicePlace()
                        option<Happy>("I'm looking for a quest.") {
                            npc<Happy>("That's lucky, I need someone to do a quest for me.")
                            if (startQuest("the_restless_ghost")) {
                                set("the_restless_ghost", "started")
                                refreshQuestJournal()
                                player<Happy>("Okay, let me help then.")
                                npc<Happy>("Thank you. The problem is, there is a ghost in the church graveyard. I would like you to get rid of it.")
                                npc<Happy>("If you need any help, my friend Father Urhney is an expert on ghosts.")
                                npc<Happy>("I believe he is currently living as a hermit in Lumbridge swamp. He has a little shack in the far west of the swamps.")
                                npc<Idle>("Exit the graveyard through the south gate to reach the swamp. I'm sure if you told him that I sent you he'd be willing to help.")
                                npc<Happy>("My name is Father Aereck by the way. Pleased to meet you.")
                                player<Happy>("Likewise.")
                                npc<Idle>("Take care travelling through the swamps, I have heard they can be quite dangerous.")
                                player<Happy>("I will, thanks.")
                            } else {
                                player<Idle>("Actually, I don't have time right now.")
                                npc<Disheartened>("Oh well. If you do have some spare time on your hands, come back and talk to me.")
                            }
                        }
                    }
                }
                "started" -> started()
                "ghost" -> ghost()
                "mining_spot" -> miningSpot()
                "found_skull" -> foundSkull()
                else -> completed()
            }
        }
    }

    private suspend fun Player.wiseOldManLetter() {
        if (get("wise_old_man_npc", "") != "father_aereck" || !carriesItem("old_mans_message")) {
            return
        }
        player<Happy>("The Wise Old Man of Draynor Village said you might reward me if I brought you this.")
        npc<Neutral>("Oh, did he?")
        val reward = OldMansMessage.reward(this) ?: return
        when (reward) {
            "runes" -> {
                items("nature_rune", "water_rune", "Faether Aereck gives you some runes.")
                npc<Happy>("Well, maybe you'll have a use for these?")
            }
            "herbs" -> item("grimy_tarromin", 400, "Faether Aereck gives you some herbs.") // TODO proper message
            "seeds" -> {
                item("potato_seed", 400, "Faether Aereck gives you some seeds.")
                npc<Happy>("Well, maybe you'll find a use for these seeds?")
            }
            "prayer" -> {
                item(167, "<navy>Father Aereck blesses you.<br>You gain some Prayer xp.")
                npc<Happy>("Well, it's still nice of you to bring the message here. Here, I shall bless you...")
            }
            "coins" -> item("coins_8", 400, "Faether Aereck gives you some coins.")
            else -> {
                item(reward, 400, "Father Aereck gives you an ${reward.toSentenceCase()}.")
                npc<Happy>("I suppose gems are always acceptable rewards!")
            }
        }
    }

    suspend fun Player.started() {
        npc<Idle>("Have you got rid of the ghost yet?")
        player<Disheartened>("I can't find Father Urhney at the moment.")
        npc<Happy>("Well, you can get to the swamp he lives in by going south through the cemetery.")
        npc<Happy>("You'll have to go right into the far western depths of the swamp, near the coastline. That is where his house is.")
    }

    suspend fun Player.ghost() {
        npc<Idle>("Have you got rid of the ghost yet?")
        player<Idle>("I had a talk with Father Urhney. He has given me this funny amulet to talk to the ghost with.")
        npc<Confused>("I always wondered what that amulet was... Well, I hope it's useful. Tell me when you get rid of the ghost!")
    }

    suspend fun Player.miningSpot() {
        npc<Idle>("Have you got rid of the ghost yet?")
        player<Idle>("I've found out that the ghost's corpse has lost its skull. If I can find the skull, the ghost should leave.")
        npc<Idle>("That WOULD explain it.")
        npc<Idle>("Hmmmmm. Well, I haven't seen any skulls.")
        player<Confused>("Yes, I think a warlock has stolen it.")
        npc<Angry>("I hate warlocks.")
        npc<Happy>("Ah well, good luck!")
    }

    suspend fun Player.foundSkull() {
        if (carriesItem("ghostspeak_amulet")) {
            npc<Idle>("Have you got rid of the ghost yet?")
            player<Happy>("I've finally found the ghost's skull!")
            npc<Happy>("Great! Put it in the ghost's coffin and see what happens!")
        } else {
            npc<Idle>("Have you got rid of the ghost yet?")
            player<Disheartened>("Well, I found the ghost's skull but then lost it.")
            npc<Idle>("Don't worry, I'm sure you'll find it again.")
        }
    }

    suspend fun Player.completed() {
        npc<Happy>("Thank you for getting rid of that awful ghost for me! May Saradomin always smile upon you!")
        choice {
            if (Settings["combat.gravestones", true]) {
                option<Quiz>("Can you change my gravestone now?") {
                    npc<Happy>("Certainly! All proceeds will be donated to the Varrockian Guards' Widows & Orphans Fund.")
                    open("gravestone_shop")
                }
            }
            option<Happy>("I'm looking for a new quest.") {
                npc<Happy>("Sorry, I only had the one quest.")
            }
            whosSaradomin()
            nicePlace()
            if (inventory.contains("clay_ring")) {
                option<Quiz>("Can you bless my ring?") {
                    npc<Happy>("Ah, you wish to show your devotion to Saradomin by dedicating a ring to him? Very well, it would be my pleasure to assist.")
                    inventory.replace("clay_ring", "ring_of_devotion")
                    set("bless_is_more_task", true)
                    statement("Father Aereck inscribes the symbol of Saradomin on your ring's signet face, and offers a brief benediction over it.")
                }
            }
        }
    }

    fun ChoiceOption.whosSaradomin() {
        option<Quiz>("Who's Saradomin?") {
            npc<Shock>("Surely you have heard of the god, Saradomin?")
            npc<Idle>("He who creates the forces of goodness and purity in this world? I cannot believe your ignorance!")
            npc<Idle>("This is the god with more followers than any other! ...At least in this part of the world.")
            npc<Idle>("He who created this world along with his brothers Guthix and Zamorak?")
            choice {
                option<Idle>("Oh, THAT Saradomin...") {
                    npc<Confused>("There... is only one Saradomin...")
                    player<Idle>("Yeah... I, uh, thought you said something else.")
                }
                option<Idle>("Oh, sorry. I'm not from this world.") {
                    npc<Shock>("...")
                    npc<Idle>("That's... strange.")
                    npc<Idle>("I thought things not from this world were all... You know. Slime and tentacles.")
                    choice {
                        option<Idle>("You don't understand. This is an online game!") {
                            npc<Confused>("I... beg your pardon?")
                            player<Idle>("Never mind.")
                        }
                        option<Happy>("I am - do you like my disguise?") {
                            npc<Shock>("Aargh! Avaunt foul creature from another dimension! Avaunt! Begone in the name of Saradomin!")
                            player<Happy>("Ok, ok, I was only joking...")
                        }
                    }
                }
            }
        }
    }

    fun ChoiceOption.nicePlace() {
        option<Happy>("Nice place you've got here.") {
            npc<Happy>("It is, isn't it? It was built over 230 years ago.")
        }
    }
}
