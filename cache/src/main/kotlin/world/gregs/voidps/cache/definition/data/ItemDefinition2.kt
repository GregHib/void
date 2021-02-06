package world.gregs.voidps.cache.definition.data

import kotlinx.serialization.*
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.Flatten
import world.gregs.voidps.cache.definition.ColourPalette
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.Recolourable
import world.gregs.voidps.cache.format.definition.Colours
import world.gregs.voidps.cache.format.definition.Indexed
import world.gregs.voidps.cache.format.definition.Operation
import world.gregs.voidps.cache.format.definition.Setter
import world.gregs.voidps.cache.format.definition.internal.ColourSerializer
import world.gregs.voidps.cache.format.definition.internal.ParameterSerializer

/**
 * @author GregHib <greg@gregs.world>
 * @since April 07, 2020
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
data class ItemDefinition2(
    @Transient
    override var id: Int = -1,
    @Operation(1)
    var modelId: UShort = 0u,
    @Operation(2)
    var name: String = "null",
    @Operation(4)
    var spriteScale: Short = 2000,
    @Operation(5)
    var spritePitch: Short = 0,
    @Operation(6)
    var spriteCameraRoll: Short = 0,
    @Operation(7)
    var spriteTranslateX: Short = 0,
    @Operation(8)
    var spriteTranslateY: Short = 0,
    @Setter(1)
    @Operation(11)
    var stackable: Int = 0,
    @Operation(12)
    var cost: Int = 1,
    @Setter(1)
    @Operation(16)
    var members: Boolean = false,
    @Operation(18)
    var multiStackSize: Short = -1,
    @Operation(23)
    var primaryMaleModel: Short = -1,
    @Operation(24)
    var secondaryMaleModel: Short = -1,
    @Operation(25)
    var primaryFemaleModel: Short = -1,
    @Operation(26)
    var secondaryFemaleModel: Short = -1,
    @Indexed([30, 31, 32, 33, 34])
    var floorOptions: Array<String?> = arrayOf(null, null, "Take", null, null, "Examine"),
    @Indexed([35, 36, 37, 38, 39])
    var options: Array<String?> = arrayOf(null, null, null, null, "Drop"),
    @Operation(40)
    var colours: Colours? = null,
    @Operation(41)
    var textures: Colours? = null,
    @Operation(42)
    var recolourPalette: ByteArray? = null,
    @Setter(1)
    @Operation(65)
    var exchangeable: Boolean = false,
    @Operation(78)
    var tertiaryMaleModel: Short = -1,
    @Operation(79)
    var tertiaryFemaleModel: Short = -1,
    @Operation(90)
    var primaryMaleDialogueHead: Short = -1,
    @Operation(91)
    var primaryFemaleDialogueHead: Short = -1,
    @Operation(92)
    var secondaryMaleDialogueHead: Short = -1,
    @Operation(93)
    var secondaryFemaleDialogueHead: Short = -1,
    @Operation(95)
    var spriteCameraYaw: Short = 0,
    @Operation(96)
    var dummyItem: Byte = 0,
    @Operation(96)
    var noteId: Short = -1,
    @Operation(97)
    var notedTemplateId: Short = -1,
    @Indexed([100, 101, 102, 103, 104, 105, 106, 107, 108, 109])
    var stack: ShortArray = ShortArray(10),
    @Operation(110)
    var floorScaleX: Short = 128,
    @Operation(111)
    var floorScaleY: Short = 128,
    @Operation(112)
    var floorScaleZ: Short = 128,
    @Operation(113)
    var ambience: Byte = 0,
    @Operation(114)
    var diffusion: Byte = 0,
    @Operation(115)
    var team: Byte = 0,
    @Operation(121)
    var lendId: Short = -1,
    @Operation(122)
    var lendTemplateId: Short = -1,
    @Operation(125)
    var maleWieldX: Byte = 0,
    @Operation(125)
    var maleWieldY: Byte = 0,
    @Operation(125)
    var maleWieldZ: Byte = 0,
    @Operation(126)
    var femaleWieldX: Byte = 0,
    @Operation(126)
    var femaleWieldY: Byte = 0,
    @Operation(126)
    var femaleWieldZ: Byte = 0,
    @Operation(127)
    var primaryCursorOpcode: Byte = -1,
    @Operation(127)
    var primaryCursor: Short = -1,
    @Operation(128)
    var secondaryCursorOpcode: Byte = -1,
    @Operation(128)
    var secondaryCursor: Short = -1,
    @Operation(129)
    var primaryInterfaceCursorOpcode: Byte = -1,
    @Operation(129)
    var primaryInterfaceCursor: Short = -1,
    @Operation(130)
    var secondaryInterfaceCursorOpcode: Byte = -1,
    @Operation(130)
    var secondaryInterfaceCursor: Short = -1,
    @Operation(132)
    var campaigns: ShortArray? = null,
    @Operation(134)
    var pickSizeShift: UByte = 0u,
    @Operation(130)
    var singleNoteId: Short = -1,
    @Operation(140)
    var singleNoteTemplateId: Short = -1,
    @Operation(249)
    @Serializable(with = ParameterSerializer::class)
    var params: Map<Int, @Contextual Any>? = null,
    @Transient
    override var extras: Map<String, Any> = emptyMap()
) : Definition, Extra {

    val noted: Boolean
        get() = notedTemplateId.toInt() != -1

    val lent: Boolean
        get() = lendTemplateId.toInt() != -1

    val singleNote: Boolean
        get() = singleNoteTemplateId.toInt() != -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemDefinition2

        if (id != other.id) return false
        if (modelId != other.modelId) return false
        if (name != other.name) return false
        if (spriteScale != other.spriteScale) return false
        if (spritePitch != other.spritePitch) return false
        if (spriteCameraRoll != other.spriteCameraRoll) return false
        if (spriteTranslateX != other.spriteTranslateX) return false
        if (spriteTranslateY != other.spriteTranslateY) return false
        if (stackable != other.stackable) return false
        if (cost != other.cost) return false
        if (members != other.members) return false
        if (multiStackSize != other.multiStackSize) return false
        if (primaryMaleModel != other.primaryMaleModel) return false
        if (secondaryMaleModel != other.secondaryMaleModel) return false
        if (primaryFemaleModel != other.primaryFemaleModel) return false
        if (secondaryFemaleModel != other.secondaryFemaleModel) return false
        if (!floorOptions.contentEquals(other.floorOptions)) return false
        if (!options.contentEquals(other.options)) return false
        if (colours != other.colours) return false
        if (textures != other.textures) return false
        if (recolourPalette != null) {
            if (other.recolourPalette == null) return false
            if (!recolourPalette.contentEquals(other.recolourPalette)) return false
        } else if (other.recolourPalette != null) return false
        if (exchangeable != other.exchangeable) return false
        if (tertiaryMaleModel != other.tertiaryMaleModel) return false
        if (tertiaryFemaleModel != other.tertiaryFemaleModel) return false
        if (primaryMaleDialogueHead != other.primaryMaleDialogueHead) return false
        if (primaryFemaleDialogueHead != other.primaryFemaleDialogueHead) return false
        if (secondaryMaleDialogueHead != other.secondaryMaleDialogueHead) return false
        if (secondaryFemaleDialogueHead != other.secondaryFemaleDialogueHead) return false
        if (spriteCameraYaw != other.spriteCameraYaw) return false
        if (dummyItem != other.dummyItem) return false
        if (noteId != other.noteId) return false
        if (notedTemplateId != other.notedTemplateId) return false
        if (!stack.contentEquals(other.stack)) return false
        if (floorScaleX != other.floorScaleX) return false
        if (floorScaleY != other.floorScaleY) return false
        if (floorScaleZ != other.floorScaleZ) return false
        if (ambience != other.ambience) return false
        if (diffusion != other.diffusion) return false
        if (team != other.team) return false
        if (lendId != other.lendId) return false
        if (lendTemplateId != other.lendTemplateId) return false
        if (maleWieldX != other.maleWieldX) return false
        if (maleWieldY != other.maleWieldY) return false
        if (maleWieldZ != other.maleWieldZ) return false
        if (femaleWieldX != other.femaleWieldX) return false
        if (femaleWieldY != other.femaleWieldY) return false
        if (femaleWieldZ != other.femaleWieldZ) return false
        if (primaryCursorOpcode != other.primaryCursorOpcode) return false
        if (primaryCursor != other.primaryCursor) return false
        if (secondaryCursorOpcode != other.secondaryCursorOpcode) return false
        if (secondaryCursor != other.secondaryCursor) return false
        if (primaryInterfaceCursorOpcode != other.primaryInterfaceCursorOpcode) return false
        if (primaryInterfaceCursor != other.primaryInterfaceCursor) return false
        if (secondaryInterfaceCursorOpcode != other.secondaryInterfaceCursorOpcode) return false
        if (secondaryInterfaceCursor != other.secondaryInterfaceCursor) return false
        if (campaigns != null) {
            if (other.campaigns == null) return false
            if (!campaigns.contentEquals(other.campaigns)) return false
        } else if (other.campaigns != null) return false
        if (pickSizeShift != other.pickSizeShift) return false
        if (singleNoteId != other.singleNoteId) return false
        if (singleNoteTemplateId != other.singleNoteTemplateId) return false
        if (params != other.params) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + modelId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + spriteScale
        result = 31 * result + spritePitch
        result = 31 * result + spriteCameraRoll
        result = 31 * result + spriteTranslateX
        result = 31 * result + spriteTranslateY
        result = 31 * result + stackable
        result = 31 * result + cost
        result = 31 * result + members.hashCode()
        result = 31 * result + multiStackSize
        result = 31 * result + primaryMaleModel
        result = 31 * result + secondaryMaleModel
        result = 31 * result + primaryFemaleModel
        result = 31 * result + secondaryFemaleModel
        result = 31 * result + floorOptions.contentHashCode()
        result = 31 * result + options.contentHashCode()
        result = 31 * result + (colours?.hashCode() ?: 0)
        result = 31 * result + (textures?.hashCode() ?: 0)
        result = 31 * result + (recolourPalette?.contentHashCode() ?: 0)
        result = 31 * result + exchangeable.hashCode()
        result = 31 * result + tertiaryMaleModel
        result = 31 * result + tertiaryFemaleModel
        result = 31 * result + primaryMaleDialogueHead
        result = 31 * result + primaryFemaleDialogueHead
        result = 31 * result + secondaryMaleDialogueHead
        result = 31 * result + secondaryFemaleDialogueHead
        result = 31 * result + spriteCameraYaw
        result = 31 * result + dummyItem
        result = 31 * result + noteId
        result = 31 * result + notedTemplateId
        result = 31 * result + stack.contentHashCode()
        result = 31 * result + floorScaleX
        result = 31 * result + floorScaleY
        result = 31 * result + floorScaleZ
        result = 31 * result + ambience
        result = 31 * result + diffusion
        result = 31 * result + team
        result = 31 * result + lendId
        result = 31 * result + lendTemplateId
        result = 31 * result + maleWieldX
        result = 31 * result + maleWieldY
        result = 31 * result + maleWieldZ
        result = 31 * result + femaleWieldX
        result = 31 * result + femaleWieldY
        result = 31 * result + femaleWieldZ
        result = 31 * result + primaryCursorOpcode
        result = 31 * result + primaryCursor
        result = 31 * result + secondaryCursorOpcode
        result = 31 * result + secondaryCursor
        result = 31 * result + primaryInterfaceCursorOpcode
        result = 31 * result + primaryInterfaceCursor
        result = 31 * result + secondaryInterfaceCursorOpcode
        result = 31 * result + secondaryInterfaceCursor
        result = 31 * result + (campaigns?.contentHashCode() ?: 0)
        result = 31 * result + pickSizeShift.hashCode()
        result = 31 * result + singleNoteId
        result = 31 * result + singleNoteTemplateId
        result = 31 * result + (params?.hashCode() ?: 0)
        return result
    }
}