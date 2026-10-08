package content.minigame.stealing_creation

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.Unimpressed
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory

class Mystic2 : Script {

    private enum class PersonalScoreTopic {
        GATHERING,
        DEPOSITING,
        FIGHTING,
        WINNING_BONUS,
    }

    init {
        npcOperate("Talk-to", "mystic_2") {
            npc<Neutral>("Hail, Holy Ones!")
            mainMenu()
        }
    }

    private suspend fun Player.mainMenu(hideHolyChoice: Boolean = false) {
        choice("Select an option") {
            option("Just put me on a team") {
                assignTeam()
            }
            option<Neutral>("I have some questions about the primordial realm...") {
                primordialRealmQuestions()
            }
            if (!hideHolyChoice) {
                option<Quiz>("I'm not THAT holy...") {
                    npc<Neutral>("I not talking about you! I talk about spirits of great warriors who make Big High War God proud in ancient battle, and who still fight under Wilderness.")
                    player<Unimpressed>("Oh. Shame. Ah well.")
                    npc<Neutral>("We on great mission to bring Holy Ones to life!")
                    player<Shock>("Aren't the dead usually better left dead?")
                    npc<Angry>("You not know what you say. Holy Ones holy. We must bring them back for glory of Big High War God!")
                    mainMenu(hideHolyChoice = true)
                }
            }
            option<Quiz>("Can I help you?") {
                npc<Happy>("You help us! If you alone, go into all comer start area on west side. If you in clan, go into clan challenge area on east side.")
            }
            option<Neutral>("Never mind.")
        }
    }

    private suspend fun Player.assignTeam() {
        if (!inventory.isEmpty() || !equipment.isEmpty()) {
            npc<Neutral>("You have to get rid of stuff first. You not allowed to take stuff. You put stuff in bank box.")
            return
        }
        statement("You are teleported into one of the free-for-all enclosures.")
        statement("The mystic assigns you a team.")
    }

