package world.gregs.voidps.engine.entity.character.update

/**
 * @author GregHib <greg@gregs.world>
 * @since May 14, 2020
 */
sealed class LocalChange(val id: Int) {
    object Update : LocalChange(0)
    object Walk : LocalChange(1)
    object Crawl : LocalChange(2)
    object Run : LocalChange(2)
    object Tele : LocalChange(3)
    object TeleGlobal : LocalChange(3)
    object Remove : LocalChange(3)
}

sealed class RegionChange(val id: Int) {
    object Update : RegionChange(0)
    object Height : RegionChange(1)
    object Local : RegionChange(2)
    object Global : RegionChange(3)
}