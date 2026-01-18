package content.entity.player.command

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Direction
import kotlin.text.toIntOrNull

class NPCCommands : Script {

    init {
        modCommand("npcs", desc = "Get total npc count") {
            message("NPCs: ${NPCs.count()}")
        }
        adminCommand("npc", stringArg("npc-id", autofill = NPCDefinitions.ids.keys), desc = "Spawn an npc", handler = ::spawn)
    }

    fun spawn(player: Player, args: List<String>) {
        val id = args[0].toIntOrNull()
        val definition = if (id != null) NPCDefinitions.getOrNull(id) else NPCDefinitions.getOrNull(args[0])
        if (definition == null) {
            player.message("Unable to find npc with id ${args[0]}.")
            return
        }
        println("{ id = \"${definition.stringId}\", x = ${player.tile.x}, y = ${player.tile.y}, level = ${player.tile.level}, members = true },")
        val npc = NPCs.add(definition.stringId, player.tile, Direction.NORTH)
        npc.start("movement_delay", -1)
    }
}
