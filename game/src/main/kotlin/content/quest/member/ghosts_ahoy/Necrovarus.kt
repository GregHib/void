package content.quest.member.ghosts_ahoy

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Necrovarus : Script {
    init {
        npcOperate("Talk-To", "ahoy_necrovarus") { (target) ->
            if (!checkGhostspeak()) return@npcOperate
            when (ghosts_ahoy) {
                0 -> chitchatOptions()
                1 -> pleaForVelorina()
                2, 3 -> ignored()
                4 -> petitionFlow(target)
                6 -> commandPhase()
                7, 8 -> postQuestTaunt()
            }
        }
    }

    private suspend fun Player.chitchatOptions() {
        choice {
            option<Quiz>("What is this place?") {
                randomRebuff()
            }
            option<Quiz>("What happened to everyone here?") {
                randomRebuff()
            }
            option<Quiz>("How do I get into the town?") {
                randomRebuff()
            }
        }
    }

    private suspend fun Player.randomRebuff() {
        when ((0..3).random()) {
            0 -> npc<Neutral>("Speak to me again and I will rend the soul from your flesh.")
            1 -> npc<Neutral>("I will answer questions when you are dead!!")
            2 -> npc<Neutral>("You dare to speak to me??? Have you lost your wits????")
            3 -> npc<Neutral>("I do not answer questions, mortal fool!!")
        }
    }

    private suspend fun Player.pleaForVelorina() {
        player<Neutral>("I must speak with you on behalf of Velorina.")
        npc<Neutral>("You dare to speak that name in this place?????")
        player<Scared>("She wants to pass-")
        npc<Neutral>("Silence!! Or I will incinerate the flesh from your bones!!!")
        player<Scared>("But she-")
        ghosts_ahoy = 2
        npc<Neutral>(
            "Get out of my sight!! Or I promise you that you will regret your insolence for " +
                "the rest of eternity!!!",
        )
    }

    private suspend fun Player.ignored() {
        player<Neutral>("Please, listen to me-")
        npc<Neutral>("No - listen to me. Go from this place and do not return, or I will remove your head.")
    }

    private suspend fun Player.petitionFlow(necrovarus: world.gregs.voidps.engine.entity.character.npc.NPC) {
        val petition = get("ahoy_signaturecounter", 0)
        val doorUnlocked = get("ahoy_templedoor_unlocked", false)
        when {
            petition in 1..11 && !inventory.contains("petition_form") || doorUnlocked -> {
                player<Neutral>(
                    "It matters not that you ignore your citizens' wishes, Necrovarus. Wheels " +
                        "have been set in motion, wheels that will set them free.",
                )
                npc<Neutral>(
                    "I have almost completely lost patience with you, mortal. Another word, " +
                        "and every threat I have uttered will be made real for you.",
                )
            }

            petition == 31 -> {
                player<Neutral>("So, have you changed your mind yet?")
                npc<Neutral>("NEVER!!!!!")
                if (ownsItem("bone_key_ghosts_ahoy")) {
                    npc<Neutral>("WHICH IS A VERY LONG TIME!!!!!!")
                } else {
                    statement("In his rage, Necrovarus drops a key on the floor.")
                    FloorItems.add(necrovarus.tile, "bone_key_ghosts_ahoy", revealTicks = 0, disappearTicks = 300, owner = this)
                }
            }

            petition == 11 -> presentPetition(necrovarus)
            petition >= 1 -> {
                player<Neutral>(
                    "I must let you know, Necrovarus, that I am collecting signatures from " +
                        "the citizens of Port Phasmatys.",
                )
                npc<Neutral>(
                    "Oh, I do admire an activist. Do let me know when you're finished, and " +
                        "I'll give it my fullest consideration.",
                )
            }

            else -> {
                player<Neutral>(
                    "Wheels have been set in motion, Necrovarus; wheels that will set the " +
                        "citizens of Port Phasmatys free.",
                )
                npc<Neutral>("Oh goody goody. I just can't wait.")
            }
        }
    }

    private suspend fun Player.presentPetition(necrovarus: world.gregs.voidps.engine.entity.character.npc.NPC) {
        player<Neutral>(
            "Necrovarus, I am presenting you with a petition form that has been signed by 10 " +
                "citizens of Port Phasmatys.",
        )
        npc<Neutral>("A petition you say? Continue, mortal.")
        player<Neutral>(
            "It says that the citizens of Port Phasmatys should have the right to choose " +
                "whether they pass over into the next world or not, and not have this " +
                "decided by the powers that be on their behalf.",
        )
        npc<Neutral>("I see.")
        player<Neutral>("So you will let them pass over if they wish?")
        npc<Neutral>("Oh yes.")
        player<Shifty>("Really?")
        npc<Neutral>(
            "NO!!!!! Get out of my sight before I burn every ounce of flesh from your bones!!!!!",
        )
        set("ahoy_signaturecounter", 31)
        inventory.remove("petition_form")
        addOrDrop("ashes")
        statement("The petition form turns to ashes in your hand.")
        statement("In his rage, Necrovarus drops a key on the floor.")
        FloorItems.add(necrovarus.tile, "bone_key_ghosts_ahoy", revealTicks = 0, disappearTicks = 300, owner = this)
    }

    private suspend fun Player.commandPhase() {
        npc<Neutral>(
            "You dare to face me again - you must be truly insane!!!!",
        )
        if (!equipment.contains("ghostspeak_amulet_enchanted")) {
            return
        }
        player<Neutral>(
            "No, Necrovarus, I am not insane. With this enchanted amulet of ghostspeak I have " +
                "the power to command you to do my will!",
        )
        commandChoice()
    }

    private suspend fun Player.commandChoice() {
        choice("What do you want to command Necrovarus to do?") {
            option<Neutral>("Let any ghost who so wishes pass on into the next world.") {
                releaseBan()
            }
            option<Neutral>("Tell me a joke.") {
                joke()
            }
            option<Neutral>("Do a chicken impression.") {
                chicken()
            }
        }
    }

    private suspend fun Player.releaseBan() {
        item(
            item = "ghostspeak_amulet_enchanted",
            text = "A beam of intense green light radiates out from the amulet of ghostspeak, " +
                "enveloping Necrovarus in its power. His eyes become softer, and appear to " +
                "stare into nothingness.",
        )
        ghosts_ahoy = 7
        equipment.replace(EquipSlot.Weapon.index, "ghostspeak_amulet_enchanted", "ghostspeak_amulet")
        set("ahoy_templedoor_unlocked", true)
        npc<Neutral>("I - will - let ...")
        player<Neutral>("Carry on...")
        npc<Neutral>(" ... any ... ")
        player<Neutral>("Yes?")
        npc<Neutral>("... ghost who so wishes ... ")
        player<Neutral>("I think we're almost getting there...")
        npc<Neutral>("... pass into the next world.")
    }

    private suspend fun Player.joke() {
        item(
            item = "ghostspeak_amulet_enchanted",
            text = "A beam of intense green light radiates out from the amulet of ghostspeak, " +
                "enveloping Necrovarus in its power. His eyes become softer, and appear to " +
                "stare into nothingness.",
        )
        npc<Neutral>("Knock knock")
        player<Neutral>("Who's there?")
        npc<Neutral>("Egbert.")
        player<Neutral>("Egbert who?")
        npc<Neutral>("Egbert no bacon.")
        item(
            item = "ghostspeak_amulet_enchanted",
            text = "Luckily the amulet of ghostspeak does not seem to have fully discharged.",
        )
    }

    private suspend fun Player.chicken() {
        item(
            item = "ghostspeak_amulet_enchanted",
            text = "A beam of intense green light radiates out from the amulet of ghostspeak, " +
                "enveloping Necrovarus in its power. His eyes become softer, and appear to " +
                "stare into nothingness.",
        )
        npc<Neutral>("Cluck cluck squuuaaaakkkk cluck cluck")
        npc<Neutral>("I think I've laid an egg...")
        item(
            item = "ghostspeak_amulet_enchanted",
            text = "Luckily the amulet of ghostspeak does not seem to have fully discharged.",
        )
    }

    private suspend fun Player.postQuestTaunt() {
        player<Neutral>(
            "Told you I'd defeat you, Necrovarus. My advice to you is to pass over to the " +
                "next world yourself with everybody else.",
        )
        npc<Neutral>("I should fry you for what you have done...")
        player<Neutral>(
            "Quiet, evil priest!! If you try anything I will command you again, but this time " +
                "it will be to throw yourself into the Endless Void for the rest of eternity.",
        )
        npc<Neutral>("Please no! I will do whatever you say!!")
    }
}
