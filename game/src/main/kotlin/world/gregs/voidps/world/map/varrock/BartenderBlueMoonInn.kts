package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.Main
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCApproach
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.interact.dialogue.Angry
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.Sad
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.obj.door.Door

npcApproach("Talk-to", "bartender_blue_moon_inn") {
    player.approachRange(2)
    pause()
    npc<Quiz>("What can I do yer for?")
    choice {
        option<Talk>("A glass of your finest ale please.") {
            npc<Talk>("No problemo. That'll be 2 coins.")
            player.inventory.transaction {
                remove("coins", 2)
                add("beer")
            }
            when (player.inventory.transaction.error) {
                is TransactionError.Full -> player.inventoryFull()
                TransactionError.None -> player.message("You buy a pint of beer.")
                else -> player<Sad>("Oh dear. I don't seem to have enough money.")
            }
        }
        option<Quiz>("Can you recommend where an adventurer might make his fortune?") {
            npc<Angry>("Ooh I don't know if I should be giving away information, makes the game too easy.")
            choice {
                option<Talk>("Oh ah well...")
                option<Quiz>("Game? What are you talking about?") {
                    npc<Angry>("This world around us... is an online game... called ${Main.name}.")
                    player<Quiz>("Nope, still don't understand what you are talking about. What does 'online' mean?")
                    npc<Angry>("It's a sort of connection between magic boxes across the world, big boxes on people's desktops and little ones people can carry. They can talk to each other to play games.")
                    player<Angry>("I give up. You're obviously completely mad!")
                }
                option<Quiz>("Just a small clue?") {
                    npc<Angry>("Go and talk to the bartender at the Jolly Boar Inn, he doesn't seem to mind giving away clues.")
                }
                option<Quiz>("Do you know where I can get some good equipment?") {
                    npc<Talk>("Well, there's the sword shop across the road, or there's also all sorts of shops up around the market.")
                }
            }
        }
    }
}

itemOnNPCApproach("barcrawl_card", "bartender_blue_moon_inn") {
    player<Talk>("I'm doing Alfred Grimhand's Barcrawl.")
    npc<Sad>("Oh no not another of you guys. These barbarian barcrawls cause too much damage to my bar.")
    npc<Talk>("You're going to have to pay 50 gold for the Uncle Humphrey's Gutrot.")
    if (!player.inventory.remove("coins", 50)) {
        player<Sad>("Oh dear. I don't seem to have enough money.")
        return@itemOnNPCApproach
    }
    player.message("You buy some Uncle Humphrey's Gutrot.")
    pause(2)
    player.message("You drink the Uncle Humphrey's Gutrot.")
    pause()
    player.message("Your insides feel terrible.")
    pause()
    player.forceChat = "Blearrgh!"
    player.message("The bartender signs your card.")
    player.addVarbit("barcrawl_signatures", "uncle_humphreys_gutrot")
}