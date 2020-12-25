package rs.dusk.tools.map.obj

import rs.dusk.ai.Option

open class ObjectIdentification(
    val name: String,
    override val targets: ObjectIdentificationContext.() -> List<GameObjectOption>,
    override val considerations: Set<ObjectIdentificationContext.(GameObjectOption) -> Double>,
    override val momentum: Double = 1.0,
    override val weight: Double = 1.0
) : Option<ObjectIdentificationContext, GameObjectOption>