package content.area.fremennik_province.rellekka

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class Longships : Script {

    init {
        npcOperate("Travel-Jatizso", "mord_gunnars_rellekka") {
            if (fremennik()) {
                sail("Jatizso", Tile(2421, 3781), "longship_to_jatizso", 14)
            }
        }

        npcOperate("Travel-Rellekka", "mord_gunnars_jatizo") {
            sail("Rellekka", Tile(2644, 3710), "longship_from_jatizso", 14)
        }

        npcOperate("Talk-to", "mord_gunnars_rellekka") {
            npc<Happy>("Would you like to sail to Jatizso?")
            choice {
                option<Happy>("Yes, please!") {
                    if (fremennik()) {
                        sail("Jatizso", Tile(2421, 3781), "longship_to_jatizso", 14)
                    }
                }
                option<Neutral>("No, thank you.")
            }
        }

        npcOperate("Talk-to", "mord_gunnars_jatizo") {
            npc<Happy>("Would you like to sail back to Rellekka?")
            choice {
                option<Happy>("Yes, please!") {
                    sail("Rellekka", Tile(2644, 3710), "longship_from_jatizso", 14)
                }
                option<Neutral>("No, thank you.")
            }
        }

        npcOperate("Travel-Neitiznot", "maria_gunnars_rellekka") {
            if (fremennik()) {
                sail("Neitiznot", Tile(2310, 3782), "longship_to_neitiznot", 15)
            }
        }

        npcOperate("Travel-Rellekka", "maria_gunnars") {
            sail("Rellekka", Tile(2644, 3710), "longship_from_neitiznot", 15)
        }

        npcOperate("Talk-to", "maria_gunnars_rellekka") {
            if (!questCompleted("fremennik_trials")) {
                npc<Sad>("I do not speak to outerlanders. Maybe you should speak with Rellekka's Chieftain.")
                return@npcOperate
            }
            npc<Happy>("Welcome back, $name. Do you have any questions?")
            choice {
                option<Neutral>("Can you ferry me to Neitiznot?") {
                    npc<Happy>("Let's set sail then.")
                    sail("Neitiznot", Tile(2310, 3782), "longship_to_neitiznot", 15)
                }
                option<Happy>("I just stopped to say 'hello'.") {
                    npc<Happy>("Thanks!")
                    player<Neutral>("I may be back later.")
                }
            }
        }

        npcOperate("Talk-to", "maria_gunnars") {
            npc<Happy>("Hi, $name, you should speak with Burgher Mawnis Burowgar in the main chambers. Do you have any questions?")
            choice {
                option<Neutral>("Can you ferry me to Rellekka?") {
                    npc<Neutral>("Sure.")
                    sail("Rellekka", Tile(2644, 3710), "longship_from_neitiznot", 15)
                }
                option<Neutral>("I have nothing to ask you right now.") {
                    npc<Neutral>("Fare thee well.")
                }
            }
        }

        npcOperate("Travel-Miscellania", "sailor_rellekka") {
            if (fremennik()) {
                sail("Miscellania", Tile(2581, 3845), "longship_to_miscellania", 10)
            }
        }

        npcOperate("Travel-Rellekka", "sailor_miscellania") {
            if (fremennik()) {
                sail("Rellekka", Tile(2629, 3693), "longship_from_miscellania", 8)
            }
        }

        npcOperate("Talk-to", "jarvald_rellekka") {
            if (questCompleted("fremennik_trials")) {
                npc<Happy>("Greetings, $name!")
                npc<Happy>("So what brings you back to fair Rellekka? It has been too long since you have drunk in the long hall with us and sang of your battles!")
            } else {
                npc<Neutral>("What do you want from me outerlander? It is our policy not to associate with those not of our tribe.")
            }
            choice("What would you like to ask about?") {
                if (!questCompleted("fremennik_trials")) {
                    option("Where is your chieftain?") {
                        player<Quiz>("Where is your chieftain? I find it highly discriminatory to refuse to talk to someone on the grounds that they are not part of your tribe.")
                        npc<Neutral>("I don't rightly understand your speech outerlander, but my loyalty is with Chieftain Brundt. He resides in our long hall; it is the large building over there, you should speak to him for he speaks for us all.")
                    }
                }
                option("What Jarvald is doing.") {
                    player<Quiz>("So what are you doing here?")
                    if (questCompleted("fremennik_trials")) {
                        npc<Happy>("You have not heard, $name? I am leading an expedition to Waterbirth Island!")
                    } else {
                        npc<Neutral>("This should not concern you, outerlander. I am awaiting other Fremenniks to join me on an expedition to Waterbirth Island.")
                    }
                    expedition()
                }
                option("Nothing") {

                    player<Neutral>("Actually, I don't think I have anything to speak to you about...")
                }
            }
        }

        npcOperate("Travel-Waterbirth", "jarvald_rellekka") {
            if (questCompleted("fremennik_trials")) {
                sail("Waterbirth Island", Tile(2544, 3759), "longship_to_waterbirth_island", 9)
            } else if (get("jarvald_expedition_asked", false)) {
                payForTrip()
            } else {
                npc<Neutral>("No, outerlander I am not taking you there.")
            }
        }

        npcOperate("Talk-to", "jarvald_waterbirth_island") {
            if (questCompleted("fremennik_trials")) {
                npc<Happy>("Ah, $name! Such glorious battle makes you feel glad of life, does it not?")
                npc<Quiz>("So what say you, stay here for the hunt, or return home to sweet Rellekka to feast and drink with your tribe?")
            } else {
                npc<Quiz>("Ah, you live yet, outerlander! Have you had your fill of the hunt and wish to return, or are you still feeling the joy of the cull?")
            }
            choice("Leave island?") {
                option("Yes") {
                    npc<Neutral>("Then let us away; There will be death to bring here another day!")
                    sail("Rellekka", Tile(2620, 3685), "longship_from_waterbirth_island", 8)
                }
                option("No") {
                    npc<Laugh>("Ha Ha Ha! A true huntsman at heart!")
                    npc<Happy>("I myself have killed over a hundred of the daggermouths, and did not think it too many!")
                }
            }
        }

        npcOperate("Travel-Rellekka", "jarvald_waterbirth_island") {
            sail("Rellekka", Tile(2620, 3685), "longship_from_waterbirth_island", 8)
        }
    }

    private fun Player.fremennik(): Boolean {
        if (questCompleted("fremennik_trials")) {
            return true
        }
        message("You must have completed The Fremennik Trials quest.")
        return false
    }

    private suspend fun Player.expedition() {
        val member = questCompleted("fremennik_trials")
        choice {
            option("Waterbirth Island?") {
                if (member) {
                    npc<Neutral>("You have not ever travelled to Waterbirth Island, $name? I am surprised, it is a place of outstanding natural beauty.")
                    npc<Neutral>("Or at least it used to be! But things have now changed...")
                    player<Quiz>("Changed? How do you mean, changed?")
                    npc<Neutral>("It seems as though the sea-beasts known to us as the daggermouths have begun their hatching once again... And there may be others of their ilk there too.")
                    player<Quiz>("Daggermouths?")
                    npc<Neutral>("Aye, the daggermouths! The vile creatures lived near here once, but we had thought them all driven back to the ocean depths many moons past.")
                    npc<Neutral>("I can only imagine a daggermouth queen has nested somewhere nearby, and spawned her foul brood under the sea once more, and some of them have migrated to fair Waterbirth Island.")
                    player<Quiz>("So you're scared they might attack Rellekka?")
                    npc<Laugh>("Scared? Ha ha ha!")
                    npc<Laugh>("You wound us with your questioning, $name!")
                    npc<Happy>("We are glad the daggermouths have returned to these shores, for it means we will get the chance to hunt them once again as our ancestors did!")
                    npc<Happy>("When treated in the correct manner, the creatures' remains can be used to make fine battleworthy armour!")
                    expedition()
                } else {
                    npc<Neutral>("It is a small crescent-shaped island just north-west of here, outerlander. We have many legends about it, such as the tale of the broken sky, and the day of the green seas. The reason I am travelling there is more serious Fremennik business, however. I doubt an outerlander would be interested.")
                }
            }
            option("Can I come?") {
                if (member) {
                    player<Quiz>("Can I come?")
                    npc<Happy>("Of course, $name! Your presence is more than welcome on this cull! You wish to leave now?")
                    choice {
                        option("YES") {
                            sail("Waterbirth Island", Tile(2544, 3759), "longship_to_waterbirth_island", 9)
                        }
                        option("NO") {
                            declineTrip()
                        }
                    }
                    return@option
                }
                if (!get("jarvald_expedition_asked", false)) {
                    player<Quiz>("Hey, scary barbarian type guy, think I can join you on this expedition?")
                    npc<Neutral>("An outerlander join us on an honoured hunt??? Well.... I guess... I might be able to allow you to join us, although it is a breach of many of our customs...")
                    player<Happy>("Oh, pleeeeease? I really LOVE killing stuff!")
                    npc<Neutral>("Well... I remain unconvinced that it would be wise to allow an outerlander to join us in such dangerous battle, but your enthusiasm seems genuine enough... I will allow you to escort us, but you must pay me a sum of money first.")
                    player<Quiz>("What? That's outrageous, why charge me money? And, uh, how much does it cost me?")
                    npc<Neutral>("Ah, the outerlanders have stolen from my people for many years, in this way you can help my community with a small amount of money... Let us say... 1000 coins. Payable in advance, of course. For this I will take you to Waterbirth Island on my boat, and will bring you back here when you have had your fill of the hunt. Assuming you are still alive to wish to leave, of course.")
                    set("jarvald_expedition_asked", true)
                }
                payForTrip()
            }
            option("Nice hat!") {
                player<Happy>("Hey, I have to say, that's a fine looking hat you are wearing there.")
                if (member) {
                    npc<Happy>("Aye, that it is $name! Skulgrimen fashioned it for me from the carcass of one of the monsters on Waterbirth Island after our last hunt! I hope to kill enough creatures to fashion some fine armour as well when next we leave!")
                    expedition()
                } else {
                    npc<Neutral>("It is actually a helm, outerlander, but the sentiment is appreciated nonetheless.")
                }
            }
            option("Ok, 'bye.") {
                player<Happy>("Wow, you Fremenniks sure know how to party. Well, see ya around.")
            }
        }
    }

    private suspend fun Player.payForTrip() {
        npc<Quiz>("So do you have the 1000 coins for my service, and are you ready to leave now?")
        choice {
            option("YES") {
                if (!inventory.remove("coins", 1000)) {
                    message("You can not afford that.")
                    npc<Neutral>("Ah, outerlander... I go beyond my duty to allow you to accompany me, and you attempt to swindle me out of a reasonable fee? This is no way even for an outerlander to behave...")
                    return@option
                }
                sail("Waterbirth Island", Tile(2544, 3759), "longship_to_waterbirth_island", 9)
            }
            option("NO") {
                declineTrip()
            }
        }
    }

    private suspend fun Player.declineTrip() {
        player<Neutral>("No, actually I have some stuff to do here first.")
        npc<Neutral>("As you wish. Come and see me when your bloodlust needs sating.")
    }

    private suspend fun Player.sail(name: String, tile: Tile, animation: String, ticks: Int) {
        open("fade_out")
        message("You board the longship...")
        delay(2)
        tele(tile)
        open("longship_journey")
        interfaces.sendAnimation("longship_journey", "ship", animation)
        delay(ticks)
        close("longship_journey")
        open("fade_in")
        message("The ship arrives at $name.")
    }
}
