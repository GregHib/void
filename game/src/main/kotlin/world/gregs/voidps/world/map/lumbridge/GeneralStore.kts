package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

on<NPCOption>({ (def.name == "Shopkeeper" || def.name == "Shop assistant") && option == "Trade" }) { player: Player ->
    player.openShop("lumbridge_general_store")
}

on<NPCOption>({ def.name == "Shopkeeper" && option == "Talk-to" }) { player: Player ->
    npc<Talking>("Can I help you at all?")
    val choice = choice("""
        Yes please. What are you selling?
        How should I use your shop?
        No thanks.
    """)
    when (choice) {
        1 -> player.openShop("lumbridge_general_store")
        2 -> {
            npc<Talk>("""
                I'm glad you ask! The shop has two sections to it: 'Main
                stock' and 'Free sample items'.
            """)
            npc<Talking>("""
                From 'Main Stock' you can buy as many of the stocked
                items as you wish. I also offer free samples to help
                get you started and to keep you coming back.
            """)
            npc<Talking>("""
                Once you take a free sample, I won't give you  another
                for about half an hour. I'm not make of money, you know!
            """)
            npc<Talking>("You can also sell most items to the shop.")
            player<Cheerful>("Thank you.")
        }
    }
}

on<NPCOption>({ def.name == "Shop assistant" && option == "Talk-to" }) { player: Player ->
    npc<Cheerful>("Can I help you at all?")
    val choice = choice("""
        Yes please. What are you selling?
        How should I use your shop?
        No thanks.
    """)
    when (choice) {
        1 -> player.openShop("lumbridge_general_store")
        2 -> {
            npc<Talk>("""
                I'm glad you ask! You can buy as many of the items
                stocked as you wish. You can also sell most items to the
                shop.
            """)
            player<Cheerful>("Thank you.")
        }
    }
}