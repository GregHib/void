package world.gregs.voidps.tools.inv.item;

import world.gregs.voidps.cache.Cache;
import world.gregs.voidps.cache.CacheDelegate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Standalone entry point: dumps every item's inventory icon straight from a
 * local cache directory, without booting the applet/login/game-loop machinery.
 * <p>
 * Usage: {@code java CacheItemSpriteDumper <cache directory> [output directory]}
 * <p>
 * &lt;cache directory&gt; must contain the classic flat cache files
 * (main_file_cache.dat2, main_file_cache.idx0 .. idx255). Only the archives
 * actually needed to build and render item icons are loaded: models (7),
 * item definitions (19), and the three archives the material/texture manager
 * needs (materials 26, texture data 9, texture sprites 8), plus fonts (31)
 * since the off-screen renderer expects one.
 */
public final class CacheItemSpriteDumper {

    private static final int IDX_MODELS = 7;
    private static final int IDX_TEXTURE_SPRITES = 8;
    private static final int IDX_TEXTURE_DATA = 9;
    private static final int IDX_ITEMS = 19;
    private static final int IDX_MATERIALS = 26;
    private static final int IDX_FONTS = 31;

    public static void main(String[] args) {
        String cacheDir = args.length > 0 ? args[0] : "C:\\Users\\Greg\\IdeaProjects\\void\\data\\cache\\";

        Cache cache = new CacheDelegate(cacheDir, null, null);
        try {
            System.out.println(Arrays.toString(cache.getVersionTable()));
            System.out.println(Arrays.toString(cache.data(0, 0, 26, null)));
            aa_Sub3.aJs5_5207 = loadArchive(cacheDir, cache, IDX_MODELS, false);
            Class174.aJs5_2306 = loadArchive(cacheDir, cache, IDX_ITEMS, false);
            Class348_Sub11.aJs5_4770 = loadArchive(cacheDir, cache, IDX_MATERIALS, true);
            Class369_Sub2.aJs5_8589 = loadArchive(cacheDir, cache, IDX_TEXTURE_DATA, false);
            Class21.aJs5_322 = loadArchive(cacheDir, cache, IDX_TEXTURE_SPRITES, false);
            Class136.aJs5_4796 = loadArchive(cacheDir, cache, IDX_FONTS, true);

            Class348_Sub40_Sub4.aTextureSource9113 = new Js5TextureSource(Class348_Sub11.aJs5_4770, Class369_Sub2.aJs5_8589, Class21.aJs5_322);
            Exception_Sub1.itemTypeList = new ItemTypeList(0, true, null, Class174.aJs5_2306, aa_Sub3.aJs5_5207);
        } catch (IOException | RuntimeException exception) {
            System.err.println("Failed to load cache from " + cacheDir);
            exception.printStackTrace();
            System.exit(1);
            return;
        }

        File outDir = new File(args.length > 1 ? args[1] : "item_sprites");
        ItemSpriteDumper.dump(outDir);
    }

    private static Js5 loadArchive(String cacheDir, Cache cache, int index, boolean unpackEagerly) throws IOException {
        return new Js5(new DiskFileStore(cacheDir, index, cache), unpackEagerly, 1);
    }

    private CacheItemSpriteDumper() {
        /* empty */
    }
}
