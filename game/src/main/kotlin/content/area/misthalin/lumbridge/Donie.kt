package content.area.misthalin.lumbridge

import content.entity.player.dialogue.Afraid
import content.entity.player.dialogue.Amazed
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Chuckle
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.type.PlayerChoice
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.random

@Script
class Donie {

    init {
        npcOperate("Talk-to", "donie") {
            npc<Happy>("Hello there, can I help you?")
            when (random.nextInt(0, 4)) {
                0 -> choice {
                    whereAmI()
                    howToday()
                    anyQuests()
                    shoeLace()
                }
                1 -> choice {
                    freeStuff()
                    anyQuests()
                    buyStick()
                }
                2 -> choice {
                    whereAmI()
                    howToday()
                    anyQuests()
                    hairCut()
                }
                3 -> choice {
                    anyQuests()
                    buyStick()
                }
            }
        }
    }

    suspend fun PlayerChoice.whereAmI(): Unit = option<Quiz>("Where am I") {
        npc<Chuckle>("This is the town of Lumbridge my friend.")
        choice {
            howToday()
            anyQuests()
            shoeLace()
        }
    }

    suspend fun PlayerChoice.howToday(): Unit = option<Quiz>("How are you today?") {
        npc<Happy>("Aye, not too bad thank you. Lovely weather in Gielinor this fine day.")
        player<Chuckle>("Weather?")
        npc<Chuckle>("Yes weather, you know.")
        npc<Quiz>("The state or condition of the atmosphere at a time and place, with respect to variables such as temperature, moisture, wind velocity, and barometric pressure.")
        player<Quiz>("...")
        npc<Chuckle>("Not just a pretty face eh? Ha ha ha.")
    }

