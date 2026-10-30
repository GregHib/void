package content.skill.slayer

import content.entity.combat.damageDealers
import content.entity.combat.hit.damage
import content.entity.combat.killer
import content.entity.player.bank.bank
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

var Player.slayerChallenge: String
    get() = this["slayer_challenge", ""]
    set(value) {
        if (value == "") {
            clear("slayer_challenge")
            clear("slayer_challenge_progress")
        } else {
            this["slayer_challenge"] = value
        }
    }

private var Player.challengeProgress: Int
    get() = this["slayer_challenge_progress", 0]
    set(value) {
        this["slayer_challenge_progress"] = value
    }

private const val ZYGOMITES = 10
private const val TOWER_MONSTERS = "crawling_hand*,banshee*,infernal_mage*,bloodveld*,aberrant_spectre*,gargoyle*,nechryael*,abyssal_demon*"

// Duradel's challenge: the slayer tower monsters to kill in a single trip
private val towerMonsters = mapOf(
    "crawling_hands" to 21,
    "banshees" to 8,
    "infernal_mage" to 9,
    "bloodvelds" to 7,
    "aberrant_spectre" to 7,
    "gargoyles" to 5,
    "nechryael" to 6,
    "abyssal_demon" to 6,
)

private val dragonColours = listOf("green", "blue", "red", "black")

// Mazchna's challenge: the monster which appears at the CKS, ALQ and BKR fairy rings
private val fairyRings = listOf(
    "ravenous_ghoul" to Tile(3447, 3470),
    "ravenous_vampire" to Tile(3597, 3495),
    "ravenous_snail" to Tile(3469, 3431),
)

// Turael's challenge: the monsters in his dungeon
private val dungeonMonsters = mapOf(
    "goblin_turaels_dungeon" to 2,
    "goblin_turaels_dungeon_2" to 2,
    "goblin_turaels_dungeon_3" to 1,
    "wolf_turaels_dungeon" to 2,
    "wolf_turaels_dungeon_2" to 3,
    "minotaur_turaels_dungeon" to 5,
)
private const val DUNGEON_MONSTERS = "goblin_turaels_dungeon*,wolf_turaels_dungeon*,minotaur_turaels_dungeon"

/**
 * Slayer masters occasionally offer a challenge in place of an assignment, which awards
 * bonus experience and points on top of those for completing an assignment.
 */
class SlayerChallenge : Script {

