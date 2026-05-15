package content.skill.summoning.pet

import content.entity.player.dialogue.type.statement
import content.skill.summoning.follower
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.definition.ItemDefinitions
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
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Tile

/**
 * Player slot for the currently-summoned pet NPC. Backed by `pet_index`.
 *
 * Mutually exclusive with `Player.follower` (the combat-familiar slot): a
 * player has at most one follower at a time. `summonPet` / `summonFamiliar`
 * both refuse to spawn when the *other* slot is occupied.
 */
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

/**
 * Spawns the given pet as a follower at the player's tile.
 *
 * @param def the pet definition (registry entry).
 * @param itemId current-stage item string id.
 * @param restart true when re-spawning on login; suppresses welcome messaging.
 */
fun Player.summonPet(def: PetDefinition, itemId: String, restart: Boolean = false): Boolean {
    if (pet != null || follower != null) {
        if (!restart) message("You already have a follower.")
        return false
    }
    if (!has(Skill.Summoning, def.summoningLevel)) {
        message("You need a Summoning level of ${def.summoningLevel} to raise this pet.")
        return false
    }
    val stage = def.stageForItem(itemId) ?: return false
    val npcStringId = def.npcFor(stage) ?: return false
    val spawned = NPCs.add(npcStringId, tile)
    spawned.mode = Follow(spawned, this)
    set("pet_active_item", itemId)
    if (!restart) {
        anim("climb_down")
    }
    softQueue("summon_pet", 2) {
        player.pet = spawned
        player.timers.start("pet_tick")
        // Activates the summoning orb on the minimap. The orb's CS2 keys off
        // these varps/varbits (set by `updateFamiliarInterface` for familiars);
        // we set them here so the orb shows its options panel for pets too.
        player.updatePetInterface()
    }
    return true
}

/**
 * Returns the spawned pet to the player's inventory (Pick-up option).
 */
/** Pickup-pet convenience: looks up [PetDefinitions] via Koin. */
fun Player.pickupPet(): Boolean = pickupPet(get<PetDefinitions>())

