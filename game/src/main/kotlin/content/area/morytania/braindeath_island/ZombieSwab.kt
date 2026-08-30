package content.area.morytania.braindeath_island

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male

/**
 * The six zombie swabs (2843-2848) guarding the blindweed patch outside the brewery.
 *
 * Each swab has its own varbit tracking whether the player has intimidated it. Until then
 * "Intimidate" runs the heckling routine and the swab attacks on sight; afterwards the swab
 * only cowers when talked to.
 */
class ZombieSwab : Script {

    init {
        // Swabs use their own hunt mode rather than "cowardly" so aggression can be judged per
        // player - the generic handler attacks regardless of how far along the quest is.
        huntPlayer("zombie_swab*", "zombie_swab") { target ->
            if (!Settings["world.npcs.aggression", true]) {
                return@huntPlayer
            }
            // A swab that has been put in its place won't start anything, and there's nothing
            // left to guard once the rum is brewed
            if (target.intimidated(this) || target.questCompleted("rum_deal")) {
                return@huntPlayer
            }
            interactPlayer(target, "Attack")
        }

        npcOperate("Talk-to", "zombie_swab*") { (target) ->
            if (!intimidated(target)) {
                return@npcOperate message("I don't think he wants to talk to you.")
            }
            cower(target)
        }

        npcOperate("Intimidate", "zombie_swab*") { (target) ->
            if (intimidated(target)) {
                return@npcOperate message("I don't think he will be causing any more trouble.")
            }
            heckle(target)
        }
    }

    private suspend fun Player.cower(swab: NPC) {
        when (swab.number()) {
            1 -> npc<Scared>("Arr! Do what ye like, just don't hurt my face!")
            2 -> npc<Scared>("(Gulp) Be there anything ye want, ${sirOrMiss()}?")
            3 -> npc<Scared>("Be there anything I can say to get out of here undead?")
            4 -> npc<Scared>("Arrgh! It's ${himOrHer()}! Run fer it lads!")
            5 -> npc<Scared>("No! Not the flyswatter! I'll be good!")
            6 -> npc<Scared>("Egad! 'Tis ${himOrHer()}!")
        }
    }

    private suspend fun Player.heckle(swab: NPC) {
        val number = swab.number()
        player<Angry>("Hey you!")
        when (number) {
            1 -> npc<Angry>("Arr! Prepare to die!")
            2 -> npc<Angry>("Yer a deadman, sez I!")
            3 -> npc<Angry>("Yer brave, but stupid!")
            4 -> npc<Laugh>("Hahahaha!")
            5 -> npc<Angry>("Who? Me?")
            6 -> npc<Angry>("Hey yerself, landlubber!")
        }
        if (number == 4) {
            npc<Angry>("Yer goin ter die!")
        }
        statement(
            "We interrupt this insult to bring you this soothing picture of a kitten. " +
                "Normal service will be resumed shortly. Thank you for your patience.",
            clickToContinue = false,
        )
        open("rum_deal_censor")
        open("total_blackness")
        delay(8)
        close("rum_deal_censor")
        close("total_blackness")
        set(swab.varbit(), 1)
        anim("emote_angry")
        when (number) {
            1 -> {
                player<Angry>(
                    "...meaning you'll need a dozen Ogres and a team of highly skilled surgeons " +
                        "to remove them!",
                )
                backDown(swab)
            }
            2 -> {
                player<Angry>("...leading to the eventual, violent explosion of those organs!")
                backDown(swab)
            }
            3 -> {
                player<Angry>(
                    "...and then I'll use a wrench to remove the handles, so you'll see it " +
                        "every time you cough!",
                )
                backDown(swab)
            }
            4, 5 -> {
                message("He seems to have vanished...you must have scared him off.")
                swab.respawn(swab.def["respawn_delay", 50])
            }
            6 -> {
                player<Angry>(
                    "...so that you will know exactly what time it is due to the horrible pain " +
                        "in your earlobes!",
                )
                backDown(swab)
            }
        }
    }

    private suspend fun Player.backDown(swab: NPC) {
        npc<Scared>("That be the most intimidatin' thing I've ever heard! I'm not messin' with you!")
        swab.anim("swab_panic")
    }

    private fun Player.intimidated(swab: NPC): Boolean = get(swab.varbit(), 0) == 1

    /** Swabs are named `zombie_swab` then `zombie_swab_2` through `zombie_swab_6`. */
    private fun NPC.number(): Int = id.substringAfterLast('_').toIntOrNull() ?: 1

    private fun NPC.varbit(): String = "rum_deal_swab_${'a' + number() - 1}"

    private fun Player.sirOrMiss(): String = if (male) "sir" else "miss"

    private fun Player.himOrHer(): String = if (male) "him" else "her"
}
