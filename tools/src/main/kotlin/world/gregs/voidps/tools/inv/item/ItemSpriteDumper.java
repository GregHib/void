package world.gregs.voidps.tools.inv.item;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Debug utility: renders every item's inventory icon (the same 36x32 raster
 * produced for bank/inventory/shop interfaces) and writes each one out as a
 * standalone PNG. Intended to be invoked once, after item definitions and
 * the model archive are loaded.
 */
final class ItemSpriteDumper {

    private static final int WIDTH = 36;
    private static final int HEIGHT = 32;

    /** Toggle the black selection outline that item icons are normally drawn with. */
    private static final boolean OUTLINE = true;

    /**
     * Ids outside the normal item definition range that still need dumping:
     * 799 is the "note" background/overlay, 13009 the "lent" item background.
     */
    private static final int[] EXTRA_IDS = {799, 13009};

    /**
     * Class348_Sub8.aHa6654 is constructed once very early (during the loading
     * progress bar, before Class348_Sub40_Sub4.aD9113 is set) and never rebuilt,
     * so it permanently carries a null material/texture provider - any item
     * icon whose model references a material (e.g. item 799) NPEs inside
     * Class64_Sub1 when drawn with it. Build our own small off-screen renderer
     * instead, the same way Class22.method294's icon-queue does, so it picks up
     * the real (by-then-initialised) texture provider.
     */
    private static Toolkit renderer;

    private static Toolkit renderer() {
        if (renderer == null) {
            Canvas canvas = new Canvas();
            canvas.setSize(WIDTH, HEIGHT);
            renderer = Class104.method958(true, 0, Class348_Sub40_Sub4.aD9113, 0, canvas, Class136.aJs5_4796);
        }
        return renderer;
    }

    static void dump() {
        dump(new File("item_sprites"));
    }

    static void dump(File dir) {
        dir.mkdirs();
        int count = Exception_Sub1.itemTypeList.num;
        int dumped = 0;
        for (int id = 0; id < count; id++) {
            if (dumpItem(dir, id)) dumped++;
        }
        for (int id : EXTRA_IDS) {
            if (id >= count && dumpItem(dir, id)) dumped++;
        }
        System.out.println("ItemSpriteDumper: wrote " + dumped + " item icons to " + dir.getAbsolutePath());
    }

    private static boolean dumpItem(File dir, int id) {
        try {
            ItemType def = Exception_Sub1.itemTypeList.list(-104, id);
            Toolkit toolkit = renderer();
            int[] pixels = def.sprite(1, false, 0, toolkit, toolkit, null, null, 0, (byte) -102, OUTLINE ? 1 : 0);
            if (pixels == null) return false;
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
            ImageIO.write(image, "png", new File(dir, id + "_" + sanitize(def.name) + ".png"));
            return true;
        } catch (IOException ioexception) {
            return false;
        } catch (RuntimeException runtimeexception) {
            return false;
        }
    }

    private static String sanitize(String name) {
        if (name == null || name.length() == 0) return "unnamed";
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private ItemSpriteDumper() {
        /* empty */
    }
}
