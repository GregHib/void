package content.quest.member.ghosts_ahoy

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class GhostVillager : Script {
    init {
        npcOperate("Talk-To", "ahoy_ghost_villager") { (target) ->
            if (!checkGhostspeak()) return@npcOperate
            val petition = get("ahoy_signaturecounter", 0)
            val hasForm = inventory.contains("petition_form")
            val isCollecting = petition in 1..11 && hasForm

            if (!isCollecting) {
                randomChat()
                return@npcOperate
            }

            if (petition == 11) {
                player<Neutral>("Thank you for your kind support.")
                return@npcOperate
            }

            player<Quiz>("Would you sign this petition form, please?")
            when {
                equipment.contains("bedsheet_ectoplasm") -> bedsheetSignature(target)
                equipment.contains("bedsheet") -> {
                    npc<Neutral>(
                        "Why are you wearing that bedsheet? If you're trying to pretend to be " +
                            "one of us, you're not fooling anybody - you're not even green!!",
                    )
                }
                else -> npc<Neutral>(
                    "I'm sorry, but it's hard to believe that a mortal could be interested in " +
                        "helping us.",
                )
            }
        }
    }

    private suspend fun Player.randomChat() {
        when ((0..3).random()) {
            0 -> npc<Neutral>("This cold wind blows right through you, doesn't it?")
            1 -> npc<Neutral>("Worship the Ectofuntus all you want, but don't bother us, human.")
            2 -> npc<Neutral>("Why did we have to listen to that maniacal priest?")
            3 -> npc<Neutral>("We do not talk to the warm-bloods.")
        }
    }

    private suspend fun Player.bedsheetSignature(target: world.gregs.voidps.engine.entity.character.npc.NPC) {
        if (get("ahoy_last_villager_index", -1) == target.index) {
            npc<Neutral>("You only just asked me the same thing! Leave me alone - I've had my say!")
            return
        }
        set("ahoy_last_villager_index", target.index)
        when ((0..2).random()) {
            0 -> refuseSignature()
            1 -> happilySign()
            2 -> bribeSignature()
        }
    }

    private suspend fun Player.refuseSignature() {
        when ((0..4).random()) {
            0 -> npc<Quiz>("I don't have time for this nonsense.")
            1 -> npc<Quiz>("How dare you accost me in the street?")
            2 -> npc<Quiz>("Get lost.")
            3 -> npc<Quiz>("My answer is no.")
            4 -> npc<Quiz>("I will have you know that I am a fervent supporter of Necrovarus.")
        }
    }

    private suspend fun Player.happilySign() {
        when ((0..3).random()) {
            0 -> npc<Neutral>("Most certainly, I will.")
            1 -> npc<Neutral>("Yes, of course.")
            2 -> npc<Neutral>("Yes! It's about time somebody did something about Necrovarus.")
            3 -> npc<Neutral>("I'll do anything that annoys Necrovarus.")
        }
        recordSignature(0)
    }

    private suspend fun Player.bribeSignature() {
        when ((0..2).random()) {
            0 -> npc<Neutral>("I will if you make it worth my while...")
            1 -> npc<Neutral>("You scratch my back and I'll scratch yours...")
            else -> npc<Neutral>("It'll cost you...")
        }
        player<Shock>("How much?")
        val cost = (1..5).random()
        npc<Neutral>("Oh, it'll cost you $cost ecto-tokens.")
        if (!inventory.contains("ecto_token", cost)) {
            player<Neutral>("I don't have that many on me.")
            npc<Neutral>("No tokens, no signature.")
            return
        }
        choice {
            option<Bored>("Okay, if you insist.") {
                inventory.remove("ecto_token", cost)
                recordSignature(cost)
            }
            option<Angry>("There's no way I'm giving in to corruption.") {
                npc<Neutral>("Suit yourself.")
            }
        }
    }

    private suspend fun Player.recordSignature(@Suppress("UNUSED_PARAMETER") tokensPaid: Int) {
        val newCount = get("ahoy_signaturecounter", 0) + 1
        set("ahoy_signaturecounter", newCount)
        val total = newCount - 1
        val text = when (total) {
            10 -> "You have succeeded in obtaining 10 signatures on the petition form!"
            1 -> "The ghost signs your petition.<br>You have obtained 1 signature so far."
            else -> "The ghost signs your petition.<br>You have obtained $total signatures so far."
        }
        item(item = "petition_form", text = text)
    }
}
