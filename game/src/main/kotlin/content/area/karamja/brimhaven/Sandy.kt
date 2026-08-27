package content.area.karamja.brimhaven

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class Sandy : Script {
    init {
        npcOperate("Talk-to", "sandy_brimhaven*") {
            when (quest("hand_in_the_sand")) {
                "unstarted", "investigate_hand", "ask_wizards", "bert_hours" -> {
                    randomGreeting()
                    player<Quiz>("Errrr... no.")
                    npc<Angry>("Then go away!")
                }
                "visit_sandy" -> introduction()
                "confront_bert" -> rotaHint()
                "distract_sandy" -> {
                    npc<Angry>("I don't have time to talk to you. Go away!")
                    distractionAttempt()
                }
                "activate_orb" -> statement(
                    "You need to activate the magical scrying orb, obtained from the " +
                        "wizard in Yanille, to capture the conversation with Sandy!",
                )
                "interrogate_sandy" -> {
                    if (inventory.contains("magical_orb_active")) {
                        interrogation()
                    } else {
                        statement(
                            "Don't forget you need to capture what Sandy says on the magical " +
                                "scrying orb that the wizard in Yanille gave you!",
                        )
                    }
                }
                else -> npc<Angry>("I don't have time to talk to you. Go away!")
            }
        }

        npcOperate("Pickpocket", "sandy_brimhaven*") {
            if (quest("hand_in_the_sand") == "unstarted") {
                message("Nothing interesting happens.")
                return@npcOperate
            }
            anim("pick_pocket")
            if (inventory.spaces < 1) {
                statement("I'd better make room in my inventory first!")
                return@npcOperate
            }
            if (random.nextInt(3) != 0) {
                statement("You felt something but it slipped through your fingers...")
                return@npcOperate
            }
            addOrDrop("sand")
            statement("You rummage around in Sandy's pockets.....")
            statement("You find a small amount of sand.")
        }
    }

    private suspend fun Player.randomGreeting() {
        when (random.nextInt(3)) {
            0 -> npc<Neutral>("Nice day for sand isn't it?")
            1 -> npc<Happy>("Sand is yellow, Sand is grand, Sand puts money, In my hand!")
            else -> npc<Neutral>("I'm Sandy of Sandy's Sand Corp, are you in the sand business?")
        }
    }

    private suspend fun Player.introduction() {
        player<Neutral>("Hello Sir, do you run the Sand Corp?")
        npc<Shifty>("Who wants to know?")
        player<Neutral>("I'm $name. I'm here investigating the possible murder of a wizard.")
        npc<Angry>(
            "I don't care about that, I have far too much work to " +
                "do. Let the authorities take care of things like murder " +
                "and stop snooping around my office!",
        )
        statement("Sandy seems very keen to get you out of the office, perhaps you should take a look around.")
    }

    private suspend fun Player.rotaHint() {
        if (inventory.contains("sandys_rota")) {
            statement("You already have the rota, perhaps you should take both rotas back to Bert in Yanille.")
            return
        }
        statement("Perhaps you should look around Sandy's office a bit.")
    }

    private suspend fun Player.distractionAttempt() {
        var success = false
        choice {
            option<Shock>("There's a herd of huge mutant herring about to drop from the sky!") {
                success = rollDistraction()
            }
            option<Shock>("But the pygmy shrews have eaten all the sand!") {
                success = rollDistraction()
            }
            option<Shock>("A small parrot with a pink banana is sitting outside your window!") {
                success = rollDistraction()
            }
        }
        if (!success) {
            return
        }
        set("hand_in_the_sand", "drug_coffee")
        set("handsand_sandy_multi", 1)
        message("Sandy turns to look out of the window, now is your chance!")
    }

    private suspend fun Player.rollDistraction(): Boolean {
        val won = random.nextInt(3) == 0
        if (won) {
            npc<Shock>("Wow! I must see this!")
        } else {
            npc<Angry>("I'm not falling for that one!")
        }
        return won
    }

    private suspend fun Player.interrogation() {
        player<Quiz>("Now, I'm going to ask you some questions and I want you to answer me truthfully...")
        npc<Neutral>("Ok...")
        askQuestions()
    }

    private suspend fun Player.askQuestions() {
        val q1 = get("handsand_question1", false)
        val q2 = get("handsand_question2", false)
        val q3 = get("handsand_question3", false)
        if (!q1 && !q2 && !q3) {
            finishInterrogation()
            return
        }
        choice {
            if (q1) {
                option<Quiz>("Why is Bert's rota different from the original?") {
                    set("handsand_question1", false)
                    npc<Neutral>("Because... I changed it.")
                    askQuestions()
                }
            }
            if (q2) {
                option<Quiz>("Why doesn't Bert remember the change in his hours?") {
                    set("handsand_question2", false)
                    npc<Neutral>(
                        "Because.... because.... I bribed a wizard to put a spell " +
                            "on him so he would believe everything I say!!",
                    )
                    askQuestions()
                }
            }
            if (q3) {
                option<Quiz>("What happened to the wizard?") {
                    set("handsand_question3", false)
                    npc<Angry>(
                        "I...I... I KILLED HIM! So I wouldn't have to pay him " +
                            "and no one would know. I put his body in the next " +
                            "load of sand.",
                    )
                    askQuestions()
                }
            }
            option<Neutral>("Ok, I'm done with you.") {
                statement("You need to ask Sandy all the questions.")
                askQuestions()
            }
        }
    }

    private suspend fun Player.finishInterrogation() {
        player<Shock>(
            "I think I have enough evidence now, you can go for " +
                "now, but I think you're up to your neck in it!",
        )
        set("hand_in_the_sand", "return_orb")
        item(
            item = "magical_orb_active",
            text = "Sandy has told you all he knows. The magical scrying " +
                "orb is full and needs to be returned to the Wizard in " +
                "Yanille.",
        )
    }
}
