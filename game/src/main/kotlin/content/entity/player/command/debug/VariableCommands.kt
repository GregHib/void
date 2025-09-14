package content.entity.player.command.debug

import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.encode.*

@Script
class VariableCommands {

    val definitions: InterfaceDefinitions by inject()
    val variables: VariableDefinitions by inject()

    init {
        adminCommand("var", stringArg("variable-name", autofill = variables.definitions.keys), stringArg("value"), desc = "set a variable") { player, args ->
            player[args.first()] = args.last().toBooleanStrictOrNull() ?: args.last().toIntOrNull() ?: args.last()
        }

        adminCommand("varp", stringArg("id", autofill = variables.definitions.keys), intArg("value"), desc = "send player-variable to client") { player, args ->
            val intId = args.first().toIntOrNull()
            if (intId == null) {
                player.variables.set(args.first(), args.last().toInt())
                return@adminCommand
            }
            val name = variables.getVarp(intId)
            if (name == null) {
                player.client?.sendVarp(intId, args.last().toInt())
            } else {
                player.variables.set(name, args.last().toInt())
            }
        }

        adminCommand("varbit", stringArg("id", autofill = variables.definitions.keys), intArg("value"), desc = "send variable-bit to client") { player, args ->
            val intId = args.first().toIntOrNull()
            if (intId == null) {
                player.variables.set(args.first(), args.last().toInt())
                return@adminCommand
            }
            val name = variables.getVarbit(intId)
            if (name == null) {
                player.client?.sendVarbit(intId, args.last().toInt())
            } else {
                player.variables.set(name, args.last().toInt())
            }
        }

        adminCommand("varc", stringArg("id", autofill = variables.definitions.keys), intArg("value"), desc = "send client-variable to client") { player, args ->
            val intId = args.first().toIntOrNull()
            if (intId == null) {
                player.variables.set(args.first(), args.last().toInt())
            } else {
                player.client?.sendVarc(intId, args.last().toInt())
            }
        }

        adminCommand("varcstr", stringArg("id", autofill = variables.definitions.keys), stringArg("value"), desc = "send variable-client-string to client") { player, args ->
            val intId = args.first().toIntOrNull()
            val string = args.drop(1).joinToString(" ")
            if (intId == null) {
                player.variables.set(args.first(), string)
            } else {
                player.client?.sendVarcStr(intId, string)
            }
        }
    }

    fun open(player: Player, args: List<String>) {
        val id = args[0].toIntOrNull()
        if (id == null) {
            player.interfaces.open(args[0])
            return
        }
        if (id == -1 && closeInterface(player)) {
            return
        }
        val inter = definitions.get(args[0])
        var parent = if (player.interfaces.resizable) 746 else 548
        var index = if (player.interfaces.resizable) 5 else 8
        val p = inter["parent_${if (player.interfaces.resizable) "resize" else "fixed"}", ""]
        if (p.isNotBlank()) {
            parent = definitions.get(p).id
            index = inter["index_${if (player.interfaces.resizable) "resize" else "fixed"}", -1]
        }
        if (id == -1) {
            player.client?.closeInterface(InterfaceDefinition.pack(parent, index))
        } else {
            println("Open $parent $index $id")
            player.client?.openInterface(false, InterfaceDefinition.pack(parent, index), id)
        }
    }

    fun closeInterface(player: Player): Boolean {
        val id = player.interfaces.get("main_screen") ?: player.interfaces.get("wide_screen") ?: player.interfaces.get("underlay") ?: return false
        return player.interfaces.close(id)
    }
}
