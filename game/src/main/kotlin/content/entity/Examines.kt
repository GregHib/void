package content.entity

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.network.client.instruction.ExamineItem
import world.gregs.voidps.network.client.instruction.ExamineNpc
import world.gregs.voidps.network.client.instruction.ExamineObject

class Examines(
    val itemDefinitions: ItemDefinitions,
    val npcDefinitions: NPCDefinitions,
    val objectDefinitions: ObjectDefinitions,
) : Script {

    init {
        interfaceOption("Examine", "inventory:inventory", ::examineItem)
        interfaceOption("Examine", "worn_equipment:item", ::examineItem)
        interfaceOption("Examine", "bank:inventory", ::examineItem)
        interfaceOption("Examine", "price_checker:items", ::examineItem)
        interfaceOption("Examine", "equipment_bonuses:inventory", ::examineItem)
        interfaceOption("Examine", "trade_main:offer_options", ::examineItem)
        interfaceOption("Examine", "trade_main:offer_warning", ::examineItem)
        interfaceOption("Examine<col=FF9040>", "trade_main:other_options", ::examineItem)
        interfaceOption("Examine", "trade_main:other_warning", ::examineItem)
        interfaceOption("Examine", "trade_main:loan_item", ::examineItem)
        interfaceOption("Examine", "trade_main:other_loan_item", ::examineItem)
        interfaceOption("Examine", "farming_equipment_store_side:*", ::examineItem)
        interfaceOption("Examine", "farming_equipment_store:*", ::examineItem)

        itemOption("Examine", inventory = "*") { (item) ->
            message(item.def.getOrNull("examine") ?: return@itemOption, ChatType.ItemExamine)
        }

        objectApproach("Examine") { (target) ->
            message(target.def.getOrNull("examine") ?: return@objectApproach, ChatType.ObjectExamine)
        }

        npcApproach("Examine") { (target) ->
            message(target.def(this).getOrNull("examine") ?: return@npcApproach, ChatType.NPCExamine)
        }

        instruction<ExamineItem> { player ->
            val definition = itemDefinitions.get(itemId)
            if (definition.contains("examine")) {
                player.message(definition["examine"], ChatType.Game)
            }
        }

        instruction<ExamineNpc> { player ->
            val definition = npcDefinitions.get(npcId)
            if (definition.contains("examine")) {
                player.message(definition["examine"], ChatType.Game)
            }
        }

        instruction<ExamineObject> { player ->
            val definition = objectDefinitions.get(objectId)
            if (definition.contains("examine")) {
                player.message(definition["examine"], ChatType.Game)
            }
        }
    }

    private fun examineItem(player: Player, option: InterfaceOption) {
        player.message(option.item.def.getOrNull("examine") ?: return, ChatType.ItemExamine)
    }
}
