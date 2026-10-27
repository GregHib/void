package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object ClientScriptLoader : Loader<ClientScriptDefinitions> {
    val instance = ClientScriptDefinitions()
    override val name = "client_scripts"
    override val label = "client script definition"
    override val files = listOf("definitions.clientScripts")

    override fun load(context: LoadContext): ClientScriptDefinitions = ClientScriptDefinitions().load(context.paths(this))

    override fun total(state: ClientScriptDefinitions) = state.ids.size

    override fun install(state: ClientScriptDefinitions) = instance.copyFrom(state)
}
