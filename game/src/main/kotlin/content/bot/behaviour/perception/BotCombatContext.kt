package content.bot.behaviour.perception

import content.bot.Bot
import content.bot.BotMetrics
import content.entity.combat.Target
import content.entity.combat.attacker
import content.entity.combat.target
import content.entity.combat.underAttack
import content.entity.player.effect.Dragonfire
import content.entity.player.effect.antifire
import content.entity.player.effect.superAntifire
import content.entity.player.equip.Equipment
import content.skill.melee.weapon.Weapon
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.get

class BotCombatContext(
    val incomingAttackStyle: String?,
    private var scan: Map<Int, List<Player>>? = null,
    private val spiralScanner: () -> Map<Int, List<Player>>,
) {
    /**
     * Holds the spiral-scan output. Only built the first time [enemiesByTile] is read. Reactive
     * actions that don't touch this field (e.g. BotAttackerStyle) pay nothing for the scan.
     */
    val enemiesByTile: Map<Int, List<Player>>
        get() {
            val cached = scan
            if (cached != null) {
                return cached
            }
            val fresh = spiralScanner.invoke()
            scan = fresh
            return fresh
        }

    companion object {
        private const val DEFAULT_RADIUS = 15

        /**
         * Builds the cheap part of the context immediately and defers the spiral scan to first access
         * of [BotCombatContext.enemiesByTile]. Reactive actions that only need
         * [BotCombatContext.incomingAttackStyle] pay nothing for the scan.
         */
        operator fun invoke(bot: Bot, radius: Int = DEFAULT_RADIUS): BotCombatContext {
            val player = bot.player
            // Whatever is hitting the bot, else the npc it is going for: prayed against before its first hit lands
            val enemy = player.attacker?.takeIf { player.underAttack } ?: player.target?.takeIf { it is NPC && !it.dead }
            return BotCombatContext(
                incomingAttackStyle = enemy?.let { threat(player, it) },
                spiralScanner = { scan(player, radius) },
            )
        }

        /**
         * The style to protect against: for an npc, the attack from its combat table that would hit the bot
         * hardest, with what the bot wears and drank taken off dragonfire (a shield or antifire and it prays melee
         * against a dragon after all); worked out once per enemy and kept in bot variables until the enemy
         * changes, so the prayer doesn't flip with every blow. A player, whose gear can change, is read live.
         */
        fun threat(player: Player, enemy: Character): String? {
            if (enemy !is NPC) {
                return categorize(enemy)
            }
            if (player["pray_enemy", -1] == enemy.index) {
                return player.get<String>("pray_enemy_style")
            }
            val style = table(player, enemy) ?: categorize(enemy)
            player["pray_enemy"] = enemy.index
            if (style == null) player.clear("pray_enemy_style") else player["pray_enemy_style"] = style
            return style
        }

        /** The style of [npc]'s hardest-hitting attack by its combat table, or null when it has none. */
        fun table(player: Player, npc: NPC): String? {
            val definition = get<CombatDefinitions>().getOrNull(npc.transformDef["combat_def", npc.id]) ?: return null
            var best: String? = null
            var hardest = -1
            for (attack in definition.attacks.values) {
                for (hit in attack.targetHits) {
                    val style = style(hit.offense) ?: continue
                    val max = if (hit.offense == "dragonfire") dragonfire(player, npc) else hit.max
                    if (max > hardest) {
                        hardest = max
                        best = style
                    }
                }
            }
            return best
        }

        /** What [npc]'s fire would do to [player] as they are now, unprayed. */
        private fun dragonfire(player: Player, npc: NPC): Int = Dragonfire.maxHit(
            type = Dragonfire.type(npc),
            success = true,
            shield = Equipment.antiDragonShield(player),
            potion = when {
                player.superAntifire -> 2
                player.antifire -> 1
                else -> 0
            },
        )

        private fun style(offense: String): String? = when (offense) {
            "stab", "slash", "crush", "melee", "typeless_crush" -> "melee"
            "range", "ranged" -> "ranged"
            "magic", "dragonfire", "blaze", "icy_breath" -> "magic"
            else -> null
        }

        private fun scan(player: Player, radius: Int): Map<Int, List<Player>> {
            BotMetrics.incScans()
            val byTile = mutableMapOf<Int, MutableList<Player>>()
            Players.forEachInRadius(player.tile, radius) { other ->
                if (other === player || other.dead) {
                    return@forEachInRadius
                }
                if (Target.attackable(player, other, message = false)) {
                    byTile.getOrPut(other.tile.id) { mutableListOf() }.add(other)
                }
            }
            return byTile
        }

        private fun categorize(attacker: Character): String? = when (Weapon.type(attacker)) {
            "melee" -> "melee"
            "range" -> "ranged"
            "magic" -> "magic"
            else -> null
        }
    }
}