    private suspend fun Player.primordialRealmQuestions() {
        choice("Select an option") {
            option<Neutral>("What am I meant to do?") {
                npc<Neutral>("You gather as much sacred clay as you can.")
                npc<Neutral>("You can turn clay into things using creation kilns. These things help you gather clay more quick and help you fight other team.")
                npc<Neutral>("There clay in five levels. Called classes. Clay with higher class make better equipment and worth more points, but morer hard to gather.")
                objectiveQuestion()
            }
            option<Neutral>("How do I gather clay?") {
                npc<Neutral>("You find class 1 clay lying on ground. You not need tools to pick it up.")
                npc<Neutral>("You find higher-class clay in four different forms. You need skill to get these, and you need tools to get them quick!")
                gatherClayQuestion()
            }
            option<Neutral>("How do I use the creation kilns?") {
                npc<Neutral>("You can use kiln thingy to make things out of sacred clay. If you have clay in your inventory, you can click on kiln to open creation menu.")
                npc<Neutral>("Kiln will normally use highest class of clay you carrying. If you want use different class you select one on right side of menu.")
                npc<Neutral>("There four groups of object you can make: weapons, armour, equipment and tools. Select a group on left side of the menu.")
                npc<Neutral>("Objects you can make appear in middle of menu. Click on thing to make it. You can make several things at once by right-clicking on name of thing.")
                npc<Neutral>("You will need high enough level in right skill: Smithing for metal armour, Cooking for food, and so on.")
                simpleQuestionReturn()
            }
            option<Neutral>("What skill level do I need?") {
                npc<Neutral>("You not need skill for class 1 clay. To use class 2 clay you need level 20 in the right skill; class 3 requires level 40; class 4, level 60; and class 5, level 80.")
                npc<Neutral>("For example, to mine level 3 clay you need level 40 Mining. You turn that clay into helmet, you need level 40 Smithing. Then to wear helmet you need level 40 Defence to wear.")
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

    private suspend fun Player.gatherClayQuestion() {
        choice("Select an option") {
            option<Neutral>("Tell me about what tools I need.") {
                npc<Neutral>("You need a pickaxe to mine clay from rock, hatchet to cut down tree, harpoon to catch clay in pool and butterfly net to gather from swarm.")
                npc<Neutral>("You should try to use tool of one class below the place you gathering from, or better. Sot o mine class 3 rock you should have class 2 pickaxe.")
                npc<Laugh>("You can get clay without the right tool, but it very slow!")
                choice("Select an option") {
                    option<Neutral>("Tell me about the level requirements.") {
                        npc<Neutral>("You not need level to get class 1 clay fragments. To get class 2 clay you need level 20; class 3 you need level 40; class 4, level 60; and class 5, level 80.")
                        gatherClayToolsReturnQuestion()
                    }
                    option("Back to my other questions...") {
                        primordialRealmQuestions()
                    }
                    option<Neutral>("That's all I need to know.")
                }
            }
            option<Neutral>("Tell me about the level requirements.") {
                npc<Neutral>("You not need level to get class 1 clay fragments. To get class 2 clay you need level 20; class 3 you need level 40; class 4, level 60; and class 5, level 80.")
                npc<Neutral>("To gather class 2 clay you need level 20 in the relevant skill; class 3 requires level 40; class 4, level 60; and class 5 level 80.")
                gatherClayQuestion()
            }
            option("Back to my other questions...") {
                primordialRealmQuestions()
            }
            option<Neutral>("That's all I need to know.")
        }
    }

    private suspend fun Player.gatherClayToolsReturnQuestion() {
        choice("Select an option") {
            option<Neutral>("Tell me about what tools I need.") {
                npc<Neutral>("You find class 1 clay lying on ground. You not need tools to pick it up.")
                npc<Neutral>("You find higher-class clay in four different forms. You need skill to get these, and you need tools to get them quick!")
                choice("Select an option") {
                    option<Neutral>("Tell me about the level requirements.") {
                        npc<Neutral>("You not need level to get class 1 clay fragments. To get class 2 clay you need level 20; class 3 you need level 40; class 4, level 60; and class 5, level 80.")
                        gatherClayToolsReturnQuestion()
                    }
                    option("Back to my other questions...") {
                        primordialRealmQuestions()
                    }
                    option<Neutral>("That's all I need to know.")
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
                npc<Neutral>("Each team has score, and everyone in game has own score. You can be on losing team and score good, or be on winning team and score bad!")
                scoreQuestion()
            }
            option<Quiz>("How can I give items to my team mates?") {
                npc<Neutral>("If you use thing on player who on your team, you will give them thing, as long as they have space.")
                npc<Neutral>("If you want to give them several things, you can right-click on them and use Give-to option. This work like the trade menu, but you only give and not take.")
                npc<Neutral>("You can also leave things on the tables in base, and team mates can pick them up.")
                giveItemsQuestion()
            }
            option<Quiz>("How do the tables work?") {
                npc<Neutral>("Each table store different type of thing. When you put thing on any table it will jump to right table.")
                npc<Neutral>("You can take things by picking them up as normal. Or you can click on table to get a menu of things. That may be easier if there lots of things.")
                npc<Neutral>("Table in middle of base let you deposit all things you carrying very quick. When you do this you will keep best tool of each type.")
                tablesQuestion()
            }
            option<Quiz>("What happens if I die?") {
                npc<Neutral>("If you die in the primordial realm you come back in team start area or insider barrier of your team, whichever closer.")
                npc<Neutral>("You drop all things you carrying, unless you pray to Big High War God and he let you keep one thing.")
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
                npc<Neutral>("You see own score in top left of window.")
                npc<Neutral>("You get points for getting clay, making things from clay, and putting clay or things in the base. You also get some points for hurting other players, and you get bonus if your team win in the end.")
                personalScoreQuestion()
            }
            option<Neutral>("Tell me about the team score.") {
                npc<Neutral>("Your team's score based on the amount and quality of sacred clay in base. You can find out the current scores by talking to me.")
                npc<Neutral>("If your team has the highest score at the end of the game, you get a 10% bonus added to personal score.")
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
                    npc<Neutral>("Putting raw clay in base give you points based on class of clay: 15 for class 1, 30 for class 2, 45 for class 3, 60 for class 4, and 75 for class 5. Things you make are worth twice as many points.")
                    npc<Neutral>("You lose points for taking things from the base. Do not take thing unless you use it good!")
                    npc<Neutral>("Sometimes you make many things from one clay piece. Then whole set of things is worth normal number of points, so each thing worth less.")
                    personalScoreQuestion(PersonalScoreTopic.DEPOSITING)
                }
            }
            if (lastAsked != PersonalScoreTopic.FIGHTING) {
                option<Quiz>("What do I score for fighting enemies?") {
                    npc<Neutral>("You get five points for each hitpoint of damage you do to enemy. You should also pick up things they drop and bring them back to base.")
                    personalScoreQuestion(PersonalScoreTopic.FIGHTING)
                }
            }
            if (lastAsked != PersonalScoreTopic.WINNING_BONUS) {
                option<Quiz>("What bonus do I get for being on the winning team?") {
                    npc<Neutral>("If you are on winning team you get 10% bonus added to your score. Winning team is the one that has the most sacred clay at end of the game.")
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
                npc<Neutral>("You see own score in top left of window.")
                npc<Neutral>("You get points for getting clay, making things from clay, and putting clay or things in the base. You also get some points for hurting other players, and you get bonus if your team win in the end.")
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
                npc<Neutral>("Each table store different type of thing. When you put thing on any table it will jump to right table.")
                npc<Neutral>("You can take things by picking them up as normal. Or you can click on table to get a menu of things. That may be easier if there lots of things.")
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
                npc<Neutral>("You can make sacred clay into barriers using kiln.")
                npc<Neutral>("You set barriers up around some places by using them on barrier hotspot. You need four barrier objects to set them up.")
                npc<Neutral>("Setting up barrier around place means only your team can get to place. Skilly players can work safe inside barrier.")
                npc<Neutral>("If you die, you respawn in your team's barrier, if that closer than team start area.")
                npc<Neutral>("Other team can attack barriers and break barriers. Barriers also fall apart over time. Barriers made of better clay last longer.")
                barriersQuestion()
            }
            option<Neutral>("Tell me about Summoning creatures.") {
                npc<Neutral>("You can make Summoning pouches and scrolls out of sacred clay at kiln.")
                npc<Neutral>("Once you have scroll, you can summon creature as normal. Creatures made from higher-class clay are better in combat.")
                npc<Neutral>("Creatures also carry things for you. Those made of higher-class clay can carry more things at one time.")
                npc<Neutral>("Sacred clay Summoning scroll make your familiar teleport all the things it carrying to base.")
                npc<Neutral>("You can get Summoning points back at altars.")
                summoningQuestion()
            }
            option<Neutral>("Tell me about fog patches.") {
                npc<Neutral>("If you go into fog, you hidden. No one see you or attack you until you come out. When you leave fog patch you may be dizzy.")
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
                npc<Neutral>("Game normally last 20 minutes. You should see clock in the top right corner of your window showing you time left.")
                npc<Neutral>("The game will end if all clay in the area is gone, or if all players on one team quit.")
                gameEndQuestion()
            }
            option<Quiz>("What happens if I leave the game early?") {
                npc<Neutral>("If you leave game early you not score any points, and you not join a new game for five minutes.")
                npc<Neutral>("If you last player on your team and you quit, the game end. Otherwise it will carry on without you. Any things you are carrying when you quit appear in your team's base.")
                leaveEarlyQuestion()
            }
            option<Quiz>("What are the rewards?") {
                npc<Neutral>("You earn points in primordial realm, and you can swap these for things we have made from sacred clay.")
                npc<Neutral>("You can buy morphic tool that change into many different types of tool, and morphic armour and weapons that change for different combat styles.")
                npc<Neutral>("You want know more, talk to rewards mystic in north-east of camp.")
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
