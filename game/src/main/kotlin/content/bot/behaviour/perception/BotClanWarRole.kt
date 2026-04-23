package content.bot.behaviour.perception

import content.skill.melee.weapon.combatStyle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory

enum class BotClanWarRole {
    TANK,
    PURE,
    HYBRID,
    HEALER,
    ;

    companion object {
        fun detect(player: Player): BotClanWarRole {
            val attack = player.levels.getMax(Skill.Attack)
            val strength = player.levels.getMax(Skill.Strength)
            val defence = player.levels.getMax(Skill.Defence)
            val constitution = player.levels.getMax(Skill.Constitution)
            val ranged = player.levels.getMax(Skill.Ranged)
            val magic = player.levels.getMax(Skill.Magic)
            val prayer = player.levels.getMax(Skill.Prayer)
            val style = melee(player.combatStyle)

            if (attack >= 60 && strength >= 85 && defence <= 10) {
                return PURE
            }
            if (defence >= 70 && constitution >= 80 && style == "melee") {
                return TANK
            }
            if (prayer >= 70 && style == "magic" && healingItems(player) >= 4) {
                return HEALER
            }
            if (attack >= 60 && (ranged >= 60 || magic >= 60) && defence >= 40) {
                return HYBRID
            }
            return HYBRID
        }

        private fun melee(style: String): String? = when (style) {
            "stab", "slash", "crush" -> "melee"
            "range" -> "ranged"
            "magic" -> "magic"
            else -> null
        }

        private fun healingItems(player: Player): Int {
            val inv = player.inventory
            var count = 0
            for (index in inv.indices) {
                val item = inv[index]
                val id = item.def.stringId
                if (id == "shark" || id.startsWith("saradomin_brew_")) {
                    count += item.amount
                }
            }
            return count
        }
    }
}
