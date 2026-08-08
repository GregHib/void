package world.gregs.voidps.engine.data

/**
 * A point-in-time count of a watched item across the game economy
 * @param timestamp Epoch millisecond timestamp the snapshot was taken
 * @param item Canonical item id
 * @param players Count across all player inventories, online and offline
 * @param floor Count in items on the floor
 * @param exchange Count escrowed in open grand exchange offers
 */
data class CensusSnapshot(
    val timestamp: Long,
    val item: String,
    val players: Long,
    val floor: Long,
    val exchange: Long,
)
