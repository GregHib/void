package rs.dusk.tools.map.obj

import rs.dusk.ai.Option

open class ObjectIdentification<T: Any>(
    val name: String,
    override val targets: ObjectIdentificationContext.() -> List<T>,
    override val considerations: Set<ObjectIdentificationContext.(T) -> Double>,
    override val momentum: Double = 1.0,
    override val weight: Double = 1.0
) : Option<ObjectIdentificationContext, T>