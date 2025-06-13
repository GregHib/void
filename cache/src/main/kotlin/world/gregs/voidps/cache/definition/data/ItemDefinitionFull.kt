package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.ColourPalette
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.Recolourable

data class ItemDefinitionFull(
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
    var exchangeable: Boolean = false,
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
    var floorScaleZ: Int = 128,
    var floorScaleY: Int = 128,
    var ambience: Int = 0,
    var diffusion: Int = 0,
    var team: Int = 0,
    var lendId: Int = -1,
    var lendTemplateId: Int = -1,
    var maleWieldX: Int = 0,
    var maleWieldZ: Int = 0,
    var maleWieldY: Int = 0,
    var femaleWieldX: Int = 0,
    var femaleWieldZ: Int = 0,
    var femaleWieldY: Int = 0,
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
    override var params: Map<Int, Any>? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Recolourable,
    ColourPalette,
    Parameterized,
    Extra {

    val noted: Boolean
        get() = notedTemplateId != -1

    val lent: Boolean
        get() = lendTemplateId != -1

    val singleNote: Boolean
        get() = singleNoteTemplateId != -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemDefinitionFull

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
        if (originalColours != null) {
            if (other.originalColours == null) return false
            if (!originalColours.contentEquals(other.originalColours)) return false
        } else if (other.originalColours != null) {
            return false
        }
        if (modifiedColours != null) {
            if (other.modifiedColours == null) return false
            if (!modifiedColours.contentEquals(other.modifiedColours)) return false
        } else if (other.modifiedColours != null) {
            return false
        }
        if (originalTextureColours != null) {
            if (other.originalTextureColours == null) return false
            if (!originalTextureColours.contentEquals(other.originalTextureColours)) return false
        } else if (other.originalTextureColours != null) {
            return false
        }
        if (modifiedTextureColours != null) {
            if (other.modifiedTextureColours == null) return false
            if (!modifiedTextureColours.contentEquals(other.modifiedTextureColours)) return false
        } else if (other.modifiedTextureColours != null) {
            return false
        }
        if (recolourPalette != null) {
            if (other.recolourPalette == null) return false
            if (!recolourPalette.contentEquals(other.recolourPalette)) return false
        } else if (other.recolourPalette != null) {
            return false
        }
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
        if (stackIds != null) {
            if (other.stackIds == null) return false
            if (!stackIds.contentEquals(other.stackIds)) return false
        } else if (other.stackIds != null) {
            return false
        }
        if (stackAmounts != null) {
            if (other.stackAmounts == null) return false
            if (!stackAmounts.contentEquals(other.stackAmounts)) return false
        } else if (other.stackAmounts != null) {
            return false
        }
        if (floorScaleX != other.floorScaleX) return false
        if (floorScaleZ != other.floorScaleZ) return false
        if (floorScaleY != other.floorScaleY) return false
        if (ambience != other.ambience) return false
        if (diffusion != other.diffusion) return false
        if (team != other.team) return false
        if (lendId != other.lendId) return false
        if (lendTemplateId != other.lendTemplateId) return false
        if (maleWieldX != other.maleWieldX) return false
        if (maleWieldZ != other.maleWieldZ) return false
        if (maleWieldY != other.maleWieldY) return false
        if (femaleWieldX != other.femaleWieldX) return false
        if (femaleWieldZ != other.femaleWieldZ) return false
        if (femaleWieldY != other.femaleWieldY) return false
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
        } else if (other.campaigns != null) {
            return false
        }
        if (pickSizeShift != other.pickSizeShift) return false
        if (singleNoteId != other.singleNoteId) return false
        if (singleNoteTemplateId != other.singleNoteTemplateId) return false
        if (params != other.params) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + modelId
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
        result = 31 * result + (originalColours?.contentHashCode() ?: 0)
        result = 31 * result + (modifiedColours?.contentHashCode() ?: 0)
        result = 31 * result + (originalTextureColours?.contentHashCode() ?: 0)
        result = 31 * result + (modifiedTextureColours?.contentHashCode() ?: 0)
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
        result = 31 * result + (stackIds?.contentHashCode() ?: 0)
        result = 31 * result + (stackAmounts?.contentHashCode() ?: 0)
        result = 31 * result + floorScaleX
        result = 31 * result + floorScaleZ
        result = 31 * result + floorScaleY
        result = 31 * result + ambience
        result = 31 * result + diffusion
        result = 31 * result + team
        result = 31 * result + lendId
        result = 31 * result + lendTemplateId
        result = 31 * result + maleWieldX
        result = 31 * result + maleWieldZ
        result = 31 * result + maleWieldY
        result = 31 * result + femaleWieldX
        result = 31 * result + femaleWieldZ
        result = 31 * result + femaleWieldY
        result = 31 * result + primaryCursorOpcode
        result = 31 * result + primaryCursor
        result = 31 * result + secondaryCursorOpcode
        result = 31 * result + secondaryCursor
        result = 31 * result + primaryInterfaceCursorOpcode
        result = 31 * result + primaryInterfaceCursor
        result = 31 * result + secondaryInterfaceCursorOpcode
        result = 31 * result + secondaryInterfaceCursor
        result = 31 * result + (campaigns?.contentHashCode() ?: 0)
        result = 31 * result + pickSizeShift
        result = 31 * result + singleNoteId
        result = 31 * result + singleNoteTemplateId
        result = 31 * result + (params?.hashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + extras.hashCode()
        return result
    }

    fun toLend(item: ItemDefinitionFull?, template: ItemDefinitionFull?) {
        if (item == null || template == null) {
            return
        }
        modifiedColours = item.modifiedColours
        primaryMaleDialogueHead = item.primaryMaleDialogueHead
        secondaryMaleDialogueHead = item.secondaryMaleDialogueHead
        tertiaryMaleModel = item.tertiaryMaleModel
        team = item.team
        params = item.params
        members = item.members
        modifiedTextureColours = item.modifiedTextureColours
        maleWieldZ = item.maleWieldZ
        secondaryFemaleModel = item.secondaryFemaleModel
        spriteCameraYaw = template.spriteCameraYaw
        floorOptions = item.floorOptions
        secondaryFemaleDialogueHead = item.secondaryFemaleDialogueHead
        recolourPalette = item.recolourPalette
        femaleWieldZ = item.femaleWieldZ
        spritePitch = template.spritePitch
        primaryFemaleModel = item.primaryFemaleModel
        modelId = template.modelId
        options = arrayOfNulls(5)
        spriteCameraRoll = template.spriteCameraRoll
        spriteTranslateY = template.spriteTranslateY
        originalTextureColours = item.originalTextureColours
        femaleWieldX = item.femaleWieldX
        secondaryMaleModel = item.secondaryMaleModel
        cost = 0
        maleWieldY = item.maleWieldY
        originalColours = item.originalColours
        spriteTranslateX = template.spriteTranslateX
        femaleWieldY = item.femaleWieldY
        primaryFemaleDialogueHead = item.primaryFemaleDialogueHead
        spriteScale = template.spriteScale
        name = item.name
        tertiaryFemaleModel = item.tertiaryFemaleModel
        primaryMaleModel = item.primaryMaleModel
        maleWieldX = item.maleWieldX
        System.arraycopy(item.options, 0, options, 0, 4)
        options[4] = "Discard"
    }

    fun toNote(template: ItemDefinitionFull?, item: ItemDefinitionFull?) {
        if (item == null || template == null) {
            return
        }
        spriteTranslateY = template.spriteTranslateY
        originalColours = template.originalColours
        cost = item.cost
        name = item.name
        modifiedTextureColours = template.modifiedTextureColours
        spriteCameraRoll = template.spriteCameraRoll
        spriteCameraYaw = template.spriteCameraYaw
        originalTextureColours = template.originalTextureColours
        modelId = template.modelId
        spriteScale = template.spriteScale
        recolourPalette = template.recolourPalette
        stackable = 1
        spritePitch = template.spritePitch
        spriteTranslateX = template.spriteTranslateX
        members = item.members
        modifiedColours = template.modifiedColours
    }

    fun toSingleNote(template: ItemDefinitionFull?, item: ItemDefinitionFull?) {
        if (item == null || template == null) {
            return
        }
        cost = 0
        tertiaryMaleModel = item.tertiaryMaleModel
        stackable = item.stackable
        members = item.members
        recolourPalette = item.recolourPalette
        spriteTranslateY = template.spriteTranslateY
        team = item.team
        secondaryMaleModel = item.secondaryMaleModel
        options = arrayOfNulls(5)
        floorOptions = item.floorOptions
        maleWieldZ = item.maleWieldZ
        primaryMaleDialogueHead = item.primaryMaleDialogueHead
        femaleWieldZ = item.femaleWieldZ
        name = item.name
        spriteScale = template.spriteScale
        originalColours = item.originalColours
        secondaryFemaleDialogueHead = item.secondaryFemaleDialogueHead
        params = item.params
        primaryFemaleModel = item.primaryFemaleModel
        spritePitch = template.spritePitch
        spriteCameraRoll = template.spriteCameraRoll
        femaleWieldX = item.femaleWieldX
        secondaryMaleDialogueHead = item.secondaryMaleDialogueHead
        tertiaryFemaleModel = item.tertiaryFemaleModel
        modifiedTextureColours = item.modifiedTextureColours
        maleWieldX = item.maleWieldX
        primaryFemaleDialogueHead = item.primaryFemaleDialogueHead
        modelId = template.modelId
        modifiedColours = item.modifiedColours
        secondaryFemaleModel = item.secondaryFemaleModel
        spriteTranslateX = template.spriteTranslateX
        spriteCameraYaw = template.spriteCameraYaw
        primaryMaleModel = item.primaryMaleModel
        femaleWieldY = item.femaleWieldY
        maleWieldY = item.maleWieldY
        originalTextureColours = item.originalTextureColours
        System.arraycopy(item.options, 0, options, 0, 4)
        options[4] = "Discard"
    }

    companion object {
        val EMPTY = ItemDefinitionFull()
    }
}