    init {
        npcDeath("kalphite_queen") {
            val total = damageDealers.values.sum()
            for ((dealer, damage) in damageDealers) {
                // The challenger must have done the majority of the damage
                if (dealer is Player && dealer.slayerChallenge == "kalphite_queen" && damage * 2 > total) {
                    dealer.completeChallenge()
                }
            }
        }

        npcDeath(TOWER_MONSTERS) {
            val player = killer as? Player ?: return@npcDeath
            if (player.slayerChallenge != "slayer_tower" || player.tile !in Areas["slayer_tower"]) {
                return@npcDeath
            }
            val category = categories.firstOrNull { it in towerMonsters } ?: return@npcDeath
            val required = towerMonsters.getValue(category)
            val killed = player["slayer_tower_$category", 0]
            if (killed >= required) {
                return@npcDeath
            }
            player["slayer_tower_$category"] = killed + 1
            if (towerMonsters.all { (category, required) -> player["slayer_tower_$category", 0] >= required }) {
                player.completeChallenge()
            }
        }

        exited("slayer_tower") {
            if (slayerChallenge != "slayer_tower") {
                return@exited
            }
            if (towerMonsters.keys.none { contains("slayer_tower_$it") }) {
                return@exited
            }
            for (category in towerMonsters.keys) {
                clear("slayer_tower_$category")
            }
            message("You have left the Slayer Tower; you'll have to start Duradel's challenge again.")
        }

        npcDeath("tztok_jad") {
            val player = killer as? Player ?: return@npcDeath
            if (player.slayerChallenge == "fight_cave" && player["fight_cave_wave", -1] == 63) {
                player.completeChallenge()
            }
        }

        npcDeath("*_dragon*,*dragon_*") {
            val player = killer as? Player ?: return@npcDeath
            if (player.slayerChallenge != "dragon_scales") {
                return@npcDeath
            }
            val colour = dragonColours.firstOrNull { categories.contains("${it}_dragons") } ?: return@npcDeath
            val scale = "perfect_${colour}_dragon_scale"
            // "Common" on the wiki drop tables
            if (player.ownsItem(scale) || random.nextInt(1024) >= 64) {
                return@npcDeath
            }
            FloorItems.add(tile, scale, revealTicks = 100, disappearTicks = 200, owner = player)
        }

        npcDeath("ravenous_ghoul,ravenous_vampire,ravenous_snail") {
            val player = killer as? Player ?: return@npcDeath
            if (player.slayerChallenge != "ravenous_creatures") {
                return@npcDeath
            }
            val ring = fairyRings.indexOfFirst { it.first == id }
            player.challengeProgress = player.challengeProgress or (1 shl ring)
            if (player.challengeProgress == (1 shl fairyRings.size) - 1) {
                player.completeChallenge()
            }
            val id = id
            player.queue("mazchna_contact") {
                ravenousCreatureKilled(id)
            }
        }

        objTeleportTakeOff("Climb-down", "turaels_dungeon_ladder_down") { _, _ ->
            if (slayerChallenge == "turaels_dungeon") {
                return@objTeleportTakeOff Teleport.CONTINUE
            }
            message("<red>You can only enter Turael's dungeon when you have accepted his special Slayer challenge.")
            Teleport.CANCEL
        }

        entered("turaels_dungeon") {
            if (slayerChallenge != "turaels_dungeon" || contains("turaels_dungeon_remaining")) {
                return@entered
            }
            val area = Areas["turaels_dungeon"]
            var spawned = 0
            for ((id, amount) in dungeonMonsters) {
                repeat(amount) {
                    if (NPCs.addRandom(id, area, owner = this) != null) {
                        spawned++
                    }
                }
            }
            set("turaels_dungeon_remaining", spawned)
        }

        npcDeath(DUNGEON_MONSTERS) {
            val player = killer as? Player ?: return@npcDeath
            if (player.slayerChallenge != "turaels_dungeon" || this["owner", ""] != player.accountName) {
                return@npcDeath
            }
            val remaining = player["turaels_dungeon_remaining", 0] - 1
            player["turaels_dungeon_remaining"] = remaining
            if (remaining <= 0) {
                player.clear("turaels_dungeon_remaining")
                player.completeChallenge()
            }
        }

        exited("turaels_dungeon") {
            if (!contains("turaels_dungeon_remaining")) {
                return@exited
            }
            clear("turaels_dungeon_remaining")
            // Every monster must be killed in a single visit
            for (npc in NPCs.filter { it.id in dungeonMonsters && it["owner", ""] == accountName }) {
                NPCs.remove(npc)
            }
            if (slayerChallenge == "turaels_dungeon") {
                message("You have left Turael's dungeon; you'll have to start his challenge again.")
            }
        }

        itemOnNPCOperate("experimental_spray", "zygomite*") { (target) ->
            arriveDelay()
            spray(this, target)
        }
    }

    private fun spray(player: Player, target: NPC) {
        if (player.slayerChallenge != "zygomites") {
            player.message("You should return this to Chaeldar.")
            return
        }
        if (player.challengeProgress >= ZYGOMITES) {
            player.message("You've already tested the spray on enough zygomites; return to Chaeldar.")
            return
        }
        val hitpoints = target.levels.get(Skill.Constitution)
        player.face(target)
        player.sound("zygomite_spray")
        if (hitpoints >= 80) {
            player.message("The zygomite is not weak enough to succumb to the effects of the fungicide!")
            return
        }
        player.challengeProgress++
        when (random.nextInt(3)) {
            0 -> {
                target.damage(hitpoints, source = player)
                player.message("The experimental spray has killed the zygomite!", type = ChatType.Filter)
            }
            1 -> {
                target.levels.restore(Skill.Constitution, target.levels.getMax(Skill.Constitution) / 4)
                player.message("The experimental spray has sent the zygomite into a furious rage!", type = ChatType.Filter)
            }
            else -> {
                target.levels.clear(Skill.Constitution)
                player.message("The experimental spray has healed the zygomite!", type = ChatType.Filter)
            }
        }
        if (player.challengeProgress >= ZYGOMITES) {
            player.message("You have tested the experimental spray on ten zygomites; return to Chaeldar.")
        }
    }
}

/**
 * Offer [master]'s challenge instead of a regular assignment.
 * @return true if a challenge was accepted
 */
