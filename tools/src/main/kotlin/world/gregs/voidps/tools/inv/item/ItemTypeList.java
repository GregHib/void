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
    Class175 aClass175_3288 = new Class175(250);
    private ItemSpriteCacheKey aItemSpriteCacheKey_3289 = new ItemSpriteCacheKey();
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
    final Sprite sprite(Toolkit scratchToolkit, int i, int i_0_, Font font, PlayerModel playerModel, int i_1_, boolean temp, byte i_2_, Toolkit realToolkit, int i_4_, boolean bool_5_, int i_6_) {
        try {
            anInt3266++;
            Sprite cachedSprite = getCachedSprite(i_6_, (byte) -74, i_4_, i_0_, i, i_1_, realToolkit, playerModel);
            if (cachedSprite != null) return cachedSprite;
            ItemType itemType = list(90, i_4_);
            if (i_0_ > 1 && itemType.stackIds != null) {
                int stackId = -1;
                for (int i_8_ = 0; i_8_ < 10; i_8_++) {
                    if ((itemType.stackAmounts[i_8_] <= i_0_) && itemType.stackAmounts[i_8_] != 0) stackId = itemType.stackIds[i_8_];
                }
                if (stackId != -1) itemType = list(127, stackId);
            }
            int[] image = itemType.sprite(i_0_, bool_5_, i, realToolkit, scratchToolkit, font, playerModel, i_1_, (byte) -102, i_6_);
            if (image == null) return null;
            Sprite sprite;
            if (temp) sprite = scratchToolkit.createSprite(36, image, (byte) 94, 0, 36, 32);
            else sprite = realToolkit.createSprite(36, image, (byte) 94, 0, 36, 32);
            if (!temp) {
                ItemSpriteCacheKey cacheKey = new ItemSpriteCacheKey();
                cacheKey.anInt4982 = i;
                cacheKey.itemWearCol = playerModel != null;
                cacheKey.anInt4989 = i_0_;
                cacheKey.anInt4992 = i_4_;
                cacheKey.anInt4981 = i_1_;
                cacheKey.toolkitIndex = realToolkit.index;
                cacheKey.anInt4993 = i_6_;
                /* this.aClass175_3288.method1348(70, class105_9_, class126);
                 * unreachable per JaCoCo coverage - both real call sites
                 * pass bool == true, so "if (!bool)" never executes. Not
                 * calling into Class175 here (owned by a parallel agent,
                 * off limits; its trimmed genuine-methods list lacks
                 * method1348 already). */
            }
            return sprite;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.C(" + (scratchToolkit != null ? "{...}" : "null") + ',' + i + ',' + i_0_ + ',' + (font != null ? "{...}" : "null") + ',' + (playerModel != null ? "{...}" : "null") + ',' + i_1_ + ',' + temp + ',' + i_2_ + ',' + (realToolkit != null ? "{...}" : "null") + ',' + i_4_ + ',' + bool_5_ + ',' + i_6_ + ')'));
        }
    }

    final ItemType list(int i, int i_13_) {
        anInt3283++;
        ItemType type;
        synchronized (recentUse) {
            type = (ItemType) recentUse.method583(i_13_, 90);
        }
        if (type != null) return type;
        byte[] data;
        synchronized (index) {
            data = index.getFile(-1860, Class54.method500(7, i_13_), Class251.method1914(-23590, i_13_));
        }
        type = new ItemType();
        type.list = this;
        type.id = i_13_;
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
                    if (class254.aBoolean3261) class348.method2715((byte) 60);
                    else bool = true;
                }
                if (!bool) type.params = null;
            }
        }
        synchronized (recentUse) {
            recentUse.method582(type, i_13_, (byte) -118);
        }
        return type;
    }

    final Sprite getCachedSprite(int i, byte i_15_, int i_16_, int i_17_, int i_18_, int i_19_, Toolkit var_toolkit, PlayerModel playerModel) {
        try {
            aItemSpriteCacheKey_3289.anInt4992 = i_16_;
            aItemSpriteCacheKey_3289.anInt4989 = i_17_;
            aItemSpriteCacheKey_3289.toolkitIndex = var_toolkit.index;
            aItemSpriteCacheKey_3289.anInt4981 = i_19_;
            if (i_15_ != -74) aItemSpriteCacheKey_3289 = null;
            aItemSpriteCacheKey_3289.itemWearCol = playerModel != null;
            aItemSpriteCacheKey_3289.anInt4982 = i_18_;
            anInt3282++;
            aItemSpriteCacheKey_3289.anInt4993 = i;
            return (Sprite) this.aClass175_3288.method1340(123, aItemSpriteCacheKey_3289);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ub.D(" + i + ',' + i_15_ + ',' + i_16_ + ',' + i_17_ + ',' + i_18_ + ',' + i_19_ + ',' + (var_toolkit != null ? "{...}" : "null") + ',' + (playerModel != null ? "{...}" : "null") + ')'));
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
