package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Special moves not yet ported. They are registered under the interaction kind the live move uses
 * so clicking the cast button gives feedback, but each returns false from [castFamiliarSpecial] so
 * no scroll or special-move points are spent. Grouped by what each still needs:
 *
 *  - Item interactions (cook/incubate/smelt): Bunyip, Spirit cobra, Pyre lord
 *  - Skill-system scenery: Hydra (Regrowth) (Beaver's Multichop is done)
 *  - "Charge the next attack" buffs: Iron titan, Steel titan (Spirit scorpion + Honey badger are done)
 *  - AoE hits: Smoke devil (Giant chinchompa's Explode is done)
 *
 * The Gorajo (bloodrager/deathslinger/...), meerkat and phoenix familiars have no 2009-era special
 * and so are intentionally left unregistered (the cast button does nothing for them).
 */
class FamiliarSpecialStubs : Script {

    private val notImplemented = "Your familiar's special move isn't available yet."

    init {
        // Instant self / AoE / charge specials.
        FamiliarSpecialMoves.instant(
            "smoke_devil_familiar",
            "iron_titan_familiar",
            "steel_titan_familiar",
        ) { stub() }

        // Scenery-target specials. (Beaver's Multichop is done - see Beaver.kt.)
        FamiliarSpecialMoves.obj("hydra_familiar") { _ -> stub() }

        // Item-target specials (use an item on the familiar).
        itemOnNPCApproach("*", "bunyip_familiar") { handleItemStub() }
        itemOnNPCApproach("*", "spirit_cobra_familiar") { handleItemStub() }
        itemOnNPCApproach("*", "pyrelord_familiar") { handleItemStub() }
    }

    private fun Player.stub(): Boolean {
        message(notImplemented)
        return false
    }

    private fun Player.handleItemStub() {
        if (follower == null) {
            return
        }
        castFamiliarSpecial { stub() }
    }
}
