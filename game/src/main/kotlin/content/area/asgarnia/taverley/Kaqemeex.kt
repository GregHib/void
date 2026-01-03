package content.area.asgarnia.taverley

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.softQueue

class Kaqemeex : Script {

    init {
        npcOperate("Talk-to", "kaqemeex") {
            when (quest("druidic_ritual")) {
                "unstarted" -> {
                    player<Idle>("Hello there.")
                    npc<Quiz>("What brings you to our holy monument?")
                    choice {
                        option("Who are you?") {
                            npc<Idle>("We are the druids of Guthix. We worship our god at our famous stone circles. You will find them located throughout these lands.")
                            choice {
                                option<Quiz>("What about the stone circle full of dark wizards?") {
                                    startedQuest()
                                }
                                option<Quiz>("So what's so good about Guthix?") {
                                    npc<Idle>("Guthix is the oldest and most powerful god in Gielinor. His existence is vital to this world. He is the god of balance, and nature; he is also a very part of this world.")
                                    npc<Idle>("He exists in the trees, and the flowers, the water and the rocks. He is everywhere. His purpose is to ensure balance in everything in this world, and as such we worship him.")
                                    player<Confused>("He sounds kind of boring...")
                                    npc<Idle>("Some day when your mind achieves enlightenment you will see the true beauty of his power.")
                                }
                                option<Idle>("Well, I'll be on my way now.") {
                                    npc<Idle>("Goodbye adventurer. I feel we shall meet again.")
                                }
                            }
                        }
                        option<Idle>("I'm in search of a quest.") {
                            npc<Idle>("Hmm. I think I may have a worthwhile quest for you actually. I don't know if you are familiar with the stone circle south of Varrock or not, but...")
                            startedQuest()
                        }
                        option<Quiz>("Did you build this?") {
                            npc<Idle>("What, personally? No, of course I didn't. However, our forefathers did. The first Druids of Guthix built many stone circles across these lands over eight hundred years ago.")
                            npc<Disheartened>("Unfortunately we only know of two remaining, and of those only one is usable by us anymore.")
                            choice {
                                option<Quiz>("What about the stone circle full of dark wizards?") {
                                    startedQuest()
                                }
                                option<Idle>("I'm in search of a quest.") {
                                    npc<Idle>("Hmm. I think I may have a worthwhile quest for you actually. I don't know if you are familiar with the stone circle south of Varrock or not, but...")
                                    startedQuest()
                                }
                                option<Idle>("Well, I'll be on my way now.") {
                                    npc<Idle>("Goodbye adventurer. I feel we shall meet again.")
                                }
                            }
                        }
                    }
                }
                "started", "cauldron" -> started()
                "kaqemeex" -> kaqemeex()
                else -> completed()
            }
        }
    }

    suspend fun Player.startedQuest() {
        npc<Idle>("That used to be OUR stone circle. Unfortunately, many many years ago, dark wizards cast a wicked spell upon it so that they could corrupt its power for their own evil ends.")
        npc<Idle>("When they cursed the rocks for their rituals they made them useless to us and our magics. We require a brave adventurer to go on a quest for us to help purify the circle of Varrock.")
        choice("Start the Druidic Ritual quest?") {
            option("Yes.") {
                player<Idle>("Okay, I will try and help.")
                set("druidic_ritual", "started")
                refreshQuestJournal()
                npc<Idle>("Excellent. Go to the village south of this place and speak to my fellow druid, Sanfew, who is working on the purification ritual. He knows what is required to complete it.")
                refreshQuestJournal()
                player<Idle>("Will do.")
            }
            option("No.") {
                player<Confused>("No, that doesn't sound very interesting.")
                npc<Idle>("I will not try and change your mind adventurer. Some day when you have matured you may reconsider your position. We will wait until then.")
            }
        }
    }

    suspend fun Player.started() {
        player<Idle>("Hello there.")
        npc<Idle>("Hello again, adventurer. You will need to speak to my fellow druid Sanfew in the village south of here to continue in your quest.")
        player<Happy>("Okay, thanks.")
    }

    suspend fun Player.kaqemeex() {
        player<Idle>("Hello there.")
        npc<Idle>("I have word from Sanfew that you have been very helpful in assisting him with his preparations for the purification ritual. As promised I will now teach you the ancient arts of Herblore.")
        questComplete()
    }

