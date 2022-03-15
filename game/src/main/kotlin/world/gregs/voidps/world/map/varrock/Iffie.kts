import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop

on<NPCOption>({ npc.id == "iffie" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("cheerful", """
            Hello, dearie! Were you wanting to collect a random
            event costume, or is there something else I can do for
            you today?
        """)
        when (choice("""
            I've come for a random event costume.
            Aren't you selling anything?
            I just came for a chat.
        """)) {
            1 -> {
                npc("cheerful", "Some of these costumes even come with a free emote!")
                npc("cheerful", """
                    Just buy one piece of the mine of zombie costumes and
                    I'll show you the relevant moves.
                """)
                player.events.emit(OpenShop("iffies_random_costume_shop"))
            }
            2 -> {
                player("unsure", "Aren't you selling anything?")
                npc("chuckle", """
                    Oh, yes, but only costumes.
                    Thessalia sells some other clothes and runs the makeover
                    service.
                """)
            }
            3 -> {
                player("talk", "I just came for a chat.")
                npc("sad", """
                    Oh, I'm sorry, but I'll never get my knitting
                    done if I stop for a chit-chat with every young ${if (player.male) "lad" else "lass"}
                    who wanders through the shop!
                """)
            }
        }
    }
}

on<NPCOption>({ npc.id == "iffie" && option == "Claim-costume" }) { player: Player ->
    player.events.emit(OpenShop("iffies_random_costume_shop"))
}