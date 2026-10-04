package content.minigame.pest_control

import content.entity.combat.attacking
import content.entity.combat.hit.directHit
import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.inv.item.addOrDrop
import content.quest.clearInstance
import content.quest.joinInstance
import content.quest.setInstanceLogout
import content.skill.summoning.follower
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Board one of the three landers at the Void Knights' Outpost to defend the Void Knight on a nearby island.
 * https://runescape.wiki/w/Pest_Control
 */
class PestControl(val combatDefinitions: CombatDefinitions) : Script {

    private val waiting = mutableMapOf<String, MutableList<Player>>()
    private val departures = mutableMapOf<String, Int>()
    private val games = mutableMapOf<Int, PestControlGame>()
    private var nextGameId = 0

    init {
        worldSpawn {
            World.timers.start("pest_control")
        }

        worldTimerStart("pest_control") { 1 }

        worldTimerTick("pest_control") {
            for (lander in LANDERS) {
                tickLander(lander)
            }
            for (game in games.values.toList()) {
                tickGame(game)
            }
            Timer.CONTINUE
        }

        objectOperate("Cross", "novice_lander_gangplank,intermediate_lander_gangplank,veteran_lander_gangplank") { (target) ->
            board(target.id.removeSuffix("_lander_gangplank"))
        }

        objectOperate("Climb", "novice_lander_ladder,intermediate_lander_ladder,veteran_lander_ladder") {
            leaveLander()
        }

        for (lander in LANDERS) {
            exited("${lander}_lander") {
                removeFromLander()
            }
        }

        playerSpawn {
            for (lander in LANDERS) {
                if (tile in Areas["${lander}_lander"]) {
                    tele(Tables.tile("pest_control_landers.$lander.exit"))
                    break
                }
            }
        }

        playerDespawn {
            removeFromLander()
            val game = game() ?: return@playerDespawn
            game.players.remove(this)
            clear("pest_control_game")
        }

        playerDeath { death ->
            val game = game() ?: return@playerDeath
            death.dropItems = false
            death.teleport = landingTile(game)
        }

        npcCombatDamage("portal_*,splatter_*,shifter_*,ravager_*,spinner_*,torcher_*,defiler_*,brawler_*") { damage ->
            val player = damage.source as? Player ?: return@npcCombatDamage
            val game = player.game() ?: return@npcCombatDamage
            if (!contains("pest_control_game")) {
                return@npcCombatDamage
            }
            val total = player.inc("pest_control_damage", damage.damage)
            player.interfaces.sendText("pest_control_playing", "damage", if (total >= REWARD_DAMAGE) "<col=00FF00>$total" else total.toString())
            if (id.startsWith("portal")) {
                updatePortals(game)
            }
        }

        npcDeath("portal_*") {
            val game = games[get("pest_control_game", -1)] ?: return@npcDeath
            portalDestroyed(game, this)
        }

        npcDeath("void_knight") {
            val game = games[get("pest_control_game", -1)] ?: return@npcDeath
            end(game, won = false)
        }

        npcDeath("splatter_*,shifter_*,ravager_*,spinner_*,torcher_*,defiler_*,brawler_*") {
            val game = games[get("pest_control_game", -1)] ?: return@npcDeath
            game.monsters.remove(this)
        }
    }

    private fun Player.game(): PestControlGame? = games[get("pest_control_game", -1)]

    /*
        Landers
     */

    private suspend fun Player.board(lander: String) {
        val required = Tables.int("pest_control_landers.$lander.combat")
        if (combatLevel < required) {
            message("You need a combat level of $required or higher to board this lander.")
            return
        }
        if (follower != null) {
            message("You can't take a follower on the lander.")
            return
        }
        tele(Tables.tile("pest_control_landers.$lander.boat"))
        message("You board the lander.")
        val players = waiting.getOrPut(lander) { mutableListOf() }
        if (!players.contains(this)) {
            players.add(this)
        }
        set("pest_control_lander", lander)
        open("pest_control_waiting")
        interfaces.sendText("pest_control_waiting", "lander", lander.replaceFirstChar { it.uppercase() })
        interfaces.sendText("pest_control_waiting", "points", "Points: ${get("pest_control_points", 0)}")
        updateWaiting(lander)
    }

