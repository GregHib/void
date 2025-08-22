package world.gregs.voidps.engine.event

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import world.gregs.voidps.engine.event.EventField.*
import kotlin.reflect.KClass

class EventProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = EventProcessor(
        codeGenerator = environment.codeGenerator,
        logger = environment.logger,
        eventSchemas
    )

    val map: Map<KClass<out Annotation>, Map<String, List<EventField>>> = mapOf(
        UseOn::class to mapOf(

        ),
        Option::class to mapOf(
            "NPCOption<Player>" to listOf(
                Event("player_operate_npc"),
            ),
            "NPCOption<NPC>" to listOf(
                Event("npc_operate_npc"),
            )
        )
    )

    companion object {
        val eventSchemas: MutableMap<String, List<EventField>> = mutableMapOf(
            "ObjectOption<Player>" to listOf(
                Event("player_operate_object"),
                Option,
                Ids,
                Player
            ),
            "ObjectOption<NPC>" to listOf(
                Event("_operate_object"),
                Option,
                Ids,
                Npc
            ),
            "InterfaceOnNPC" to listOf(
                Event("interface_on_operate_npc"),
                Npc,
                Ids,
                Component
            )
        )
    }
}
