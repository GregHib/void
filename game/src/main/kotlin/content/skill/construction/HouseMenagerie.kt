package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Menagerie furniture: pet houses to store pets, feeders and the small obelisk for renewing summoning points
 */
class HouseMenagerie : Script {
    init {
        objectOperate("Store", PET_HOUSES) {
            notImplemented()
        }

        objectOperate("Feed-all", PET_FEEDERS) {
            notImplemented()
        }

        objectOperate("Upgrade", "$PET_HOUSES,oak_pet_feeder,teak_pet_feeder") {
            notImplemented()
        }

        objectOperate("Renew-points", "house_small_obelisk") {
            notImplemented()
        }
    }

    companion object {
        private const val PET_HOUSES = "oak_pet_house,teak_pet_house,mahogany_pet_house,consecrated_pet_house,desecrated_pet_house,natural_pet_house"
        private const val PET_FEEDERS = "oak_pet_feeder,teak_pet_feeder,mahogany_pet_feeder"
    }
}
