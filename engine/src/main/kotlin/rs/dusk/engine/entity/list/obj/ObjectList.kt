package rs.dusk.engine.entity.list.obj

import rs.dusk.engine.entity.model.IObject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
data class ObjectList(override val delegate: HashMap<Int, MutableSet<IObject>> = hashMapOf()) : Objects