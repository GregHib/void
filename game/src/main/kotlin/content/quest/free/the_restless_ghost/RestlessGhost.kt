package content.quest.free.the_restless_ghost

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.SuspendableContext

@Script
class RestlessGhost {

    init {
        npcOperate("Talk-to", "restless_ghost") {
            when (player.quest("the_restless_ghost")) {
                "unstarted" -> {
                    npc<Neutral>("Wooooo! Ooooooh!")
                    player<Uncertain>("I can't understand a word you are saying. Maybe Father Aereck will be able to help.")
                }
                "started", "ghost" -> ghost()
                "mining_spot", "found_skull" -> miningSpot()
                else -> player.message("The ghost doesn't appear to be interested in talking.")
            }
        }
    }

    suspend fun SuspendableContext<Player>.ghost() {
        if (player.equipment.contains("ghostspeak_amulet")) {
            player<Neutral>("Hello ghost, how are you?")
            npc<Neutral>("Not very good actually.")
            player<Quiz>("What's the problem then?")
            npc<Neutral>("Did you just understand what I said???")
            choice {
                option<Neutral>("Yep, now tell me what the problem is.") {
                    npc<Neutral>("WOW! This is INCREDIBLE! I didn't expect anyone to ever understand me again!")
                    player<Angry>("Ok, Ok, I can understand you!")
                    player<Quiz>("But have you any idea WHY you're doomed to be a ghost?")
                    npc<Neutral>("Well, to be honest, I'm not sure.")
                    task()
                }
                option<Happy>("No, you sound like you're speaking nonsense to me.") {
                    npc<Neutral>("Oh that's a pity. You got my hopes up there.")
                    player<Neutral>("Yeah, it is a pity. Sorry about that.")
                    npc<Neutral>("Hang on a second... you CAN understand me!")
                    choice {
                        option<Neutral>("No I can't.") {
                            npc<Neutral>("Great.")
                            npc<Neutral>("The first person I can speak to in ages...")
                            npc<Neutral>("..and they're a moron.")
                        }
                        option<Happy>("Yep, clever aren't I?") {
                            npc<Neutral>("I'm impressed. You must be very powerful. I don't suppose you can stop me being a ghost?")
                            helpMe()
                        }
                    }
                }
                option<Happy>("Wow, this amulet works!") {
                    npc<Neutral>("Oh! It's your amulet that's doing it! I did wonder. I don't suppose you can help me? I don't like being a ghost.")
                    helpMe()
                }
            }
        } else if (player.inventory.contains("ghostspeak_amulet")) {
            npc<Neutral>("Wooo wooo wooooo!")
            player<Neutral>("Why can't I understand you? Oh, yeah, it might help if I wear this amulet!")
        } else {
            noGhostAmulet()
        }
    }

    suspend fun SuspendableContext<Player>.noGhostAmulet() {
        player<Neutral>("Hello ghost, how are you?")
        npc<Neutral>("Wooo wooo wooooo!")
        choice {
            option<Uncertain>("Sorry, I don't speak ghost.") {
                dontSpeakGhost()
            }
            option<Happy>("Ooh... THAT'S interesting.") {
                npc<Neutral>("Woo wooo. Woooooooooooooooooo!")
                choice {
                    option<Quiz>("Did he really?") {
                        npc<Neutral>("Woo.")
                        choice {
                            option<Neutral>("My brother had EXACTLY the same problem.") {
                                npc<Neutral>("Woo Wooooo!")
                                npc<Neutral>("Wooooo Woo woo woo!")
                                choice {
                                    option<Neutral>("Goodbye. Thanks for the chat.") {
                                        npc<Neutral>("Wooo wooo?")
                                    }
                                    option<Neutral>("You'll have to give me the recipe some time...") {
                                        npc<Neutral>("Wooooooo woo woooooooo.")
                                        choice {
                                            option<Neutral>("Goodbye. Thanks for the chat.") {
                                                npc<Neutral>("Wooo wooo?")
                                            }
                                            option<Quiz>("Hmm... I'm not so sure about that.") {
                                                notSoSure()
                                            }
                                        }
                                    }
                                }
                            }
                            option<Neutral>("Goodbye. Thanks for the chat.") {
                                npc<Neutral>("Wooo wooo?")
                            }
                        }
                    }
                    option<Happy>("Yeah, that's what I thought.") {
                        npc<Neutral>("Wooo woooooooooooooo...")
                        choice {
                            option<Neutral>("Goodbye. Thanks for the chat.") {
                                npc<Neutral>("Wooo wooo?")
                            }
                            option<Quiz>("Hmm... I'm not so sure about that.") {
                                notSoSure()
                            }
                        }
                    }
                }
            }
            option<Quiz>("Any hints where I can find some treasure?") {
                npc<Neutral>("Wooooooo woo! Wooooo woo wooooo woowoowoo woo Woo wooo. Wooooo woo woo? Woooooooooooooooooo!")
                choice {
                    option<Uncertain>("Sorry, I don't speak ghost.") {
                        dontSpeakGhost()
                    }
                    option<Happy>("Thank you. You've been very helpful.") {
                        npc<Neutral>("Wooooooo.")
                    }
                }
            }
        }
    }

