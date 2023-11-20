package world.gregs.voidps.world.interact.entity.player.combat.prayer

import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar

object Prayer {

    fun hasActive(player: Player): Boolean {
        return (player.variables as PlayerVariables).temp.any { (key, value) -> key.startsWith("prayer_") && value == true }
    }

    fun usingProtectionPrayer(source: Character, target: Character, type: String): Boolean {
        return type == "melee" && target.protectMelee() ||
                type == "range" && target.protectRange() ||
                type == "magic" && target.protectMagic() ||
                source.isFamiliar && target.protectSummoning()
    }

    fun usingDeflectPrayer(source: Character, target: Character, type: String): Boolean {
        return (type == "melee" && target.praying("deflect_melee")) ||
                (type == "range" && target.praying("deflect_missiles")) ||
                (type == "magic" && target.praying("deflect_magic")) ||
                source.isFamiliar && (target.praying("deflect_summoning"))
    }

    fun hitThroughProtectionPrayer(source: Character, target: Character?, type: String, weapon: Item, special: Boolean): Boolean {
        if (target == null) {
            return false
        }
        if (special && weapon.id == "ancient_mace" && type == "melee") {
            return target.protectMelee()
        }
        return false
    }
}