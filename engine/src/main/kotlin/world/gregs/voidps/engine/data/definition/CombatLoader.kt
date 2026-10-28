package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.config.CombatDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.serializer.StringKeyedMapSerializer

object CombatLoader : Loader<Map<String, CombatDefinition>> {
    val instance = CombatDefinitions()
    override val name = "combat"
    override val label = "combat definition"
    override val files = listOf("definitions.combatAttacks")
    override val codec = SnapshotFormat.codec(StringKeyedMapSerializer(CombatDefinition.serializer()))

    override fun load(context: LoadContext): Map<String, CombatDefinition> = instance.parse(context.paths(this))

    override fun total(state: Map<String, CombatDefinition>) = state.size

    override fun install(state: Map<String, CombatDefinition>) = instance.install(state)
}
