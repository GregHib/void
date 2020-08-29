package rs.dusk.world.interact.entity.npc

import rs.dusk.Dusk.name
import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.npc.NPCOption
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.community.trade.lend.Loan.getTimeRemaining
import rs.dusk.world.interact.dialogue.type.choice
import rs.dusk.world.interact.dialogue.type.npc

NPCOption where { npc.def.name == "Banker" && option == "Talk-to" } then {
    player.dialogue(npc) {
        npc("Good day. How may I help you?")
        val loanReturned = getTimeRemaining(player, "lend_timeout") < 0
        val collection = false

        if (loanReturned) {
            npc("""
                Before we go any further, I should inform you that an
                item you lent out has been returned to you.
            """)
        } else if (collection) {
            npc("""
                Before we go any further, I should inform you that you
                have items ready for collection from the Grand Exchange.
            """)
        }

        menu()
    }
}

suspend fun DialogueContext.menu() {
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
            npc("""
                This is a branch of the Bank of $name. We have
                branches in many towns.
            """)
            choice = choice("""
                And what do you do?
                Didn't you used to be called the Bank of Varrock?
            """)
            when (choice) {
                1 -> npc("""
                    We will look after your items and money for you.
                    Leave your valuables with us if you want to keep them
                    safe.
                """
                )
                2 -> npc("""
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

NPCOption where { npc.def.name == "Banker" && option == "Bank" } then {
    player.open("bank")
}

NPCOption where { npc.def.name == "Banker" && option == "Collect" } then {
    player.open("collection_box")
}