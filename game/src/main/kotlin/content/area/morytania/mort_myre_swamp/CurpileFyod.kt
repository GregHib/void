package content.area.morytania.mort_myre_swamp

import content.entity.combat.hit.damage
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.member.myreque.hasAllWeapons
import content.quest.member.myreque.myrequeStage
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random

class CurpileFyod : Script {
    init {
        npcOperate("Talk-to", "curpile_fyod_mort_myre_swamp") { (curpile) ->
            val progress = questStage("in_search_of_the_myreque")
            when {
                progress < myrequeStage("reached_hollows") -> {
                    npc<Neutral>("Hey...you's tresspassin' be off wiv ya.")
                }
                progress == myrequeStage("reached_hollows") || progress == myrequeStage("questioned_by_curpile") -> {
                    npc<Quiz>("Hey...what're you doin' here?")
                    initialChoice(curpile)
                }
                progress > myrequeStage("questioned_by_curpile") -> {
                    npc<Quiz>("Hey...leave me be...I'm guardin' this place.")
                }
            }
        }
    }

    suspend fun Player.initialChoice(curpile: NPC) {
        choice {
            comeToHelp(curpile)
            dontHaveToAnswer(curpile)
            noReason(curpile)
            askForWayOut(curpile)
            haveToGo()
        }
    }

    fun ChoiceOption.comeToHelp(curpile: NPC): Unit = option<Happy>("I've come to help the Myreque, I've brought weapons.") {
        if (!hasAllWeapons()) {
            npc<Angry>(
                "It doesn't look as if you have the right weapons to me. Get out of here you fake!",
            )
            return@option
        }
        npc<Neutral>(
            "Ok, I see ya got da weapons...but how'd I know you're not gonna use em against my " +
                "friends?",
        )
        player<Neutral>("But I just want to help deliver these weapons.")
        npc<Neutral>(
            "Well, dat's as maybe, and I'm not doubting your sincerity here, you seems all sincered " +
                "up to me...it's choking me up right here, you're making me cry...but hey, I's godda " +
                "do my job or da kids don't get fed! Ok,",
        )
        if (questStage("in_search_of_the_myreque") == myrequeStage("reached_hollows")) {
            set("in_search_of_the_myreque", "questioned_by_curpile")
        }
        npc<Neutral>(
            "so say I asks you a few questions and you were to answer them all correct and so on, " +
                "well that'd make me believe you's...I'd get to feeling that you was the real deal " +
                "an all. How's dat sound?",
        )
        player<Neutral>("Sounds fine to me...go ahead, shoot!")
        npc<Neutral>("Hey...don't tempt me! You's dicing wiv death here my friend!")
        runQuiz(curpile)
    }

    fun ChoiceOption.dontHaveToAnswer(curpile: NPC): Unit = option<Angry>("I don't have to answer to you!") {
        npc<Angry>("You do if you know what's good for you!")
        sendPunch(curpile)
    }

    fun ChoiceOption.noReason(curpile: NPC): Unit = option<Neutral>("No reason!") {
        npc<Neutral>(
            "Come on...speak up! I may be able to help you. However, yous gotta know that your " +
                "tresspassing and that by rights I should slap your around a bit, you know, just " +
                "like as a warning.",
        )
        initialChoice(curpile)
    }

    fun ChoiceOption.askForWayOut(curpile: NPC): Unit = option<Quiz>("I'm lost, can you show me the way out?") {
        npc<Neutral>(
            "You got yourself in here... now get yourself out.. and quickly if I were you.",
        )
        initialChoice(curpile)
    }

    fun ChoiceOption.haveToGo(): Unit = option<Neutral>("I have to go.") {
        npc<Neutral>("Sure...be on your way stranger.")
    }

    private suspend fun Player.runQuiz(curpile: NPC) {
        val asked = (0..QUESTIONS).shuffled(random).take(3)
        var correct = 0

        for ((index, q) in asked.withIndex()) {
            val prefix = when (index) {
                0 -> "Ok, first question."
                1 -> "Ok, second question."
                else -> "Ok, third and final question."
            }
            if (askQuestion(q, prefix)) {
                correct++
            }
            val ack = when (index) {
                0 -> "Ok, interesting answer. First question answered."
                1 -> "An interesting response. Second question answered."
                else -> "Hmm, a calculated retort. Third question answered."
            }
            npc<Neutral>(ack)
        }

        if (correct == 3) {
            set("in_search_of_the_myreque", "answered_questions")
            npc<Neutral>("Ok, I believes ya...you can go on.")
            player<Quiz>("What's the combination to the door?")
            npc<Neutral>("Oh, there isn't one. I'll unlock it for you.")
        } else {
            val word = if (correct == 1) "question" else "questions"
            npc<Neutral>("I'm sorry but you got $correct $word correct! Go on get outta here...")
            knockOutPlayer(curpile, correct)
        }
    }

