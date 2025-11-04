package content.skill.constitution.food

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script

class PoisonKarambwan : Script {

    init {
        consumed("poison_karambwan") { _, _ ->
            directHit(50, "poison")
        }
    }
}
