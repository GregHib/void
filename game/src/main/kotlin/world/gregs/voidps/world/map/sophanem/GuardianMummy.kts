package world.gregs.voidps.world.map.sophanem

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.Transaction
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.dialogue.type.statement

val ivory = listOf("ivory_comb", "pottery_scarab", "pottery_statuette")
val stone = listOf("stone_seal", "stone_scarab", "stone_statuette")
val gold = listOf("gold_seal", "gold_scarab", "gold_statuette")

on<NPCOption>({ operate && target.id == "guardian_mummy" && option == "Talk-to" }) { player: Player ->
    if (player.holdsItem("pharaohs_sceptre")) {
        sceptreRecharging()
        return@on
    }
    notAnother()
}

on<NPCOption>({ operate && target.id == "guardian_mummy" && option == "Start-activity" }) { player: Player ->
    player<Happy>("I know what I'm doing - let's get on with it.")
    npc<Talk>("Fine. I'll take you to the first room now...")
}

suspend fun CharacterContext.notAnother() {
    npc<Talk>("*sigh* Not another one.")
    player<Unsure>("Another what?")
    npc<Talk>("""
        Another 'archaeologist'. I'm not going to let you
        plunder my master's tomb you know.
    """)
    player<Upset>("That's a shame. Have you got anything else I could do while I'm here?")
    npc<Talk>("""
        If it will keep you out of mischief I suppose I
        could set something up for you...
    """)
    npc<Talk>("""
        I have a few rooms full of some things you humans
        might consider valuable, do you want to give it a go?
    """)
    menu()
}

suspend fun CharacterContext.menu() {
    choice("Play the 'Pyramid Plunder' minigame?") {
        option<Talk>("That sounds like fun, what do I do?") {
            npc<Talk>("""
                You have five minutes to explore the treasure rooms and
                collect as many artefacts as you can.
            """)
            npc<Talk>("""
                The artefacts are in the urns, chests and sarcophagi found
                in each room.
            """)
            npc<Talk>("""
                There are eight treasure rooms, each subsequent room requires
                higher thieving skills to both enter the room and thieve from
                the urns and other containers.
            """)
            npc<Talk>("""
                The rewards also become more lucrative the further into
                the tomb you go.
            """)
            npc<Talk>("""
                You will also have to deactivate a trap in order to enter
                the main part of each room.
            """)
            npc<Talk>("""
                When you want to move onto the next room you need to find
                the correct door first.
            """)
            npc<Talk>("""
                There are four possible exits, you must open the door before
                finding out whether it is the exit or not.
            """)
            npc<Talk>("""
                Opening the doors require picking their locks. Having a lockpick
                will make this easier.
            """)
        }
        option("Not right now")
        option("I know what I'm doing let's get on with it.")
        option("I want to charge or remove charges from my sceptre.") {

            if (player.holdsItem("pharaohs_sceptre")) {
                sceptreRecharging()
            } else {
                val empty = 1
                if (empty > 1) {
                    npc<Talk>("If I must. You have $empty sceptres with charges. Do you want them all emptied?")
                    choice {
                        option("Yes, uncharge all my sceptres.") {
                            npc<Talk>("It is done.")
                            menu()
                        }
                        option("No, I'll hand you the one I want emptied.")
                    }
                } else {

                }
            }
        }
    }
}

suspend fun CharacterContext.questions() {
    choice("Do you have any more questions?") {
    }
}
suspend fun CharacterContext.sceptreRecharging() {
    player<Talk>("This sceptre seems to have run out of charges.")
    npc<Talk>("You shouldn't have that thing in the first place, thief!")
    player<Talk>("""
        If I gave you back some of the artefacts I've taken
        from the tomb, would you recharge the sceptre for me?
    """)
    npc<Talking>("""
        *sigh* Oh alright. But only if the sceptre is fully
        empty, I'm not wasting the King's magic...
    """)
    choice("Recharge the sceptre with...") {
        option("Gold artefacts?") {
            if (player.inventory.transaction { remove(6, gold) }) {
                statement("You recharge your sceptre with gold artefacts.")
            } else {
                npc<Talk>("You need to have 6 gold artefacts to recharge your sceptre.")
            }
        }
        option("Stone artefacts?") {
            if (player.inventory.transaction { remove(12, stone) }) {
                statement("You recharge your sceptre with stone artefacts.")
            } else {
                npc<Talk>("You need to have 12 stone artefacts to recharge your sceptre.")
            }
        }
        option("Pottery and Ivory artefacts?") {
            if (player.inventory.transaction { remove(24, ivory) }) {
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

fun Transaction.remove(amount: Int, items: List<String>) {
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


on<ItemOnNPC>({ operate && target.id == "guardian_mummy" && item.id != "pharaohs_sceptre" }) { player: Player ->
    player.message("The Mummy is not interested in this")
}