package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Study furniture: lecterns for making tablets, charts, telescopes and the statue plinth,
 * along with bookcases which are also built in parlours and quest halls.
 */
class HouseStudy : Script {
    init {
        objectOperate("Study", "$LECTERNS,alchemical_chart,astronomical_chart,infernal_chart") {
            notImplemented()
        }

        objectOperate("Observe", "wooden_telescope,teak_telescope,mahogany_telescope") {
            notImplemented()
        }

        objectOperate("Check", "dahmaroc_statue_plinth") {
            notImplemented()
        }

        objectOperate("Search", "wooden_bookcase,oak_bookcase,mahogany_bookcase") {
            notImplemented()
        }
    }

    companion object {
        private const val LECTERNS = "oak_lectern,eagle_lectern,demon_lectern,teak_eagle_lectern,teak_demon_lectern,mahogany_eagle_lectern,mahogany_demon_lectern"
    }
}
