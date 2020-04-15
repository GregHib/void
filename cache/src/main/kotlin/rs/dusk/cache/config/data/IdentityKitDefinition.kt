package rs.dusk.cache.config.data

import rs.dusk.cache.Definition
import rs.dusk.cache.definition.Recolourable

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class IdentityKitDefinition(
    override var id: Int = -1,
    var modelIds: IntArray? = null,
    override var originalColours: ShortArray? = null,
    override var modifiedColours: ShortArray? = null,
    override var originalTextureColours: ShortArray? = null,
    override var modifiedTextureColours: ShortArray? = null,
    val headModels: IntArray = intArrayOf(-1, -1, -1, -1, -1)
) : Definition, Recolourable