package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.Entity
import kotlin.reflect.KClass

data class EventHandler(
    val event: KClass<out Event>,
    val condition: Event.(Entity) -> Boolean,
    val priority: Priority = Priority.MEDIUM,
    val block: Event.(Entity) -> Unit
)