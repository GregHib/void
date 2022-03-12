package world.gregs.voidps.world.map.al_kharid

import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.quest.completed
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop

on<NPCOption>({ npc.id == "zeke" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("talk", "A thousand greetings, ${if (player.male) "sir" else "madam"}.")
        val choice = choice("""
            Do you want to trade?
            Nice cloak.
            Could you sell me a dragon scimitar?
            What do you think of Ali Morrisane?
        """)
        when (choice) {
            1 -> {
                npc("cheerful", "Yes, certainly. I deal in scimitars.")
                player.events.emit(OpenShop("zekes_superior_scimitars"))
            }
            2 -> {
                player("unsure", "Nice cloak.")
                npc("unsure", "Thank you.")
            }
            3 -> {
                player("unsure", "Could you sell me a dragon scimitar?")
                npc("angry", "A dragon scimitar? A DRAGON scimitar?")
                npc("angry", "No way, man!")
                npc("furious", """
                    The banana-brained nitwits who make them would never
                    dream of selling any to me.
                """)
                npc("sad", """
                    Seriously, you'll be a monkey's uncle before you'll ever
                    hold a dragon scimitar.
                """)
                if (player.completed("monkey_madness")) {
                    player("uncertain", "Hmmm, funny you should say that...")
                } else {
                    player("unsure", "Oh well, thanks anyway.")
                }
                npc("unsure", "Perhaps you'd like to take a look at my stock?")
                takeALook()
            }
            4 -> {
                player("unsure", "What do you think of Ali Morrisane?")
                npc("unsure", "He is a dangerous man.")
                npc("unsure", """
                    Although he does not appear to be dangerous, he has
                    brought several men to this town who have
                    threatened me and several others.
                """)
                npc("shock", """
                    One man even threatened me with a hammer, saying that
                    when he set up his smithy, my shoddy workmanship
                    would be revealed!
                """)
                player("unsure", "What will you do about these threats?")
                npc("talk", """
                    Oh, I am quite confident in the quality of
                    my work...as will you be if you take a look at my wares.
                """)
                takeALook()
            }
        }
    }
}

suspend fun DialogueContext.takeALook() {
    val choice = choice("""
                    Yes please, Zeke.
                    Not today, thank you.
                """)
    when (choice) {
        1 -> player.events.emit(OpenShop("zekes_superior_scimitars"))
        2 -> player("unsure", "Not today, thank you.")
    }
}