    suspend fun SuspendableContext<Player>.dontSpeakGhost() {
        npc<Neutral>("Woo woo?")
        player<Neutral>("Nope, still don't understand you.")
        npc<Neutral>("WOOOOOOOOO!")
        player<Neutral>("Never mind.")
    }

    suspend fun SuspendableContext<Player>.notSoSure() {
        npc<Neutral>("Wooo woo?")
        player<Angry>("Well, if you INSIST.")
        npc<Neutral>("Wooooooooo!")
        player<Neutral>("Ah well, better be off now...")
        npc<Neutral>("Woo.")
        player<Neutral>("Bye.")
    }

    suspend fun SuspendableContext<Player>.task() {
        npc<Neutral>("I should think it's because I've lost my head.")
        player<Neutral>("What? I can see your head perfectly fine well, see through it at least.")
        npc<Neutral>("No, no, I mean from my REAL body. If you look in my coffin you'll see my corpse is without its skull. Last thing I remember was being attacked by a warlock while I was mining. It was at the mine just south of this")
        npc<Neutral>("graveyard.")
        player<Neutral>("Okay. I'll try to get your skull back for you so you can rest in peace.")
        player["the_restless_ghost"] = "mining_spot"
    }

    suspend fun SuspendableContext<Player>.helpMe() {
        choice {
            option<Happy>("Yes, ok. Do you know WHY you're a ghost?") {
                task()
            }
            option<Neutral>("No, you're scary!") {
                npc<Neutral>("Great.")
                npc<Neutral>("The first person I can speak to in ages...")
                npc<Neutral>("..and they're an idiot.")
            }
        }
    }

    suspend fun SuspendableContext<Player>.miningSpot() {
        if (player.equipment.contains("ghostspeak_amulet")) {
            if (player.inventory.contains("muddy_skull")) {
                player<Neutral>("Hello ghost, how are you?")
                npc<Neutral>("How are you doing finding my skull?")
                player<Happy>("I have found it!")
                npc<Neutral>("Hurrah! Now I can stop being a ghost! You just need to put it in my coffin there, and I will be free!")
            } else {
                player<Neutral>("Hello ghost, how are you?")
                npc<Neutral>("How are you doing finding my skull?")
                player<Sad>("Sorry, I can't find it at the moment.")
                npc<Neutral>("Ah well. Keep on looking.")
                npc<Neutral>("I'm pretty sure it's somewhere near the mining spot south of here. I really hope it's still there somewhere.")
            }
        } else if (player.inventory.contains("ghostspeak_amulet")) {
            npc<Neutral>("Wooo wooo wooooo!")
            player<Neutral>("Why can't I understand you? Oh, yeah, it might help if I wear this amulet!")
        } else {
            noGhostAmulet()
        }
    }
}
