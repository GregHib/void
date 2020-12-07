package rs.dusk.tools.map.render

import rs.dusk.engine.map.region.obj.GameObjectLoc
import rs.dusk.engine.map.region.tile.TileData

class RegionData(
    val regionId: Int,
    val objects: List<GameObjectLoc>?,
    val tiles: Array<Array<Array<TileData?>>>?
)