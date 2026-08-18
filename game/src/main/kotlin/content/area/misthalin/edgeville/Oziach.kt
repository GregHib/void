package content.area.misthalin.edgeville

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Amazed
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Oziach : Script {

    private val cost = 1_250_000

    init {
        npcOperate("Talk-to", "oziach") { (target) ->
            npc<Happy>("What can I do for ye, mighty dragon slayer?")
            choice {
                if (inventory.contains("draconic_visage")) {
                    option<Quiz>("Can you do anything with this draconic visage?") {
                        offerShield()
                    }
                }
                option<Quiz>("Can I see your wares?") {
                    npc<Happy>("Of course.")
                    openShop(target.def["shop"])
                }
                option<Neutral>("Nothing, thanks.")
            }
        }
    }

    private suspend fun Player.offerShield() {
        npc<Amazed>("Ye've found a draconic visage! Amazin'! Ye can almost feel it pulsin' with draconic power!")
        npc<Happy>("Now, if ye want me to, I could attach this to yer anti-dragonbreath shield and make something pretty special.")
        npc<Neutral>("The shield won't be easy to wield though; ye'll need level 75 Defence.")
        npc<Neutral>("I'll charge 1,250,000 coins to construct it. What d'ye say?")
        choice {
            option<Happy>("Yes, please!") {
                forgeShield()
            }
            option<Neutral>("No, thanks.") {
                npc<Happy>("Talk to me again if ye change yer mind, mighty dragon slayer.")
            }
            option<Sad>("That's a bit expensive!") {
                npc<Neutral>("It's the price ye pay to make such a magnificent shield.")
            }
        }
    }

    private suspend fun Player.forgeShield() {
        if (!inventory.contains("anti_dragon_shield")) {
            npc<Neutral>("Ye need an anti-dragonbreath shield for me to attach this onto, talk to me again once ye do.")
            return
        }
        if (!inventory.contains("coins", cost)) {
            player<Sad>("I don't seem to have enough coins, I will return once I do.")
            return
        }
        val success = inventory.transaction {
            remove("draconic_visage")
            remove("anti_dragon_shield")
            remove("coins", cost)
            add("dragonfire_shield_uncharged")
        }
        if (!success) {
            return
        }
        item("dragonfire_shield_uncharged", "Oziach skilfully forges the shield and visage into a new shield.")
        npc<Happy>("There ye go. Now, the more dragonfire yer shield absorbs, the more powerful it'll become.")
    }
}
