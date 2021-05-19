package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Recolourable

@Suppress("ArrayInDataClass")
data class IdentityKitDefinition(
    override var id: Int = -1,
    var bodyPartId: Int = 0,
    var modelIds: IntArray? = null,
    override var originalColours: ShortArray? = null,
    override var modifiedColours: ShortArray? = null,
    override var originalTextureColours: ShortArray? = null,
    override var modifiedTextureColours: ShortArray? = null,
    val headModels: IntArray = intArrayOf(-1, -1, -1, -1, -1)
) : Definition, Recolourable