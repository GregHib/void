package content.entity.npc.shop.general

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player

npcOperate("Trade", "shopkeeper*", "shop_assistant*") {
    player.openShop(target.def.getOrNull<String>("shop") ?: return@npcOperate)
}

npcOperate("Talk-to", "shopkeeper*") {
    npc<Neutral>("Can I help you at all?")
    choice {
        option("Yes please. What are you selling?") {
            player.openShop(target.def.getOrNull<String>("shop") ?: return@option)
        }
        option("How should I use your shop?") {
            if (target.id.endsWith("lumbridge")) {
                npc<Talk>("I'm glad you ask! The shop has two sections to it: 'Main stock' and 'Free sample items'.")
                npc<Neutral>("From 'Main Stock' you can buy as many of the stocked items as you wish. I also offer free samples to help get you started and to keep you coming back.")
                npc<Neutral>("Once you take a free sample, I won't give you another for about half an hour. I'm not make of money, you know!")
                npc<Neutral>("You can also sell most items to the shop.")
                player<Happy>("Thank you.")
            } else {
                npc<Talk>("I'm glad you ask! You can buy as many of the items stocked as you wish. You can also sell most items to the shop.")
                player<Happy>("Thank you.")
            }
        }
        option("No thanks.")
    }
}

npcOperate("Talk-to", "shop_assistant*") {
    if (target.id.endsWith("musa_point")) {
        npc<Happy>("It's a beautiful day today, no? Can I do anything for you?")
    } else {
        npc<Happy>("Can I help you at all?")
    }
    choice {
        option("Yes please. What are you selling?") {
            player.openShop(target.def.getOrNull<String>("shop") ?: return@option)
        }
        option("How should I use your shop?") {
            npc<Talk>("I'm glad you ask! You can buy as many of the items stocked as you wish. You can also sell most items to the shop.")
            player<Happy>("Thank you.")
        }
        option<Neutral>("No thanks.")
    }
}