package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

internal data class InterfaceComponentDefinitionFull(
    override var id: Int = -1,
    internal var type: Int = 0,
    internal var unknown: String? = null,
    internal var contentType: Int = 0,
    internal var basePositionX: Int = 0,
    internal var basePositionY: Int = 0,
    internal var baseWidth: Int = 0,
    internal var baseHeight: Int = 0,
    internal var horizontalSizeMode: Byte = 0,
    internal var verticalSizeMode: Byte = 0,
    internal var horizontalPositionMode: Byte = 0,
    internal var verticalPositionMode: Byte = 0,
    internal var parent: Int = -1,
    internal var hidden: Boolean = false,
    internal var disableHover: Boolean = false,
    internal var scrollWidth: Int = 0,
    internal var scrollHeight: Int = 0,
    internal var colour: Int = 0,
    internal var filled: Boolean = false,
    internal var alpha: Int = 0,
    internal var fontId: Int = -1,
    internal var monochrome: Boolean = true,
    internal var text: String = "",
    internal var lineHeight: Int = 0,
    internal var horizontalTextAlign: Int = 0,
    internal var verticalTextAlign: Int = 0,
    internal var shaded: Boolean = false,
    internal var lineCount: Int = 0,
    internal var defaultImage: Int = -1,
    internal var imageRotation: Int = 0,
    internal var aBoolean4861: Boolean = false,
    internal var imageRepeat: Boolean = false,
    internal var rotation: Int = 0,
    internal var backgroundColour: Int = 0,
    internal var flipVertical: Boolean = false,
    internal var flipHorizontal: Boolean = false,
    internal var aBoolean4782: Boolean = true,
    internal var defaultMediaType: Int = 1,
    internal var defaultMediaId: Int = 0,
    internal var animated: Boolean = false,
    internal var centreType: Boolean = false,
    internal var ignoreZBuffer: Boolean = false,
    internal var viewportX: Int = 0,
    internal var viewportY: Int = 0,
    internal var viewportZ: Int = 0,
    internal var spritePitch: Int = 0,
    internal var spriteRoll: Int = 0,
    internal var spriteYaw: Int = 0,
    internal var spriteScale: Int = 100,
    internal var animation: Int = -1,
    internal var viewportWidth: Int = 0,
    internal var viewportHeight: Int = 0,
    internal var lineWidth: Int = 1,
    internal var lineMirrored: Boolean = false,
    internal var keyRepeat: ByteArray? = null,
    internal var keyCodes: ByteArray? = null,
    internal var keyModifiers: IntArray? = null,
    internal var clickable: Boolean = false,
    internal var name: String = "",
    var options: Array<String>? = null,
    internal var mouseIcon: IntArray? = null,
    internal var optionOverride: String? = null,
    internal var anInt4708: Int = 0,// Drag type
    internal var anInt4795: Int = 0,// Drag slider?
    internal var anInt4860: Int = 0,// Friends list icons/buttons?
    internal var useOption: String = "",
    internal var anInt4698: Int = -1,
    internal var anInt4839: Int = -1,// Unused
    internal var anInt4761: Int = -1,// Unused
    internal var setting: InterfaceComponentSetting = InterfaceComponentSetting(0, -1),
    internal val params: HashMap<Long, Any>? = null,
    var anObjectArray4758: Array<Any>? = null,
    internal var mouseEnterHandler: Array<Any>? = null,
    internal var mouseExitHandler: Array<Any>? = null,
    internal var anObjectArray4771: Array<Any>? = null,
    internal var anObjectArray4768: Array<Any>? = null,
    internal var stateChangeHandler: Array<Any>? = null,
    internal var invUpdateHandler: Array<Any>? = null,
    internal var refreshHandler: Array<Any>? = null,
    internal var updateHandler: Array<Any>? = null,
    internal var anObjectArray4770: Array<Any>? = null,
    internal var anObjectArray4751: Array<Any>? = null,
    internal var mouseMotionHandler: Array<Any>? = null,
    internal var mousePressedHandler: Array<Any>? = null,
    internal var mouseDraggedHandler: Array<Any>? = null,
    internal var mouseReleasedHandler: Array<Any>? = null,
    internal var mouseDragPassHandler: Array<Any>? = null,
    internal var anObjectArray4852: Array<Any>? = null,
    internal var anObjectArray4711: Array<Any>? = null,
    internal var anObjectArray4753: Array<Any>? = null,
    internal var anObjectArray4688: Array<Any>? = null,
    internal var anObjectArray4775: Array<Any>? = null,
    internal var clientVarp: IntArray? = null,
    internal var containers: IntArray? = null,
    internal var anIntArray4789: IntArray? = null,
    internal var clientVarc: IntArray? = null,
    internal var anIntArray4805: IntArray? = null,
    internal var hasScript: Boolean = false,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Definition, Extra {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceComponentDefinitionFull

        if (id != other.id) return false
        if (type != other.type) return false
        if (unknown != other.unknown) return false
        if (contentType != other.contentType) return false
        if (basePositionX != other.basePositionX) return false
        if (basePositionY != other.basePositionY) return false
        if (baseWidth != other.baseWidth) return false
        if (baseHeight != other.baseHeight) return false
        if (horizontalSizeMode != other.horizontalSizeMode) return false
        if (verticalSizeMode != other.verticalSizeMode) return false
        if (horizontalPositionMode != other.horizontalPositionMode) return false
        if (verticalPositionMode != other.verticalPositionMode) return false
        if (parent != other.parent) return false
        if (hidden != other.hidden) return false
        if (disableHover != other.disableHover) return false
        if (scrollWidth != other.scrollWidth) return false
        if (scrollHeight != other.scrollHeight) return false
        if (colour != other.colour) return false
        if (filled != other.filled) return false
        if (alpha != other.alpha) return false
        if (fontId != other.fontId) return false
        if (monochrome != other.monochrome) return false
        if (text != other.text) return false
        if (lineHeight != other.lineHeight) return false
        if (horizontalTextAlign != other.horizontalTextAlign) return false
        if (verticalTextAlign != other.verticalTextAlign) return false
        if (shaded != other.shaded) return false
        if (lineCount != other.lineCount) return false
        if (defaultImage != other.defaultImage) return false
        if (imageRotation != other.imageRotation) return false
        if (aBoolean4861 != other.aBoolean4861) return false
        if (imageRepeat != other.imageRepeat) return false
        if (rotation != other.rotation) return false
        if (backgroundColour != other.backgroundColour) return false
        if (flipVertical != other.flipVertical) return false
        if (flipHorizontal != other.flipHorizontal) return false
        if (aBoolean4782 != other.aBoolean4782) return false
        if (defaultMediaType != other.defaultMediaType) return false
        if (defaultMediaId != other.defaultMediaId) return false
        if (animated != other.animated) return false
        if (centreType != other.centreType) return false
        if (ignoreZBuffer != other.ignoreZBuffer) return false
        if (viewportX != other.viewportX) return false
        if (viewportY != other.viewportY) return false
        if (viewportZ != other.viewportZ) return false
        if (spritePitch != other.spritePitch) return false
        if (spriteRoll != other.spriteRoll) return false
        if (spriteYaw != other.spriteYaw) return false
        if (spriteScale != other.spriteScale) return false
        if (animation != other.animation) return false
        if (viewportWidth != other.viewportWidth) return false
        if (viewportHeight != other.viewportHeight) return false
        if (lineWidth != other.lineWidth) return false
        if (lineMirrored != other.lineMirrored) return false
        if (keyRepeat != null) {
            if (other.keyRepeat == null) return false
            if (!keyRepeat.contentEquals(other.keyRepeat)) return false
        } else if (other.keyRepeat != null) return false
        if (keyCodes != null) {
            if (other.keyCodes == null) return false
            if (!keyCodes.contentEquals(other.keyCodes)) return false
        } else if (other.keyCodes != null) return false
        if (keyModifiers != null) {
            if (other.keyModifiers == null) return false
            if (!keyModifiers.contentEquals(other.keyModifiers)) return false
        } else if (other.keyModifiers != null) return false
        if (clickable != other.clickable) return false
        if (name != other.name) return false
        if (options != null) {
            if (other.options == null) return false
            if (!options.contentEquals(other.options)) return false
        } else if (other.options != null) return false
        if (mouseIcon != null) {
            if (other.mouseIcon == null) return false
            if (!mouseIcon.contentEquals(other.mouseIcon)) return false
        } else if (other.mouseIcon != null) return false
        if (optionOverride != other.optionOverride) return false
        if (anInt4708 != other.anInt4708) return false
        if (anInt4795 != other.anInt4795) return false
        if (anInt4860 != other.anInt4860) return false
        if (useOption != other.useOption) return false
        if (anInt4698 != other.anInt4698) return false
        if (anInt4839 != other.anInt4839) return false
        if (anInt4761 != other.anInt4761) return false
        if (setting != other.setting) return false
        if (params != other.params) return false
        if (anObjectArray4758 != null) {
            if (other.anObjectArray4758 == null) return false
            if (!anObjectArray4758.contentEquals(other.anObjectArray4758)) return false
        } else if (other.anObjectArray4758 != null) return false
        if (mouseEnterHandler != null) {
            if (other.mouseEnterHandler == null) return false
            if (!mouseEnterHandler.contentEquals(other.mouseEnterHandler)) return false
        } else if (other.mouseEnterHandler != null) return false
        if (mouseExitHandler != null) {
            if (other.mouseExitHandler == null) return false
            if (!mouseExitHandler.contentEquals(other.mouseExitHandler)) return false
        } else if (other.mouseExitHandler != null) return false
        if (anObjectArray4771 != null) {
            if (other.anObjectArray4771 == null) return false
            if (!anObjectArray4771.contentEquals(other.anObjectArray4771)) return false
        } else if (other.anObjectArray4771 != null) return false
        if (anObjectArray4768 != null) {
            if (other.anObjectArray4768 == null) return false
            if (!anObjectArray4768.contentEquals(other.anObjectArray4768)) return false
        } else if (other.anObjectArray4768 != null) return false
        if (stateChangeHandler != null) {
            if (other.stateChangeHandler == null) return false
            if (!stateChangeHandler.contentEquals(other.stateChangeHandler)) return false
        } else if (other.stateChangeHandler != null) return false
        if (invUpdateHandler != null) {
            if (other.invUpdateHandler == null) return false
            if (!invUpdateHandler.contentEquals(other.invUpdateHandler)) return false
        } else if (other.invUpdateHandler != null) return false
        if (refreshHandler != null) {
            if (other.refreshHandler == null) return false
            if (!refreshHandler.contentEquals(other.refreshHandler)) return false
        } else if (other.refreshHandler != null) return false
        if (updateHandler != null) {
            if (other.updateHandler == null) return false
            if (!updateHandler.contentEquals(other.updateHandler)) return false
        } else if (other.updateHandler != null) return false
        if (anObjectArray4770 != null) {
            if (other.anObjectArray4770 == null) return false
            if (!anObjectArray4770.contentEquals(other.anObjectArray4770)) return false
        } else if (other.anObjectArray4770 != null) return false
        if (anObjectArray4751 != null) {
            if (other.anObjectArray4751 == null) return false
            if (!anObjectArray4751.contentEquals(other.anObjectArray4751)) return false
        } else if (other.anObjectArray4751 != null) return false
        if (mouseMotionHandler != null) {
            if (other.mouseMotionHandler == null) return false
            if (!mouseMotionHandler.contentEquals(other.mouseMotionHandler)) return false
        } else if (other.mouseMotionHandler != null) return false
        if (mousePressedHandler != null) {
            if (other.mousePressedHandler == null) return false
            if (!mousePressedHandler.contentEquals(other.mousePressedHandler)) return false
        } else if (other.mousePressedHandler != null) return false
        if (mouseDraggedHandler != null) {
            if (other.mouseDraggedHandler == null) return false
            if (!mouseDraggedHandler.contentEquals(other.mouseDraggedHandler)) return false
        } else if (other.mouseDraggedHandler != null) return false
        if (mouseReleasedHandler != null) {
            if (other.mouseReleasedHandler == null) return false
            if (!mouseReleasedHandler.contentEquals(other.mouseReleasedHandler)) return false
        } else if (other.mouseReleasedHandler != null) return false
        if (mouseDragPassHandler != null) {
            if (other.mouseDragPassHandler == null) return false
            if (!mouseDragPassHandler.contentEquals(other.mouseDragPassHandler)) return false
        } else if (other.mouseDragPassHandler != null) return false
        if (anObjectArray4852 != null) {
            if (other.anObjectArray4852 == null) return false
            if (!anObjectArray4852.contentEquals(other.anObjectArray4852)) return false
        } else if (other.anObjectArray4852 != null) return false
        if (anObjectArray4711 != null) {
            if (other.anObjectArray4711 == null) return false
            if (!anObjectArray4711.contentEquals(other.anObjectArray4711)) return false
        } else if (other.anObjectArray4711 != null) return false
        if (anObjectArray4753 != null) {
            if (other.anObjectArray4753 == null) return false
            if (!anObjectArray4753.contentEquals(other.anObjectArray4753)) return false
        } else if (other.anObjectArray4753 != null) return false
        if (anObjectArray4688 != null) {
            if (other.anObjectArray4688 == null) return false
            if (!anObjectArray4688.contentEquals(other.anObjectArray4688)) return false
        } else if (other.anObjectArray4688 != null) return false
        if (anObjectArray4775 != null) {
            if (other.anObjectArray4775 == null) return false
            if (!anObjectArray4775.contentEquals(other.anObjectArray4775)) return false
        } else if (other.anObjectArray4775 != null) return false
        if (clientVarp != null) {
            if (other.clientVarp == null) return false
            if (!clientVarp.contentEquals(other.clientVarp)) return false
        } else if (other.clientVarp != null) return false
        if (containers != null) {
            if (other.containers == null) return false
            if (!containers.contentEquals(other.containers)) return false
        } else if (other.containers != null) return false
        if (anIntArray4789 != null) {
            if (other.anIntArray4789 == null) return false
            if (!anIntArray4789.contentEquals(other.anIntArray4789)) return false
        } else if (other.anIntArray4789 != null) return false
        if (clientVarc != null) {
            if (other.clientVarc == null) return false
            if (!clientVarc.contentEquals(other.clientVarc)) return false
        } else if (other.clientVarc != null) return false
        if (anIntArray4805 != null) {
            if (other.anIntArray4805 == null) return false
            if (!anIntArray4805.contentEquals(other.anIntArray4805)) return false
        } else if (other.anIntArray4805 != null) return false
        if (hasScript != other.hasScript) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + type
        result = 31 * result + (unknown?.hashCode() ?: 0)
        result = 31 * result + contentType
        result = 31 * result + basePositionX
        result = 31 * result + basePositionY
        result = 31 * result + baseWidth
        result = 31 * result + baseHeight
        result = 31 * result + horizontalSizeMode
        result = 31 * result + verticalSizeMode
        result = 31 * result + horizontalPositionMode
        result = 31 * result + verticalPositionMode
        result = 31 * result + parent
        result = 31 * result + hidden.hashCode()
        result = 31 * result + disableHover.hashCode()
        result = 31 * result + scrollWidth
        result = 31 * result + scrollHeight
        result = 31 * result + colour
        result = 31 * result + filled.hashCode()
        result = 31 * result + alpha
        result = 31 * result + fontId
        result = 31 * result + monochrome.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + lineHeight
        result = 31 * result + horizontalTextAlign
        result = 31 * result + verticalTextAlign
        result = 31 * result + shaded.hashCode()
        result = 31 * result + lineCount
        result = 31 * result + defaultImage
        result = 31 * result + imageRotation
        result = 31 * result + aBoolean4861.hashCode()
        result = 31 * result + imageRepeat.hashCode()
        result = 31 * result + rotation
        result = 31 * result + backgroundColour
        result = 31 * result + flipVertical.hashCode()
        result = 31 * result + flipHorizontal.hashCode()
        result = 31 * result + aBoolean4782.hashCode()
        result = 31 * result + defaultMediaType
        result = 31 * result + defaultMediaId
        result = 31 * result + animated.hashCode()
        result = 31 * result + centreType.hashCode()
        result = 31 * result + ignoreZBuffer.hashCode()
        result = 31 * result + viewportX
        result = 31 * result + viewportY
        result = 31 * result + viewportZ
        result = 31 * result + spritePitch
        result = 31 * result + spriteRoll
        result = 31 * result + spriteYaw
        result = 31 * result + spriteScale
        result = 31 * result + animation
        result = 31 * result + viewportWidth
        result = 31 * result + viewportHeight
        result = 31 * result + lineWidth
        result = 31 * result + lineMirrored.hashCode()
        result = 31 * result + (keyRepeat?.contentHashCode() ?: 0)
        result = 31 * result + (keyCodes?.contentHashCode() ?: 0)
        result = 31 * result + (keyModifiers?.contentHashCode() ?: 0)
        result = 31 * result + clickable.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (options?.contentHashCode() ?: 0)
        result = 31 * result + (mouseIcon?.contentHashCode() ?: 0)
        result = 31 * result + (optionOverride?.hashCode() ?: 0)
        result = 31 * result + anInt4708
        result = 31 * result + anInt4795
        result = 31 * result + anInt4860
        result = 31 * result + useOption.hashCode()
        result = 31 * result + anInt4698
        result = 31 * result + anInt4839
        result = 31 * result + anInt4761
        result = 31 * result + setting.hashCode()
        result = 31 * result + (params?.hashCode() ?: 0)
        result = 31 * result + (anObjectArray4758?.contentHashCode() ?: 0)
        result = 31 * result + (mouseEnterHandler?.contentHashCode() ?: 0)
        result = 31 * result + (mouseExitHandler?.contentHashCode() ?: 0)
        result = 31 * result + (anObjectArray4771?.contentHashCode() ?: 0)
        result = 31 * result + (anObjectArray4768?.contentHashCode() ?: 0)
        result = 31 * result + (stateChangeHandler?.contentHashCode() ?: 0)
        result = 31 * result + (invUpdateHandler?.contentHashCode() ?: 0)
        result = 31 * result + (refreshHandler?.contentHashCode() ?: 0)
        result = 31 * result + (updateHandler?.contentHashCode() ?: 0)
        result = 31 * result + (anObjectArray4770?.contentHashCode() ?: 0)
        result = 31 * result + (anObjectArray4751?.contentHashCode() ?: 0)
        result = 31 * result + (mouseMotionHandler?.contentHashCode() ?: 0)
        result = 31 * result + (mousePressedHandler?.contentHashCode() ?: 0)
        result = 31 * result + (mouseDraggedHandler?.contentHashCode() ?: 0)
        result = 31 * result + (mouseReleasedHandler?.contentHashCode() ?: 0)
        result = 31 * result + (mouseDragPassHandler?.contentHashCode() ?: 0)
        result = 31 * result + (anObjectArray4852?.contentHashCode() ?: 0)
        result = 31 * result + (anObjectArray4711?.contentHashCode() ?: 0)
        result = 31 * result + (anObjectArray4753?.contentHashCode() ?: 0)
        result = 31 * result + (anObjectArray4688?.contentHashCode() ?: 0)
        result = 31 * result + (anObjectArray4775?.contentHashCode() ?: 0)
        result = 31 * result + (clientVarp?.contentHashCode() ?: 0)
        result = 31 * result + (containers?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray4789?.contentHashCode() ?: 0)
        result = 31 * result + (clientVarc?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray4805?.contentHashCode() ?: 0)
        result = 31 * result + hasScript.hashCode()
        result = 31 * result + stringId.hashCode()
        result = 31 * result + extras.hashCode()
        return result
    }
}