package content.entity

import content.skill.melee.armour.durabilityMessage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.client.instruction.ExamineItem
import world.gregs.voidps.network.client.instruction.ExamineNpc
import world.gregs.voidps.network.client.instruction.ExamineObject

class Examines : Script {

    init {
        interfaceOption("Examine", "inventory:inventory", ::examineItem)
        interfaceOption("Examine", "worn_equipment:item", ::examineItem)
        interfaceOption("Examine", "bank:inventory", ::examineItem)
        interfaceOption("Examine", "bank_side:inventory", ::examineItem)
        interfaceOption("Examine", "price_checker:items", ::examineItem)
        interfaceOption("Examine", "beast_of_burden:items", ::examineItem)
        interfaceOption("Examine", "summoning_side:inventory", ::examineItem)
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
            showItemDetails(item.def.getOrNull("examine"), item)
        }
        itemOption("Check", inventory = "*") { (item) ->
            showDurability(item)
        }
        itemOption("Check-charges", inventory = "*") { (item) ->
            showDurability(item)
        }
        itemOption("Inspect", inventory = "*") { (item) ->
            showDurability(item)
        }

        objectApproach("Examine") { (target) ->
            message(target.def(this).getOrNull("examine") ?: return@objectApproach, ChatType.ObjectExamine)
        }

        npcApproach("Examine") { (target) ->
            message(target.def(this).getOrNull("examine") ?: return@npcApproach, ChatType.NPCExamine)
        }

        instruction<ExamineItem> { player ->
            val definition = ItemDefinitions.get(itemId)
            if (definition.contains("examine")) {
                player.message(definition["examine"], ChatType.Game)
            }
        }

        instruction<ExamineNpc> { player ->
            val definition = NPCDefinitions.get(npcId)
            if (definition.contains("examine")) {
                player.message(definition["examine"], ChatType.Game)
            }
        }

        instruction<ExamineObject> { player ->
            val definition = ObjectDefinitions.get(objectId)
            if (definition.contains("examine")) {
                player.message(definition["examine"], ChatType.Game)
            }
        }
    }

    private fun Player.showItemDetails(examine: String?, item: Item) {
        examine?.let { message(it, ChatType.ItemExamine) }
        showDurability(item)
    }

    private fun Player.showDurability(item: Item) {
        item.durabilityMessage(this)?.let { message(it, ChatType.ItemExamine) }
    }

    private fun examineItem(player: Player, option: InterfaceOption) {
        player.showItemDetails(option.item.def.getOrNull("examine"), option.item)
    }
}