    private fun Player.leaveLander() {
        val lander: String = get("pest_control_lander") ?: return
        removeFromLander()
        tele(Tables.tile("pest_control_landers.$lander.exit"))
    }

    private fun Player.removeFromLander() {
        val lander: String = remove("pest_control_lander") ?: return
        waiting[lander]?.remove(this)
        close("pest_control_waiting")
        updateWaiting(lander)
    }

    private fun tickLander(lander: String) {
        val players = waiting[lander]
        if (players.isNullOrEmpty()) {
            departures[lander] = Settings["pestControl.departureTicks", DEPARTURE_TICKS]
            return
        }
        val remaining = departures.getOrDefault(lander, Settings["pestControl.departureTicks", DEPARTURE_TICKS]) - 1
        departures[lander] = remaining
        if (remaining % 10 == 0) {
            updateWaiting(lander)
        }
        if (remaining > 0 && players.size < MAX_PLAYERS) {
            return
        }
        if (players.size < Settings["pestControl.minimumPlayers", MIN_PLAYERS]) {
            departures[lander] = RETRY_TICKS
            return
        }
        start(lander, players.take(MAX_PLAYERS))
        departures[lander] = Settings["pestControl.departureTicks", DEPARTURE_TICKS]
    }

    private fun updateWaiting(lander: String) {
        val players = waiting[lander] ?: return
        val ticks = departures.getOrDefault(lander, Settings["pestControl.departureTicks", DEPARTURE_TICKS])
        val departure = when {
            ticks > 100 -> "Next Departure: ${ticks / 100} min"
            ticks > 50 -> "Next Departure: 1 min"
            else -> "Next Departure: 30 seconds"
        }
        for (player in players) {
            player.interfaces.sendText("pest_control_waiting", "departure", departure)
            player.interfaces.sendText("pest_control_waiting", "players_ready", "Players Ready: ${players.size}")
        }
    }

    /*
        Game
     */

    private fun start(lander: String, players: List<Player>) {
        val instance = Instances.small()
        get<DynamicZones>().copy(ISLAND, instance, levels = 1)
        val row = Rows.get("pest_control_landers.$lander")
        val portals = Tables.get("pest_control_portals").rows()
        val game = PestControlGame(nextGameId++, row, instance, instance.offset(ISLAND), portals.map { it.rowId }.shuffled(random))
        games[game.id] = game

        val knight = spawn(game, "void_knight", KNIGHT)
        game.knight = knight
        game.squire = spawn(game, "squire_instance_pest_control", SQUIRE)
        for (portal in portals) {
            val npc = spawn(game, portal.npc("shielded"), portal.tile("tile"))
            npc.levels.set(Skill.Constitution, row.int("portal_hitpoints"))
            game.portals[portal.rowId] = npc
        }
        for (player in players) {
            waiting[lander]?.remove(player)
            player.clear("pest_control_lander")
            player.close("pest_control_waiting")
            join(game, player)
        }
        updatePortals(game)
    }

    private fun spawn(game: PestControlGame, id: String, tile: Tile): NPC {
        val npc = NPCs.add(id, tile.add(game.offset))
        npc["pest_control_game"] = game.id
        npc["in_multi_combat"] = true
        return npc
    }

    private fun join(game: PestControlGame, player: Player) {
        game.players.add(player)
        player["pest_control_game"] = game.id
        player["pest_control_damage"] = 0
        player["in_multi_combat"] = true
        player.joinInstance(game.instance)
        player["instance_offset"] = game.offset.id
        player["instance_logout"] = true
        player.setInstanceLogout(game.lander.tile("exit"))
        player.tele(landingTile(game))
        player.open("pest_control_playing")
        player.interfaces.sendText("pest_control_playing", "damage", "0")
        player.queue("pest_control_start", 1) {
            npc<Angry>("squire_instance_pest_control", "You must defend the Void Knight while the portals are unsummoned. The ritual takes twenty minutes though, so you can help out by destroying them yourselves! Now GO GO GO!")
        }
    }

