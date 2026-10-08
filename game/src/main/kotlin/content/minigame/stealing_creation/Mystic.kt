package content.minigame.stealing_creation

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory

class Mystic : Script {

    private enum class PersonalScoreTopic {
        GATHERING,
        DEPOSITING,
        FIGHTING,
        WINNING_BONUS,
    }

    private enum class GatherClayTopic {
        TOOLS,
        LEVEL_REQUIREMENTS,
    }

    init {
        npcOperate("Talk-to", "mystic") {
            npc<Neutral>("Greetings, adventurer.")
            mainMenu()
        }
    }

    private suspend fun Player.mainMenu(hideFinishedChoice: Boolean = false) {
        choice("Select an option") {
            option("Just put me on a team") {
                assignTeam()
            }
            option<Neutral>("I have some questions about the primordial realm...") {
                primordialRealmQuestions()
            }
            if (!hideFinishedChoice) {
                option<Quiz>("What is this place?") {
                    npc<Neutral>("This is our humble encampment. It all started when our master received, in his dreams, a call from the sacred beings of the Wilderness.")
                    player<Confused>("The sacred beings of the Wilderness?")
                    npc<Neutral>("Yes. The ghostly remains of some of the greatest warriors of all time. I believe your people call them the revenants.")
                    npc<Neutral>("These sacred beings dwell the Wilderness, continuing to fight even though their ancient wars are long finished. They showed our master how to open the portal to a new world.")
                    npc<Happy>("But not just any world! That world they led us to contains the very stuff of creation! A sacred clay that can be turned into anything we want, and it assumes the properties of the shape it adopts!")
                    player<Shock>("That sounds awesome!")
                    npc<Neutral>("Indeed. It's with this very material that we intend on achieving our goal.")
                    mainMenu(hideFinishedChoice = true)
                }
            }
            option<Quiz>("Can I help you?") {
                npc<Happy>("Of course! If you're on your own, just join one of the two all-comers teams by entering one of the enclosures to the west of the camp.")
                npc<Happy>("If you have friends with you, you could make a clan and challenge another clan in the clan enclosure to the east. You can even ally your clan with up to three others!")
                canHelpMenu()
            }
            option<Neutral>("Never mind.")
        }
    }

    private suspend fun Player.assignTeam() {
        if (!inventory.isEmpty() || !equipment.isEmpty()) {
            npc<Neutral>("You can't take any items into the primordial realm. You can store your items in the bank deposit box.")
            return
        }
        statement("You are teleported into one of the free-for-all enclosures.")
        statement("The mystic assigns you a team.")
    }

    private suspend fun Player.canHelpMenu() {
        choice("Select an option") {
            option<Neutral>("I have some questions about the primordial realm.") {
                primordialRealmQuestions()
            }
            option("Just put me on a team") {
                assignTeam()
            }
            option<Neutral>("Never mind.")
        }
    }

    private suspend fun Player.primordialRealmQuestions() {
        choice("Select an option") {
            option<Quiz>("What am I meant to do?") {
                npc<Neutral>("Your task in the primordial realm is to gather as much sacred clay as you can.")
                npc<Neutral>("You can transform the clay into items using the creation kilns. These items will help you gather clay more quickly and aid you in battle against the other team.")
                npc<Neutral>("The clay appears in five levels of quality, called classes. Clay with a higher class will make better equipment and is worth more points, but is more difficult to gather.")
                objectiveQuestion()
            }
            option<Quiz>("How do I gather clay?") {
                npc<Neutral>("Fragments of class 1 can be found lying on the ground. Anyone can pick these up without any tools.")
                npc<Neutral>("Higher-class clay can be found in four different forms, which require different skills to gather them, and cannot be gathered quickly without tools.")
                gatherClayQuestion()
            }
            option<Quiz>("How do I use the creation kilns?") {
                npc<Neutral>("You can use the creation kilns to make items out of sacred clay. If you have some clay in your inventory you can click on the kiln to open the creation menu.")
                npc<Neutral>("By default, the kiln will use the highest class of clay you are carrying. If you want to use a different class you can select one on the right side of the menu.")
                npc<Neutral>("There are four categories of objects you can make: weapons, armour, equipment and tools. Select a category on the left side of the menu.")
                npc<Neutral>("The objects you can make appear in the middle of the menu. Click on one to make it. You can make several items at a time by right-clicking on the item name.")
                npc<Neutral>("You will need a high enough level in whatever skill is associated with making that type of item: Smithing for metal armour, Cooking for food, and so forth.")
                simpleQuestionReturn()
            }
            option<Quiz>("What skill level do I need?") {
                npc<Neutral>("There is no requirement associated with class 1 clay. To use class 2 clay you need level 20 in the relevant skill; class 3 requires level 40; class 4, level 60; and class 5, level 80.")
                npc<Neutral>("For example, mining level 3 clay requires level 40 Mining. Turning that clay into a helmet would require level 40 Smithing, and the helmet would require level 40 Defence to wear.")
                simpleQuestionReturn()
            }
            option("(More...)") {
                moreQuestions()
            }
        }
    }

