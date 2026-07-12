package content.skill.summoning.pet

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.familiarChatheadAnimation
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.skill.summoning.follower
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.canFit
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.type.Tile

var Player.pet: NPC?
    get() {
        val index = get("pet_index", -1)
        if (index < 0) return null
        val npc = NPCs.indexed(index) ?: return null
        // NPC slot indices are reused. Without cross-checking against the
        // active item + a known pet row, a despawned-and-respawned slot
        // could resolve to an unrelated NPC.
        if (get("pet_active_item", "").isBlank()) return null
        if (petRowForNpc(npc.id) == null) return null
        return npc
    }
    set(value) {
        if (value != null) {
            set("pet_index", value.index)
        } else {
            set("pet_index", -1)
        }
    }

fun Player.summonPet(row: RowDefinition, itemId: String, restart: Boolean = false): Boolean {
    if (pet != null ||
        follower != null ||
        (!restart && get("pet_active_item", "").isNotBlank())
    ) {
        if (!restart) message("You already have a follower.")
        return false
    }
    val level = row.int("summoning_level")
    // Row "skill" defaults to Summoning when unset, but Soul Wars Slayer pets
    // (creeping hand, minitrice, baby basilisk, baby kurask, abyssal minion)
    // gate on Slayer and sneakerpeeper on Dungeoneering — so the skill is
    // row-driven rather than hardcoded.
    val skill = row.skillOrNull("skill") ?: Skill.Summoning
    // hasMax: requirements check the player's real level, not the current one - for Summoning
    // that's the points pool, which drains as points are spent.
    if (!hasMax(skill, level)) {
        message("You need a ${skill.name} level of $level to raise this pet.")
        return false
    }
    // Optional secondary gate — sneakerpeeper requires Summoning 80 on top of
    // its primary Dungeoneering 80 check.
    val secondarySkill = row.skillOrNull("secondary_skill")
    val secondaryLevel = row.intOrNull("secondary_level") ?: 0
    if (secondarySkill != null && secondaryLevel > 0 && !hasMax(secondarySkill, secondaryLevel)) {
        message("You also need a ${secondarySkill.name} level of $secondaryLevel to raise this pet.")
        return false
    }
    val stage = row.stageForItem(itemId) ?: return false
    val npcStringId = row.npcFor(stage) ?: return false
    // Consume the inventory item up front (skipped on restart, which is the
    // login-time re-spawn path with no item to consume). If the remove fails
    // we never commit active_item or spawn the NPC.
    if (!restart && !inventory.remove(itemId)) {
        return false
    }
    val spawned = NPCs.add(npcStringId, tile)
    spawned.mode = Follow(spawned, this)
    // Set the pet index synchronously so a second drop on the same tick sees
    // pet != null and trips the "You already have a follower." gate, instead
    // of slipping through and spawning a parallel NPC.
    pet = spawned
    set("pet_active_item", itemId)
    if (!restart) {
        anim("climb_down")
    }
    // Timer must start synchronously: a strong-priority action in the next
    // two ticks would wipe a weakQueue, leaving the pet without hunger or
    // growth updates for its entire lifetime.
    timers.start("pet_tick")
    queue("summon_pet", 2) {
        updatePetInterface()
    }
    return true
}

fun Player.pickupPet(): Boolean {
    val npc = pet ?: return false
    val row = petRowForNpc(npc.id) ?: return false
    val stage = row.stageForNpc(npc.id) ?: return false
    val itemId = row.itemFor(stage) ?: return false
    if (!inventory.add(itemId)) {
        message("You don't have enough room in your inventory.")
        return false
    }
    NPCs.remove(npc)
    pet = null
    clear("pet_active_item")
    timers.stop("pet_tick")
    anim("climb_down")
    deactivateSummoningOrb()
    return true
}

fun Player.updatePetInterface() {
    val pet = pet ?: return
    val row = petRowForNpc(pet.id)
    // Cats use iface 663 ("pet_details") which exposes cat-flavoured option
    // labels like "Release Cat". Everything else (dogs, dragons, etc.) uses
    // the generic follower iface 662 ("familiar_details").
    val ifaceId = if (row?.isCatLike() == true) "pet_details" else "familiar_details"
    val rowAnim = row?.animOrNull("chathead_anim")?.let { AnimationDefinitions.getOrNull(it)?.id }
    val chatheadNpc = row?.npcOrNull("chathead_npc") ?: pet.id
    // The varbit's "values" map keys are the only strings that translate to a
    // real int via MapValues.toInt; anything else collapses to -1 and breaks
    // the CS2 enum lookup. Check membership before any set() so unmapped pets
    // skip the update and keep the persisted value instead of being wiped to
    // -1 (which renders no chathead animation at all).
    val chatheadNpcMapped = (VariableDefinitions.get("follower_details_chathead_animation")?.values as? MapValues)
        ?.values?.containsKey(chatheadNpc) == true
    if (rowAnim != null && chatheadNpcMapped) {
        // Pre-set BEFORE iface open so the client CS2 on first render reads
        // the correct enum 1276 index instead of whatever stale value was
        // persisted (sneakerpeeper relies on this via pet_sneakerpeeper = 37).
        set("follower_details_chathead_animation", chatheadNpc)
    }
    interfaces.open(ifaceId)
    val itemStringId = get("pet_active_item", "")
    val itemIntId = ItemDefinitions.getOrNull(itemStringId)?.id ?: 0
    set("follower_details_name", itemIntId)
    set("follower_details_chathead", pet.def.id)
    when {
        rowAnim != null -> interfaces.sendAnimation(ifaceId, "chathead", rowAnim)
        row?.boolOrNull("chathead_disabled") == true -> interfaces.sendAnimation(ifaceId, "chathead", -1)
        chatheadNpcMapped -> set("follower_details_chathead_animation", chatheadNpc)
    }
    sendPetDetailsStats()
}

