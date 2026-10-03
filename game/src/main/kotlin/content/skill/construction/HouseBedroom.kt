package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Bedroom furniture: wardrobes and drawers to change clothes and dressers to change hairstyles
 */
class HouseBedroom : Script {
    init {
        objectOperate("Change-clothes", WARDROBES) {
            notImplemented()
        }

        objectOperate("Preen", DRESSERS) {
            notImplemented()
        }
    }

    companion object {
        private const val WARDROBES = "shoe_box,oak_drawers,oak_wardrobe,teak_drawers,teak_wardrobe,mahogany_wardrobe,gilded_wardrobe"
        private const val DRESSERS = "shaving_stand,oak_shaving_stand,oak_dresser,teak_dresser,fancy_teak_dresser,mahogany_dresser,gilded_dresser"
    }
}
