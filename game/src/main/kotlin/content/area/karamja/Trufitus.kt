package content.area.karamja

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Trufitus : Script {

    init {
        npcOperate("Talk-to", "trufitus_tai_bwo_wannai") {
            when (quest("jungle_potion")) {
                "unstarted" -> {
                    npc<Happy>("Greetings Bwana! I am Trufitus Shakaya of the Tai Bwo Wannai village.")
                    npc<Happy>("Welcome to our humble village.")
                    choice {
                        option<Happy>("What does Bwana mean?") {
                            npc<Happy>("Gracious ${if (male) "sir" else "lady"}, it means friend. And friends come in peace. I assume that you come in peace?")
                            choice {
                                option<Happy>("Yes, of course I do.") {
                                    if (has(Skill.Herblore, 3)) {
                                        npc<Happy>("Well, that is good news, as I may have a proposition for you.")
                                        choice {
                                            option<Happy>("A proposition eh? Sounds interesting!") {
                                                npc<Neutral>("I hoped you would think so. My people are afraid to stay in the village.")
                                                npc<Neutral>("They have returned to the jungle and I need to commune with the gods")
                                                npc<Neutral>("to see what fate befalls us. You can help me by collecting some herbs that I need.")
                                                choice {
                                                    option<Happy>("Me? How can I help?") {
                                                        howCanIHelp()
                                                    }
                                                    option<Happy>("I am very sorry, but I don't have time for that.") {
                                                        npc<Neutral>("Very well then, may your journeys bring you much joy.")
                                                        npc<Neutral>("Maybe you will pass this way again and you then take up my proposal?")
                                                        npc<Neutral>("But for now, fare thee well.")
                                                    }
                                                }
                                            }
                                            noSorry()
                                        }
                                    } else {
                                        npc<Happy>("Well, that is good news. Now, I'm sorry to cut this conversation short, but there is something important I need to do.")
                                        statement("You do not meet all of the requirements to start the Jungle Potion quest.")
                                    }
                                }
                                option<Happy>("What does a warrior like me know about peace?") {
                                    npc<Sad>("When you grow weary of violence and seek a more enlightened path, please pay me a visit")
                                    npc<Sad>("as I may have a proposition for you.  Now I need to attend to the plight of my people. Please excuse me...")
                                }
                            }
                        }
                        option<Happy>("Tai Bwo Wannai? What does that mean?") {
                            npc<Happy>("It means 'small clearing in the jungle' but it is now the name of our village.")
                            choice {
                                option<Happy>("It's a nice village, where is everyone?") {
                                    whereIsEveryone()
                                }
                                noSorry()
                            }
                        }
                        option<Happy>("It's a nice village, where is everyone?") {
                            whereIsEveryone()
                        }
                    }
                }
                "started", "found_snake_weed" -> {
                    npc<Neutral>("Hello Bwana, do you have the Snake Weed?")
                    choice {
                        option<Happy>("Of course!") {
                            if (inventory.contains("clean_snake_weed")) {
                                inventory.remove("clean_snake_weed")
                                set("jungle_potion", "gave_snake_weed")
                                item("clean_snake_weed","You give the Snake Weed to Trufitus.")
                                npc<Neutral>("Great, you have the Snake Weed! Many thanks. Ok, the next herb is called Ardrigal. It is related to the palm and grows to the east in its brother's shady profusion.")
                                npc<Neutral>("To the east you will find a small peninsula, it is just after the cliffs come down to meet the sands, here is where you should search for it.")
                            } else if (inventory.contains("grimy_snake_weed")) {
                                npc<Confused>("Sorry, Bwana, that herb is so dirty that I can't even tell whether it is fresh. Please clean it first.")
                            } else {
                                npc<Neutral>("Please don't try to deceive me.")
                                npc<Neutral>("I really need that Snake Weed if I am to make this potion.")
                            }
                        }
                        option<Happy>("Not yet, sorry, what's the clue again?") {
                            npc<Neutral>("It grows near vines in an area to the south west where the ground turns soft and the water kisses your feet.")
                            npc<Neutral>("I really need that Snake Weed if I am to make this potion.")
                        }
                    }
                }
                "gave_snake_weed", "found_ardrigal" -> {
                    npc<Neutral>("Hello Bwana, have you been able to get the Ardrigal?")
                    choice {
                        option<Happy>("Of course!") {
                            if (inventory.contains("clean_ardrigal")) {
                                inventory.remove("clean_ardrigal")
                                set("jungle_potion", "gave_ardrigal")
                                item("clean_ardrigal","You give the Ardrigal to Trufitus.")
                                npc<Neutral>("Great, you have the Ardrigal! Many thanks.")
                                npc<Neutral>("You are doing well Bwana. The next herb is called Sito Foil, and it grows best where the ground has been blackened by the living flame.")
                            } else if (inventory.contains("grimy_ardrigal")) {
                                npc<Confused>("Sorry, Bwana, that herb is so dirty that I can't even tell whether it is fresh. Please clean it first.")
                            } else {
                                npc<Neutral>("Please don't try to deceive me.")
                                npc<Neutral>("I really need that Ardrigal if I am to make this potion.")
                            }
                        }
                        option<Happy>("Not yet, sorry, what's the clue again?") {
                            npc<Neutral>("You are looking for Ardrigal. It is related to the palm  and grows in its brothers shady profusion.")
                            npc<Neutral>("To the east you will find a small peninsula, it is just after the cliffs come down to meet the sands, here is where you should search for it.")
                            npc<Neutral>("I really need that Ardrigal if I am to make this potion.")
                        }
                    }
                }
                "gave_ardrigal", "found_sito_foil" -> {
                    npc<Neutral>("Greetings Bwana, have you been successful in getting the Sito Foil?")
                    choice {
                        option<Happy>("Of course!") {
                            if (inventory.contains("clean_sito_foil")) {
                                inventory.remove("clean_sito_foil")
                                set("jungle_potion", "gave_sito_foil")
                                item("clean_sito_foil","You give the Sito Foil to Trufitus.")
                                npc<Neutral>("Well done Bwana, just two more herbs to collect.")
                                npc<Neutral>("The next herb is called Volencia Moss. It clings to rocks for its existence. It is difficult to see, so you must search for it well.")
                                npc<Neutral>("It prefers rocks of high metal content and a frequently disturbed environment. There is some, I believe to the south east of this village.")
                            } else if (inventory.contains("grimy_sito_foil")) {
                                npc<Confused>("Sorry, Bwana, that herb is so dirty that I can't even tell whether it is fresh. Please clean it first.")
                            } else {
                                npc<Neutral>("Please don't try to deceive me.")
                                npc<Neutral>("I really need that Sito Foil if I am to make this potion.")
                            }
                        }
                        option<Happy>("Not yet, sorry, what's the clue again?") {
                            npc<Neutral>("You are looking for Sito Foil, and it grows best where the ground has been blackened by the living flame.")
                            npc<Neutral>("I really need that Sito Foil if I am to make this potion.")
                        }
                    }
                }
                "gave_sito_foil", "found_volencia_moss" -> {
                    npc<Neutral>("Greetings Bwana, have you been successful in getting the Volencia Moss?")
                    choice {
                        option<Happy>("Of course!") {
                            if (inventory.contains("clean_volencia_moss")) {
                                inventory.remove("clean_volencia_moss")
                                set("jungle_potion", "gave_volencia_moss")
                                item("clean_volencia_moss","You give the Volencia Moss to Trufitus.")
                                npc<Neutral>("Ah Volencia Moss, beautiful. One final herb and the potion will be complete. This is the most difficult to find as it inhabits the darkness of the underground. It is called Rogue's Purse, and is only to be found in")
                                npc<Neutral>("caverns in the northern part of this island. A secret entrance to the caverns is set into the northern cliffs of this land. Take care Bwana as it may be dangerous.")
                            } else if (inventory.contains("grimy_volencia_moss")) {
                                npc<Confused>("Sorry, Bwana, that herb is so dirty that I can't even tell whether it is fresh. Please clean it first.")
                            } else {
                                npc<Neutral>("Please don't try to deceive me! I really need that Volencia Moss if I am to make this potion.")
                            }
                        }
                        option<Happy>("Not yet, sorry, what's the clue again?") {
                            npc<Neutral>("You are looking for Volencia Moss. It clings to rocks for its existence. It is difficult to see, so you must search for it well.")
                            npc<Neutral>("It prefers rocks of high metal content and a frequently disturbed environment. There is some, I believe to the south east of this village.")
                            npc<Neutral>("I really need that Volencia Moss if I am to make this potion.")
                        }
                    }
                }
                "gave_volencia_moss", "found_rogues_purse" -> {
                    npc<Neutral>("Greetings Bwana, have you been successful in getting the Rogue's Purse?")
                    choice {
                        option<Happy>("Of course!") {
                            if (inventory.contains("clean_rogues_purse")) {
                                inventory.remove("clean_rogues_purse")
                                set("jungle_potion", "gave_rogues_purse")
                                npc<Neutral>("Most excellent Bwana! You have returned all the herbs to me and, I can finish the preparations for the potion, and at last divine with the gods.")
                                npc<Neutral>("Many blessings on you! I must now prepare, please excuse me while I make the arrangements.")
                                statement("Trufitus shows you some techniques in Herblore. You gain some experience in Herblore")
                                questComplete()
                            } else if (inventory.contains("grimy_volencia_moss")) {
                                npc<Confused>("Sorry, Bwana, that herb is so dirty that I can't even tell whether it is fresh. Please clean it first.")
                            } else {
                                npc<Neutral>("Please don't try to deceive me, I really need that Rogue's Purse if I am to make this potion.")
                            }
                        }
                        option<Happy>("Not yet, sorry, what's the clue again?") {
                            npc<Neutral>("You are looking for Rogues Purse.")
                            npc<Neutral>("It inhabits the darkness of the underground, and grows in caverns to the north. A secret entrance to the caverns is set into the northern cliffs, be careful Bwana.")
                            npc<Neutral>("I really need that Rogues Purse if I am to make this potion.")
                        }
                    }
                }
                "gave_rogues_purse" -> {//todo find the right dialogue. added so the quest can be completed
                    npc<Neutral>("I must now prepare, please excuse me while I make the arrangements.")
                    statement("Trufitus shows you some techniques in Herblore. You gain some experience in Herblore")
                    questComplete()
                } else -> completed()
            }
        }
    }

    suspend fun Player.whereIsEveryone() {
        npc<Sad>("My people are afraid to stay in the village. They have returned to the jungle. I need to commune with the gods to see what fate befalls us.")
        if (has(Skill.Herblore, 3)) {
            npc<Sad>("You may be able to help with this.")
            choice {
                option<Happy>("Me? How can I help?") {
                    howCanIHelp()
                }
                noSorry()
            }
        } else {
            player<Quiz>("Can I help?")
            npc<Neutral>("You are kind for asking, but I don't think so.")
            statement("You do not meet all of the requirements to start the Jungle Potion quest.")
        }
    }

    fun ChoiceOption.noSorry(): Unit = option<Happy>("I am sorry, but I am very busy.") {
        npc<Neutral>("Very well then, may your journeys bring you much joy.")
        npc<Neutral>("Maybe you will pass this way again and you then take up my proposal?")
        npc<Neutral>("But for now, fare thee well.")
    }

    suspend fun Player.howCanIHelp() {
        npc<Neutral>("I need to make a special brew! A potion that helps me to commune with the gods. For this potion, I need very special herbs, that are only found in the deep jungle.")
        npc<Neutral>("I can only guide you so far as the herbs are not easy to find. With some luck, you will find each herb in turn and bring it to me. I will then give you details of where to find the next herb.")
        npc<Neutral>("In return for this great favour I will give you training in Herblore.")
        choice("Start the Jungle Potion quest?") {
            option("Yes.") {
                player<Happy>("It sounds like just the challenge for me. And it would make a nice break from killing things!")
                set("jungle_potion", "started")
                refreshQuestJournal()
                npc<Neutral>("That is excellent Bwana! The first herb that you need to gather is called")
                npc<Neutral>("Snake Weed.")
                npc<Neutral>("It grows near vines in an area to the south west where")
                npc<Neutral>("the ground turns soft and the water kisses your feet.")
            }
            option("No.") {
                player<Neutral>("Hmmm, sounds difficult, I don't know if I am ready for the challenge.")//todo find expression
                npc<Neutral>("Very well then Bwana, maybe you will return to me invigorated and ready to take up the challenge one day?")//todo find expression
            }
        }
    }
    suspend fun Player.completed() {
        npc<Neutral>("My greatest respects Bwana, I have communed with my gods and the future")
        npc<Neutral>("looks good for my people. We are happy now that the gods are not angry with us.")
        npc<Neutral>("With some blessings we will be safe here.")
        npc<Neutral>("You should deliver the good news to Bwana Timfraku, Chief of Tai Bwo Wannai.")
        //set("jungle_potion", "13") //todo find out why it sets jungle_potion to 13
    }

    fun Player.questComplete() {
        AuditLog.event(this, "quest_completed", "jungle_potion")
        set("jungle_potion", "completed")
        set("sharimika", "trading_stick")
        set("mamma_bufetta", "trading_stick")
        set("layleen", "trading_stick")
        set("karaday", "trading_stick")
        set("safta_doc", "trading_stick")
        set("gabooty", "trading_stick")
        set("fanellaman", "trading_stick")
        set("jagbakoba", "trading_stick")
        set("murcaily", "trading_stick")
        set("rionasta", "trading_stick")
        jingle("quest_complete_2")
        exp(Skill.Herblore, 775.0)
        inc("quest_points")
        message("Congratulations, you've completed a quest: <navy>Jungle Potion")
        refreshQuestJournal()
        questComplete(
            "Jungle Potion",
            "1 Quest Point",
            "775 Herblore XP",
            item = "clean_marrentill",
        )
    }
}
