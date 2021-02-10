package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.ai.Option

open class ObjectIdentification<T: Any>(
    val name: String,
    override val targets: ObjectIdentificationContext.() -> List<T>,
    override val considerations: Set<ObjectIdentificationContext.(T) -> Double>,
    override val momentum: Double = 1.0,
    override val weight: Double = 1.0
) : Option<ObjectIdentificationContext, T> {
    override val action: (ObjectIdentificationContext.(T) -> Unit)? = null
}