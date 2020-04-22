package rs.dusk.engine.view

import rs.dusk.engine.entity.model.Entity

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
data class TrackingSet<T : Entity>(
    val add: MutableSet<T> = mutableSetOf(),
    var remove: MutableSet<T> = mutableSetOf(),
    var current: MutableSet<T> = mutableSetOf()
) {

    fun flip() {
        val temp = current
        current = remove
        remove = temp
    }

    fun update(set: Set<T>) {
        for (entity in set) {
            if (remove.remove(entity)) {// Flipped so remove is actually current list here
                current.add(entity)
            } else {
                add.add(entity)
            }
        }
    }
}
