package content.skill.slayer

import content.entity.combat.damageDealers
import content.entity.combat.hit.damage
import content.entity.combat.killer
import content.entity.player.bank.bank
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
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
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.removeToLimit
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
internal fun Player.contactNearFairyRing(): Boolean {
    if (slayerChallenge != "ravenous_creatures") {
        return false
    }
    for ((index, ring) in fairyRings.withIndex()) {
        val (id, tile) = ring
        if (!this.tile.within(tile, 10) || challengeProgress and (1 shl index) != 0) {
            continue
        }
        if (NPCs.at(tile).any { it.id == id }) {
            return true
        }
        val npc = NPCs.add(id, tile, ticks = TimeUnit.MINUTES.toTicks(5), owner = this)
        npc.interactPlayer(this, "Attack")
        return true
    }
    return false
}

internal fun Player.completeChallenge() {
    val master = slayerMaster
    val xp = Rows.getOrNull("slayer_challenges.$master")?.int("xp") ?: 0
    val points = if (questCompleted("smoking_kills")) Rows.getOrNull("slayer_challenges.$master")?.int("points") ?: 0 else 0
    slayerChallenge = ""
    exp(Skill.Slayer, xp.toDouble())
    slayerPoints += points
    if (points > 0) {
        message("You've completed your special challenge and receive $xp Slayer experience and $points points.")
    } else {
        message("You've completed your special challenge and receive $xp Slayer experience.")
    }
    completeAssignment(this)
}