fun Player.sendPetDetailsStats() {
    val itemStringId = get("pet_active_item", "")
    if (itemStringId.isBlank()) return
    val row = petRowForItem(itemStringId) ?: return
    // The client CS2 (script 753 on iface 662, script 820 on iface 663) reads
    // varbits 4285 / 4286 — the growth and hunger sub-fields of varp 1175 —
    // and checks for the sentinel value 101. When it sees 101 it renders
    // "NA" instead of "X%". Send 101 for growth on a fully-grown pet so the
    // percentage label is automatically swapped out by the client.
    val fullyGrown = row.isFinalStage(itemStringId)
    val growth = if (fullyGrown) NA_SENTINEL else (getPetGrowth(row.rowId) / 100).coerceIn(0, 100)
    val hunger = (getPetHunger(row.rowId) / 100).coerceIn(0, 100)
    set("pet_details_growth_percentage", growth)
    set("pet_details_hunger_percentage", hunger)
}

/** CS2 sentinel meaning "this metric does not apply" — pet panel renders it as "NA". */
private const val NA_SENTINEL = 101

suspend fun Player.talkToPet(row: RowDefinition, pet: NPC) {
    val stageKey = row.stageForNpc(pet.id)?.name?.lowercase() ?: ""
    val candidates = setOf(row.rowId, row.petTalksKey())
    val rows = Tables.get("pet_talks").rows().filter {
        val stages = it.stringList("stage")
        it.string("pet") in candidates && (stages.isEmpty() || stageKey in stages)
    }
    val matchingConditional = rows.filter { matchesPetCondition(it.string("condition")) }
    val chosen = matchingConditional.randomOrNull()
        ?: rows.filter { it.string("condition").isBlank() }.randomOrNull()
    if (chosen == null) {
        row.ambientPhrases().randomOrNull()?.let { pet.say(it) }
        return
    }
    // Mapped pets (sneakerpeeper) resolve their chathead animation through
    // familiarChatheadAnimation like familiars do; the expression is only a
    // fallback for pets outside the varbit map.
    val expression = familiarChatheadAnimation(pet.id)?.toString() ?: "neutral"
    for (line in chosen.stringList("lines")) {
        val rendered = substitutePlayerName(line, name)
        when {
            rendered.startsWith("npc:") -> npc(npcId = pet.id, expression = expression, text = breakParenTranslation(rendered.removePrefix("npc:").trim()))
            rendered.startsWith("player:") -> player<Happy>(rendered.removePrefix("player:").trim())
            rendered.startsWith("overhead:") -> pet.say(rendered.removePrefix("overhead:").trim())
            rendered.startsWith("[") && rendered.endsWith("]") -> statement(rendered.removePrefix("[").removeSuffix("]").trim())
            else -> statement(rendered)
        }
    }
}

/**
 * Replaces the literal stand-in "Player" used by some wiki-sourced pet
 * dialogue (sneakerpeeper in particular) with the player's display name.
 * Matches whole words only so "Player-skin" / "Player-lips" / "Player," all
 * substitute correctly without touching unrelated substrings.
 */
private val PLAYER_NAME_TOKEN = Regex("\\bPlayer\\b")

private fun substitutePlayerName(line: String, name: String): String = PLAYER_NAME_TOKEN.replace(line, name)

/**
 * Splits `Bark! (Translation!)` into `Bark!<br>(Translation!)` so the chathead
 * renders the bark on one line and the bracketed translation on the next.
 */
private fun breakParenTranslation(line: String): String {
    val parenStart = line.indexOf('(')
    if (parenStart <= 0) return line
    val bark = line.substring(0, parenStart).trimEnd()
    if (bark.isEmpty()) return line
    val translation = line.substring(parenStart)
    return "$bark<br>$translation"
}

