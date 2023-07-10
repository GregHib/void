package world.gregs.voidps.world.map.neitiznot

import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ operate && npc.id == "thakkrad_sigmundson" && option == "Talk-to" }) { player: Player ->
    npc<Talk>("""
        Thank you for leading the Burgher's militia against the
        Troll King. Now that the trolls are leaderless I have
        repaired the bridge to the central isle for you as best I can.
    """)
    player<Unsure>("""
        Thanks Thakkrad. Does that mean I have
        access to the runite ores on that island?
    """)
    npc<Talk>("Yes, you should be able to mine runite there if you wish.")
}

on<NPCOption>({ operate && npc.id == "thakkrad_sigmundson" && option == "Craft-goods" }) { player: Player ->
    choice("What can I help you with?") {
        option("Cure my yak hide, please.") {
            cureHide()
        }
        option<Talk>("Nothing, thanks.") {
            npc<Talk>("""
                See you later. You won't find anyone else
                who can cure yak-hide.
            """)
        }
    }
}

on<ItemOnNPC>({ operate && npc.id == "thakkrad_sigmundson" && item.id == "yak_hide" }) { player: Player ->
    cureHide()
}

suspend fun PlayerContext.cureHide() {
    player<Talk>("Cure my yak hide please.")
    npc<Talk>("I will cure yak-hide for a fee of 5 gp per hide.")
    choice("How many hides do you want cured?") {
        option("Cure all my hides.") {
            cure(player.inventory.count("yak_hide"))
        }
        option("Cure one hide.") {
            cure(1)
        }
        option("Cure no hide.") {
            npc<Talk>("Bye.")
        }
        option<Unsure>("Can you cure any type of leather?") {
            npc<Uncertain>("""
                Other types of leather?
                Why would you need any other type of leather?
            """)
            player<Talk>("I'll take that as a no then.")
        }
    }
}

suspend fun PlayerContext.cure(amount: Int) {
    if (!player.inventory.contains("yak_hide")) {
        npc<Talk>("You have no yak-hide to cure.")
        return
    }
    player.inventory.transaction {
        val removed = removeToLimit("yak_hide", amount)
        remove("coins", removed * 5)
        add("cured_yak_hide", removed)
    }
    when (player.inventory.transaction.error) {
        is TransactionError.Deficient -> npc<Talk>("You don't have enough gold to pay me!.")
        TransactionError.None -> npc<Talk>("There you go.")
        else -> {}
    }
}