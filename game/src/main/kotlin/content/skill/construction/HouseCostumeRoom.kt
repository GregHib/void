package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Costume room storage: cape racks, magic wardrobes, toy boxes, treasure chests, fancy dress boxes and armour cases
 */
class HouseCostumeRoom : Script {
    init {
        objectOperate("Search", CAPE_RACKS) {
            notImplemented()
        }

        objectOperate("Open", "$MAGIC_WARDROBES,$CHESTS") {
            notImplemented()
        }
    }

    companion object {
        private const val CAPE_RACKS = "oak_cape_rack,teak_cape_rack,mahogany_cape_rack,gilded_cape_rack,marble_cape_rack,magic_cape_rack"
        private const val MAGIC_WARDROBES = "oak_magic_wardrobe,carved_oak_magic_wardrobe,teak_magic_wardrobe,carved_teak_magic_wardrobe,mahogany_magic_wardrobe,gilded_magic_wardrobe,marble_magic_wardrobe"
        private const val CHESTS = "oak_toy_box,teak_toy_box,mahogany_toy_box,oak_treasure_chest,teak_treasure_chest,mahogany_treasure_chest," +
            "oak_fancy_dress_box,teak_fancy_dress_box,mahogany_fancy_dress_box,oak_armour_case,teak_armour_case,mahogany_armour_case"
    }
}
