package world.gregs.voidps.tools.photobooth

import world.gregs.voidps.cache.config.data.IdentityKitDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinitionFull
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.tools.photobooth.render.BodyColourPalettes
import world.gregs.voidps.tools.photobooth.render.ModelComposer
import world.gregs.voidps.tools.photobooth.render.RenderModel

/**
 * Builds 3D player [RenderModel]s from a [PhotoSnapshot] by mirroring the client/`BodyParts`
 * appearance assembly: for each of the 12 body parts, pick the worn item's model or the IdentityKit
 * body-part model (or nothing when covered/hidden), compose each source with its own colour swap,
 * and merge. Two variants, matching Jagex's avatar service:
 *  - [assembleBody]: the full paperdoll (IdentityKit body modelIds + item worn models) -> full.gif
 *  - [assembleHead]: the chathead (IdentityKit headModels + item dialogue-head models) -> chat.gif
 *
 * The player's 5 chosen body colours are applied via [BodyColourPalettes] (client-accurate).
 *
 * Fidelity gap (documented): HairMid/HairLow overrides (party/wizard hats shrinking hair) are not
 * applied; hair shows in full.
 */
class PlayerModelAssembler(
    private val composer: ModelComposer,
    private val equipIndexToItemId: Map<Int, Int>,
    private val fullItems: Array<ItemDefinitionFull>,
    private val identityKits: Array<IdentityKitDefinition>,
    private val itemType: (Int) -> EquipType,
) {

    /** Full-body paperdoll model. */
    fun assembleBody(snapshot: PhotoSnapshot): RenderModel? = assemble(snapshot, head = false)

    /** Chathead model (head, hair, beard, helmet, amulet) as shown in dialogue / forum avatars. */
    fun assembleHead(snapshot: PhotoSnapshot): RenderModel? = assemble(snapshot, head = true)

    private fun assemble(snapshot: PhotoSnapshot, head: Boolean): RenderModel? {
        val parts = IntArray(12)
        for (part in BodyPart.entries) {
            parts[part.ordinal] = value(part, snapshot)
        }

        val itemEquipIndices = LinkedHashSet<Int>()
        val kitIds = LinkedHashSet<Int>()
        for (value in parts) {
            when {
                value == 0 -> {}
                value and 0x8000 != 0 -> itemEquipIndices.add(value and 0x7FFF)
                value >= 0x100 -> kitIds.add(value - 0x100)
            }
        }

        val sources = ArrayList<RenderModel>()
        for (equipIndex in itemEquipIndices) {
            val itemId = equipIndexToItemId[equipIndex] ?: continue
            val full = fullItems.getOrNull(itemId) ?: continue
            val modelIds = if (head) itemHeadModels(full, snapshot.male) else itemModels(full, snapshot.male)
            if (modelIds.isEmpty()) continue
            composer.compose(itemId, modelIds, full.originalColours.toIntArrayOrNull(), full.modifiedColours.toIntArrayOrNull())
                ?.let { sources.add(it) }
        }
        for (kitId in kitIds) {
            val kit = identityKits.getOrNull(kitId) ?: continue
            val modelIds = (if (head) kit.headModels else kit.modelIds)?.filter { it != -1 }?.toIntArray()
            if (modelIds == null || modelIds.isEmpty()) continue
            composer.compose(KIT_ID_BASE + kitId, modelIds, kit.originalColours.toIntArrayOrNull(), kit.modifiedColours.toIntArrayOrNull())
                ?.let { sources.add(it) }
        }
        val merged = ModelComposer.merge(PLAYER_MODEL_ID, sources) ?: return null
        // Apply the player's 5 chosen body colours to the assembled model, exactly as the client does.
        BodyColourPalettes.apply(merged.faceColors, snapshot.colours)
        return merged
    }

    /** Mirrors BodyParts.update() (minus the HairMid/HairLow overrides). */
    private fun value(part: BodyPart, snapshot: PhotoSnapshot): Int {
        val equipIndex = wornEquipIndex(part.slot, snapshot)
        val itemId = if (equipIndex >= 0) equipIndexToItemId[equipIndex] else null
        val present = itemId != null
        val type = if (itemId != null) itemType(itemId) else EquipType.None
        return when {
            showItem(part, present, type) -> if (equipIndex >= 0) equipIndex or 0x8000 else 0
            showBodyPart(part, type, snapshot) -> snapshot.looks[part.index] + 0x100
            showDefault(part, snapshot) -> defaultLook(snapshot.male, part.index) + 0x100
            else -> 0
        }
    }

    private fun wornEquipIndex(slot: EquipSlot, snapshot: PhotoSnapshot): Int {
        if (slot == EquipSlot.None) return -1
        return snapshot.equipment.getOrElse(slot.index) { -1 }
    }

    private fun showItem(part: BodyPart, present: Boolean, type: EquipType): Boolean = present && when (part) {
        BodyPart.Hair, BodyPart.Beard -> false
        BodyPart.Arms -> type != EquipType.Sleeveless
        else -> true
    }

    private fun showBodyPart(part: BodyPart, type: EquipType, snapshot: PhotoSnapshot): Boolean =
        part.index != -1 && snapshot.looks.getOrElse(part.index) { -1 } >= 0 && when (part) {
            BodyPart.Hair -> type != EquipType.FullFace && type != EquipType.Hair
            BodyPart.Beard -> type != EquipType.FullFace && type != EquipType.Mask
            else -> true
        }

    private fun showDefault(part: BodyPart, snapshot: PhotoSnapshot): Boolean {
        if (part != BodyPart.Arms) return false
        val chestEquip = wornEquipIndex(BodyPart.Chest.slot, snapshot)
        val chestId = if (chestEquip >= 0) equipIndexToItemId[chestEquip] else null
        if (chestId == null || itemType(chestId) != EquipType.Sleeveless) return false
        return part.index != -1 && snapshot.looks.getOrElse(part.index) { 0 } < 0
    }

    private fun itemModels(full: ItemDefinitionFull, male: Boolean): IntArray {
        val ids = if (male) {
            intArrayOf(full.primaryMaleModel, full.secondaryMaleModel, full.tertiaryMaleModel)
        } else {
            intArrayOf(full.primaryFemaleModel, full.secondaryFemaleModel, full.tertiaryFemaleModel)
        }
        return ids.filter { it != -1 }.toIntArray()
    }

    private fun itemHeadModels(full: ItemDefinitionFull, male: Boolean): IntArray {
        val ids = if (male) {
            intArrayOf(full.primaryMaleDialogueHead, full.secondaryMaleDialogueHead)
        } else {
            intArrayOf(full.primaryFemaleDialogueHead, full.secondaryFemaleDialogueHead)
        }
        return ids.filter { it != -1 }.toIntArray()
    }

    private fun defaultLook(male: Boolean, index: Int): Int =
        (if (male) DEFAULT_LOOK_MALE else DEFAULT_LOOK_FEMALE).getOrElse(index) { 0 }

    private fun ShortArray?.toIntArrayOrNull(): IntArray? =
        this?.let { array -> IntArray(array.size) { array[it].toInt() and 0xFFFF } }

    companion object {
        // Mirrors BodyParts.DEFAULT_LOOK_*.
        private val DEFAULT_LOOK_MALE = intArrayOf(0, 14, 18, 26, 34, 38, 42)
        private val DEFAULT_LOOK_FEMALE = intArrayOf(45, -1, 58, 61, 68, 72, 80)
        private const val KIT_ID_BASE = 0x100000
        private const val PLAYER_MODEL_ID = 0x200000
    }
}
