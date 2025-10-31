package content.entity

import content.entity.player.inv.inventoryOption
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.client.instruction.ExamineItem
import world.gregs.voidps.network.client.instruction.ExamineNpc
import world.gregs.voidps.network.client.instruction.ExamineObject

class Examines : Script {

    val itemDefinitions: ItemDefinitions by inject()
    val npcDefinitions: NPCDefinitions by inject()
    val objectDefinitions: ObjectDefinitions by inject()

    init {
        interfaceOption("Examine", id = "*") {
            player.message(item.def.getOrNull("examine") ?: return@interfaceOption, ChatType.ItemExamine)
        }

        inventoryOption("Examine") {
            player.message(item.def.getOrNull("examine") ?: return@inventoryOption, ChatType.ItemExamine)
        }

        objectApproach("Examine") { (target) ->
            message(target.def.getOrNull("examine") ?: return@objectApproach, ChatType.ObjectExamine)
        }

        npcApproach("Examine") {
            player.message(def.getOrNull("examine") ?: return@npcApproach, ChatType.NPCExamine)
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
}
