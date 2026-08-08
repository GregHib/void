package content.area.karamja.shilo_village

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Tile

class Yohnus : Script {

    init {
        npcOperate("Talk-to", "yohnus_shilo_village") {
            furnace()
        }
        objectOperate("Open", "blacksmiths_door_closed") { (target) ->
            if (tile.y > target.tile.y) {
                enterDoor(target)
                return@objectOperate
            }
            if (this["yohnus_paid", false]) {
                clear("yohnus_paid")
                enterDoor(target)
                return@objectOperate
            }
            talkWith(NPCs.findBySpawn(Tile(2857, 2963), "yohnus_shilo_village"))
            furnace()
            if (this["yohnus_paid", false]) {
                clear("yohnus_paid")
                enterDoor(target)
            }
        }
    }
}

private suspend fun Player.furnace() {
    npc<Neutral>("Sorry but the blacksmiths is closed. But I can let you use the furnace at the cost of 20 gold pieces.")
    choice {
        option<Neutral>("Use Furnace - 20 Gold") {
            inventory.transaction {
                remove("coins", 20)
            }
            when (inventory.transaction.error) {
                TransactionError.None -> {
                    this["yohnus_paid"] = true
                    npc<Happy>("Thanks Bwana! Enjoy the facilities!")
                }
                else -> npc<Neutral>("Sorry, you don't have enough coins.")
            }
        }
        option<Neutral>("No thanks!") {
            npc<Neutral>("Very well Bwana, have a nice day.")
        }
    }
}
