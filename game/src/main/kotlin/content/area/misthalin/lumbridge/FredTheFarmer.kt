package content.area.misthalin.lumbridge

import content.entity.player.bank.bank
import content.entity.player.dialogue.Afraid
import content.entity.player.dialogue.Amazed
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Chuckle
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.RollEyes
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.removeToLimit

class FredTheFarmer : Script {

    var Player.giveBlackBallsOfWool: Int
        get() = get("sheep_shearer_black_balls_of_wool", -1)
        set(value) = set("sheep_shearer_black_balls_of_wool", value)

    init {
        npcOperate("Talk-to", "fred_the_farmer_lumbridge") {
            when (quest("sheep_shearer_miniquest")) {
                "unstarted" -> {
                    npc<Angry>("What are you doing on my land? You're not the one who keeps leaving all my gates open and letting out all my sheep are you?")
                    jobNotStarted()
                }

                "started" -> startedJob()
                "completed" -> secondReward()
                else -> fullyCompleted()
            }
        }
        itemOnNPCOperate("ball_of_black_wool", "fred_the_farmer_lumbridge") {
            when (quest("sheep_shearer_miniquest")) {
                "started" -> {
                    giveBlackBallsOfWool > 0
                    giveBlackWool()
                }
            }
        }
        itemOnNPCOperate("black_wool", "fred_the_farmer_lumbridge") {
            if (quest("sheep_shearer_miniquest") == "started") {
                player<Neutral>("I've got some wool. I've not managed to make it into a ball, though.")
                npc<Neutral>("Well, go find a spinning wheel then. You can find one on the first floor of Lumbridge Castle; just turn right and follow the path when leaving my house and you'll find Lumbridge.")
            }
        }
        itemOnNPCOperate("wool", "fred_the_farmer_lumbridge") {
            if (quest("sheep_shearer_miniquest") == "started") {
                npc<Neutral>("That wool is white, I'm dealing with a guy looking to do business with black wool only.")
            }
        }
    }

    suspend fun Player.jobNotStarted() {
        choice {
            option<Neutral>("I'm looking for something to kill.") {
                npc<Angry>("What, on my land? Leave my livestock alone!")
            }
            option<Sad>("I'm lost.") {
                npc<Quiz>("How can you be lost? Just follow the road east and south. You'll end up in Lumbridge fairly quickly.")
            }
            option<Neutral>("I'm looking for work.") {
                npc<Happy>("Oh? Well, I could do with a bit of help, since you're offering.")
                npc<Neutral>("I need to collect some black wool from my sheep and I'd be much obliged if you could shear them for me. While you're at it, spin the wool into balls for me too.")
                player<Quiz>("Does it have to be black wool?")
                npc<Neutral>("Has to be. I'm doing business with some guy after black clothing - something to do with black looking 'cool'.")
                choice {
                    option<Neutral>("It takes all sorts, I suppose.") {
                        npc<Neutral>("Indeed. So if you bring me twenty balls of black wool, I'm sure I could sort out some sort of payment.")
                        player<Quiz>("So is this a quest?")
                        npc<RollEyes>("No, it isn't. It's work. You do what I say, then you get paid.")
                    }
                    option<Neutral>("Black clothing cool? I'm not sure that's true.") {
                        npc<Neutral>("That's what I thought, but I'm certainly not going to turn down the business. So if you bring me twenty balls of black wool, I'm sure I could sort out some sort of payment.")
                        player<Quiz>("So is this a quest?")
                        npc<RollEyes>("No, it isn't. It's work. You do what I say, then you get paid.")
                    }
                }
                choice {
                    option<RollEyes>("That doesn't sound very exciting.") {
                        npc<Angry>("Well, what do you expect if you ask a farmer?")
                    }
                    option<Happy>("I'll take the job.") {
                        takeJob()
                    }
                }
            }
        }
    }

    fun ChoiceOption.alreadyKnowShearing(): Unit = option<Happy>("Of course!") {
        npc<Neutral>("And you know how to spin wool into balls?")
        choice {
            woolExpert()
            spinWool()
        }
    }

    fun ChoiceOption.spinWool(): Unit = option<Neutral>("I don't know how to spin wool, sorry.") {
        npc<Neutral>("Don't worry it's quite simple!")
        npc<Neutral>("The nearest Spinning Wheel can be found on the first floor of Lumbridge Castle.")
        npc<Neutral>("To get to Lumbridge Castle just follow the road east.")
        item("spinning_wheel_icon", 1200, "This icon denotes a Spinning Wheel on the world map.")
        player<Happy>("Thank you!")
    }

    fun ChoiceOption.woolExpert(): Unit = option<Happy>("I'm something of an expert, actually.") {
        npc<Neutral>("Well, you can stop grinning and get to work then.")
        npc<Angry>("I'm not paying you by the hour!")
    }