    suspend fun Player.completed() {
        player<Idle>("Hello there.")
        npc<Idle>("Hello again. How is the Herblore going?")
        choice {
            option<Idle>("Very well, thank you.") {
                npc<Idle>("That is good to hear.")
            }
            option<Disheartened>("I need more practice at it...") {
                npc<Idle>("Persistence is key to success.")
            }
            option<Confused>("Can you explain the fundamentals again?") {
                npc<Idle>("Indeed I will...")
                npc<Idle>("Herblore is the skill of working with herbs and other ingredients, to make useful potions and poison.")
                npc<Idle>("First you will need a vial, which can be found or made with the crafting skill.")
                npc<Idle>("Then you must gather the herbs needed to make the potion you want.")
                npc<Idle>("You must fill your vial with water and add the ingredients you need. There are normally 2 ingredients to each type of potion.")
                npc<Idle>("Bear in mind, you must first clean each herb before you can use it.")
                npc<Idle>("You may also have to grind some ingredients before you can use them. You will need a pestle and mortar in order to do this.")
                npc<Idle>("Herbs can be found on the ground, and are also dropped by some monsters when you kill them.")
                npc<Idle>("Let's try an example Attack potion: The first ingredient is Guam leaf; the next is Eye of Newt.")
                npc<Idle>("Mix these in your water-filled vial, and you will produce an Attack potion. Drink this potion to increase your Attack level.")
                npc<Idle>("Different potions also require different Herblore levels before you can make them.")
                npc<Happy>("Good luck with your Herblore practices, and a good day to you.")
                player<Happy>("Thanks for your help.")
            }
            if (hasMax(Skill.Herblore, 99)) {
                option<Quiz>("May I buy a Herblore skillcape, please?") {
                    if (inventory.spaces < 2) {
                        npc<Disheartened>("Unfortunately all Skillcapes are only available with a free hood; it's part of a skill promotion deal - buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
                        return@option
                    }
                    if (!inventory.contains("coins", 99000)) {
                        npc<Disheartened>("Most certainly, but I must ask for a donation of 99,000 coins to cover the expense of the cape.")
                        return@option
                    }
                    npc<Idle>("Most certainly; the Nardah Herbalist will recognize this cape and create unfinished potions for you and it may be searched for a pestle and mortar.")
                    npc<Idle>("It has been a pleasure to watch you grow as a herbalist. I am privileged to have been instrumental in your learning, but I must ask for a donation of 99,000 coins to cover the expense of the cape.")
                    choice {
                        option<Disheartened>("I'm afraid that's too much money for me.") {
                            npc<Idle>("Not at all; there are many other adventurers who would love the opportunity to purchase such a prestigious item. You can find me here if you change your mind.")
                        }
                        option<Happy>("Okay, here's 99,000 coins.") {
                            inventory.transaction {
                                add("herblore_cape")
                                add("herblore_hood")
                                remove("coins", 99000)
                            }
                            when (inventory.transaction.error) {
                                TransactionError.None -> npc<Happy>("Good luck to you, $name.")
                                is TransactionError.Deficient -> {
                                    player<Sad>("But, unfortunately, I was mistaken.")
                                    npc<Idle>("Well, come back and see me when you do.")
                                }
                                is TransactionError.Full, is TransactionError.Invalid -> {
                                    npc<Sad>("Unfortunately all Skillcapes are only available with a free hood, it's part of a skill promotion deal; buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
                                }
                            }
                        }
                    }
                }
            } else {
                option<Quiz>("What must I do to wear a Herblore skillcape?") {
                    npc<Idle>("To earn the right to wear any skillcape you need to have mastered that skill to the highest level possible and it is no different for Herblore.")
                    npc<Idle>("With the cape equipped the Nardah Herbalist will create unfinished potions for you and you may search the cape for a pestle and mortar. When you have achieved a level of 99, come back and talk to me again.")
                }
            }
        }
    }

    fun Player.questComplete() {
        AuditLog.event(this, "quest_completed", "druidic_ritual")
        set("druidic_ritual", "completed")
        jingle("quest_complete_1")
        experience.add(Skill.Herblore, 250.0)
        refreshQuestJournal()
        inc("quest_points", 4)
        softQueue("quest_complete", 1) {
            questComplete(
                "Druidic Ritual",
                "4 Quest Points",
                "Access to the Herblore Skill",
                "250 Herblore XP",
                item = "clean_marrentill",
            )
        }
    }
}
