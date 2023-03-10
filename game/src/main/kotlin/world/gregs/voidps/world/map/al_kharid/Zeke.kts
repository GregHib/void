package world.gregs.voidps.world.map.al_kharid

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.quest.completed
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

on<NPCOption>({ operate && npc.id == "zeke" && option == "Talk-to" }) { player: Player ->
    npc<Talk>("A thousand greetings, ${if (player.male) "sir" else "madam"}.")
    val choice = choice("""
        Do you want to trade?
        Nice cloak.
        Could you sell me a dragon scimitar?
        What do you think of Ali Morrisane?
    """)
    when (choice) {
        1 -> {
            npc<Cheerful>("Yes, certainly. I deal in scimitars.")
            player.openShop("zekes_superior_scimitars")
        }
        2 -> {
            player<Unsure>("Nice cloak.")
            npc<Unsure>("Thank you.")
        }
        3 -> {
            player<Unsure>("Could you sell me a dragon scimitar?")
            npc<Angry>("A dragon scimitar? A DRAGON scimitar?")
            npc<Angry>("No way, man!")
            npc<Furious>("""
                The banana-brained nitwits who make them would never
                dream of selling any to me.
            """)
            npc<Sad>("""
                Seriously, you'll be a monkey's uncle before you'll ever
                hold a dragon scimitar.
            """)
            if (player.completed("monkey_madness")) {
                player<Uncertain>("Hmmm, funny you should say that...")
            } else {
                player<Unsure>("Oh well, thanks anyway.")
            }
            npc<Unsure>("Perhaps you'd like to take a look at my stock?")
            takeALook()
        }
        4 -> {
            player<Unsure>("What do you think of Ali Morrisane?")
            npc<Unsure>("He is a dangerous man.")
            npc<Unsure>("""
                Although he does not appear to be dangerous, he has
                brought several men to this town who have
                threatened me and several others.
            """)
            npc<Shock>("""
                One man even threatened me with a hammer, saying that
                when he set up his smithy, my shoddy workmanship
                would be revealed!
            """)
            player<Unsure>("What will you do about these threats?")
            npc<Talk>("""
                Oh, I am quite confident in the quality of
                my work...as will you be if you take a look at my wares.
            """)
            takeALook()
        }
    }
}

suspend fun Interaction.takeALook() {
    val choice = choice("""
            Yes please, Zeke.
            Not today, thank you.
        """)
    when (choice) {
        1 -> player.openShop("zekes_superior_scimitars")
        2 -> player<Unsure>("Not today, thank you.")
    }
}