internal suspend fun Player.offerChallenge(master: String): Boolean {
    val challenge = Tables.stringOrNull("slayer_challenges.$master.challenge") ?: return false
    if (random.nextInt(100) >= Settings["slayer.challengeChance", 10]) {
        return false
    }
    npc<Quiz>("I have a special challenge for you, if you think you're up to it?")
    var accepted = false
    choice {
        option<Happy>("Yes, I'll take the challenge.") {
            accepted = true
        }
        option<Neutral>("No thanks, I'd rather have a normal assignment.")
    }
    if (!accepted) {
        return false
    }
    slayerMaster = master
    slayerChallenge = challenge
    when (challenge) {
        "ravenous_creatures" -> {
            if (!inventory.contains("enchanted_gem")) {
                inventory.add("enchanted_gem")
            }
            npc<Neutral>("Investigate the fairy rings west of Canifis, in the Haunted Woods and in Mort Myre Swamp. Contact me with your enchanted gem when you're near each of them.")
        }
        "dragon_scales" -> npc<Neutral>("Bring me a perfect scale from a green, blue, red and black dragon.")
        "zygomites" -> {
            inventory.add("experimental_spray")
            npc<Neutral>("Test my new experimental fungicide spray on ten mutated zygomites, then come back and tell me the results.")
        }
        "kalphite_queen" -> npc<Neutral>("Travel to the Kalphite Hive and slay the Kalphite Queen. You may bring a partner, but you must do the majority of the damage.")
        "slayer_tower" -> npc<Neutral>("Go to the Slayer Tower and, without leaving, kill 21 crawling hands, 8 banshees, 9 infernal mages, 7 bloodvelds, 7 aberrant spectres, 5 gargoyles, 6 nechryael and 6 abyssal demons.")
        "fight_cave" -> npc<Neutral>("Complete the TzHaar Fight Cave and slay TzTok-Jad.")
        "turaels_dungeon" -> npc<Neutral>("I've constructed a basement and filled it with monsters. Enter it through the ladder in my house and kill all of the monsters inside in one visit.")
    }
    return true
}

/**
 * Talk to [master] about the current challenge, completing it if it's one they need to be shown.
 */
internal suspend fun Player.challengeDialogue(master: String) {
    if (master != slayerMaster) {
        npc<Neutral>("You're still working on a challenge for ${slayerMaster.replaceFirstChar { it.uppercase() }}.")
        return
    }
    when (slayerChallenge) {
        "dragon_scales" -> {
            val scales = dragonColours.map { "perfect_${it}_dragon_scale" }
            if (!scales.all { inventory.contains(it) }) {
                npc<Neutral>("You still need to bring me a perfect scale from a green, blue, red and black dragon.")
                return
            }
            // Extra scales are destroyed when the challenge is complete
            for (scale in scales) {
                inventory.removeToLimit(scale, Int.MAX_VALUE)
                bank.removeToLimit(scale, Int.MAX_VALUE)
            }
            npc<Happy>("Excellent, these are perfect.")
            completeChallenge()
        }
        "zygomites" -> {
            if (challengeProgress < ZYGOMITES) {
                if (!ownsItem("experimental_spray")) {
                    inventory.add("experimental_spray")
                    npc<Neutral>("Here's another spray; test it on ten mutated zygomites.")
                } else {
                    npc<Neutral>("You still need to test my spray on ${ZYGOMITES - challengeProgress} more zygomites.")
                }
                return
            }
            inventory.removeToLimit("experimental_spray", Int.MAX_VALUE)
            bank.removeToLimit("experimental_spray", Int.MAX_VALUE)
            npc<Happy>("Fascinating results, thank you.")
            completeChallenge()
        }
        else -> npc<Neutral>("You haven't completed my challenge yet.")
    }
}

/**
 * Contacting Mazchna with an enchanted gem near a Morytania fairy ring summons a monster for their challenge.
 * @return true if a monster was summoned
 */
