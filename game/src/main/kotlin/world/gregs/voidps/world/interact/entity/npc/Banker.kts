package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.Main.name
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.community.trade.lend.Loan.getSecondsRemaining
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc

on<NPCOption>({ approach && def.name == "Banker" && option == "Talk-to" }) { player: Player ->
    player.approachRange(2)
    pause()
    npc<Unsure>("Good day. How may I help you?")
    val loanReturned = getSecondsRemaining(player, "lend_timeout") < 0
    val collection = false

    if (loanReturned) {
        npc<Talk>("""
            Before we go any further, I should inform you that an
            item you lent out has been returned to you.
        """)
    } else if (collection) {
        npc<Talk>("""
            Before we go any further, I should inform you that you
            have items ready for collection from the Grand Exchange.
        """)
    }

    menu()
}

suspend fun Interaction.menu() {
    var choice = choice("""
        I'd like to access my bank account, please.
        I'd like to check my PIN settings.
        I'd like to see my collection box.
        What is this place?
    """)
    when (choice) {
        1 -> player.open("bank")
        2 -> player.open("bank_pin")
        3 -> player.open("collection_box")
        4 -> {
            npc<Talk>("""
                This is a branch of the Bank of $name. We have
                branches in many towns.
            """)
            choice = choice("""
                And what do you do?
                Didn't you used to be called the Bank of Varrock?
            """)
            when (choice) {
                1 -> npc<Talk>("""
                    We will look after your items and money for you.
                    Leave your valuables with us if you want to keep them
                    safe.
                """
                )
                2 -> npc<Talk>("""
                    Yes we did, but people kept on coming into our
                    branches outside of Varrock and telling us that our
                    signs were wrong. They acted as if we didn't know
                    what town we were in or something.
                """)
            }
            menu()
        }
    }
}

on<NPCOption>({ approach && def.name == "Banker" && option == "Bank" }) { player: Player ->
    player.approachRange(2)
    pause()
    player.open("bank")
}

on<NPCOption>({ approach && def.name == "Banker" && option == "Collect" }) { player: Player ->
    player.approachRange(2)
    pause()
    player.open("collection_box")
}