    fun ChoiceOption.shearingTutorial(): Unit = option<Neutral>("Actually, no, I don't.") {
        if (inventory.contains("shears")) {
            npc<Happy>("Well, you're halfway there already! You have a set of shears in your inventory. Just use those on a Sheep to shear it.")
            player<Neutral>("That's all I have to do?")
            npc<Neutral>("Well, once you've collected some wool you'll need to spin it into balls.")
            npc<Quiz>("Do you know how to spin wool?")
            choice {
                spinWool()
                woolExpert()
            }
        } else {
            npc<Neutral>("Well, first things first, you need some shears. There's a pair in my house on the table.")
            npc<Neutral>("Or you could buy your own pair from Lumbridge General Store or from the Grand Exchange in Varrock.")
            npc<Neutral>("To get to the store, turn right when leaving the farm then follow the path. The store will be just down the road on your right.")
            item("general_store_icon", 1186, "General stores are marked on the map by this symbol.")
            npc<Neutral>("Once you have some shears, use them on the sheep in my field.")
            player<Happy>("Sounds easy.")
            npc<Chuckle>("That's what they all say! Some of the sheep don't like it and will run away from you. Persistence is the key.")
            npc<Afraid>("And watch out for rams! They will attack you if you try to shear them. You can tell them apart from the rest of the sheep by their curly horns.")
            npc<Neutral>("Once you've collected some wool you can spin it into balls.")
            npc<Quiz>("Do you know how to spin wool?")
            choice {
                spinWool()
                woolExpert()
            }
        }
    }

    suspend fun Player.takeJob() {
        set("sheep_shearer_miniquest", "started")
        giveBlackBallsOfWool = 20
        npc<Neutral>("Good. Hopefully, you'll be safe from 'The Thing'! Do you actually know how to shear a sheep?")
        choice {
            alreadyKnowShearing()
            shearingTutorial()
            option<Quiz>("What do you mean, 'The Thing'?") {
                npc<Shifty>("Well now, no one has ever seen 'The Thing'. That's why we call it 'The Thing, cos we don't know what it is.")
                npc<Afraid>("Some say it's a black hearted shapeshifter, hungering for the souls of hard working decent folk like me. Others say it's just a sheep.")
                npc<Angry>("Well I don't have all day to stand around and gossip. Did you say you knew how to shear sheep?")
                choice {
                    alreadyKnowShearing()
                    shearingTutorial()
                }
            }
        }
    }

    suspend fun Player.startedJob() {
        if (giveBlackBallsOfWool == 0) {
            allTheWool()
            return
        }
        choice {
            if (inventory.contains("ball_of_black_wool")) {
                option<Happy>("I have some balls of black wool for you.") {
                    npc<Neutral>("Give 'em here then.")
                    giveBlackWool()
                }
            } else {
                if (inventory.contains("black_wool")) {
                    option<Neutral>("How many more balls of wool do you need?") {
                        npc<Neutral>("You need to collect $giveBlackBallsOfWool more balls of wool")
                        player<Neutral>("I've got some wool. I've not managed to make it into a ball, though.")
                        npc<Neutral>("Well, go find a spinning wheel then. You can find one on the first floor of Lumbridge Castle; just turn right and follow the path when leaving my house and you'll find Lumbridge.")
                    }
                } else {
                    if (!inventory.contains("ball_of_black_wool")) {
                        option<Neutral>("How many more balls of wool do you need?") {
                            npc<Neutral>("You need to collect $giveBlackBallsOfWool more balls of wool")
                            player<Sad>("I haven't got any at the moment.")
                            npc<Happy>("Ah well at least you haven't been eaten.")
                        }
                    }
                }
            }
            option<Neutral>("Can you remind me how to get balls of wool?") {
                npc<Neutral>("Sure. You need to shear sheep and then spin the wool on a spinning wheel. Anything else?")
                choice {
                    remindMeShearing()
                    remindMeSpinning()
                    option<Happy>("That's all, thanks.")
                }
            }
            if (get("the_thing_interacted", false)) {
                theThing()
            }
        }
    }

    fun ChoiceOption.theThing(): Unit = option<Amazed>("Fred! Fred! I've seen 'The Thing!'") {
        npc<Afraid>("You...you actually saw it?")
        npc<Afraid>("Run for the hills! $name, grab as many chickens as you can! We have to...")
        player<Amazed>("Fred!")
        npc<Afraid>("..flee! Oh, woe is me! The shape-shifter is coming! We're all...")
        player<Angry>("FRED!")
        npc<Uncertain>("..doomed. What?")
        player<Neutral>("It's not a shape-shifter or any other kind of monster.")
        npc<Quiz>("Well, what is it, boy?")
        player<Uncertain>("Well, it's just two penguins...disguised as a sheep.")
        npc<Uncertain>("...")
        npc<Amazed>("Have you been out in the sun too long?")
    }

