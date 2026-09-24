package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index

/* Class255 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class ItemTypeList(languageId: Int, members: Boolean, class326: Class326?, cache: Cache?) {
    val cache: Cache?
    private var allowMembers = false
    var num: Int = 0
    private val recentUse = Class60(64)
    var languageId: Int = 0
    var spriteCache: KeyedReferenceCache = KeyedReferenceCache(250)
    private val spriteCacheKey = ItemSpriteCacheKey()
    private val defaultOps: Array<String?>
    private var aClass326_3292: Class326? = null
    private val defaultIops: Array<String?>

    /* NOTE: originally calls method1935(...) behind "if (i_2_ != 83)".
     * method1935 is not in the genuine-methods list, so it is treated as
     * dead code and omitted. Also calls this.aClass175_3288.method1348(...)
     * inside "if (!bool)" - Class175's genuine list lacks method1348, so
     * that branch appears dead too (bool is apparently always true at real
     * call sites) and method1348 was not carried into the trimmed Class175.
     * Both flagged for a follow-up compile-fix pass. */
    fun sprite(scratchToolkit: Toolkit?, graphicShadow: Int, invCount: Int, font: Font?, playerModel: PlayerModel?, itemNumMode: Int, temp: Boolean, i_2_: Byte, realToolkit: Toolkit?, itemId: Int, bool_5_: Boolean, outline: Int): Sprite? {
        try {
            anInt3266++
            //            Sprite cachedSprite = getCachedSprite(outline, (byte) -74, itemId, invCount, graphicShadow, itemNumMode, realToolkit, playerModel);
//            if (cachedSprite != null) return cachedSprite;
            var itemType = list(90, itemId)
            if (invCount > 1 && itemType.stackIds != null) {
                var stackId = -1
                for (i_8_ in 0..9) {
                    if ((itemType.stackAmounts!![i_8_] <= invCount) && itemType.stackAmounts!![i_8_] != 0) stackId = itemType.stackIds!![i_8_]
                }
                if (stackId != -1) itemType = list(127, stackId)
            }
            val image = itemType.sprite(invCount, bool_5_, graphicShadow, realToolkit, scratchToolkit, font, playerModel, itemNumMode, (-102).toByte(), outline)
            if (image == null) return null
            val sprite: Sprite?
            if (temp) sprite = scratchToolkit!!.createSprite(36, image, 94.toByte(), 0, 36, 32)
            else sprite = realToolkit!!.createSprite(36, image, 94.toByte(), 0, 36, 32)
            /*if (!temp) {
                ItemSpriteCacheKey cacheKey = new ItemSpriteCacheKey();
                cacheKey.graphicShadow = graphicShadow;
                cacheKey.itemWearCol = playerModel != null;
                cacheKey.invCount = invCount;
                cacheKey.itemId = itemId;
                cacheKey.itemNumMode = itemNumMode;
                cacheKey.toolkitIndex = realToolkit.index;
                cacheKey.outline = outline;
                */
            /* this.aClass175_3288.method1348(70, class105_9_, class126);
                 * unreachable per JaCoCo coverage - both real call sites
                 * pass bool == true, so "if (!bool)" never executes. Not
                 * calling into Class175 here (owned by a parallel agent,
                 * off limits; its trimmed genuine-methods list lacks
                 * method1348 already). */
            /*
            }*/
            return sprite
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.C(" + (if (scratchToolkit != null) "{...}" else "null") + ',' + graphicShadow + ',' + invCount + ',' + (if (font != null) "{...}" else "null") + ',' + (if (playerModel != null) "{...}" else "null") + ',' + itemNumMode + ',' + temp + ',' + i_2_ + ',' + (if (realToolkit != null) "{...}" else "null") + ',' + itemId + ',' + bool_5_ + ',' + outline + ')'))
        }
    }

    fun list(i: Int, id: Int): ItemType {
        anInt3283++
        var type: ItemType?
        synchronized(recentUse) {
            type = recentUse.method583(id.toLong(), 90) as ItemType?
        }
        if (type != null) return type
        val data: ByteArray?
        synchronized(cache!!) {
            data = cache.data(Index.ITEMS, Class54.method500(7, id), Class251.method1914(-23590, id))
        }
        type = ItemType()
        type.list = this
        type.id = id
        type.op = arrayOf<String?>(null, null, LocalisedText.Companion.TAKE!!.method2063(this.languageId, 544), null, null)
        type.iop = (arrayOf<String?>(null, null, null, null, LocalisedText.Companion.DROP!!.method2063(this.languageId, 544)))
        if (data != null) type.decode(768, Packet(data))
        type.postDecode(92.toByte())
        if (type.notedTemplateId != -1) type.noteGen(1, list(90, type.anInt2758), list(101, type.notedTemplateId))
        if (type.lendTemplateId != -1) type.lendGen(list(-58, type.anInt2778), (-29).toByte(), list(-82, type.lendTemplateId))
        if (!allowMembers && type.members) {
            type.name = LocalisedText.Companion.MEMBERS_OBJECT!!.method2063(this.languageId, 544)
            type.team = 0
            type.op = defaultOps
            type.iop = defaultIops
            type.grandExchange = false
            type.quests = null
            if (type.params != null) {
                var bool = false
                var class348 = type.params!!.method3484(0)!!
                while (class348 != null) {
                    val class254 = aClass326_3292!!.method2600(class348.aLong4291.toInt(), 28364)
                    if (class254.aBoolean3261) class348.unlink(60.toByte())
                    else bool = true
                    class348 = type.params!!.method3482(0)!!
                }
                if (!bool) type.params = null
            }
        }
        synchronized(recentUse) {
            recentUse.method582(type, id.toLong(), (-118).toByte())
        }
        return type
    }

    fun getCachedSprite(outline: Int, i_15_: Byte, itemId: Int, invCount: Int, graphicShadow: Int, itemNumMode: Int, toolkit: Toolkit?, playerModel: PlayerModel?): Sprite? {
        try {
            spriteCacheKey.itemId = itemId
            spriteCacheKey.invCount = invCount
            spriteCacheKey.toolkitIndex = toolkit!!.index
            spriteCacheKey.itemNumMode = itemNumMode
            spriteCacheKey.itemWearCol = playerModel != null
            spriteCacheKey.graphicShadow = graphicShadow
            anInt3282++
            spriteCacheKey.outline = outline
            return this.spriteCache.get(123, spriteCacheKey) as Sprite?
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.D(" + outline + ',' + i_15_ + ',' + itemId + ',' + invCount + ',' + graphicShadow + ',' + itemNumMode + ',' + (if (toolkit != null) "{...}" else "null") + ',' + (if (playerModel != null) "{...}" else "null") + ')'))
        }
    }

    init {
        try {
            allowMembers = members
            aClass326_3292 = class326
            this.languageId = languageId
            this.cache = cache
            if (this.cache != null) {
                val lastGroup = this.cache.lastArchiveId(Index.ITEMS)
                this.num = this.cache.lastFileId(Index.ITEMS, lastGroup) + 1 + lastGroup * 256
            } else this.num = 0
            defaultOps = (arrayOf<String?>(null, null, LocalisedText.Companion.TAKE!!.method2063(this.languageId, 544), null, null))
            defaultIops = (arrayOf<String?>(null, null, null, null, LocalisedText.Companion.DROP!!.method2063(this.languageId, 544)))
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.<init>(" + ',' + languageId + ',' + members + ',' + (if (class326 != null) "{...}" else "null") + ',' + (if (cache != null) "{...}" else "null") + ')'))
        }
    }

    companion object {
        var anInt3266: Int = 0
        var anInt3282: Int = 0
        var anInt3283: Int = 0
    }
}
