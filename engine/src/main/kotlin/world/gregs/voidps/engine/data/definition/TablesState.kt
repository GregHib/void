package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.config.TableDefinition
import kotlin.collections.set

class TablesState(
    val definitions: Map<String, TableDefinition>,
    val rows: Array<RowDefinition>,
    val ids: Map<String, Int>,
)