    private fun landingTile(game: PestControlGame): Tile = LANDING.add(random.nextInt(4), random.nextInt(6)).add(game.offset)

    private fun tickGame(game: PestControlGame) {
        if (game.over) {
            return
        }
        game.ticks++
        game.players.removeIf { it.get("pest_control_game", -1) != game.id }
        if (game.players.isEmpty()) {
            end(game, won = false)
            return
        }
        val shield = game.ticks / SHIELD_TICKS - 1
        if (game.ticks % SHIELD_TICKS == 0 && shield in game.shieldOrder.indices) {
            dropShield(game, game.shieldOrder[shield])
        }
        if (game.ticks % SPAWN_TICKS == 0) {
            spawnMonsters(game)
        }
        if (game.ticks % 2 == 0) {
            for (monster in game.monsters.toList()) {
                monsterTick(game, monster)
            }
        }
        if (game.ticks % 10 == 0) {
            updateStatus(game)
        }
        if (game.ticks >= Settings["pestControl.gameTicks", GAME_TICKS]) {
            end(game, won = true)
        }
    }

    private fun dropShield(game: PestControlGame, colour: String) {
        val portal = game.portals[colour] ?: return
        portal.transform(Tables.npc("pest_control_portals.$colour.portal"))
        val message = "The ${Tables.string("pest_control_portals.$colour.name")} portal shield has dropped!"
        game.squire?.say(message)
        for (player in game.players) {
            player.message(message)
        }
    }

    private fun spawnMonsters(game: PestControlGame) {
        if (game.monsters.size >= MAX_MONSTERS) {
            return
        }
        val types = Tables.get("pest_control_monsters").rows()
        val tier = game.lander.int("tier")
        for (portal in game.portals.values) {
            if (portal.index == -1 || portal["dead", false]) {
                continue
            }
            val amount = tier + 1 + game.players.size / 10
            for (i in 0 until amount) {
                val tiers = types.random(random).npcList("tiers")
                val id = tiers[(tier + random.nextInt(3)).coerceAtMost(tiers.lastIndex)]
                val monster = NPCs.add(id, portal.tile.add(random.nextInt(-2, 3), random.nextInt(-2, 3)))
                monster["pest_control_game"] = game.id
                monster["in_multi_combat"] = true
                game.monsters.add(monster)
            }
        }
    }

    /**
     * Monsters attack nearby players otherwise head for the void knight and attack him
     */
    private fun monsterTick(game: PestControlGame, monster: NPC) {
        if (monster.index == -1 || monster["dead", false]) {
            game.monsters.remove(monster)
            return
        }
        if (monster.attacking) {
            return
        }
        val player = game.players.firstOrNull { it.tile.level == monster.tile.level && it.tile.distanceTo(monster.tile) <= AGGRESSION_RANGE }
        if (player != null) {
            monster.interactPlayer(player, "Attack")
            return
        }
        val knight = game.knight ?: return
        if (monster.tile.distanceTo(knight.tile) > 1) {
            monster.walkTo(knight.tile)
            return
        }
        if (GameLoop.tick < monster["pest_control_next_hit", 0]) {
            return
        }
        monster["pest_control_next_hit"] = GameLoop.tick + monster.def["attack_speed", 4]
        monster.face(knight)
        val attack = combatDefinitions.getOrNull(monster.def["combat_def", monster.id])?.attacks?.values?.firstOrNull()
        if (attack != null && attack.anim.isNotEmpty()) {
            monster.anim(attack.anim)
        }
        knight.directHit(monster, random.nextInt(monster.def["max_hit_melee", 50] + 1))
    }

