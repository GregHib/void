package world.gregs.voidps.tools.inv.item;/* Class255 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class ItemTypeList {
    private final Js5 index;
    Js5 meshes;
    private boolean allowMembers;
    int num;
    static int anInt3266;
    static int anInt3282;
    static int anInt3283;
    private final Class60 recentUse = new Class60(64);
    int languageId;
    KeyedReferenceCache spriteCache = new KeyedReferenceCache(250);
    private ItemSpriteCacheKey spriteCacheKey = new ItemSpriteCacheKey();
    private final String[] defaultOps;
    private Class326 aClass326_3292;
    private final String[] defaultIops;

    /* NOTE: originally calls method1935(...) behind "if (i_2_ != 83)".
     * method1935 is not in the genuine-methods list, so it is treated as
     * dead code and omitted. Also calls this.aClass175_3288.method1348(...)
     * inside "if (!bool)" - Class175's genuine list lacks method1348, so
     * that branch appears dead too (bool is apparently always true at real
     * call sites) and method1348 was not carried into the trimmed Class175.
     * Both flagged for a follow-up compile-fix pass. */
    final Sprite sprite(Toolkit scratchToolkit, int graphicShadow, int invCount, Font font, PlayerModel playerModel, int itemNumMode, boolean temp, byte i_2_, Toolkit realToolkit, int itemId, boolean bool_5_, int outline) {
        try {
            anInt3266++;
//            Sprite cachedSprite = getCachedSprite(outline, (byte) -74, itemId, invCount, graphicShadow, itemNumMode, realToolkit, playerModel);
//            if (cachedSprite != null) return cachedSprite;
            ItemType itemType = list(90, itemId);
            if (invCount > 1 && itemType.stackIds != null) {
                int stackId = -1;
                for (int i_8_ = 0; i_8_ < 10; i_8_++) {
                    if ((itemType.stackAmounts[i_8_] <= invCount) && itemType.stackAmounts[i_8_] != 0) stackId = itemType.stackIds[i_8_];
                }
                if (stackId != -1) itemType = list(127, stackId);
            }
            int[] image = itemType.sprite(invCount, bool_5_, graphicShadow, realToolkit, scratchToolkit, font, playerModel, itemNumMode, (byte) -102, outline);
            if (image == null) return null;
            Sprite sprite;
            if (temp) sprite = scratchToolkit.createSprite(36, image, (byte) 94, 0, 36, 32);
            else sprite = realToolkit.createSprite(36, image, (byte) 94, 0, 36, 32);
            /*if (!temp) {
                ItemSpriteCacheKey cacheKey = new ItemSpriteCacheKey();
                cacheKey.graphicShadow = graphicShadow;
                cacheKey.itemWearCol = playerModel != null;
                cacheKey.invCount = invCount;
                cacheKey.itemId = itemId;
                cacheKey.itemNumMode = itemNumMode;
                cacheKey.toolkitIndex = realToolkit.index;
                cacheKey.outline = outline;
                *//* this.aClass175_3288.method1348(70, class105_9_, class126);
                 * unreachable per JaCoCo coverage - both real call sites
                 * pass bool == true, so "if (!bool)" never executes. Not
                 * calling into Class175 here (owned by a parallel agent,
                 * off limits; its trimmed genuine-methods list lacks
                 * method1348 already). *//*
            }*/
            return sprite;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.C(" + (scratchToolkit != null ? "{...}" : "null") + ',' + graphicShadow + ',' + invCount + ',' + (font != null ? "{...}" : "null") + ',' + (playerModel != null ? "{...}" : "null") + ',' + itemNumMode + ',' + temp + ',' + i_2_ + ',' + (realToolkit != null ? "{...}" : "null") + ',' + itemId + ',' + bool_5_ + ',' + outline + ')'));
        }
    }

    final ItemType list(int i, int id) {
        anInt3283++;
        ItemType type;
        synchronized (recentUse) {
            type = (ItemType) recentUse.method583(id, 90);
        }
        if (type != null) return type;
        byte[] data;
        synchronized (index) {
            data = index.getFile(-1860, Class54.method500(7, id), Class251.method1914(-23590, id));
        }
        type = new ItemType();
        type.list = this;
        type.id = id;
        type.op = new String[]{null, null, LocalisedText.TAKE.method2063(this.languageId, 544), null, null};
        type.iop = (new String[]{null, null, null, null, LocalisedText.DROP.method2063(this.languageId, 544)});
        if (data != null) type.decode(768, new Packet(data));
        type.postDecode((byte) 92);
        if (type.notedTemplateId != -1) type.noteGen(1, list(90, type.anInt2758), list(101, type.notedTemplateId));
        if (type.lendTemplateId != -1) type.lendGen(list(-58, type.anInt2778), (byte) -29, list(-82, type.lendTemplateId));
        if (!allowMembers && type.members) {
            type.name = LocalisedText.MEMBERS_OBJECT.method2063(this.languageId, 544);
            type.team = 0;
            type.op = defaultOps;
            type.iop = defaultIops;
            type.grandExchange = false;
            type.quests = null;
            if (type.params != null) {
                boolean bool = false;
                for (Class348 class348 = type.params.method3484(0); class348 != null; class348 = type.params.method3482(0)) {
                    Class254 class254 = aClass326_3292.method2600((int) class348.aLong4291, 28364);
                    if (class254.aBoolean3261) class348.unlink((byte) 60);
                    else bool = true;
                }
                if (!bool) type.params = null;
            }
        }
        synchronized (recentUse) {
            recentUse.method582(type, id, (byte) -118);
        }
        return type;
    }

    final Sprite getCachedSprite(int outline, byte i_15_, int itemId, int invCount, int graphicShadow, int itemNumMode, Toolkit toolkit, PlayerModel playerModel) {
        try {
            spriteCacheKey.itemId = itemId;
            spriteCacheKey.invCount = invCount;
            spriteCacheKey.toolkitIndex = toolkit.index;
            spriteCacheKey.itemNumMode = itemNumMode;
            spriteCacheKey.itemWearCol = playerModel != null;
            spriteCacheKey.graphicShadow = graphicShadow;
            anInt3282++;
            spriteCacheKey.outline = outline;
            return (Sprite) this.spriteCache.get(123, spriteCacheKey);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.D(" + outline + ',' + i_15_ + ',' + itemId + ',' + invCount + ',' + graphicShadow + ',' + itemNumMode + ',' + (toolkit != null ? "{...}" : "null") + ',' + (playerModel != null ? "{...}" : "null") + ')'));
        }
    }

    ItemTypeList(int languageId, boolean members, Class326 class326, Js5 index, Js5 meshes) {
        try {
            allowMembers = members;
            aClass326_3292 = class326;
            this.meshes = meshes;
            this.languageId = languageId;
            this.index = index;
            if (this.index != null) {
                int lastGroup = -1 + this.index.groupSize(-1);
                this.num = this.index.fileLimit(0, lastGroup) + lastGroup * 256;
            } else this.num = 0;
            defaultOps = (new String[]{null, null, LocalisedText.TAKE.method2063(this.languageId, 544), null, null});
            defaultIops = (new String[]{null, null, null, null, LocalisedText.DROP.method2063(this.languageId, 544)});
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.<init>(" + ',' + languageId + ',' + members + ',' + (class326 != null ? "{...}" : "null") + ',' + (index != null ? "{...}" : "null") + ',' + (meshes != null ? "{...}" : "null") + ')'));
        }
    }
}