fun Player.callPet() {
    val pet = pet ?: return
    val steps: StepValidator = get()
    var target: Tile? = null
    for (tile in tile.spiral(pet.size)) {
        if (tile == this.tile) continue
        if (!steps.canFit(tile, pet.collision, pet.size, pet.blockMove)) continue
        target = tile
        break
    }
    if (target == null) {
        message("Your pet is too large to fit in the area you are standing in. Move into a larger space and try again.")
        return
    }
    pet.tele(target, clearMode = false)
    pet.watch(this)
}

fun Player.dismissPet() {
    val npc = pet ?: return
    NPCs.remove(npc)
    pet = null
    clear("pet_active_item")
    timers.stop("pet_tick")
    deactivateSummoningOrb()
}

private fun Player.deactivateSummoningOrb() {
    interfaces.close("pet_details")
    interfaces.close("familiar_details")
    sendScript("reset_summoning_orb")
    weakQueue("reset_familiar_vars", 1) {
        this["follower_details_name"] = 0
        this["follower_details_chathead"] = 0
        this["familiar_details_minutes_remaining"] = 0
        this["familiar_details_seconds_remaining"] = 0
        this["pet_details_growth_percentage"] = 0
        this["pet_details_hunger_percentage"] = 0
    }
}

private fun Player.canSummonPet(row: RowDefinition): Boolean {
    if (pet != null || follower != null || get("pet_active_item", "").isNotBlank()) {
        return false
    }
    // Mirror summonPet: the gating skill is row-driven (Slayer for Soul Wars
    // pets, Dungeoneering for sneakerpeeper) and only defaults to Summoning.
    val skill = row.skillOrNull("skill") ?: Skill.Summoning
    return hasMax(skill, row.int("summoning_level"))
}

private suspend fun Player.dropPet(row: RowDefinition, itemId: String) {
    // Run the precondition gate before the cat-amulet flavour dialogue,
    // otherwise a player who can't summon (no level, follower active, etc.)
    // sits through three chathead lines just to get a failure message.
    if (!canSummonPet(row)) {
        summonPet(row, itemId, restart = false)
        return
    }
    val amulet = hasCatspeakAmulet()
    if (row.isCatLike() && amulet) {
        summonCatWithAmulet(row)
    }
    if (summonPet(row, itemId, restart = false)) {
        if (row.isCatLike() && !amulet) {
            // pet is wired up in summonPet's own +2 weakQueue; wait a tick past
            // that so pet?.say() targets the newly-summoned NPC.
            weakQueue("cat_drop_meow", 3) {
                pet?.say("Miaow!")
            }
        }
    }
}

class PetScripts : Script {

    init {
        val rows = allPetRows()
        val itemIds = rows.flatMap {
            listOfNotNull(it.itemOrNull("baby_item"), it.itemOrNull("grown_item"), it.itemOrNull("overgrown_item"))
        }.toSet().joinToString(",")
        val npcIds = rows.flatMap {
            listOfNotNull(it.npcOrNull("baby_npc"), it.npcOrNull("grown_npc"), it.npcOrNull("overgrown_npc"))
        }.toSet().joinToString(",")

        itemOption("Drop", itemIds) { (item) ->
            val row = petRowForItem(item.id) ?: return@itemOption
            dropPet(row, item.id)
        }

        itemOption("Release", itemIds) { (item) ->
            val row = petRowForItem(item.id) ?: return@itemOption
            dropPet(row, item.id)
        }

        npcOperate("Pick-up", npcIds) { interact ->
            val owner = pet
            if (owner == null || owner.index != interact.target.index) {
                message("This isn't your pet.")
                return@npcOperate
            }
            if (hasCatspeakAmulet() && isAdultCat(owner)) {
                pickupCatWithAmulet(owner)
            } else {
                pickupPet()
            }
        }

        npcOperate("Talk-to", npcIds) { interact ->
            val owner = pet
            if (owner == null || owner.index != interact.target.index) {
                message("This isn't your pet.")
                return@npcOperate
            }
            val row = petRowForNpc(interact.target.id) ?: return@npcOperate
            when {
                row.isCatLike() -> {
                    if (row.stageForNpc(owner.id) == PetStage.Baby) {
                        resetKittenLoneliness(row.rowId)
                    }
                    if (hasCatspeakAmulet() && isAdultCat(owner)) {
                        talkToCatWithAmulet(owner)
                    } else {
                        talkToCatPlain(owner)
                    }
                }
                row.dogBreed() != null -> talkToDog(row, owner)
                else -> talkToPet(row, owner)
            }
        }

        playerSpawn {
            val itemId = get("pet_active_item", "")
            if (itemId.isBlank()) return@playerSpawn
            val row = petRowForItem(itemId) ?: return@playerSpawn
            set("pet_index", -1)
            variables.send("follower_details_name")
            variables.send("follower_details_chathead")
            variables.send("follower_details_chathead_animation")
            summonPet(row, itemId, restart = true)
        }
    }
}
