package content.area.karamja.shilo_village

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Yohnus : Script {

    init {
        npcOperate("Talk-to", "yohnus_shilo_village") {
            npc<Neutral>("Sorry but the blacksmiths is closed. But I can let you use the furnace at the cost of 20 gold pieces.")
            choice {
                option<Neutral>("Use Furnace - 20 Gold") {
                    inventory.transaction {
                        remove("coins", 20)
                    }
                    when (inventory.transaction.error) {
                        TransactionError.None -> {
                            npc<Happy>("Thanks Bwana! Enjoy the facilities!")
                        }
                        else -> npc<Neutral>("Sorry, you don't have enough coins.")
                    }
                }
                option<Neutral>("No thanks!") {
                    player<Neutral>("No thanks!")
                    npc<Neutral>("Very well Bwana, have a nice day.")
                }
            }
        }
        objectOperate("Open", "blacksmiths_door_closed") { (target) ->
            if (tile.y > target.tile.y) {
                enterDoor(target)
                return@objectOperate
            }
            if (!inventory.contains("coins", 20)) {
                npc<Quiz>(
                    "yohnus_shilo_village",
                    "Sorry but the blacksmiths is closed. But I can let you use the furnace at the cost of 20 gold pieces."
                )
                return@objectOperate
            }
            inventory.transaction {
                remove("coins", 20)
            }
            when (inventory.transaction.error) {
                TransactionError.None -> {
                    npc<Quiz>("yohnus_shilo_village", "Thanks Bwana! Enjoy the facilities!")
                    enterDoor(target)
                }
                else -> {
                    npc<Quiz>("yohnus_shilo_village", "Sorry, you don't have enough coins.")
                }
            }
        }
    }
}