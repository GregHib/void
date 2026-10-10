package content.skill.construction

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.skill.construction.House.Companion.houseOwner
import content.skill.construction.House.Companion.inOwnHouse
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

/**
 * Skill hall head trophies which can be talked to and the quest hall's mounted amulet of glory which teleports without charges
 * https://oldschool.runescape.wiki/w/Skill_hall
 * https://oldschool.runescape.wiki/w/Quest_hall
 */
class HouseHalls : Script {
    init {
        objectOperate("Talk-to", "crawling_hand_trophy") {
            if (!inOwnHouse()) {
                player<Happy>("Hey, a crawling hand!")
                npc<Neutral>("poh_mounted_crawling_hand", "Yes, what?")
                player<Laugh>("${ownerName()} must be pretty handy to have slayed that!")
                return@objectOperate
            }
            when (random.nextInt(0, 3)) {
                0 -> {
                    player<Happy>("Hey, I was going to make some furniture, do you think you could lend a HAND?")
                    npc<Neutral>("poh_mounted_crawling_hand", "Very funny.")
                }
                1 -> {
                    player<Quiz>("Hey, hand, do you want to know how I slayed you?")
                    npc<Sad>("poh_mounted_crawling_hand", "I don't know, how?")
                    player<Laugh>("Because you're just a hand! You're ARMLESS!")
                }
                else -> {
                    player<Quiz>("Hey, you're just a hand, right? So what do you eat?")
                    npc<Happy>("poh_mounted_crawling_hand", "Finger food, of course!")
                }
            }
        }

        objectOperate("Talk-to", "cockatrice_head_trophy") {
            if (!inOwnHouse()) {
                player<Happy>("Hey, a cockatrice!")
                npc<Sad>("poh_mounted_cockatrice", "${ownerName()} deaded me! That wasn't very nice!")
                return@objectOperate
            }
            npc<Neutral>("poh_mounted_cockatrice", "You deaded me!")
            player<Neutral>("Well, yes.")
            npc<Sad>("poh_mounted_cockatrice", "What did you do that for?")
            choice {
                option<Neutral>("A slayer master told me to.") {
                    npc<Sad>("poh_mounted_cockatrice", "Why do the slayer masters all pick on poor cockatrice?")
                    player<Neutral>("They pick on lots of other creatures too.")
                    npc<Sad>("poh_mounted_cockatrice", "Then mount one of them on your wall and let poor cockatrice rest in peace!")
                }
                option<Neutral>("So I could mount your head on my wall.") {
                    npc<Sad>("poh_mounted_cockatrice", "Another cockatrice falls victim to the dreaded mirror shield!")
                    player<Happy>("Don't take it personally! You look good on my wall!")
                    npc<Neutral>("poh_mounted_cockatrice", "I don't care! I think I looked better with a body!")
                }
                option<Neutral>("I just wanted to.") {
                    npc<Neutral>("poh_mounted_cockatrice", "You dirty rotten swine, you!")
                    player<Quiz>("Steady on...")
                    npc<Neutral>("poh_mounted_cockatrice", "I will kill you with my paralyzing-type magic eyes look!")
                    npc<Neutral>("poh_mounted_cockatrice", "Dots appear in air between eyes and victim. Dot! Dot! Dotty!")
                    player<Confused>("Er, nothing's happening...")
                    npc<Neutral>("poh_mounted_cockatrice", "Concentrates mental power. Eyes narrow beak clenches veins on head stand out.")
                    npc<Neutral>("poh_mounted_cockatrice", "Strain!")
                    player<Laugh>("You're dead, cockatrice. Your eyes are glass beads. It won't work.")
                    npc<Neutral>("poh_mounted_cockatrice", "STRA-A-AIN!")
                    player<Neutral>("I think I'll leave you to it.")
                }
            }
        }

        objectOperate("Talk-to", "basilisk_head_trophy") {
            npc<Neutral>("poh_mounted_basilisk", "What do you want?")
            if (!inOwnHouse()) {
                player<Shifty>("Oh, er, nothing!")
                return@objectOperate
            }
            choice {
                option<Neutral>("I want to mock you.") {
                    npc<Neutral>("poh_mounted_basilisk", "All right. Go on then.")
                    player<Laugh>("You're a ${INSULT_ADJECTIVES.random(random)} ${INSULT_NOUNS.random(random)}-${INSULT_SUFFIXES.random(random)} ${INSULT_TARGETS.random(random)}!")
                    npc<Neutral>("poh_mounted_basilisk", "I'm going back to sleep.")
                }
                option<Neutral>("I want to apologise for killing you.") {
                    npc<Neutral>("poh_mounted_basilisk", "Go on then.")
                    player<Sad>("I'm, um, very sorry I killed you.")
                    npc<Neutral>("poh_mounted_basilisk", "Really sorry?")
                    choice {
                        option<Neutral>("No, not really!") {
                            npc<Neutral>("poh_mounted_basilisk", "I don't care.")
                        }
                        option<Neutral>("Yes, really!") {
                            npc<Neutral>("poh_mounted_basilisk", "Really really?")
                            basiliskReallyReally()
                        }
                    }
                }
                option<Neutral>("I just wanted to check that you're okay.") {
                    npc<Neutral>("poh_mounted_basilisk", "Apart from being dead and stuffed and hanging on a wall, you mean?")
                    player<Confused>("Uh... yeah, apart from that are you okay?")
                    npc<Neutral>("poh_mounted_basilisk", "Actually there's something blocking my view of the far wall.")
                    player<Quiz>("I don't see anything.")
                    npc<Neutral>("poh_mounted_basilisk", "Perhaps if you were to move to one side of me.")
                    statement("You walk to the side of the basilisk head...")
                    player<Quiz>("I still don't see anything.")
                    npc<Neutral>("poh_mounted_basilisk", "Oh, it's moved away. I can see now.")
                }
                option<Neutral>("Nothing.") {
                    npc<Neutral>("poh_mounted_basilisk", "Leave me alone.")
                }
            }
        }

        objectOperate("Talk-to", "kurask_head_trophy") {
            npc<Neutral>("poh_mounted_kurask", "I KILL YOU!!!")
            if (!inOwnHouse()) {
                player<Angry>("No, ${ownerName()} kill you!")
                return@objectOperate
            }
            player<Angry>("No, I kill you!")
            npc<Neutral>("poh_mounted_kurask", "UUUHRG! Now I kill you!")
            player<Laugh>("How are you going to do that? You're just a head on a wall!")
            npc<Neutral>("poh_mounted_kurask", "Uhhhhrrr...")
            choice {
                option<Quiz>("Why are you so violent?") {
                    npc<Neutral>("poh_mounted_kurask", "You kill me! Uuurgh! That make me angry!")
                    choice {
                        option<Neutral>("You seemed pretty angry before I killed you!") {
                            npc<Neutral>("poh_mounted_kurask", "I like angry!")
                        }
                        option<Sad>("I'm sorry I killed you.") {
                            npc<Neutral>("poh_mounted_kurask", "I hate sorry! Makes me more angry! WANT TO KILL YOU!")
                            choice {
                                option<Scared>("Please try to calm down!") {
                                    npc<Neutral>("poh_mounted_kurask", "Hate calm! Smash it! Hur hur hur!")
                                }
                                option<Neutral>("I'm not really sorry!") {
                                    npc<Neutral>("poh_mounted_kurask", "That make me more angry! Uuuurgh!")
                                    player<Quiz>("Is there anything that doesn't make you angry?")
                                    npc<Neutral>("poh_mounted_kurask", "No! I like angry! Hur hur hur!")
                                }
                            }
                        }
                        option<Laugh>("I killed you really easily!") {
                            kuraskKilledEasily()
                        }
                    }
                }
                option<Quiz>("What do you think about up there?") {
                    npc<Neutral>("poh_mounted_kurask", "Think?")
                    player<Quiz>("You know, what goes through your tiny stuffed head?")
                    npc<Neutral>("poh_mounted_kurask", "Little bugs...")
                    player<Shock>("You have bugs living in you? Eww!")
                    npc<Neutral>("poh_mounted_kurask", "Little bugs! Stomp and crush and stomp!")
                    choice {
                        option<Happy>("Yeah! Stomp the bugs!") {
                            npc<Neutral>("poh_mounted_kurask", "Stomp crush splat!")
                            npc<Neutral>("poh_mounted_kurask", "Smash! Destroy! Crunch break tear destroy splunch! Hurt wound kill hit punch stab slash kill!")
                            choice {
                                option<Quiz>("'Splunch'? That's not a word!") {
                                    npc<Neutral>("poh_mounted_kurask", "I HATE WORDS! Kill all words!")
                                }
                                option<Quiz>("You said 'kill' twice!") {
                                    npc<Neutral>("poh_mounted_kurask", "I like kill! Hur hur hur hur!")
                                }
                                option<Happy>("Yeah! Kill smash destroy!") {
                                    npc<Neutral>("poh_mounted_kurask", "Kill smash destroy! Hur hur hur!")
                                }
                            }
                        }
                        option<Quiz>("What have the bugs done to you?") {
                            npc<Neutral>("poh_mounted_kurask", "Skitter skitter through head noise in ears behind eyes.")
                            npc<Neutral>("poh_mounted_kurask", "HATE THEM! Kill kill kill!")
                        }
                        option<Laugh>("You can't, you've got no feet!") {
                            npc<Sad>("poh_mounted_kurask", "No feet...")
                            npc<Neutral>("poh_mounted_kurask", "Hate lack of feet! Stomp lack of feet! Kill crush destroy smash!")
                            player<Confused>("That makes no sense! You can't destroy the absence of something!")
                            npc<Neutral>("poh_mounted_kurask", "Hate requirement to make sense! Smash it kill it destroy kill kill!")
                            player<Confused>("You can't physically destroy an abstract concept! It's impossible!")
                            npc<Neutral>("poh_mounted_kurask", "Hate abstract concepts! Hate impossible! Kill kill kill destroy smash!")
                            player<Bored>("This is getting both surreal and repetitive.")
                        }
                    }
                }
                option<Laugh>("I killed you really easily!") {
                    kuraskKilledEasily()
                }
            }
        }

        objectOperate("Talk-to", "abyssal_head_trophy") {
            if (!inOwnHouse()) {
                player<Amazed>("${ownerName()} killed an abyssal demon! Cool!")
                npc<Neutral>("poh_mounted_abyssal", "Cool for ${ownerObjectPronoun()} maybe. How would you like to be stuck on a wall?")
                return@objectOperate
            }
            npc<Neutral>("poh_mounted_abyssal", "Have you considered visiting THE ABYSS?")
            choice {
                option<Happy>("I visit the abyss all the time!") {
                    npc<Neutral>("poh_mounted_abyssal", "I bet you just rush through it though. Everyone there is in such a rush. No one stops to appreciate the beauty of THE ABYSS.")
                    choice {
                        option<Neutral>("I have to run through it quickly or I'll die!") {
                            npc<Neutral>("poh_mounted_abyssal", "Death is a small thing compared to the beauty of THE ABYSS.")
                        }
                        option<Neutral>("The abyss looks pretty ugly to me.") {
                            npc<Sad>("poh_mounted_abyssal", "Poor deluded fool. There is no hope for you at all.")
                        }
                    }
                }
                option<Scared>("It's too scary for me!") {
                    npc<Neutral>("poh_mounted_abyssal", "But does not the fear contribute to your appreciation of THE ABYSS?")
                    choice {
                        option<Neutral>("No, it's just scary.") {
                            npc<Neutral>("poh_mounted_abyssal", "Poor human. You must not judge THE ABYSS by the standards of this world. You must learn to embrace your fear as part of the experience of THE ABYSS.")
                        }
                        option<Neutral>("I suppose fear does heighten the senses.") {
                            npc<Neutral>("poh_mounted_abyssal", "Then you should enhance them further by raising the stakes. Next time you go to THE ABYSS you should take all your most valuable items with you.")
                        }
                    }
                }
                option<Quiz>("Could I get an abyssal whip?") {
                    npc<Neutral>("poh_mounted_abyssal", "You must take all your gold and all your most valued items, and take them into THE ABYSS without weapons or armour.")
                    player<Quiz>("And then will I get an abyssal whip?")
                    npc<Neutral>("poh_mounted_abyssal", "You'll get an ABYSSAL WHIPPING!")
                    player<Unamused>("That pun was abyssmal.")
                }
            }
        }

        objectOperate("Talk-to", "kbd_heads_trophy") {
            if (!inOwnHouse()) {
                player<Amazed>("Hey, ${ownerName()} killed the King Black Dragon!")
                npc<Neutral>("poh_mounted_kbd_middle", "No ${ownerSubjectPronoun()} didn't!")
                npc<Neutral>("poh_mounted_kbd_left", "What? Oh, ah, no, of course ${ownerSubjectPronoun()} didn't. We're actually an artificial likeness of the King Black Dragon. No one could really kill the King Black Dragon!")
                npc<Neutral>("poh_mounted_kbd_middle", "No! We're - I mean, it's - far too powerful!")
                npc<Neutral>("poh_mounted_kbd_right", "What are you talking about? Of course we're the King Black Dragon!")
                npc<Neutral>("poh_mounted_kbd_middle", "Shut up, you idiot!")
                return@objectOperate
            }
            npc<Neutral>("poh_mounted_kbd_middle", "What?")
            choice {
                option<Quiz>("How do you feel about all the more powerful monsters?") {
                    npc<Neutral>("poh_mounted_kbd_left", "There are no monsters more powerful than us!")
                    npc<Neutral>("poh_mounted_kbd_middle", "We're the top monster of all Gielinor!")
                    player<Neutral>("No you're not. The Kalphite Queen is more powerful than you!")
                    npc<Neutral>("poh_mounted_kbd_middle", "Kalphite Queen? What's that?")
                    player<Neutral>("She's a giant insect who lives in the desert.")
                    npc<Neutral>("poh_mounted_kbd_middle", "An insect?")
                    npc<Neutral>("poh_mounted_kbd_right", "Ha ha ha ha!")
                    npc<Neutral>("poh_mounted_kbd_left", "No insect could be tougher than us! We're the best!")
                    player<Neutral>("No, she's way tougher than you!")
                    npc<Neutral>("poh_mounted_kbd_left", "I don't believe it!")
                    npc<Neutral>("poh_mounted_kbd_middle", "And even if this Kalphite Queen is real, which I doubt, second best isn't bad, is it?")
                    player<Neutral>("But it's not just the Kalphite Queen. What about the TzTok-Jad?")
                    npc<Neutral>("poh_mounted_kbd_left", "Never heard of it!")
                    player<Neutral>("Or the Dagannoth Rex?")
                    npc<Neutral>("poh_mounted_kbd_right", "You're making it up!")
                    player<Neutral>("Or the Chaos Elemental?")
                    npc<Neutral>("poh_mounted_kbd_middle", "Now then, how do you know you're not just making all these monsters up to demoralise us?")
                    player<Quiz>("All right then, what about me?")
                    npc<Neutral>("poh_mounted_kbd_left", "Puny human! You're not a fearsome monster!")
                    player<Happy>("I defeated you, didn't I? So I must be stronger than you!")
                    npc<Neutral>("poh_mounted_kbd_left", "You got lucky! I'll get you next time!")
                    player<Laugh>("Now that you're a stuffed head? I don't think so!")
                }
                option<Quiz>("Which of you heads is...") {
                    npc<Neutral>("poh_mounted_kbd_left", "I am!")
                    npc<Neutral>("poh_mounted_kbd_right", "Shut up! I am!")
                    npc<Neutral>("poh_mounted_kbd_middle", "Don't be silly! It's obvious that I am!")
                    player<Confused>("But you don't even know what I was going to say!")
                    npc<Neutral>("poh_mounted_kbd_middle", "It doesn't matter. I'm the strongest, cleverest, and most attractive. Whatever it is, I am the most of it!")
                    npc<Neutral>("poh_mounted_kbd_left", "Just a minute. What if it's something bad?")
                    npc<Neutral>("poh_mounted_kbd_middle", "Good point. What is it you were going to say? Because if it's something good, I'm it, but if it's something bad then it's one of these two ugly mugs.")
                    player<Neutral>("I've forgotten what I was going to ask now.")
                    npc<Neutral>("poh_mounted_kbd_right", "Me! I am!")
                    npc<Neutral>("poh_mounted_kbd_middle", "What?")
                    npc<Neutral>("poh_mounted_kbd_right", "Sorry, just said that on reflex.")
                }
            }
        }

        objectOperate("Talk-to", "kq_head_trophy") {
            npc<Angry>("poh_mounted_kq", "Soft-thing! How dare you approach the queen of the kalphite?")
            if (!inOwnHouse()) {
                player<Neutral>("${ownerName()} killed you! I can do what I like.")
                npc<Angry>("poh_mounted_kq", "${ownerName()} killed me but you could not. My successor will be as strong as me. Come down to meet her, she is unafraid.")
                return@objectOperate
            }
            player<Neutral>("I killed you, remember?")
            npc<Angry>("poh_mounted_kq", "Yes, you killed this queen, but by now another will have risen up! One kalphite may die but the hive goes on!")
            npc<Angry>("poh_mounted_kq", "The kalphite race grows stronger every day, and our young feed on the blood of the soft-things that invade from above!")
            npc<Angry>("poh_mounted_kq", "Someday we will overrun the world again and all soft creatures will die. But we will reserve the worst fate for those who have killed a queen and hung her head in their house.")
            npc<Angry>("poh_mounted_kq", "We will lay our eggs in your brain and you will not die until it explodes and a million kalphite emerge to make a grand hive of all the world!")
            choice {
                option<Neutral>("You don't scare me!") {
                    npc<Angry>("poh_mounted_kq", "Your pitiful misplaced confidence is irrelevant. You will all die!")
                }
                option<Scared>("Please don't kill me!") {
                    npc<Laugh>("poh_mounted_kq", "Ha ha ha! It is too late for pleading now, pathetic mammal! Your fate is sealed!")
                }
            }
        }

        objectOperate("Rub", "amulet_of_glory_mounted") {
            rubGlory()
        }
    }

