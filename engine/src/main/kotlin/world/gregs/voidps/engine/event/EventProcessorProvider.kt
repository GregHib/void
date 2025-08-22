package world.gregs.voidps.engine.event

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.squareup.kotlinpoet.ClassName

class EventProcessorProvider : SymbolProcessorProvider {
    private val handlers: Map<String, UseData.(Int) -> Unit> = mapOf(
        "ObjectOption<Player>" to {
            val name = ClassName("world.gregs.voidps.engine.event", "Events")
            val player = ClassName("world.gregs.voidps.engine.entity.character.player", "Player")
            val event = ClassName("world.gregs.voidps.engine.entity.obj", "ObjectOption")
//            val ids = ids
//            val name = if (approach) {
//                ClassName("world.gregs.voidps.engine.entity.obj", "objectApproach")
//            } else {
//                ClassName("world.gregs.voidps.engine.entity.obj", "objectOperate")
//            }
//            builder.addStatement("%T(%S, ${ids.joinToString(",") { "\"${it}\"" }}, arrive = ${arrive}) { %T() }", name, option, method)
            val type = if (approach) "player_approach_object" else "player_operate_object"

            builder.addStatement("val handler${it}: suspend %T<%T>.(%T) -> Unit = {", event, player, player)
            if (arrive) {
                builder.addStatement("  arriveDelay()")
            }
            builder.addStatement("  %T()", method)
            builder.addStatement("}")
            for (id in ids) {
                builder.addStatement("%T.events.insert(4, %S, %S, %S, \"player\", handler${it} as suspend Event.(EventDispatcher) -> Unit)", name, type, option, id)
            }
        }
    )

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = EventProcessor(
        codeGenerator = environment.codeGenerator,
        logger = environment.logger,
        handlers
    )
}
