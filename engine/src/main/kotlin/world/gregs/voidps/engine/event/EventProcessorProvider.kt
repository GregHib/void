package world.gregs.voidps.engine.event

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import world.gregs.voidps.engine.event.handle.*

class EventProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = EventProcessor(
        codeGenerator = environment.codeGenerator,
        logger = environment.logger,
        eventSchemas = mapOf(
            UseOn::class to UseOnSchema,
            Option::class to OptionSchema,
            Inventory::class to InventorySchema,
            Handle::class to HandleSchema,
            Variable::class to VariableSchema,
            On::class to OnSchema,
            Combat::class to CombatSchema,
            Area::class to AreaSchema,
            Move::class to MoveSchema,
            LevelChange::class to LevelChangeSchema,
        )
    )
}