internal suspend fun Player.contactNearFairyRing(): Boolean {
    if (slayerChallenge != "ravenous_creatures") {
        return false
    }
    val index = fairyRings.indices.firstOrNull { tile.within(fairyRings[it].second, 10) && challengeProgress and (1 shl it) == 0 } ?: return false
    val (id, ring) = fairyRings[index]
    if (NPCs.at(ring).any { it.id == id }) {
        return true
    }
    npc<Quiz>(slayerMaster, "Hello, $name. Are you at one of the fairy rings now?")
    when (id) {
        "ravenous_ghoul" -> {
            player<Neutral>("Yes, I've reached the ring near the farming patch to the west of Canifis.")
            npc<Quiz>(slayerMaster, "Excellent. Can you see anything out of the ordinary?")
            player<Neutral>("'Out of the ordinary' might be difficult to define around these parts.")
            statement("A column of butterflies briefly appears swarming around the centre of the fairy ring.")
            player<Shock>("Wait! Something seems to be happening with the ring.")
            npc<Quiz>(slayerMaster, "What's happening?")
            player<Neutral>("The ring is glowing with some sort of magic.")
            summon(id, ring)
            npc<Shock>(slayerMaster, "How strange. Wait, $name! Look out!")
        }
        "ravenous_vampire" -> {
            player<Neutral>("Yes, I've reached the ring in the haunted woods to the east of Canifis.")
            npc<Quiz>(slayerMaster, "Well done, $name. Can you see anything of interest?")
            player<Shock>("I don't think so... Wait, what's that up in the sky?")
            summon(id, ring)
            npc<Shock>(slayerMaster, "Oh no! Look out, $name!")
        }
        else -> {
            player<Neutral>("Yes, I've reached the ring in the swamplands to the south of Canifis.")
            npc<Quiz>(slayerMaster, "Good work. Is there anything unnatural going on?")
            player<Neutral>("Does my standing in a festering, stinking swamp count?")
            npc<Neutral>(slayerMaster, "Not really, $name!")
            player<Neutral>("Well, I can't see anything worth reporting.")
            summon(id, ring)
            npc<Shock>(slayerMaster, "Alright then let's bring you...wait what's that noise?")
        }
    }
    return true
}

private fun Player.summon(id: String, tile: Tile) {
    val npc = NPCs.add(id, tile, ticks = TimeUnit.MINUTES.toTicks(5), owner = this)
    npc.interactPlayer(this, "Attack")
}

/**
 * Mazchna checks in after each ravenous creature is killed.
 */
private suspend fun Player.ravenousCreatureKilled(id: String) {
    if (slayerChallenge == "") {
        npc<Neutral>(slayerMaster, "I don't like this one bit, $name. These attacks have to be more than mere coincidence. I will look into this matter further. You should recover your strength now. Don't forget to come and see me if you would like a new assignment. Thank you for your help, $name.")
        return
    }
    npc<Quiz>(slayerMaster, "Are you okay, $name?")
    if (id == "ravenous_ghoul") {
        player<Neutral>("Yes, I think so.")
        npc<Neutral>(slayerMaster, "Hmm, this does not bode well. We had best continue our investigation with the other rings in the swamplands and the haunted woods.")
    } else {
        player<Neutral>("Yes, just about.")
        npc<Neutral>(slayerMaster, "The evil of the area seems to be attracted to your presence. We had best finish our investigation with the final ring in the swamplands.")
    }
}

/**
 * Whether [npc] is one of the monsters the player's challenge has them kill, which still gives slayer experience.
 */
internal fun Player.isChallengeTarget(npc: NPC): Boolean = when (slayerChallenge) {
    "kalphite_queen" -> npc.id.startsWith("kalphite_queen")
    "slayer_tower" -> tile in Areas["slayer_tower"] && npc.categories.any { it in towerMonsters }
    "fight_cave" -> contains("fight_cave_wave")
    "dragon_scales" -> dragonColours.any { npc.categories.contains("${it}_dragons") }
    "zygomites" -> npc.categories.contains("mutated_zygomites")
    "ravenous_creatures" -> fairyRings.any { it.first == npc.id }
    "turaels_dungeon" -> npc.id in dungeonMonsters
    else -> false
}

internal fun Player.completeChallenge() {
    val master = slayerMaster
    val xp = Rows.getOrNull("slayer_challenges.$master")?.int("xp") ?: 0
    val points = if (questCompleted("smoking_kills")) Rows.getOrNull("slayer_challenges.$master")?.int("points") ?: 0 else 0
    slayerChallenge = ""
    exp(Skill.Slayer, xp.toDouble())
    slayerPoints += points
    if (points > 0) {
        message("Congratulations! You have completed your challenge. You gain $xp Slayer experience and $points points.")
    } else {
        message("Congratulations! You have completed your challenge. You gain $xp Slayer experience.")
    }
    completeAssignment(this)
}
