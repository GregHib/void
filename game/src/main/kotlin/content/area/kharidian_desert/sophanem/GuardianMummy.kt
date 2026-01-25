package content.area.kharidian_desert.sophanem

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class GuardianMummy : Script {

    val ivory = listOf("ivory_comb", "pottery_scarab", "pottery_statuette")
    val stone = listOf("stone_seal", "stone_scarab", "stone_statuette")
    val gold = listOf("gold_seal", "gold_scarab", "gold_statuette")

    init {
        npcOperate("Talk-to", "guardian_mummy") {
            if (carriesItem("pharaohs_sceptre")) {
                sceptreRecharging()
                return@npcOperate
            }
            notAnother()
        }

        npcOperate("Start-activity", "guardian_mummy") {
            player<Pleased>("I know what I'm doing - let's get on with it.")
            iKnowWhatImDoing()
        }

        itemOnNPCOperate("pharaohs_sceptre_*", "guardian_mummy") {
            discharge(it.slot)
        }

        itemOnNPCOperate("pharaohs_sceptre", "guardian_mummy") {
            sceptreRecharging()
        }

        itemOnNPCOperate("*", "guardian_mummy") {
            message("The Mummy is not interested in this")
        }
    }

    suspend fun Player.notAnother() {
        npc<Neutral>("*sigh* Not another one.")
        player<Quiz>("Another what?")
        npc<Neutral>("Another 'archaeologist'.")
        npc<Neutral>("I'm not going to let you plunder my master's tomb you know.")
        player<Neutral>("That's a shame, have you got anything else I could do while I'm here?")
        npc<Neutral>("If it will keep you out of mischief I suppose I could set something up for you...")
        npc<Neutral>("I have a few rooms full of some things you humans might consider valuable, do you want to give it a go?")
        playPyramidPlunder()
    }

    suspend fun Player.playPyramidPlunder() {
        choice("Play the 'Pyramid Plunder' minigame?") {
            option<Neutral>("That sounds like fun, what do I do?") {
                soundsLikeFun()
            }
            option<Neutral>("Not right now") {
                npc<Neutral>("Well, get out of here then.")
            }
            option<Pleased>("I know what I'm doing let's get on with it.") {
                iKnowWhatImDoing()
            }
            option("I want to charge or remove charges from my sceptre.") {
                if (carriesItem("pharaohs_sceptre")) {
                    sceptreRecharging()
                } else {
                    sceptreDischarging()
                }
            }
        }
    }

    suspend fun Player.itIsDone() {
        npc<Neutral>("It is done.")
        playPyramidPlunder()
    }

    suspend fun Player.sceptreRecharging() {
        player<Neutral>("This sceptre seems to have run out of charges.")
        npc<Neutral>("You shouldn't have that thing in the first place, thief!")
        player<Neutral>("If I gave you back some of the artefacts I've taken from the tomb, would you recharge the sceptre for me.")
        npc<Neutral>("*sigh* Oh alright. But only if the sceptre is fully empty, I'm not wasting the King's magic...")
        choice("Recharge the sceptre with...") {
            option("Gold artefacts?") {
                if (inventory.chargeSceptre(6, gold)) {
                    statement("You recharge your sceptre with gold artefacts.")
                } else {
                    npc<Neutral>("You need to have 6 gold artefacts to recharge your sceptre.")
                }
            }
            option("Stone artefacts?") {
                if (inventory.chargeSceptre(12, stone)) {
                    statement("You recharge your sceptre with stone artefacts.")
                } else {
                    npc<Neutral>("You need to have 12 stone artefacts to recharge your sceptre.")
                }
            }
            option("Pottery and Ivory artefacts?") {
                if (inventory.chargeSceptre(24, ivory)) {
                    statement("You recharge your sceptre with stone artefacts.")
                } else {
                    npc<Neutral>("You need to have 24 pottery or ivory artefacts to recharge your sceptre.")
                }
            }
            option("Actually, I'm more interested in plundering the tombs.") {
                notAnother()
            }
        }
    }

    suspend fun Player.sceptreDischarging() {
        val count = inventory.items.count { it.id.startsWith("pharaohs_sceptre_") }
        if (count < 0) {
            player<Neutral>("I want to charge my sceptre.")
            npc<Confused>("What sceptre?")
            player<Quiz>("Er... I don't know.")
            npc<Quiz>("Right...")
            return
        }
        if (count == 1) {
            val index = inventory.items.indexOfFirst { it.id.startsWith("pharaohs_sceptre_") }
            discharge(index)
            return
        }
        npc<Neutral>("If I must. You have $count sceptres with charges. Do you want them all emptied?")
        choice {
            option("Yes, uncharge all my sceptres.") {
                val success = inventory.transaction {
                    for (i in 0 until count) {
                        val index = inventory.items.indexOfFirst { it.id.startsWith("pharaohs_sceptre_") }
                        replace(index, inventory[index].id, "pharaohs_sceptre")
                    }
                }
                if (success) {
                    itIsDone()
                }
            }
            option("No, I'll hand you the one I want emptied.")
        }
    }

    fun Inventory.chargeSceptre(amount: Int, items: List<String>): Boolean = transaction {
        var remaining = amount
        for (item in items) {
            if (remaining <= 0) {
                break
            }
            remaining -= removeToLimit(item, remaining)
        }
        if (remaining > 0) {
            error = TransactionError.Deficient(remaining)
        }
        replace("pharaohs_sceptre", "pharaohs_sceptre_3")
    }

    suspend fun Player.soundsLikeFun() {
        npc<Neutral>("You have five minutes to explore the treasure rooms and collect as many artefacts as you can.")
        npc<Neutral>("The artefacts are in the urns, chests and sarcophagi found in each room.")
        npc<Neutral>("There are eight treasure rooms, each subsequent room requires higher thieving skills to both enter the room and thieve from the urns and other containers.")
        npc<Neutral>("The rewards also become more lucrative the further into the tomb you go.")
        npc<Neutral>("You will also have to deactivate a trap in order to enter the main part of each room.")
        npc<Neutral>("When you want to move onto the next room you need to find the correct door first.")
        npc<Neutral>("There are four possible exits, you must open the door before finding out whether it is the exit or not.")
        npc<Neutral>("Opening the doors require picking their locks. Having a lockpick will make this easier.")
        anymoreQuestions("How do I get the artefacts?")
    }

    suspend fun Player.anymoreQuestions(option: String) {
        choice("Do you have any more questions?") {
            option<Neutral>("How do I leave the game?") {
                howDoILeave()
            }
            option<Neutral>(option) {
                howToGetArtefacts()
            }
            option<Neutral>("What do I do with the artefacts I collect?") {
                whatToDoWithArtefacts()
            }
            option("I'm ready to give it a go now.") {
                iKnowWhatImDoing()
            }
        }
    }

    suspend fun Player.iKnowWhatImDoing() {
        statement("Pyramid Plunder is not currently implemented.")
        //    npc<Talk>("Fine, I'll take you to the first room now...")
    }

    suspend fun Player.howDoILeave() {
        npc<Neutral>("If at any point you decide you need to leave just use a glowing door.")
        npc<Neutral>("The game will end and you will be taken out of the pyramid.")
        anymoreQuestions("What about the chests and sarcophagi?")
    }

    suspend fun Player.howToGetArtefacts() {
        npc<Neutral>("The artefacts are in the urns, chests and sarcophagi.")
        npc<Neutral>("Urns contain snakes that guard them.")
        npc<Neutral>("The sarcophagi take some strength to open. They take a while to open.")
        npc<Neutral>("Of course, Mummies have been known to take a nap in the sarcophagi, so beware.")
        npc<Neutral>("The golden chests generally contain better artefacts, but are also trapped with scarabs!")
        anymoreQuestions("What about the chests and sarcophagi?")
    }

    suspend fun Player.whatToDoWithArtefacts() {
        npc<Neutral>("There are a number of different artefacts, of three main types. The least valuable are the pottery statuettes and scarabs, and the ivory combs.")
        npc<Neutral>("Next are the stone scarabs, statuettes and seals, and finally the gold versions of those artefacts.")
        npc<Neutral>("They are not old, but are well made.")
        player<Neutral>("What do I do with artefacts once I've collected them?")
        npc<Neutral>("That Simon Simpleton, I mean Templeton, will probably give you some money for them.")
        npc<Neutral>("He couldn't spot a real artefact if it came up to him and bit him in the face.")
        npc<Neutral>("He usually slinks about near the pyramid north-east of Sophanem. I expect he's trying to get some poor fools to steal things from that pyramid as well.")
        npc<Neutral>("I expect he'll give you more gold for some than others.")
        anymoreQuestionsSceptre()
    }

    suspend fun Player.anymoreQuestionsSceptre() {
        choice("Do you have any more questions?") {
            option<Neutral>("How do I leave the game?") {
                howDoILeave()
            }
            option<Neutral>("What about the chests and sarcophagi?") {
                howToGetArtefacts()
            }
            option<Neutral>("What's this I hear about a Golden Sceptre?") {
                whereDidYouHearAboutThat()
            }
            option("I'm ready to give it a go now.") {
                iKnowWhatImDoing()
            }
        }
    }

    suspend fun Player.leaveTheTomb() {
        choice("Leave the Tomb?") {
            option("Yes, I'm out of here.")
            option("Ah, I think I'll stay a little longer.")
        }
    }

    suspend fun Player.whereDidYouHearAboutThat() {
        npc<Neutral>("Where did you hear about that?")
        player<Neutral>("I couldn't possibly say.")
        npc<Neutral>("It's the only genuinely valuable artefact in this place.")
        npc<Neutral>("It links all the great pyramids in the area, and can be used to travel between them.")
        npc<Neutral>("It requires charging with offerings of fine craftsmanship, that's why we have so many spare artefacts lying around.")
        npc<Neutral>("Anyway, I won't let you get your grubby little hands on the sceptre.")
        anymoreQuestions("What about the chests and sarcophagi?")
    }

    suspend fun Player.discharge(index: Int) {
        if (inventory.replace(index, inventory[index].id, "pharaohs_sceptre")) {
            itIsDone()
        }
    }
}
