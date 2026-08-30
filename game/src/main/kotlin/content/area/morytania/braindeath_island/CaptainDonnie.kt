package content.area.morytania.braindeath_island

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class CaptainDonnie : Script {
    init {
        npcOperate("Talk-to", "captain_donnie") { (target) ->
            talkWithDonnie()
        }

        itemOnNPCOperate("unsanitary_swill", "captain_donnie") {
            talkWithDonnie()
        }
    }

    // ===== Default greeting (no swill, or wrong quest progress) =====

    private suspend fun Player.talkWithDonnie() {
        when {
            inventory.contains("unsanitary_swill") && questStage("rum_deal") == 17 -> {
                deliverSwillFirstTime()
            }
            inventory.contains("unsanitary_swill") -> {
                deliverSwillRepeat()
            }
            else -> initialGreeting()
        }
    }

    private suspend fun Player.initialGreeting() {
        npc<Angry>("Arr! What be ye wantin?")
        player<Neutral>("I, err, came to...")
        approachChoice()
    }

    suspend fun Player.approachChoice() {
        choice {
            tellYouToLeave()
            askWhatYouWanted()
            joinYourCrew()
        }
    }

    fun ChoiceOption.tellYouToLeave(): Unit = option("Tell you to leave.") {
        player<Angry>("I have come to tell you to leave.")
        npc<Neutral>("Ye have?")
        player<Neutral>("Yes.")
        npc<Happy>("Hahahahahahahahahahahahaha!")
        npc<Happy>("Bwahahahahahahahahahahahaha!")
        npc<Neutral>("Wheeze...wheeze...")
        npc<Happy>(
            "Gadzooks lad, that be the funniest thing I've heard all day! Say it again!",
        )
        player<Quiz>("I have come to tell you to leave.")
        npc<Happy>("Stop lad! I'll shatter me ribs!")
    }

    fun ChoiceOption.askWhatYouWanted(): Unit = option("Ask what you wanted.") {
        player<Quiz>("I've come to ask you what you want.")
        npc<Angry>("Whadda we want? 'Rum'! When do we want it? Now!")
        player<Quiz>("So...if I were to give you 'rum' you would leave?")
        npc<Neutral>(
            "Not really lad. If ye were to give us 'rum' we'd kill ye quickly, as opposed to " +
                "over a few weeks.",
        )
        player<Shock>("Oh...")
    }

    fun ChoiceOption.joinYourCrew(): Unit = option("Join your crew.") {
        player<Happy>("I've come to join your crew!")
        player<Happy>("Err, I mean...")
        player<Happy>(
            "Arr! Shiver me mainbraces and make them landlubbers walk the scurvy plank, Cap'n! " +
                "I've come to join yer cut-throat, bilge swillin' crew! Also, arr!",
        )
        npc<Quiz>("Are ye quite done, lad?")
        player<Neutral>("Yes, for the time being anyway.")
        npc<Happy>(
            "Well, ye'll be glad to know that after that little performance I'd be glad to " +
                "have ye on me crew!",
        )
        player<Happy>("Huzzah!")
        npc<Happy>("Course, I'll have te kill ye first.")
        player<Sad>("Oh...")
        npc<Neutral>(
            "Don't ye worry, lad. After we take the island I'll have the boss haul yer body " +
                "to the temple and...",
        )
        npc<Shifty>("Err, never mind.")
        player<Quiz>("Never mind what?")
        npc<Shifty>("Ferget I said anything.")
    }

    // ===== Progress 17: First swill delivery (the trick) =====

    private suspend fun Player.deliverSwillFirstTime() {
        npc<Quiz>("Be that the finest, most abrasive 'rum' I've ever smelled?")
        player<Happy>("Yes! That it be!")
        npc<Angry>("Hand it over or I'll run ye through!")
        inventory.remove("unsanitary_swill")
        set("rum_deal", "told_donnie")
        advanceSwabVarbits()
        statement("The Captain drinks the 'rum' as quickly as possible.")
        npc<Drunk>("Arr.")
        npc<Neutral>("Ye be a good lad, fer a filthy livin' landlubber.")
        player<Quiz>("So... I take it your boss will be pleased?")
        npc<Drunk>("Arr, that he will. I'll tell...")
        npc<Shifty>("Wait a minute...")
        npc<Happy>("Arr, ye tricky dog!")
        npc<Neutral>("Ye tried to trick old Donnie!")
        player<Neutral>("Oh well, I guess I'll have to try again.")
        npc<Drunk>(
            "Arr, lad, you tried to trick me, but I was too clever for ye!",
        )
        npc<Neutral>(
            "Besides, Rabid Jack would have my hide if I told ye it were him that sent me!",
        )
        player<Shifty>("I'm sure he would. Good job you caught me out, eh!")
        npc<Drunk>("Aye! Now get ye gone, and don't return without more 'rum'!")
    }

    // ===== Repeat swill delivery (later attempts) =====

    private suspend fun Player.deliverSwillRepeat() {
        npc<Angry>("Hey you! I can smell the 'rum' on ye!")
        npc<Angry>(
            "If it isn't in my belly by the time I cout ter three, I'll have ye flogged!",
        )
        inventory.remove("unsanitary_swill")
        statement("Donnie drinks the 'rum' like it is water.")
        npc<Drunk>("Arrr...that hits the spot...")
    }

    // ===== Helpers =====

    /**
     * Once Donnie has drunk the swill the whole crew joins him, so every zombie pirate
     * outside the compound switches to its drunk variant.
     */
    private fun Player.advanceSwabVarbits() {
        for (swab in 'a'..'f') {
            set("rum_deal_swab_$swab", 1)
        }
    }
}
