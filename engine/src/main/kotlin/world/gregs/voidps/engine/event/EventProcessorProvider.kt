package world.gregs.voidps.engine.event

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class EventProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = EventProcessor(
        codeGenerator = environment.codeGenerator,
        logger = environment.logger,
    )
}