    fun ChoiceOption.remindMeShearing(): Unit = option<Neutral>("Can you tell me how to shear sheep?") {
        if (inventory.contains("shears")) {
            npc<Happy>("Well, you're halfway there already! You have a set of shears in your inventory. Just use those on a Sheep to shear it.")
            player<Neutral>("That's all I have to do?")
            npc<Neutral>("Well, once you've collected some wool you'll need to spin it into balls.")
            npc<Neutral>("Anything else?")
            choice {
                remindMeSpinning()
                option<Happy>("That's all, thanks.")
            }
        } else {
            npc<Neutral>("Well, first things first, you need some shears. There's a pair in my house on the table.")
            npc<Neutral>("Or you could buy your own pair from Lumbridge General Store or from the Grand Exchange in Varrock.")
            npc<Neutral>("To get to the store, turn right when leaving the farm then follow the path. The store will be just down the road on your right.")
            item("general_store_icon", 1186, "General stores are marked on the map by this symbol.")
            npc<Neutral>("Once you have some shears, use them on the sheep in my field.")
            player<Happy>("Sounds easy.")
            npc<Chuckle>("That's what they all say! Some of the sheep don't like it and will run away from you. Persistence is the key.")
            npc<Afraid>("And watch out for rams! They will attack you if you try to shear them. You can tell them apart from the rest of the sheep by their curly horns.")
            npc<Neutral>("Once you've collected some wool you can spin it into balls.")
            npc<Neutral>("Anything else?")
            choice {
                remindMeSpinning()
                option<Happy>("That's all, thanks.")
            }
        }
    }

    fun ChoiceOption.remindMeSpinning(): Unit = option<Neutral>("Can you tell me how to spin wool?") {
        npc<Neutral>("Don't worry it's quite simple!")
        npc<Neutral>("The nearest Spinning Wheel can be found on the first floor of Lumbridge Castle.")
        npc<Neutral>("To get to Lumbridge Castle just follow the road east.")
        item("spinning_wheel_icon", 1200, "This icon denotes a Spinning Wheel on the world map.")
        choice {
            remindMeShearing()
            option<Happy>("That's all, thanks.")
        }
    }

    suspend fun Player.giveBlackWool() {
        val give = inventory.removeToLimit("ball_of_black_wool", giveBlackBallsOfWool)
        giveBlackBallsOfWool -= give
        if (giveBlackBallsOfWool <= 0) {
            giveBlackBallsOfWool = 0
            allTheWool()
        } else {
            player<Neutral>("That's all I've got so far.")
            npc<Neutral>("I need $giveBlackBallsOfWool more before I can pay you.")
            player<Neutral>("Okay, I'll work on it.")
        }
    }

    suspend fun Player.allTheWool() {
        player<Happy>("That's the last of them.")
        npc<Happy>("A pleasure doing business with you. You can shear my sheep whenever you want. You could even sell the wool to the Grand Exchange.")
        npc<Angry>("But that's the white ones only, mind you!")
        npc<Sad>("Anyway, I guess I'd better pay you.")
        item("coins1000_2", 500, "Fred gives you some money and teaches you some Crafting techniques.")
        set("sheep_shearer_miniquest", "completed")
        inventory.removeToLimit("black_wool", Int.MAX_VALUE)
        inventory.removeToLimit("ball_of_black_wool", Int.MAX_VALUE)
        bank.removeToLimit("black_wool", Int.MAX_VALUE)
        bank.removeToLimit("ball_of_black_wool", Int.MAX_VALUE)
        experience.add(Skill.Crafting, 150.0)
        inventory.add("coins", 2000)
    }

    suspend fun Player.secondReward() {
        player<Neutral>("Hello again")
        npc<Angry>("What are you doing on my land? Ah, it's the sheep shearer! The black wool you helped me with sold exceptionally well. Please accept this as an extra thanks.")
        statement("The farmer hands over 1,940 coins.")
        if (inventory.add("coins", 1940)) {
            set("sheep_shearer_miniquest", "completed_with_second_reward")
            player<Happy>("Thank you!")
        } else {
            player<Neutral>("Great news, but I need to free up some inventory space first. I'll be right back.")
            message("Inventory full. To make more room, sell, drop or bank something.")
            return
        }
    }

    suspend fun Player.fullyCompleted() {
        npc<Angry>("What are you doing on my land? You're not the one who keeps leaving all my gates open and letting out all my sheep are you?")
        choice {
            option<Neutral>("I'm looking for something to kill.") {
                npc<Angry>("What, on my land? Leave my livestock alone!")
            }
            option<Sad>("I'm lost.") {
                npc<Quiz>("How can you be lost? Just follow the road east and south. You'll end up in Lumbridge fairly quickly.")
            }
            if (get("the_thing_interacted", false)) {
                theThing()
            }
        }
    }
}
