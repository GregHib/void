package content.skill.slayer

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.skill.slayer.SlayerChallenge.Companion.mazchnaFairyRings
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class EnchantedGem : Script {

    private val femaleMasters = setOf("chaeldar", "sumona", "kuradal", "spria")

    init {
        itemOption("Activate", "enchanted_gem") {
            if (contactNearFairyRing()) {
                return@itemOption
            }
            val master = slayerMaster
            if (master == "") {
                message("You need something new to hunt; return to a Slayer master.")
                return@itemOption
            }
            npc<Happy>(master, "Hello there ${this@itemOption.name}, what can I help you with?")
            choice {
                howAmIDoing()
                whoAreYou()
                whereAreYou()
                anyTips()
                option<Neutral>("That's all thanks.")
            }
        }

        itemOption("Kills-left", "enchanted_gem,ring_of_slaying_*") {
            if (slayerChallenge != "") {
                message("You are currently doing a special task for ${slayerMaster.toSentenceCase()}; speak with ${if (slayerMaster in femaleMasters) "her" else "him"} if you need help with this task.")
            } else if (slayerTask == "nothing") {
                message("You need something new to hunt; return to a Slayer master.")
            } else {
                message("Your current assignment is: ${slayerTask.toLowerSpaceCase()}; only $slayerTaskRemaining more to go.")
            }
        }
    }

    fun ChoiceOption.howAmIDoing() {
        option<Quiz>("How am I doing so far?") {
            if (slayerChallenge != "") {
                npc<Happy>(slayerMaster, "You're currently working on my special challenge. Your reward point tally is $slayerPoints.")
            } else if (slayerTask == "nothing") {
                npc<Confused>(slayerMaster, "You need something new to hunt. Come and see me when you can and I'll give you a new task.")
            } else {
                npc<Happy>(slayerMaster, "You're currently assigned to kill ${slayerTask.toLowerSpaceCase()}; only $slayerTaskRemaining more to go. Your reward point tally is $slayerPoints.")
            }
            choice {
                whoAreYou()
                whereAreYou()
                anyTips()
                option<Neutral>("That's all thanks.")
            }
        }
    }

    fun ChoiceOption.whoAreYou() {
        option<Quiz>("Who are you?") {
            npc<Neutral>(slayerMaster, "My name is ${slayerMaster.toSentenceCase()}, I'm a Slayer Master.")
            choice {
                howAmIDoing()
                whereAreYou()
                anyTips()
                option<Neutral>("That's all thanks.")
            }
        }
    }

    fun ChoiceOption.whereAreYou() {
        option<Quiz>("Where are you?") {
            when (slayerMaster) {
                "turael" -> npc<Neutral>(slayerMaster, "You'll find me in Burthorpe. I'll be here when you need a new task.")
                "vannaka" -> npc<Neutral>(slayerMaster, "You'll find me in Edgeville. I'll be here when you need a new task.")
                "duradel" -> npc<Neutral>(slayerMaster, "You'll find me in Shilo Village. I'll be here when you need a new task.")
                "mazchna" -> npc<Neutral>(slayerMaster, "You'll find me in Canifis. I'll be here when you need a new task.")
                "chaeldar" -> npc<Neutral>(slayerMaster, "You'll find me in Zanaris. I'll be here when you need a new task.")
                "sumona" -> npc<Neutral>(slayerMaster, "You'll find me in Pollnivneach. I'll be here when you need a new task.")
                "kuradal" -> npc<Neutral>(slayerMaster, "You'll find me in the ancient cavern, near the Dragonkin Forge. I'll be here when you need a new task.")
            }
            choice {
                howAmIDoing()
                whoAreYou()
                anyTips()
                option<Neutral>("That's all thanks.")
            }
        }
    }

    fun ChoiceOption.anyTips() {
        option<Quiz>("Got any tips for me?") {
            val tip = Tables.stringOrNull("${slayerMaster}_slayer_tasks.$slayerTask.tip") ?: return@option
            npc<Neutral>(slayerMaster, tip)
            choice {
                howAmIDoing()
                whoAreYou()
                whereAreYou()
                option<Neutral>("That's all thanks.")
            }
        }
    }

    /**
     * Contacting Mazchna with an enchanted gem near a Morytania fairy ring summons a monster for their challenge.
     * @return true if a monster was summoned
     */
    internal suspend fun Player.contactNearFairyRing(): Boolean {
        if (slayerChallenge != "ravenous_creatures") {
            return false
        }
        val index = mazchnaFairyRings.indices.firstOrNull { tile.within(mazchnaFairyRings[it].second, 10) && challengeProgress and (1 shl it) == 0 } ?: return false
        val (id, ring) = mazchnaFairyRings[index]
        if (NPCs.at(ring).any { it.id == id }) {
            return true
        }
        npc<Quiz>(slayerMaster, "Hello, $name. Are you at one of the fairy rings now?")
        when (id) {
            "ravenous_ghoul" -> {
                player<Neutral>("Yes, I've reached the ring near the farming patch to the west of Canifis.")
                npc<Quiz>(slayerMaster, "Excellent. Can you see anything out of the ordinary?")
                player<Neutral>("'Out of the ordinary' might be difficult to define around these parts.")
                statement("A column of butterflies briefly appears swarming around the centre of the fairy ring.")
                player<Shock>("Wait! Something seems to be happening with the ring.")
                npc<Quiz>(slayerMaster, "What's happening?")
                player<Neutral>("The ring is glowing with some sort of magic.")
                summon(id, ring)
                npc<Shock>(slayerMaster, "How strange. Wait, $name! Look out!")
            }
            "ravenous_vampire" -> {
                player<Neutral>("Yes, I've reached the ring in the haunted woods to the east of Canifis.")
                npc<Quiz>(slayerMaster, "Well done, $name. Can you see anything of interest?")
                player<Shock>("I don't think so... Wait, what's that up in the sky?")
                summon(id, ring)
                npc<Shock>(slayerMaster, "Oh no! Look out, $name!")
            }
            else -> {
                player<Neutral>("Yes, I've reached the ring in the swamplands to the south of Canifis.")
                npc<Quiz>(slayerMaster, "Good work. Is there anything unnatural going on?")
                player<Neutral>("Does my standing in a festering, stinking swamp count?")
                npc<Neutral>(slayerMaster, "Not really, $name!")
                player<Neutral>("Well, I can't see anything worth reporting.")
                summon(id, ring)
                npc<Shock>(slayerMaster, "Alright then let's bring you...wait what's that noise?")
            }
        }
        return true
    }

    private fun Player.summon(id: String, tile: Tile) {
        val npc = NPCs.add(id, tile, ticks = TimeUnit.MINUTES.toTicks(5), owner = this)
        npc.interactPlayer(this, "Attack")
    }
}
