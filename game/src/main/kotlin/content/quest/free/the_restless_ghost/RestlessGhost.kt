package content.quest.free.the_restless_ghost

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory

class RestlessGhost : Script {

    init {
        npcOperate("Talk-to", "restless_ghost") {
            when (quest("the_restless_ghost")) {
                "unstarted" -> {
                    npc<Idle>("Wooooo! Ooooooh!")
                    player<Confused>("I can't understand a word you are saying. Maybe Father Aereck will be able to help.")
                }
                "started", "ghost" -> ghost()
                "mining_spot", "found_skull" -> miningSpot()
                else -> message("The ghost doesn't appear to be interested in talking.")
            }
        }
    }

    suspend fun Player.ghost() {
        if (equipment.contains("ghostspeak_amulet")) {
            player<Idle>("Hello ghost, how are you?")
            npc<Idle>("Not very good actually.")
            player<Quiz>("What's the problem then?")
            npc<Idle>("Did you just understand what I said???")
            choice {
                option<Idle>("Yep, now tell me what the problem is.") {
                    npc<Idle>("WOW! This is INCREDIBLE! I didn't expect anyone to ever understand me again!")
                    player<Angry>("Ok, Ok, I can understand you!")
                    player<Quiz>("But have you any idea WHY you're doomed to be a ghost?")
                    npc<Idle>("Well, to be honest, I'm not sure.")
                    task()
                }
                option<Happy>("No, you sound like you're speaking nonsense to me.") {
                    npc<Idle>("Oh that's a pity. You got my hopes up there.")
                    player<Idle>("Yeah, it is a pity. Sorry about that.")
                    npc<Idle>("Hang on a second... you CAN understand me!")
                    choice {
                        option<Idle>("No I can't.") {
                            npc<Idle>("Great.")
                            npc<Idle>("The first person I can speak to in ages...")
                            npc<Idle>("..and they're a moron.")
                        }
                        option<Happy>("Yep, clever aren't I?") {
                            npc<Idle>("I'm impressed. You must be very powerful. I don't suppose you can stop me being a ghost?")
                            helpMe()
                        }
                    }
                }
                option<Happy>("Wow, this amulet works!") {
                    npc<Idle>("Oh! It's your amulet that's doing it! I did wonder. I don't suppose you can help me? I don't like being a ghost.")
                    helpMe()
                }
            }
        } else if (inventory.contains("ghostspeak_amulet")) {
            npc<Idle>("Wooo wooo wooooo!")
            player<Idle>("Why can't I understand you? Oh, yeah, it might help if I wear this amulet!")
        } else {
            noGhostAmulet()
        }
    }

    suspend fun Player.noGhostAmulet() {
        player<Idle>("Hello ghost, how are you?")
        npc<Idle>("Wooo wooo wooooo!")
        choice {
            option<Confused>("Sorry, I don't speak ghost.") {
                dontSpeakGhost()
            }
            option<Happy>("Ooh... THAT'S interesting.") {
                npc<Idle>("Woo wooo. Woooooooooooooooooo!")
                choice {
                    option<Quiz>("Did he really?") {
                        npc<Idle>("Woo.")
                        choice {
                            option<Idle>("My brother had EXACTLY the same problem.") {
                                npc<Idle>("Woo Wooooo!")
                                npc<Idle>("Wooooo Woo woo woo!")
                                choice {
                                    option<Idle>("Goodbye. Thanks for the chat.") {
                                        npc<Idle>("Wooo wooo?")
                                    }
                                    option<Idle>("You'll have to give me the recipe some time...") {
                                        npc<Idle>("Wooooooo woo woooooooo.")
                                        choice {
                                            option<Idle>("Goodbye. Thanks for the chat.") {
                                                npc<Idle>("Wooo wooo?")
                                            }
                                            option<Quiz>("Hmm... I'm not so sure about that.") {
                                                notSoSure()
                                            }
                                        }
                                    }
                                }
                            }
                            option<Idle>("Goodbye. Thanks for the chat.") {
                                npc<Idle>("Wooo wooo?")
                            }
                        }
                    }
                    option<Happy>("Yeah, that's what I thought.") {
                        npc<Idle>("Wooo woooooooooooooo...")
                        choice {
                            option<Idle>("Goodbye. Thanks for the chat.") {
                                npc<Idle>("Wooo wooo?")
                            }
                            option<Quiz>("Hmm... I'm not so sure about that.") {
                                notSoSure()
                            }
                        }
                    }
                }
            }
            option<Quiz>("Any hints where I can find some treasure?") {
                npc<Idle>("Wooooooo woo! Wooooo woo wooooo woowoowoo woo Woo wooo. Wooooo woo woo? Woooooooooooooooooo!")
                choice {
                    option<Confused>("Sorry, I don't speak ghost.") {
                        dontSpeakGhost()
                    }
                    option<Happy>("Thank you. You've been very helpful.") {
                        npc<Idle>("Wooooooo.")
                    }
                }
            }
        }
    }

    suspend fun Player.dontSpeakGhost() {
        npc<Idle>("Woo woo?")
        player<Idle>("Nope, still don't understand you.")
        npc<Idle>("WOOOOOOOOO!")
        player<Idle>("Never mind.")
    }

    suspend fun Player.notSoSure() {
        npc<Idle>("Wooo woo?")
        player<Angry>("Well, if you INSIST.")
        npc<Idle>("Wooooooooo!")
        player<Idle>("Ah well, better be off now...")
        npc<Idle>("Woo.")
        player<Idle>("Bye.")
    }

    suspend fun Player.task() {
        npc<Idle>("I should think it's because I've lost my head.")
        player<Idle>("What? I can see your head perfectly fine well, see through it at least.")
        npc<Idle>("No, no, I mean from my REAL body. If you look in my coffin you'll see my corpse is without its skull. Last thing I remember was being attacked by a warlock while I was mining. It was at the mine just south of this")
        npc<Idle>("graveyard.")
        player<Idle>("Okay. I'll try to get your skull back for you so you can rest in peace.")
        set("the_restless_ghost", "mining_spot")
    }

    suspend fun Player.helpMe() {
        choice {
            option<Happy>("Yes, ok. Do you know WHY you're a ghost?") {
                task()
            }
            option<Idle>("No, you're scary!") {
                npc<Idle>("Great.")
                npc<Idle>("The first person I can speak to in ages...")
                npc<Idle>("..and they're an idiot.")
            }
        }
    }

    suspend fun Player.miningSpot() {
        if (equipment.contains("ghostspeak_amulet")) {
            if (inventory.contains("muddy_skull")) {
                player<Idle>("Hello ghost, how are you?")
                npc<Idle>("How are you doing finding my skull?")
                player<Happy>("I have found it!")
                npc<Idle>("Hurrah! Now I can stop being a ghost! You just need to put it in my coffin there, and I will be free!")
            } else {
                player<Idle>("Hello ghost, how are you?")
                npc<Idle>("How are you doing finding my skull?")
                player<Disheartened>("Sorry, I can't find it at the moment.")
                npc<Idle>("Ah well. Keep on looking.")
                npc<Idle>("I'm pretty sure it's somewhere near the mining spot south of here. I really hope it's still there somewhere.")
            }
        } else if (inventory.contains("ghostspeak_amulet")) {
            npc<Idle>("Wooo wooo wooooo!")
            player<Idle>("Why can't I understand you? Oh, yeah, it might help if I wear this amulet!")
        } else {
            noGhostAmulet()
        }
    }
}
