package world.gregs.voidps.tools.avatar

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.config.data.IdentityKitDefinition
import world.gregs.voidps.cache.config.data.RenderAnimationDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinitionFull
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.tools.render.Mesh

/**
 * Builds player [Mesh]es from a [PhotoSnapshot] the same way the client does:
 *  - [body]: Class154.method1226 - per slot worn item mesh (Class213.method1558) or identity kit body mesh
 *    (Class34.method341), render animation per-slot offsets, merged in slot order, then body colours.
 *  - [head]: Class154.method1230 - per slot item dialogue head mesh (Class213.method1554) or identity kit
 *    head mesh (Class34.method343), merged without gaps, then body colours.
 *
 * Slot values are chosen by mirroring BodyParts.update() (minus the HairMid/HairLow overrides).
 */
class PlayerMeshAssembler(
    private val cache: Cache,
    private val equipIndexToItemId: Map<Int, Int>,
    private val items: Array<ItemDefinitionFull>,
    private val identityKits: Array<IdentityKitDefinition>,
    private val renderAnimations: Array<RenderAnimationDefinition>,
    private val itemType: (Int) -> EquipType,
    private val renderEmote: (Int) -> Int,
) {

    fun body(snapshot: PhotoSnapshot): Mesh? {
        val female = !snapshot.male
        val meshes = arrayOfNulls<Mesh>(SLOTS)
        for (part in BodyPart.entries) {
            val value = value(part, snapshot)
            meshes[part.ordinal] = when {
                value and ITEM != 0 -> items.getOrNull(value and ITEM.inv())?.let { wornMesh(it, female) }
                value and KIT != 0 -> identityKits.getOrNull(value and KIT.inv())?.let { kitBodyMesh(it) }
                else -> null
            }
        }
        if (meshes.all { it == null }) return null
        val offsets = renderAnimation(snapshot)?.anIntArrayArray3273
        if (offsets != null) {
            for (slot in 0 until minOf(SLOTS, offsets.size)) {
                val mesh = meshes[slot] ?: continue
                val offset = offsets[slot] ?: continue
                val pitch = offset[3] shl 3
                val yaw = offset[4] shl 3
                val roll = offset[5] shl 3
                if (pitch != 0 || yaw != 0 || roll != 0) mesh.rotate(pitch, yaw, roll)
                if (offset[0] != 0 || offset[1] != 0 || offset[2] != 0) mesh.translate(offset[0], offset[1], offset[2])
            }
        }
        val mesh = Mesh(meshes, SLOTS)
        BodyColours.apply(mesh, snapshot.colours)
        return mesh
    }

    /**
     * Class225.method1621 - the render animation's stand animation, or the first of its
     * alternatives (the client picks one at random by weight).
     */
    fun standAnimation(snapshot: PhotoSnapshot): Int {
        val definition = renderAnimation(snapshot) ?: return -1
        if (definition.primaryIdle != -1) return definition.primaryIdle
        return definition.anIntArray3294?.firstOrNull() ?: -1
    }

    private fun renderAnimation(snapshot: PhotoSnapshot): RenderAnimationDefinition? {
        val weapon = itemId(EquipSlot.Weapon, snapshot)
        return renderAnimations.getOrNull(renderEmote(weapon ?: -1))
    }

    fun head(snapshot: PhotoSnapshot): Mesh? {
        val female = !snapshot.male
        val meshes = arrayOfNulls<Mesh>(SLOTS)
        var count = 0
        for (part in BodyPart.entries) {
            val value = value(part, snapshot)
            val mesh = when {
                value and ITEM != 0 -> items.getOrNull(value and ITEM.inv())?.let { dialogueHeadMesh(it, female) }
                value and KIT != 0 -> identityKits.getOrNull(value and KIT.inv())?.let { kitHeadMesh(it) }
                else -> null
            }
            if (mesh != null) meshes[count++] = mesh
        }
        if (count == 0) return null
        val mesh = Mesh(meshes, count)
        BodyColours.apply(mesh, snapshot.colours)
        return mesh
    }

    /** Class213.method1558 */
    private fun wornMesh(definition: ItemDefinitionFull, female: Boolean): Mesh? {
        val primary = if (female) definition.primaryFemaleModel else definition.primaryMaleModel
        val secondary = if (female) definition.secondaryFemaleModel else definition.secondaryMaleModel
        val tertiary = if (female) definition.tertiaryFemaleModel else definition.tertiaryMaleModel
        if (primary == -1) return null
        var mesh = mesh(primary) ?: return null
        if (secondary != -1) {
            val second = mesh(secondary) ?: return null
            mesh = if (tertiary == -1) {
                Mesh(arrayOf(mesh, second), 2)
            } else {
                val third = mesh(tertiary) ?: return null
                Mesh(arrayOf(mesh, second, third), 3)
            }
        }
        // Opcodes 125/126 are x, y, z which ItemDefinitionFull names wield x, z, y
        val x = if (female) definition.femaleWieldX else definition.maleWieldX
        val y = if (female) definition.femaleWieldZ else definition.maleWieldZ
        val z = if (female) definition.femaleWieldY else definition.maleWieldY
        if (x != 0 || y != 0 || z != 0) mesh.translate(x, y, z)
        recolour(mesh, definition.originalColours, definition.modifiedColours, definition.originalTextureColours, definition.modifiedTextureColours)
        return mesh
    }

    /** Class213.method1554 */
    private fun dialogueHeadMesh(definition: ItemDefinitionFull, female: Boolean): Mesh? {
        val primary = if (female) definition.primaryFemaleDialogueHead else definition.primaryMaleDialogueHead
        val secondary = if (female) definition.secondaryFemaleDialogueHead else definition.secondaryMaleDialogueHead
        if (primary == -1) return null
        var mesh = mesh(primary) ?: return null
        if (secondary != -1) {
            val second = mesh(secondary) ?: return null
            mesh = Mesh(arrayOf(mesh, second), 2)
        }
        recolour(mesh, definition.originalColours, definition.modifiedColours, definition.originalTextureColours, definition.modifiedTextureColours)
        return mesh
    }

    /** Class34.method341 */
    private fun kitBodyMesh(kit: IdentityKitDefinition): Mesh? {
        val ids = kit.modelIds ?: return null
        val meshes = Array(ids.size) { mesh(ids[it]) ?: return null }
        val mesh = if (meshes.size == 1) meshes[0] else Mesh(arrayOf(*meshes), meshes.size)
        recolour(mesh, kit.originalColours, kit.modifiedColours, kit.originalTextureColours, kit.modifiedTextureColours)
        return mesh
    }

    /** Class34.method343 */
    private fun kitHeadMesh(kit: IdentityKitDefinition): Mesh? {
        val meshes = arrayOfNulls<Mesh>(kit.headModels.size)
        var count = 0
        for (id in kit.headModels) {
            if (id != -1) meshes[count++] = mesh(id) ?: return null
        }
        val mesh = Mesh(meshes, count)
        recolour(mesh, kit.originalColours, kit.modifiedColours, kit.originalTextureColours, kit.modifiedTextureColours)
        return mesh
    }

    private fun recolour(mesh: Mesh, originalColours: ShortArray?, modifiedColours: ShortArray?, originalTextures: ShortArray?, modifiedTextures: ShortArray?) {
        if (originalColours != null && modifiedColours != null) {
            for (i in originalColours.indices) mesh.recolour(originalColours[i], modifiedColours[i])
        }
        if (originalTextures != null && modifiedTextures != null) {
            for (i in originalTextures.indices) mesh.retexture(originalTextures[i], modifiedTextures[i])
        }
    }

    /** Class300.method2277 plus the version < 13 upscale every caller applies. */
    private fun mesh(id: Int): Mesh? {
        // Dialogue head ids are decoded as signed shorts
        val data = cache.data(Index.MODELS, id and 0xffff, 0) ?: return null
        val mesh = Mesh(data)
        if (mesh.version < 13) mesh.upscale(2)
        return mesh
    }

    /**
     * Mirrors BodyParts.update() (minus the HairMid/HairLow overrides) but returns an item id or'd with [ITEM]
     * or an identity kit id or'd with [KIT] instead of the appearance packet value.
     */
    private fun value(part: BodyPart, snapshot: PhotoSnapshot): Int {
        val itemId = itemId(part.slot, snapshot)
        val type = if (itemId != null) itemType(itemId) else EquipType.None
        return when {
            itemId != null && showItem(part, type) -> itemId or ITEM
            showBodyPart(part, type, snapshot) -> snapshot.looks[part.index] or KIT
            showDefault(part, snapshot) -> defaultLook(snapshot.male, part.index) or KIT
            else -> 0
        }
    }

    private fun itemId(slot: EquipSlot, snapshot: PhotoSnapshot): Int? {
        if (slot == EquipSlot.None) return null
        val equipIndex = snapshot.equipment.getOrElse(slot.index) { -1 }
        if (equipIndex < 0) return null
        return equipIndexToItemId[equipIndex]
    }

    private fun showItem(part: BodyPart, type: EquipType): Boolean = when (part) {
        BodyPart.Hair, BodyPart.Beard -> false
        BodyPart.Arms -> type != EquipType.Sleeveless
        else -> true
    }

    private fun showBodyPart(part: BodyPart, type: EquipType, snapshot: PhotoSnapshot): Boolean = part.index != -1 &&
        snapshot.looks.getOrElse(part.index) { -1 } >= 0 &&
        when (part) {
            BodyPart.Hair -> type != EquipType.FullFace && type != EquipType.Hair
            BodyPart.Beard -> type != EquipType.FullFace && type != EquipType.Mask
            else -> true
        }

    private fun showDefault(part: BodyPart, snapshot: PhotoSnapshot): Boolean {
        if (part != BodyPart.Arms) return false
        val chest = itemId(BodyPart.Chest.slot, snapshot) ?: return false
        if (itemType(chest) != EquipType.Sleeveless) return false
        return snapshot.looks.getOrElse(part.index) { 0 } < 0
    }

    private fun defaultLook(male: Boolean, index: Int): Int = (if (male) DEFAULT_LOOK_MALE else DEFAULT_LOOK_FEMALE).getOrElse(index) { 0 }

    companion object {
        private const val SLOTS = 12
        private const val ITEM = 0x40000000
        private const val KIT = 0x20000000

        // Mirrors BodyParts.DEFAULT_LOOK_*.
        private val DEFAULT_LOOK_MALE = intArrayOf(5, 14, 18, 26, 34, 38, 42)
        private val DEFAULT_LOOK_FEMALE = intArrayOf(45, -1, 58, 61, 68, 72, 80)
    }
}