    suspend fun PlayerChoice.anyQuests(): Unit = option<Quiz>("Are there any quests I can do here?") {
        npc<Quiz>("What kind of quest are you looking for?")
        choice {
            option<Happy>("I fancy a bit of fight, anything dangerous?") {
                npc<Quiz>("Hmm.. dangerous you say? What sort of creatures are you looking to fight?")
                choice {
                    option<Happy>("Big scary demons!") {
                        npc<Chuckle>("You are a brave soul indeed.")
                        npc<Uncertain>("Now that you mention it, I heard a rumour about a fortune-teller in Varrock who is rambling about some kind of grater evil.. sounds demon-like if you ask me.")
                        npc<Quiz>("Perhaps you could check it out if you are as brave as you say?")
                        if (player.questCompleted("demon_slayer")) {
                            player<Chuckle>("I've already killed the demon Delrith. He was merely a stain on my sword when I was finished with him!")
                            npc<Happy>("Well done! However I'm sure if you search around the world you will find more challenging foes to slay.")
                        } else {
                            player<Happy>("Thanks for the tip, perhaps I will.")
                        }
                    }
                    option<Amazed>("Vampyres!") {
                        npc<Chuckle>("Ha ha. I personally don't believe in such things. However, there is a man in Draynor Village who has been scaring the village folk with stories of vampyres.")
                        npc<Happy>("He's named Morgan and can be found in one of the village houses. Perhaps you could see what the matter is?")
                        if (player.questCompleted("vampire_slayer")) {
                            player<Chuckle>("Oh i have already killed that nasty blood-sucking vampyre. Draynor will be safe now.")
                            npc<Chuckle>("Yeah, yeah of course you did. Everyone knows vampyres are not real...")
                            player<Angry>("What! I did slay the beast..I really did.")
                            npc<Chuckle>("You're not fooling anyone you know.")
                            player<Angry>("..Huh.. But... Hey! I did... believe what you like.")
                        } else {
                            player<Happy>("Thanks for the tip.")
                        }
                    }
                    option<Happy>("Small.. something small would be good.") {
                        npc<Quiz>("Small? Small isn't really that dangerous though is it?")
                        player<Angry>("Yes it can be! There could be anything from an evil chicken to a poisonous spider. They attack in numbers you know!")
                        npc<Happy>("Point taken. Speaking of small monsters, I hear old Wizard Mizgog in the Wizards' Tower has just had his beads taken by a gang of mischievous imps.")
                        npc<Happy>("Sounds like it could be a quest for you?")
                        if (player.questCompleted("imp_catcher")) {
                            player<Happy>("Yes, I know of Mizgog and have already helped him with his imp problem.")
                            npc<Chuckle>("Imps will be imps!")
                        } else {
                            player<Happy>("Thanks for your help.")
                        }
                    }
                    option<Happy>("Maybe another time.")
                }
            }
            option<Happy>("Something easy please, I'm new here.") {
                npc<Happy>("I can tell you about plenty of small easy tasks.")
                npc<Uncertain>("The Lumbridge cook has been having problems and the Duke is confused over some strange rocks")
                choice("Tell me about..") {
                    option("The Lumbridge cook.") {
                        player<Happy>("Tell me about the Lumbridge cook.")
                        npc<Chuckle>("It's funny really, the cook would forget his head if it wasn't screwed on. This time he forgot to get ingredients for the Duke's birthday cake.")
                        npc<Quiz>("Perhaps you could help him? You will probably find him in the Lumbridge Castle kitchen.")
                        if (player.questCompleted("cooks_assistant")) {
                            player<Happy>("I have already helped the cook in Lumbridge")
                            npc<Happy>("Oh yes, so you have. I am sure the Duke will be pleased.")
                        } else {
                            player<Happy>("Thank you. I shall go speak with him.")
                        }
                    }
                    option("The Duke's strange stones.") {
                        player<Happy>("Tell me about the Duke's strange stones.")
                        npc<Happy>("Well the Duke of Lumbridge has found a strange stone that no one seems to understand. Perhaps you could help him? You can probably find him upstairs in Lumbridge Castle.")
                        if (player.questCompleted("rune_mysteries")) {
                            player<Happy>("Yes, I have already solved the rune mysteries.")
                            npc<Happy>("Ah excellent. Thank you very much adventurer.")
                        } else {
                            player<Quiz>("Sounds mysterious. I may just do that. Thanks.")
                        }
                    }
                    option<Happy>("Maybe another time.")
                }
            }
            option<Happy>("I'm a thinker rather than a fighter, anything skill oriented?") {
                npc<Uncertain>("Skills play a big part when you want to progress in knowledge. I know of a few skill-related quests that can get you started.")
                npc<Happy>("You may be able to help out Fred the farmer who is in need of someones crafting expertise.")
                npc<Happy>("Or, there's always Doric the dwarf who needs an errand running for him?")
                choice("Tell me about..") {
                    option("Fred the farmer.") {
                        player<Happy>("Tell me about Fred the farmer please.")
                        npc<Uncertain>("You can find Fred next to the field of sheep in Lumbridge. Perhaps you should go and speak with him.")
                        if (player.questCompleted("sheep_shearer_miniquest")) {
                            player<Happy>("I have already helped Fred the farmer. I sheared his sheep and made 20 balls of wool for him.")
                            player<Sad>("He wouldn't let me kill his chickens though.")
                            npc<Chuckle>("Lumbridge chickens do make good target practice.")
                            npc<Happy>("You will have to wait until he isn't looking.")
                        } else {
                            player<Happy>("Thanks, maybe I will.")
                        }
                    }
                    option("Doric the dwarf.") {
                        player<Happy>("Tell me about Doric the dwarf.")
                        npc<Happy>("Doric the dwarf is located north of Falador. He might be able to help you with smithing. You should speak to him. He may let you use his anvils.")
                        if (player.questCompleted("dorics_quest")) {
                            player<Happy>("Yes, I've been to see Doric already. He was happy to let me use his anvils after I ran a small errand for him.")
                            npc<Happy>("Oh, good. Thank you ${player.name}!")
                        } else {
                            player<Happy>("Thanks for the tip.")
                        }
                    }
                    option<Happy>("Maybe another time.")
                }
            }
            option<Happy>("I want to do all kinds of things, do you know of anything like that?") {
                npc<Happy>("Of course I do. Gielinor is a huge place you know, now let me think...")
                npc<Happy>("Hetty the witch in Rimmington might be able to offer help in the ways of magical abilities..")
                npc<Happy>("Also, pirates are currently docked in Port Sarim, Where pirates are, treasure is never far away...")
                npc<Happy>("Or you could go and help out Ernest who got lost in Draynor Manor, spooky place that.")
                choice("Tell me about..") {
                    option("Hetty the witch.") {
                        player<Happy>("Tell me about Hetty the witch.")
                        npc<Happy>("Hetty the witch can be found in Rimmington, south of Falador. She's currently working on some new potions. Perhaps you could give her a hand? She might be able to offer help with your magical abilities.")
                        if (player.questCompleted("witches_potion_miniquest")) {
                            player<Happy>("Yes, I have already been to see Hetty. She gave me super cosmic powers after I helped out with her potion! I could probably destroy you with a single thought.")
                            npc<Afraid>("Did she really?")
                            player<Chuckle>("No, not really.")
                            npc<Angry>("Right...")
                        } else {
                            player<Happy>("Thanks. Let's hope she doesn't turn me into a potato or something..")
                        }
                    }
                    option("Pirate's Treasure.") {
                        player<Happy>("Tell me about Pirate's Treasure.")
                        npc<Happy>("RedBeard Frank in Port Sarim's bar, the Rusty Anchor might be able to tell you about the rumored treasure that is buried somewhere in the world.")
                        if (player.questCompleted("pirates_treasure")) {
                            player<Angry>("Yarr! I already found the booty!")
                            npc<Chuckle>("Yarr indeed my friend. A most excellent find.")
                            player<Angry>("Yarr!")
                            npc<Chuckle>("Yarrr!")
                            player<Angry>("YARRR!")
                            npc<Angry>("Right, that's enough of that!")
                            player<Sad>("..Sorry.")
                        } else {
                            player<Happy>("Sounds adventurous, I may have to check that out. Thank you.")
                        }
                    }
                    option("Ernest and Draynor Manor.") {
                        player<Happy>("Tell me about Ernest please.")
                        npc<Happy>("The best place to start would be at the gate to Draynor Manor. There you will find Veronica who will be able to tell you more.")
                        npc<Happy>("I suggest you tread carefully in that place; it's haunted.")
                        if (player.questCompleted("ernest_the_chicken")) {
                            player<Happy>("Yeah, I found Ernest already. Professor Oddstein had turned him into a chicken!")
                            npc<Chuckle>("A chicken!?")
                            player<Happy>("Yeah a chicken. It could have been worse though.")
                            npc<Chuckle>("Very true, poor guy.")
                        } else {
                            player<Quiz>("Sounds like fun. I've never been to a Haunted Manor before.")
                        }
                    }
                    option<Happy>("Maybe another time.")
                }
            }
            option<Happy>("Maybe another time.")
        }
    }