fun Player.pickupPet(definitions: PetDefinitions): Boolean {
    val npc = pet ?: return false
    val def = definitions.forNpc(npc.id) ?: return false
    val stage = def.stageForNpc(npc.id) ?: return false
    val itemId = def.itemFor(stage) ?: return false
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

/**
 * Opens the familiar details interface for the player's pet. Mirrors
 * [content.skill.summoning.updateFamiliarInterface] so the summoning orb's
 * "Follower Details" left-click works for pet followers as well.
 */
fun Player.updatePetInterface() {
    val pet = pet ?: return
    interfaces.open("familiar_details")
    // `follower_details_name` is read by the cache CS2 as an item id; the
    // familiar path passes the pouch id. For pets we pass the *current-stage
    // pet item* id, so the panel renders the pet's actual item name (e.g.
    // "Pet kitten", "Pet cat", "Hellcat") instead of a blank slot.
    val itemStringId = get("pet_active_item", "")
    val itemIntId = ItemDefinitions.getOrNull(itemStringId)?.id ?: 0
    set("follower_details_name", itemIntId)
    set("follower_details_chathead", pet.def.id)
    set("follower_details_chathead_animation", pet.id)
    // On relogin the persisted values match what we just wrote, so
    // `PlayerVariables.set` short-circuits and never transmits a packet.
    // Force-send so the client always receives the current state.
    variables.send("follower_details_name")
    variables.send("follower_details_chathead")
    variables.send("follower_details_chathead_animation")
    sendPetDetailsStats()
}

fun Player.sendPetDetailsStats() {
    val itemStringId = get("pet_active_item", "")
    if (itemStringId.isBlank()) return
    val def = get<PetDefinitions>().forItem(itemStringId) ?: return
    val growth = getPetGrowth(def.id).toInt().coerceIn(0, 100)
    val hunger = getPetHunger(def.id).toInt().coerceIn(0, 100)
    val packed = (growth shl 1) or (hunger shl 9)
    set("pet_details_stats", packed)
    variables.send("pet_details_stats")
}

/**
 * Plays the pet's wiki-flavoured Talk-to dialogue. Lines prefixed `pet:` route
 * to the pet NPC as overhead chat (so they appear as a speech bubble); the
 * rest fire as narrator-style `statement`s in the chatbox.
 *
 * Falls back to a single random ambient phrase when the pet has no
 * `talk_lines` configured, so the option is never a dead click.
 */
suspend fun Player.talkToPet(def: PetDefinition, pet: NPC) {
    if (def.talkLines.isEmpty()) {
        def.ambientPhrases.randomOrNull()?.let { pet.say(it) }
        return
    }
    for (line in def.talkLines) {
        if (line.startsWith("pet:")) {
            pet.say(line.removePrefix("pet:").trim())
        } else {
            statement(line)
        }
    }
}

/**
 * Spiral-finds a free tile adjacent to the player and teleports the pet there.
 * Mirrors [content.skill.summoning.callFollower] for the pet slot. Used by the
 * summoning orb's "Call Follower" action when the active follower is a pet.
 */
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

/**
 * Removes the active pet without returning the item. Used by run-away (hunger
 * 100) and the kitten "Shoo away" option.
 */
fun Player.dismissPet() {
    val npc = pet ?: return
    NPCs.remove(npc)
    pet = null
    set("pet_active_item", "")
    timers.stop("pet_tick")
    deactivateSummoningOrb()
}

/**
 * Resets the summoning orb to its "no follower" state — closes the familiar
 * details panel, runs the `reset_summoning_orb` clientscript, and clears the
 * varps/varbits the orb reads. Mirrors the tail of [content.skill.summoning.dismissFamiliar].
 */
private fun Player.deactivateSummoningOrb() {
    interfaces.close("familiar_details")
    sendScript("reset_summoning_orb")
    softQueue("reset_familiar_vars", 1) {
        player["follower_details_name"] = 0
        player["follower_details_chathead"] = 0
        player["familiar_details_minutes_remaining"] = 0
        player["familiar_details_seconds_remaining"] = 0
        player["pet_details_stats"] = 0
    }
}

class PetScripts(private val definitions: PetDefinitions) : Script {
    init {
        for (def in definitions.all) {
            registerItemOption(def, def.babyItem)
            def.grownItem?.let { registerItemOption(def, it) }
            def.overgrownItem?.let { registerItemOption(def, it) }
            registerNpcOption(def, def.babyNpc)
            def.grownNpc?.let { registerNpcOption(def, it) }
            def.overgrownNpc?.let { registerNpcOption(def, it) }
        }

        playerSpawn {
            val itemId = get("pet_active_item", "")
            if (itemId.isBlank()) return@playerSpawn
            val def = definitions.forItem(itemId) ?: return@playerSpawn
            set("pet_index", -1)
            summonPet(def, itemId, restart = true)
        }
    }

    private fun registerItemOption(def: PetDefinition, itemId: String) {
        // Most pet items expose only "Drop" in cache options; clockwork cat exposes "Release"
        itemOption("Drop", itemId) {
            if (summonPet(def, itemId, restart = false)) {
                inventory.remove(itemId)
            }
        }
        itemOption("Release", itemId) {
            if (summonPet(def, itemId, restart = false)) {
                inventory.remove(itemId)
            }
        }
    }

    private fun registerNpcOption(def: PetDefinition, npcId: String) {
        npcOperate("Pick-up", npcId) { interact ->
            val owner = pet
            if (owner == null || owner.index != interact.target.index) {
                message("This isn't your pet.")
                return@npcOperate
            }
            pickupPet(definitions)
        }
        npcOperate("Talk-to", npcId) { interact ->
            val owner = pet
            if (owner == null || owner.index != interact.target.index) {
                message("This isn't your pet.")
                return@npcOperate
            }
            talkToPet(def, owner)
        }
    }
}
