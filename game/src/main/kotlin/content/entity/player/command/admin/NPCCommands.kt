package content.entity.player.command.admin

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.arg
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import kotlin.text.toIntOrNull

@Script
class NPCCommands {

    val npcs: NPCs by inject()
    val npcDefinitions: NPCDefinitions by inject()

    init {
        modCommand("npcs", desc = "get total npc count") { player, _ ->
            player.message("NPCs: ${npcs.count()}")
        }
        adminCommand("npc", arg<String>("npc-id", autofill = npcDefinitions.ids.keys), desc = "spawn an npc", handler = ::spawn)
    }

    fun spawn(player: Player, args: List<String>) {
        val id = args[0].toIntOrNull()
        val definition = if (id != null) npcDefinitions.getOrNull(id) else npcDefinitions.getOrNull(args[0])
        if (definition == null) {
            player.message("Unable to find npc with id ${args[0]}.")
            return
        }
        val npcs: NPCs = get()
        println("{ id = \"${definition.stringId}\", x = ${player.tile.x}, y = ${player.tile.y}, level = ${player.tile.level}, members = true },")
        val npc = npcs.add(definition.stringId, player.tile, Direction.NORTH)
        npc.start("movement_delay", -1)
    }

}