    suspend fun PlayerChoice.shoeLace(): Unit = option<Chuckle>("Your shoe lace is untied.") {
        npc<Angry>("No it's not!")
        player<Chuckle>("No you're right. I have nothing to back that up.")
        npc<Angry>("Fool! Leave me alone!")
    }

    suspend fun PlayerChoice.buyStick(): Unit = option<Quiz>("Can i buy your stick?") {
        npc<Angry>("It's not a stick! I'll have you know it's a very powerful staff!")
        player<Quiz>("Really? Show me what it can do!")
        npc<Sad>("Um..It's a bit low on power at the moment..")
        player<Chuckle>("It's a stick isn't it?")
        npc<Sad>("...Ok it's a stick.. But only while I save up for a staff. Zaff in Varrock square sells them in his shop.")
        player<Chuckle>("Well good luck with that.")
    }

    suspend fun PlayerChoice.freeStuff(): Unit = option<Quiz>("Do you have anything of value which I can have?") {
        npc<Quiz>("Are you asking for free stuff?")
        player<Quiz>("Well... er... yes.")
        npc<Angry>("No I do not have anything I can give you. If I did have anything of value I wouldn't want to give it away.")
    }

    suspend fun PlayerChoice.hairCut(): Unit = option<Quiz>("Where can I get a haircut like yours?") {
        npc<Happy>("Yes, it does look like you need a hairdresser.")
        player<Angry>("Oh thanks!")
        npc<Chuckle>("No problem. The hairdresser in Falador will probably be able to sort you out.")
        npc<Happy>("The Lumbridge general store sells useful maps if you don't know the way.")
    }
}
