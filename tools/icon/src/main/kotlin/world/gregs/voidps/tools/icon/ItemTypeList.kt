package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.tools.render.Class60
import world.gregs.voidps.tools.render.Packet
import world.gregs.voidps.tools.render.Sprite
import world.gregs.voidps.tools.render.Toolkit

/* Class255 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class ItemTypeList(languageId: Int, cache: Cache?) {
    val cache: Cache?
    var num: Int = 0
    private val recentUse = Class60(64)
    var languageId: Int = 0

    fun sprite(scratchToolkit: Toolkit?, graphicShadow: Int, invCount: Int, realToolkit: Toolkit?, itemId: Int, bool_5_: Boolean, outline: Int): Sprite? {
        var itemType = list(itemId)
        if (invCount > 1 && itemType.stackIds != null) {
            var stackId = -1
            for (i_8_ in 0..9) {
                if ((itemType.stackAmounts!![i_8_] <= invCount) && itemType.stackAmounts!![i_8_] != 0) stackId = itemType.stackIds!![i_8_]
            }
            if (stackId != -1) itemType = list(stackId)
        }
        val image = itemType.sprite(invCount, bool_5_, graphicShadow, realToolkit, scratchToolkit, outline)
        if (image == null) return null
        return scratchToolkit!!.createSprite(36, image, 36, 32)
    }

    fun list(id: Int): ItemType {
        var type: ItemType?
        synchronized(recentUse) {
            type = recentUse.method583(id.toLong()) as ItemType?
        }
        if (type != null) return type
        val data: ByteArray?
        synchronized(cache!!) {
            data = cache.data(Index.ITEMS, id ushr 8, id and 0xff)
        }
        type = ItemType()
        type.list = this
        type.id = id
        type.op = arrayOf<String?>(null, null, LocalisedText.TAKE!!.method2063(this.languageId), null, null)
        type.iop = (arrayOf<String?>(null, null, null, null, LocalisedText.DROP!!.method2063(this.languageId)))
        if (data != null) type.decode(Packet(data))
        if (type.notedTemplateId != -1) type.noteGen(list(type.anInt2758), list(type.notedTemplateId))
        if (type.lendTemplateId != -1) type.lendGen(list(type.anInt2778), list(type.lendTemplateId))
        synchronized(recentUse) {
            recentUse.method582(type, id.toLong())
        }
        return type
    }

    init {
        this.languageId = languageId
        this.cache = cache
        if (this.cache != null) {
            val lastGroup = this.cache.lastArchiveId(Index.ITEMS)
            this.num = this.cache.lastFileId(Index.ITEMS, lastGroup) + 1 + lastGroup * 256
        } else this.num = 0
    }
}
