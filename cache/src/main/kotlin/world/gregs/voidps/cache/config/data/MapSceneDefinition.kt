package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition

data class MapSceneDefinition(
    override var id: Int = -1,
    var sprite: Int = 0,
    var colour: Int = 0,
    var aBoolean1741: Boolean = false,
) : Definition
