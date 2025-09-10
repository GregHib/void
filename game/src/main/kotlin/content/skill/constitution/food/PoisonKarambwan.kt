package content.skill.constitution.food

import content.entity.combat.hit.directHit
import content.skill.constitution.consume
import world.gregs.voidps.engine.event.Script
@Script
class PoisonKarambwan {

    init {
        consume("poison_karambwan") { player ->
            player.directHit(50, "poison")
        }

    }

}
