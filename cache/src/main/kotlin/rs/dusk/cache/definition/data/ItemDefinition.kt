package rs.dusk.cache.definition.data

import rs.dusk.cache.Definition
import rs.dusk.cache.definition.ColourPalette
import rs.dusk.cache.definition.Parameterized
import rs.dusk.cache.definition.Recolourable

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class ItemDefinition(
    override var id: Int = -1,
    var modelId: Int = 0,
    var name: String = "null",
    var spriteScale: Int = 2000,
    var spritePitch: Int = 0,
    var spriteCameraRoll: Int = 0,
    var spriteTranslateX: Int = 0,
    var spriteTranslateY: Int = 0,
    var stackable: Int = 0,
    var cost: Int = 1,
    var members: Boolean = false,
    var multiStackSize: Int = -1,
    var primaryMaleModel: Int = -1,
    var secondaryMaleModel: Int = -1,
    var primaryFemaleModel: Int = -1,
    var secondaryFemaleModel: Int = -1,
    var floorOptions: Array<String?> = arrayOf(null, null, "Take", null, null, "Examine"),
    var options: Array<String?> = arrayOf(null, null, null, null, "Drop"),
    override var originalColours: ShortArray? = null,
    override var modifiedColours: ShortArray? = null,
    override var originalTextureColours: ShortArray? = null,
    override var modifiedTextureColours: ShortArray? = null,
    override var recolourPalette: ByteArray? = null,
    var unnoted: Boolean = false,
    var tertiaryMaleModel: Int = -1,
    var tertiaryFemaleModel: Int = -1,
    var primaryMaleDialogueHead: Int = -1,
    var primaryFemaleDialogueHead: Int = -1,
    var secondaryMaleDialogueHead: Int = -1,
    var secondaryFemaleDialogueHead: Int = -1,
    var spriteCameraYaw: Int = 0,
    var dummyItem: Int = 0,
    var noteId: Int = -1,
    var notedTemplateId: Int = -1,
    var stackIds: IntArray? = null,
    var stackAmounts: IntArray? = null,
    var floorScaleX: Int = 128,
    var floorScaleY: Int = 128,
    var floorScaleZ: Int = 128,
    var ambience: Int = 0,
    var diffusion: Int = 0,
    var team: Int = 0,
    var lendId: Int = -1,
    var lendTemplateId: Int = -1,
    var maleWieldX: Int = 0,
    var maleWieldY: Int = 0,
    var maleWieldZ: Int = 0,
    var femaleWieldX: Int = 0,
    var femaleWieldY: Int = 0,
    var femaleWieldZ: Int = 0,
    var primaryCursorOpcode: Int = -1,
    var primaryCursor: Int = -1,
    var secondaryCursorOpcode: Int = -1,
    var secondaryCursor: Int = -1,
    var primaryInterfaceCursorOpcode: Int = -1,
    var primaryInterfaceCursor: Int = -1,
    var secondaryInterfaceCursorOpcode: Int = -1,
    var secondaryInterfaceCursor: Int = -1,
    var campaigns: IntArray? = null,
    var pickSizeShift: Int = 0,
    var singleNoteId: Int = -1,
    var singleNoteTemplateId: Int = -1,
    override var params: HashMap<Long, Any>? = null
) : Definition, Recolourable, ColourPalette, Parameterized