    private suspend fun Player.basiliskReallyReally() {
        choice {
            option<Neutral>("Yes, really really!") {
                npc<Neutral>("poh_mounted_basilisk", "Fat lot of good that does, I'm still dead.")
                choice {
                    option<Neutral>("I'm not THAT sorry!") {
                        npc<Neutral>("poh_mounted_basilisk", "I don't care.")
                    }
                    option<Quiz>("But will you forgive me?") {
                        npc<Neutral>("poh_mounted_basilisk", "Of course I'll forgive you!")
                        player<Happy>("Really?")
                        npc<Neutral>("poh_mounted_basilisk", "No!")
                    }
                    option<Neutral>("I promise not to do it again.") {
                        npc<Neutral>("poh_mounted_basilisk", "Of course you won't do it again, you can only kill me once.")
                        choice {
                            option<Neutral>("That's why I won't do it again! There'd be no point!") {
                                npc<Neutral>("poh_mounted_basilisk", "I don't care.")
                            }
                            option<Neutral>("But I won't do it to any other basilisks!") {
                                npc<Neutral>("poh_mounted_basilisk", "Really?")
                                choice {
                                    option<Happy>("Yes, really!") {
                                        npc<Neutral>("poh_mounted_basilisk", "All right then. Apology accepted. Now leave me alone.")
                                    }
                                    option<Neutral>("No, not really!") {
                                        npc<Neutral>("poh_mounted_basilisk", "I don't care.")
                                    }
                                    option<Angry>("Don't start that again!") {
                                        npc<Neutral>("poh_mounted_basilisk", "Leave me alone then.")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            option<Angry>("Don't push it!") {
                npc<Neutral>("poh_mounted_basilisk", "I don't care anyway.")
            }
        }
    }

    private suspend fun Player.kuraskKilledEasily() {
        npc<Neutral>("poh_mounted_kurask", "Uhhhhrrr...")
        player<Laugh>("Yeah! I could kill you again in my sleep! I think I might go off and kill some other kurask!")
        npc<Neutral>("poh_mounted_kurask", "Uuuurrrrh! Hate you!")
        player<Laugh>("What are you going to do about it? Eh? I totally owned you!")
        npc<Neutral>("poh_mounted_kurask", "Hate you hate you hate you!!!")
    }

    private fun Player.ownerName(): String = houseOwner()?.name ?: get("house_owner") ?: "Someone"

    private fun Player.ownerObjectPronoun(): String {
        val owner = houseOwner() ?: return "them"
        return if (owner.male) "him" else "her"
    }

    private fun Player.ownerSubjectPronoun(): String {
        val owner = houseOwner() ?: return "they"
        return if (owner.male) "he" else "she"
    }

    private suspend fun Player.rubGlory() {
        choice("Where would you like to teleport to?") {
            option("Edgeville") {
                gloryTeleport("edgeville_teleport")
            }
            option("Karamja") {
                gloryTeleport("karamja_teleport")
            }
            option("Draynor Village") {
                gloryTeleport("draynor_village_teleport")
            }
            option("Al Kharid") {
                gloryTeleport("al_kharid_teleport")
            }
            option("Nowhere")
        }
    }

    private fun Player.gloryTeleport(area: String) {
        message("You rub the amulet...", ChatType.Filter)
        Teleport.teleport(this, area, "jewellery")
    }

    private companion object {
        val INSULT_ADJECTIVES = listOf("boring", "fat", "hideous", "puny", "smelly", "stupid")
        val INSULT_NOUNS = listOf("beetle", "chicken", "egg", "mud", "slime", "worm")
        val INSULT_SUFFIXES = listOf("brained", "eating", "like", "loving", "smelling", "witted")
        val INSULT_TARGETS = listOf("basilisk", "idiot", "lizard", "mudworm", "slimeball", "weakling")
    }
}
