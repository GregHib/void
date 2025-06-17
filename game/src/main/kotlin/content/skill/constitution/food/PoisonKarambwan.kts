package content.skill.constitution.food

import content.entity.combat.hit.directHit
import content.skill.constitution.consume

consume("poison_karambwan") { player ->
    player.directHit(50, "poison")
}