    private suspend fun Player.objectiveQuestion() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.gatherClayQuestion(lastAsked: GatherClayTopic? = null) {
        choice("Select an option") {
            if (lastAsked != GatherClayTopic.TOOLS) {
                option<Neutral>("Tell me about what tools I need.") {
                    npc<Neutral>("You will need a pickaxe to mine clay from a rock, a hatchet to cut down a tree, a harpoon to catch clay in a pool, and a butterfly net to gather from a swarm.")
                    npc<Neutral>("You should try to use a tool that is one class below the location you are gathering from, or better. For example, to mine a class 3 rock you should try to use a class 2 pickaxe.")
                    npc<Neutral>("You can gather clay without the appropriate tool, but it will be very slow.")
                    gatherClayQuestion(GatherClayTopic.TOOLS)
                }
            }
            if (lastAsked != GatherClayTopic.LEVEL_REQUIREMENTS) {
                option<Neutral>("Tell me about the level requirements.") {
                    npc<Neutral>("There is no requirement to gather class 1 clay fragments. To gather class 2 clay you need level 20 in the relevant skill; class 3 requires level 40; class 4, level 60; and class 5 level 80.")
                    gatherClayQuestion(GatherClayTopic.LEVEL_REQUIREMENTS)
                }
            }
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.simpleQuestionReturn() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.moreQuestions() {
        choice("Select an option") {
            option<Quiz>("How is the score calculated?") {
                npc<Neutral>("Each team has a score, and everyone in the game has a personal score. You can be on the losing team and still score well, or be on the winning team and score badly.")
                scoreQuestion()
            }
            option<Quiz>("How can I give items to my team mates?") {
                npc<Neutral>("If you use an item on another player who is on your team, you will give them that item, as long as they have space to take it.")
                npc<Neutral>("If you want to give them several items, you can right-click on them and use the Give-to option. This works like the trade menu, but the transfer is one-way.")
                npc<Neutral>("You can also leave items on the tables in your base, and your team mates will be able to pick them up.")
                giveItemsQuestion()
            }
            option<Quiz>("How do the tables work?") {
                npc<Neutral>("Each table stores a different type of item. When you put an item on any table it will jump to the correct one.")
                npc<Neutral>("You can take items by picking them up as normal, but you can also click on a table to bring up a menu of the items available, which may be more convenient if there are many items on the table.")
                npc<Neutral>("The table in the middle of your base allows you to deposit all the items you are carrying very quickly. When you do this you will keep your highest-class tool of each type.")
                tablesQuestion()
            }
            option<Quiz>("What happens if I die?") {
                npc<Neutral>("If you die in the primordial realm, you will instantly respawn in the the team start area or inside a barrier that your team controls, whichever is closest to the point where you died.")
                npc<Neutral>("You will drop all of the items you are carrying, unless you are using the Protect Item prayer, in which case you will keep one.")
                deathQuestion()
            }
            option("(More...)") {
                evenMoreQuestions()
            }
        }
    }

    private suspend fun Player.scoreQuestion() {
        choice("Select an option") {
            option<Neutral>("Tell me about the personal score.") {
                npc<Neutral>("Your personal score is the one shown on the top left of your window.")
                npc<Neutral>("You get points for gathering clay, processing it, and depositing it in the base. You also get a small number of points for damaging other players, and you get a bonus if your team wins overall.")
                personalScoreQuestion()
            }
            option<Neutral>("Tell me about the team score.") {
                npc<Neutral>("Your team's score is based on the amount and quality of sacred clay in your base. You can find out the current scores by talking to me.")
                npc<Neutral>("If your team has the highest score at the end of the game, you will get a 10% bonus added to your personal score.")
                teamScoreQuestion()
            }
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.personalScoreQuestion(lastAsked: PersonalScoreTopic? = null) {
        choice("Select an option") {
            if (lastAsked != PersonalScoreTopic.GATHERING) {
                option<Quiz>("What do I score for gathering and processing?") {
                    npc<Neutral>("Gathering clay gives you points according to the class of the clay: 15 for class 1, 30 for class 2, 45 for class 3, 60 for class 4, and 75 for class 5.")
                    npc<Neutral>("You get the same number of points for processing the clay at a creation kiln.")
                    personalScoreQuestion(PersonalScoreTopic.GATHERING)
                }
            }
            if (lastAsked != PersonalScoreTopic.DEPOSITING) {
                option<Quiz>("What do I score for depositing clay?") {
                    npc<Neutral>("Depositing raw clay gives you points according to the class of the clay: 15 for class 1, 30 for class 2, 45 for class 3, 60 for class 4, and 75 for class 5. Processed items are worth twice as many points.")
                    npc<Neutral>("Bear in mind that you will lose points for taking items from the base as well as gaining points for depositing them. Do not take an item unless you intend to make good use of it!")
                    npc<Neutral>("In some cases a single piece of clay will be processed into multiple items. In this case the whole set of items is worth the normal number of points, and each individual item is worth proportionally less.")
                    personalScoreQuestion(PersonalScoreTopic.DEPOSITING)
                }
            }
            if (lastAsked != PersonalScoreTopic.FIGHTING) {
                option<Quiz>("What do I score for fighting enemies?") {
                    npc<Neutral>("You get five points for each hitpoint of damage you do to an enemy. You should add to this by picking up the items they drop and bringing them back to your base.")
                    personalScoreQuestion(PersonalScoreTopic.FIGHTING)
                }
            }
            if (lastAsked != PersonalScoreTopic.WINNING_BONUS) {
                option<Quiz>("What bonus do I get for being on the winning team?") {
                    npc<Neutral>("If you are on the winning team you get a 10% bonus added to your score. The winning team is the one that has the most sacred clay in its base at the end of the game.")
                    personalScoreQuestion(PersonalScoreTopic.WINNING_BONUS)
                }
            }
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            if (lastAsked != null) {
                option<Neutral>("That's all I need to know.")
            }
        }
    }

    private suspend fun Player.teamScoreQuestion() {
        choice("Select an option") {
            option<Neutral>("Tell me about the personal score.") {
                npc<Neutral>("Your personal score is the one shown on the top left of your window.")
                npc<Neutral>("You get points for gathering clay, processing it, and depositing it in the base. You also get a small number of points for damaging other players, and you get a bonus if your team wins overall.")
                personalScoreQuestion()
            }
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.giveItemsQuestion() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Quiz>("How do the tables work?") {
                npc<Neutral>("Each table stores a different type of item. When you put an item on any table it will jump to the correct one.")
                npc<Neutral>("You can take items by picking them up as normal, but you can also click on a table to bring up a menu of the items available, which may be more convenient if there are many items on the table.")
                npc<Neutral>("The table in the middle of your base allows you to deposit all the items you are carrying very quickly. When you do this you will keep your highest-class tool of each type.")
                tablesQuestion()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.tablesQuestion() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.deathQuestion() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.evenMoreQuestions() {
        choice("Select an option") {
            option<Neutral>("Tell me about barriers.") {
                npc<Neutral>("You can make sacred clay into barriers using a creation kiln.")
                npc<Neutral>("You can then set these barriers up around certain locations by using them on a barrier hotspot. You need four barrier objects in order to set them up.")
                npc<Neutral>("Setting up a barrier around a location ensures that only members of your team can access the location. Skill-focused players can work in safety inside the barrier.")
                npc<Neutral>("If you die, you will respawn inside one of your team's barriers, if that is closer than the team start area.")
                npc<Neutral>("The other team can break down barriers by attacking them. Barriers also degrade over time. Barriers made of higher quality clay will last longer.")
                barriersQuestion()
            }
            option<Neutral>("Tell me about Summoning creatures.") {
                npc<Neutral>("You can make Summoning pouches and scrolls out of sacred clay at a creation kiln.")
                npc<Neutral>("Once you have a scroll, you can summon the creature as normal. As with everything in the primordial realm, creatures made from higher classes of clay are more powerful in combat.")
                npc<Neutral>("The creatures can also carry items for you. Those made from higher classes of clay can carry more items at a time.")
                npc<Neutral>("The sacred clay Summoning scroll causes your familiar to teleport all the items it is carrying to your team's base.")
                npc<Neutral>("You can recharge your Summoning points at the altars scattered around the realm.")
                summoningQuestion()
            }
            option<Neutral>("Tell me about fog patches.") {
                npc<Neutral>("If you enter one of the fog patches you will be hidden. No one will be able to see you or attack you until you emerge. When you leave the fog patch you may find yourself momentarily disoriented.")
                fogQuestion()
            }
            option("(More...)") {
                finalQuestions()
            }
        }
    }

    private suspend fun Player.finalQuestions() {
        choice("Select an option") {
            option<Quiz>("How does the game end?") {
                npc<Neutral>("The game will normally last 20 minutes. You should see a timer in the top right corner of your window showing you how much time is remaining.")
                npc<Neutral>("The game will end prematurely if all the clay in the area is exhausted, or if all the players on one team quit.")
                gameEndQuestion()
            }
            option<Quiz>("What happens if I leave the game early?") {
                npc<Neutral>("If you leave the game early you will not score any points for that game, and you will be unable to join a new game for five minutes.")
                npc<Neutral>("If you are the last player on your team and you quit, the game will end. Otherwise it will continue without you. Any items you are carrying when you quit will appear in your team's base.")
                leaveEarlyQuestion()
            }
            option<Quiz>("What are the rewards?") {
                npc<Neutral>("You will earn points for your activities in the primordial realm, and you can exchange these for items we have made from the sacred clay.")
                npc<Neutral>("You can buy morphic tools that change into many different types of tool, and morphic armour and weapons that will change to suit different combat styles.")
                npc<Neutral>("For more information, talk to the rewards mystic in the north-east of the camp.")
                rewardsQuestion()
            }
            option("(Back to start...)") {
                primordialRealmQuestions()
            }
        }
    }

    private suspend fun Player.barriersQuestion() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.summoningQuestion() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.fogQuestion() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.gameEndQuestion() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.leaveEarlyQuestion() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.rewardsQuestion() {
        choice("Select an option") {
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }
}
