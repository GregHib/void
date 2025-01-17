package world.gregs.voidps.world.map.sophanem

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.event.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.world.interact.dialogue.Pleased
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.dialogue.type.statement

val ivory = listOf("ivory_comb", "pottery_scarab", "pottery_statuette")
val stone = listOf("stone_seal", "stone_scarab", "stone_statuette")
val gold = listOf("gold_seal", "gold_scarab", "gold_statuette")

npcOperate("Talk-to", "guardian_mummy") {
    if (player.holdsItem("pharaohs_sceptre")) {
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
    discharge(itemSlot)
}

itemOnNPCOperate("pharaohs_sceptre", "guardian_mummy") {
    sceptreRecharging()
}

suspend fun CharacterContext<Player>.notAnother() {
    npc<Talk>("*sigh* Not another one.")
    player<Quiz>("Another what?")
    npc<Talk>("Another 'archaeologist'.")
    npc<Talk>("I'm not going to let you plunder my master's tomb you know.")
    player<Talk>("That's a shame, have you got anything else I could do while I'm here?")
    npc<Talk>("If it will keep you out of mischief I suppose I could set something up for you...")
    npc<Talk>("I have a few rooms full of some things you humans might consider valuable, do you want to give it a go?")
    playPyramidPlunder()
}

suspend fun CharacterContext<Player>.playPyramidPlunder() {
    choice("Play the 'Pyramid Plunder' minigame?") {
        option<Talk>("That sounds like fun, what do I do?") {
            soundsLikeFun()
        }
        option<Talk>("Not right now") {
            npc<Talk>("Well, get out of here then.")
        }
        option<Pleased>("I know what I'm doing let's get on with it.") {
            iKnowWhatImDoing()
        }
        option("I want to charge or remove charges from my sceptre.") {
            if (player.holdsItem("pharaohs_sceptre")) {
                sceptreRecharging()
            } else {
                sceptreDischarging()
            }
        }
    }
}

suspend fun CharacterContext<Player>.itIsDone() {
    npc<Talk>("It is done.")
    playPyramidPlunder()
}

suspend fun CharacterContext<Player>.sceptreRecharging() {
    player<Talk>("This sceptre seems to have run out of charges.")
    npc<Talk>("You shouldn't have that thing in the first place, thief!")
    player<Talk>("If I gave you back some of the artefacts I've taken from the tomb, would you recharge the sceptre for me.")
    npc<Talk>("*sigh* Oh alright. But only if the sceptre is fully empty, I'm not wasting the King's magic...")
    choice("Recharge the sceptre with...") {
        option("Gold artefacts?") {
            if (player.inventory.chargeSceptre(6, gold)) {
                statement("You recharge your sceptre with gold artefacts.")
            } else {
                npc<Talk>("You need to have 6 gold artefacts to recharge your sceptre.")
            }
        }
        option("Stone artefacts?") {
            if (player.inventory.chargeSceptre(12, stone)) {
                statement("You recharge your sceptre with stone artefacts.")
            } else {
                npc<Talk>("You need to have 12 stone artefacts to recharge your sceptre.")
            }
        }
        option("Pottery and Ivory artefacts?") {
            if (player.inventory.chargeSceptre(24, ivory)) {
                statement("You recharge your sceptre with stone artefacts.")
            } else {
                npc<Talk>("You need to have 24 pottery or ivory artefacts to recharge your sceptre.")
            }
        }
        option("Actually, I'm more interested in plundering the tombs.") {
            notAnother()
        }
    }
}

suspend fun CharacterContext<Player>.sceptreDischarging() {
    val count = player.inventory.items.count { it.id.startsWith("pharaohs_sceptre_") }
    if (count < 0) {
        player<Talk>("I want to charge my sceptre.")
        npc<Uncertain>("What sceptre?")
        player<Quiz>("Er... I don't know.")
        npc<Quiz>("Right...")
        return
    }
    if (count == 1) {
        val index = player.inventory.items.indexOfFirst { it.id.startsWith("pharaohs_sceptre_") }
        discharge(index)
        return
    }
    npc<Talk>("If I must. You have $count sceptres with charges. Do you want them all emptied?")
    choice {
        option("Yes, uncharge all my sceptres.") {
            val success = player.inventory.transaction {
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

fun Inventory.chargeSceptre(amount: Int, items: List<String>): Boolean {
    return transaction {
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
}

itemOnNPCOperate("*", "guardian_mummy") {
    player.message("The Mummy is not interested in this")
}

suspend fun CharacterContext<Player>.soundsLikeFun() {
    npc<Talk>("You have five minutes to explore the treasure rooms and collect as many artefacts as you can.")
    npc<Talk>("The artefacts are in the urns, chests and sarcophagi found in each room.")
    npc<Talk>("There are eight treasure rooms, each subsequent room requires higher thieving skills to both enter the room and thieve from the urns and other containers.")
    npc<Talk>("The rewards also become more lucrative the further into the tomb you go.")
    npc<Talk>("You will also have to deactivate a trap in order to enter the main part of each room.")
    npc<Talk>("When you want to move onto the next room you need to find the correct door first.")
    npc<Talk>("There are four possible exits, you must open the door before finding out whether it is the exit or not.")
    npc<Talk>("Opening the doors require picking their locks. Having a lockpick will make this easier.")
    anymoreQuestions("How do I get the artefacts?")
}

suspend fun CharacterContext<Player>.anymoreQuestions(option: String) {
    choice("Do you have any more questions?") {
        option<Talk>("How do I leave the game?") {
            howDoILeave()
        }
        option<Talk>(option) {
            howToGetArtefacts()
        }
        option<Talk>("What do I do with the artefacts I collect?") {
            whatToDoWithArtefacts()
        }
        option("I'm ready to give it a go now.") {
            iKnowWhatImDoing()
        }
    }
}

suspend fun CharacterContext<Player>.iKnowWhatImDoing() {
    statement("Pyramid Plunder is not currently implemented.")
//    npc<Talk>("Fine, I'll take you to the first room now...")
}

suspend fun CharacterContext<Player>.howDoILeave() {
    npc<Talk>("If at any point you decide you need to leave just use a glowing door.")
    npc<Talk>("The game will end and you will be taken out of the pyramid.")
    anymoreQuestions("What about the chests and sarcophagi?")
}

suspend fun CharacterContext<Player>.howToGetArtefacts() {
    npc<Talk>("The artefacts are in the urns, chests and sarcophagi.")
    npc<Talk>("Urns contain snakes that guard them.")
    npc<Talk>("The sarcophagi take some strength to open. They take a while to open.")
    npc<Talk>("Of course, Mummies have been known to take a nap in the sarcophagi, so beware.")
    npc<Talk>("The golden chests generally contain better artefacts, but are also trapped with scarabs!")
    anymoreQuestions("What about the chests and sarcophagi?")
}

suspend fun CharacterContext<Player>.whatToDoWithArtefacts() {
    npc<Talk>("There are a number of different artefacts, of three main types. The least valuable are the pottery statuettes and scarabs, and the ivory combs.")
    npc<Talk>("Next are the stone scarabs, statuettes and seals, and finally the gold versions of those artefacts.")
    npc<Talk>("They are not old, but are well made.")
    player<Talk>("What do I do with artefacts once I've collected them?")
    npc<Talk>("That Simon Simpleton, I mean Templeton, will probably give you some money for them.")
    npc<Talk>("He couldn't spot a real artefact if it came up to him and bit him in the face.")
    npc<Talk>("He usually slinks about near the pyramid north-east of Sophanem. I expect he's trying to get some poor fools to steal things from that pyramid as well.")
    npc<Talk>("I expect he'll give you more gold for some than others.")
    anymoreQuestionsSceptre()
}

suspend fun CharacterContext<Player>.anymoreQuestionsSceptre() {
    choice("Do you have any more questions?") {
        option<Talk>("How do I leave the game?") {
            howDoILeave()
        }
        option<Talk>("What about the chests and sarcophagi?") {
            howToGetArtefacts()
        }
        option<Talk>("What's this I hear about a Golden Sceptre?") {
            whereDidYouHearAboutThat()
        }
        option("I'm ready to give it a go now.") {
            iKnowWhatImDoing()
        }
    }
}

suspend fun CharacterContext<Player>.leaveTheTomb() {
    choice("Leave the Tomb?") {
        option("Yes, I'm out of here.")
        option("Ah, I think I'll stay a little longer.")
    }
}

suspend fun CharacterContext<Player>.whereDidYouHearAboutThat() {
    npc<Talk>("Where did you hear about that?")
    player<Talk>("I couldn't possibly say.")
    npc<Talk>("It's the only genuinely valuable artefact in this place.")
    npc<Talk>("It links all the great pyramids in the area, and can be used to travel between them.")
    npc<Talk>("It requires charging with offerings of fine craftsmanship, that's why we have so many spare artefacts lying around.")
    npc<Talk>("Anyway, I won't let you get your grubby little hands on the sceptre.")
    anymoreQuestions("What about the chests and sarcophagi?")
}

suspend fun CharacterContext<Player>.discharge(index: Int) {
    if (player.inventory.replace(index, player.inventory[index].id, "pharaohs_sceptre")) {
        itIsDone()
    }
}