    private fun portalDestroyed(game: PestControlGame, portal: NPC) {
        val colour = game.portals.entries.firstOrNull { it.value == portal }?.key ?: return
        val knight = game.knight
        if (knight != null) {
            knight.levels.restore(Skill.Constitution, KNIGHT_HEAL)
        }
        for (player in game.players) {
            player.message("The ${Tables.string("pest_control_portals.$colour.name")} portal has been destroyed!") // TODO proper message
        }
        if (game.portals.values.all { it == portal || it.index == -1 || it["dead", false] }) {
            end(game, won = true)
        }
    }

    private fun updateStatus(game: PestControlGame) {
        val remaining = (Settings["pestControl.gameTicks", GAME_TICKS] - game.ticks) / 100
        val knight = game.knight?.levels?.get(Skill.Constitution) ?: 0
        for (player in game.players) {
            player.interfaces.sendText("pest_control_playing", "time", "$remaining min")
            player.interfaces.sendText("pest_control_playing", "knight_health", if (knight > KNIGHT_WARNING) knight.toString() else "<col=FF0000>$knight")
        }
        updatePortals(game)
    }

    private fun updatePortals(game: PestControlGame) {
        for ((colour, portal) in game.portals) {
            val health = if (portal.index == -1) 0 else portal.levels.get(Skill.Constitution)
            for (player in game.players) {
                player.interfaces.sendText("pest_control_playing", "${colour}_health", if (health > 0) "<col=00FF00>$health" else "<col=FF0000>0")
            }
        }
    }

    private fun end(game: PestControlGame, won: Boolean) {
        if (game.over) {
            return
        }
        game.over = true
        games.remove(game.id)
        for (npc in listOfNotNull(game.knight, game.squire) + game.portals.values + game.monsters) {
            if (npc.index != -1) {
                NPCs.remove(npc)
            }
        }
        val exit = game.lander.tile("exit")
        val points = game.lander.int("points")
        for (player in game.players) {
            val damage = player.get("pest_control_damage", 0)
            player.clear("pest_control_game")
            player.clear("pest_control_damage")
            player.clear("in_multi_combat")
            player.close("pest_control_playing")
            player.clearInstance()
            player.clear("instance_logout")
            player.tele(exit)
            player.levels.clear()
            player.queue("pest_control_end", 1) {
                when {
                    !won -> npc<Sad>("squire_instance_pest_control", "The Void Knight was killed, another of our Order has fallen and that Island is lost.")
                    damage < REWARD_DAMAGE -> npc<Sad>("squire_instance_pest_control", "Congratulations! You managed to destroy all the portals! However, you did not succeed in reaching the required amount of damage dealt we cannot grant you a reward.")
                    else -> {
                        inc("pest_control_points", points)
                        addOrDrop("coins", combatLevel * 10)
                        npc<Happy>("squire_instance_pest_control", "Congratulations! You managed to destroy all the portals! We've awarded you $points Void Knight Commendation points. Please also accept these coins as a reward.")
                    }
                }
            }
        }
        game.players.clear()
    }

    companion object {
        private val LANDERS = listOf("novice", "intermediate", "veteran")
        private val ISLAND = Region(10536)
        private val KNIGHT = Tile(2656, 2592)
        private val SQUIRE = Tile(2654, 2607)
        private val LANDING = Tile(2656, 2609)
        private const val MAX_PLAYERS = 25
        private const val MIN_PLAYERS = 1
        private const val DEPARTURE_TICKS = 500
        private const val RETRY_TICKS = 100
        private const val GAME_TICKS = 2000
        private const val SHIELD_TICKS = 50
        private const val SPAWN_TICKS = 35
        private const val MAX_MONSTERS = 80
        private const val AGGRESSION_RANGE = 5
        private const val KNIGHT_HEAL = 500
        private const val KNIGHT_WARNING = 500

        // Guessed: 50 damage in pre-lifepoint terms
        private const val REWARD_DAMAGE = 500
    }
}
