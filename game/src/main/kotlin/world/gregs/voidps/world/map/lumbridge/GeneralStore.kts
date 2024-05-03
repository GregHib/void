package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.Neutral
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

npcOperate("Trade", "shopkeeper*", "shop_assistant*") {
    player.openShop("lumbridge_general_store")
}

npcOperate("Talk-to", "shopkeeper*") {
    npc<Neutral>("Can I help you at all?")
    choice {
        option("Yes please. What are you selling?") {
            player.openShop("lumbridge_general_store")
        }
        option("How should I use your shop?") {
            npc<Talk>("I'm glad you ask! The shop has two sections to it: 'Main stock' and 'Free sample items'.")
            npc<Neutral>("From 'Main Stock' you can buy as many of the stocked items as you wish. I also offer free samples to help get you started and to keep you coming back.")
            npc<Neutral>("Once you take a free sample, I won't give you another for about half an hour. I'm not make of money, you know!")
            npc<Neutral>("You can also sell most items to the shop.")
            player<Happy>("Thank you.")
        }
        option("No thanks.")
    }
}

npcOperate("Talk-to", "shop_assistant*") {
    npc<Happy>("Can I help you at all?")
    choice {
        option("Yes please. What are you selling?") {
            player.openShop("lumbridge_general_store")
        }
        option("How should I use your shop?") {
            npc<Talk>("I'm glad you ask! You can buy as many of the items stocked as you wish. You can also sell most items to the shop.")
            player<Happy>("Thank you.")
        }
        option("No thanks.")
    }
}