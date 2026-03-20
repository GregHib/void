package content.area.fremennik_province.lunar_isle

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open

class AstralAltar : Script {
    init {
        objectOperate("Pray", "astral_altar") {
            anim("altar_pray")
            if (interfaces.contains("lunar_spellbook")) {
                open("modern_spellbook")
                message("Lunar spells deactivated!")
            } else {
                open("lunar_spellbook")
                message("Lunar spells activated!")
            }
        }
    }
}
