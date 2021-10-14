import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "ghostly_piper" && option == "Talk-to" }) { player: Player ->
    player.dialogue(npc) {
        if (player.equipped(EquipSlot.Amulet).name == "ghostspeak_amulet") {
            choice()
        } else {
            npc("happy", "Woo, wooo. Woooo.")
            player.message("The ghost seems barely aware of your existence,")
            player.message("but you sense that resting here might recharge you for battle!")
        }
    }
}

suspend fun DialogueContext.choice() {
    val choice = choice("""
        Who are you?
        That's all for now
    """)
    when (choice) {
        1 -> {
            player("think", "Who are you?")
            npc("cheerful", """
                I play the pipes, to rouse
                the brave warriors of Saradomin for the fight!
            """)
            player("think", "Which fight?")
            npc("cheerful", """
                Why, the great battles with the forces of Zamorak,
                of course!
            """)
            player("think", "I see. How long have you been standing here then?")
            npc("cheerful", """
                Well, it is all a bit fuzzy. I remember standing at
                the front of the massed forces of Saradomin, and playing
                the Call to Arms, but after that I can't quite recall.
            """)

            player("talk", """
                I think you've been here for quite some time.
                You do know you're a gh-
            """)
            player("talking", """
                No, never mind, you look happy enough here,
                and your music is quite rousing.
                I might rest here a while. 
            """)
            choice()
        }
        2 -> exit()
    }
}

suspend fun DialogueContext.exit() {
    player("disregard", "That's all for now.")
    npc("cheerful", "Be strong and fight the good fight, my friend!")
}