package rs.dusk.engine.entity.list.obj

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import rs.dusk.engine.entity.model.IObject
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class ObjectList : Objects {
    override val delegate: SetMultimap<Tile, IObject> = HashMultimap.create()
}