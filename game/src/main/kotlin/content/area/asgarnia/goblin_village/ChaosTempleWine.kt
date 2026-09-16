package content.area.asgarnia.goblin_village

import content.entity.combat.dead
import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Taking the Wine of zamorak from its spawn in the Chaos Temple north-west of Goblin Village by hand
 * angers Zamorak: the thief is burnt, has their combat stats drained and the monks turn on them.
 * Telekinetic Grab avoids all of it.
 */
class ChaosTempleWine : Script {

    init {
        takeable("wine_of_zamorak") { item, telegrab ->
            if (telegrab || item.tile != WINE_TILE || inventory.isFull()) {
                return@takeable item.id
            }
            punishTheft()
            item.id
        }
    }

    private fun Player.punishTheft() {
        message("STOP STEALING MY WINE! GAH!")
        gfx("flames_of_zamorak_impact")
        directHit(random.nextInt(MIN_DAMAGE, MAX_DAMAGE + 1))
        for (skill in DRAINED_SKILLS) {
            val drain = (levels.get(skill) / LEVELS_PER_POINT).coerceAtMost(MAX_DRAIN)
            if (drain > 0) {
                levels.drain(skill, drain)
            }
        }
        val temple = Areas["chaos_temple_multi_area"]
        for (monk in NPCs.at(tile.regionLevel)) {
            if (monk.dead || !monk.id.startsWith("monk_of_zamorak") || monk.tile !in temple) {
                continue
            }
            monk.interactPlayer(this, "Attack")
        }
    }

    companion object {
        val WINE_TILE = Tile(2931, 3515)
        private const val MIN_DAMAGE = 30
        private const val MAX_DAMAGE = 40
        private const val LEVELS_PER_POINT = 13
        private const val MAX_DRAIN = 3
        private val DRAINED_SKILLS = listOf(Skill.Attack, Skill.Strength, Skill.Defence, Skill.Ranged, Skill.Magic)
    }
}
