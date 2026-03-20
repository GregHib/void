package content.area.fremennik_province.lunar_isle

import content.skill.prayer.protectMagic
import content.skill.prayer.protectMelee
import content.skill.prayer.protectRange
import world.gregs.voidps.engine.Script

class Suqah : Script {
    init {
        npcCondition("no_protect_melee") { !it.protectMelee() }
        npcCondition("no_protect_magic") { !it.protectMagic() }
        npcCondition("no_protect_range") { !it.protectRange() }
    }
}
