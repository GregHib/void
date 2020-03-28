package org.redrune.engine.entity.list.obj

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import org.redrune.engine.entity.model.IObject
import org.redrune.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class ObjectList : Objects {
    override val delegate: SetMultimap<Tile, IObject> = HashMultimap.create()
}