package content.skill.summoning.pet

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.skill.summoning.follower
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
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
        return NPCs.indexed(index)
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
    if (!has(Skill.Summoning, level)) {
        message("You need a Summoning level of $level to raise this pet.")
        return false
    }
    val stage = row.stageForItem(itemId) ?: return false
    val npcStringId = row.npcFor(stage) ?: return false
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
    set("pet_active_item", "")
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
    interfaces.open(ifaceId)
    val itemStringId = get("pet_active_item", "")
    val itemIntId = ItemDefinitions.getOrNull(itemStringId)?.id ?: 0
    set("follower_details_name", itemIntId)
    set("follower_details_chathead", pet.def.id)
    set("follower_details_chathead_animation", pet.id)
    sendPetDetailsStats()
}

fun Player.sendPetDetailsStats() {
    val itemStringId = get("pet_active_item", "")
    if (itemStringId.isBlank()) return
    val row = petRowForItem(itemStringId) ?: return
    val growth = (getPetGrowth(row.rowId) / 100).coerceIn(0, 100)
    val hunger = (getPetHunger(row.rowId) / 100).coerceIn(0, 100)
    val packed = (growth shl 1) or (hunger shl 9)
    set("pet_details_stats", packed)
    variables.send("pet_details_stats")
}

suspend fun Player.talkToPet(row: RowDefinition, pet: NPC) {
    val stageKey = row.stageForNpc(pet.id)?.name?.lowercase() ?: ""
    val candidates = setOf(row.rowId, row.petTalksKey())
    val rows = Tables.get("pet_talks").rows().filter {
        val stages = it.string("stage")
        it.string("pet") in candidates && (stages.isEmpty() || stages.split(',').any { s -> s.trim() == stageKey })
    }
    val matchingConditional = rows.filter { matchesPetCondition(it.string("condition")) }
    val chosen = matchingConditional.randomOrNull()
        ?: rows.filter { it.string("condition").isBlank() }.randomOrNull()
    if (chosen == null) {
        row.ambientPhrases().randomOrNull()?.let { pet.say(it) }
        return
    }
    val fallbackAnim = EnumDefinitions.get("pet_details_chathead_animations_normal").defaultInt
    for (line in chosen.stringList("lines")) {
        when {
            line.startsWith("npc:") -> npc(pet.id, fallbackAnim, line.removePrefix("npc:").trim())
            line.startsWith("player:") -> player<Happy>(line.removePrefix("player:").trim())
            line.startsWith("overhead:") -> pet.say(line.removePrefix("overhead:").trim())
            line.startsWith("[") && line.endsWith("]") -> statement(line.removePrefix("[").removeSuffix("]").trim())
            else -> statement(line)
        }
    }
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
    set("pet_active_item", "")
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
        this["pet_details_stats"] = 0
    }
}

private suspend fun Player.dropPet(row: RowDefinition, itemId: String) {
    val amulet = hasCatspeakAmulet()
    if (row.isCatLike() && amulet) {
        summonCatWithAmulet(row)
    }
    if (summonPet(row, itemId, restart = false)) {
        inventory.remove(itemId)
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
