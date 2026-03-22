package content.area.misthalin.zanaris

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

class Lunderwin : Script {
    init {
        npcOperate("Talk-to", "lunderwin") { (target) ->
            npc<Neutral>("Buying cabbage am I, not have such thing where I from. Will pay money much handsome for wondrous object, cabbage you called.")
            npc<Neutral>("Say I 100 gold coins each fair price to be giving yes?")
            if (!carriesItem("cabbage")) {
                player<Sad>("Alas, I have no cabbages either...")
                npc<Sad>("Pity be that, I want badly do.")
                return@npcOperate
            }
            choice {
                option<Neutral>("Yes, I will sell you all my cabbages.") {
                    inventory.transaction {
                        val removed = removeToLimit("cabbage", 28)
                        add("coins", removed * 100)
                    }
                    when (inventory.transaction.error) {
                        TransactionError.None -> npc<Neutral>("Business good doing with you! Please, again come, buying always.")
                        else -> {}
                    }
                }
                option<Neutral>("No, I will keep my cabbbages.")
            }
        }
    }
}