    private suspend fun Player.askQuestion(questionIndex: Int, prefix: String): Boolean = when (questionIndex) {
        0 -> {
            npc<Neutral>("$prefix What is the boatman's name?")
            askBoatmanName()
        }
        1 -> {
            npc<Neutral>("$prefix Who is the leader of the Myreque?")
            askMyrequeLeader()
        }
        2 -> {
            npc<Neutral>("$prefix Name the only female member of the Myreque.")
            askFemaleMember()
        }
        3 -> {
            npc<Neutral>("$prefix Who is the youngest member of the Myreque?")
            askYoungestMember()
        }
        4 -> {
            npc<Neutral>("$prefix What family is rumoured to rule Morytania?")
            askRulingFamily()
        }
        else -> {
            npc<Neutral>(
                "$prefix Which member of the 'Myreque' originally had a profession as a scholar?",
            )
            askScholar()
        }
    }

    private suspend fun Player.askBoatmanName(): Boolean {
        var correct = false
        choice("What is the boatman's name?") {
            option("Geof Paddleman") {}
            option("Cyreg Paddlebone") {}
            option("Gyrec Paddlehorn") {}
            option("Cyreg Paddlehorn") { correct = true }
            option("I don't know!") {}
        }
        return correct
    }

    private suspend fun Player.askMyrequeLeader(): Boolean {
        var correct = false
        choice("Who is the leader of the Myreque?") {
            option("Sani Piliu") {}
            option("Harold Evans") {}
            option("Veliaf Hurtz") { correct = true }
            option("Radigad Ponfit") {}
            option("Don't know!") {}
        }
        return correct
    }

    private suspend fun Player.askFemaleMember(): Boolean {
        var correct = false
        choice("Name the only female member of the Myreque?") {
            option("Sani Piliu") { correct = true }
            option("Santi Peliou") {}
            option("Sani Peridou") {}
            option("Sandi Pherimou") {}
            option("Don't know!") {}
        }
        return correct
    }

    private suspend fun Player.askYoungestMember(): Boolean {
        var correct = false
        choice("Who is the youngest member of the Myreque?") {
            option("Sani Piliu") {}
            option("Ivan Strom") { correct = true }
            option("Veliaf Hurtz") {}
            option("Radigad Ponfit") {}
            option("Don't know!") {}
        }
        return correct
    }

    private suspend fun Player.askRulingFamily(): Boolean {
        var correct = false
        choice("What family is rumoured to rule Morytania?") {
            option("Drunken") {}
            option("Drakan") { correct = true }
            option("Draynor") {}
            option("Praymore") {}
            option("I don't know!") {}
        }
        return correct
    }

    private suspend fun Player.askScholar(): Boolean {
        var correct = false
        choice("Who was previously a scholar?") {
            option("Sani Piliu") {}
            option("Harold Evans") {}
            option("Radogad Ponfit") {}
            option("Polmafi Ferdygris") { correct = true }
            option("Don't know!") {}
        }
        return correct
    }

    private suspend fun Player.sendPunch(curpile: NPC) {
        curpile.anim("curpile_punch")
        delay(1)
        damage(30, source = curpile)
    }

    private suspend fun Player.knockOutPlayer(curpile: NPC, correct: Int) {
        val word = if (correct == 1) "question" else "questions"
        message("Curpile Fyod: I'm sorry but you got $correct $word correct! Go on get outta here...")
        curpile.anim("curpile_knockout_punch")
        gfx("curpile_knockout_stars")
        delay(2)
        anim("myreque_knockout")
        delay(1)
        open("fade_out")
        delay(3)
        clearAnim()
        tele(3522, 3285)
        delay(2)
        close("fade_out")
        open("fade_in")
        delay(3)
        statement("Curpile knocked you out...You wake up back in Mort'ton...")
    }

    private companion object {
        private const val QUESTIONS = 5
    }
}
