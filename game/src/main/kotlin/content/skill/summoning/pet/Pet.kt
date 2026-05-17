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
    weakQueue("summon_pet", 2) {
        pet = spawned
        timers.start("pet_tick")
        updatePetInterface()
    }
    return true
}

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

fun Player.updatePetInterface() {
    val pet = pet ?: return
    interfaces.open("pet_details")
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
    val def = get<PetDefinitions>().forItem(itemStringId) ?: return
    val growth = getPetGrowth(def.id).toInt().coerceIn(0, 100)
    val hunger = getPetHunger(def.id).toInt().coerceIn(0, 100)
    val packed = (growth shl 1) or (hunger shl 9)
    set("pet_details_stats", packed)
    variables.send("pet_details_stats")
}

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

private suspend fun Player.dropPet(def: PetDefinition, itemId: String) {
    val amulet = hasCatspeakAmulet()
    if (def.isCatLike && amulet) {
        summonCatWithAmulet(def, itemId)
    }
    if (summonPet(def, itemId, restart = false)) {
        inventory.remove(itemId)
        if (def.isCatLike && !amulet) {
            // pet is wired up in summonPet's own +2 weakQueue; wait a tick past
            // that so pet?.say() targets the newly-summoned NPC.
            weakQueue("cat_drop_meow", 3) {
                pet?.say("Miaow!")
            }
        }
    }
}

class PetScripts(private val definitions: PetDefinitions) : Script {

    init {
        val itemIds = definitions.all.flatMap {
            listOfNotNull(it.babyItem, it.grownItem, it.overgrownItem)
        }.toSet().joinToString(",")
        val npcIds = definitions.all.flatMap {
            listOfNotNull(it.babyNpc, it.grownNpc, it.overgrownNpc)
        }.toSet().joinToString(",")

        itemOption("Drop", itemIds) { (item) ->
            val def = definitions.forItem(item.id) ?: return@itemOption
            dropPet(def, item.id)
        }
        itemOption("Release", itemIds) { (item) ->
            val def = definitions.forItem(item.id) ?: return@itemOption
            dropPet(def, item.id)
        }

        npcOperate("Pick-up", npcIds) { interact ->
            val owner = pet
            if (owner == null || owner.index != interact.target.index) {
                message("This isn't your pet.")
                return@npcOperate
            }
            if (hasCatspeakAmulet() && isAdultCat(owner)) {
                pickupCatWithAmulet(owner, definitions)
            } else {
                pickupPet(definitions)
            }
        }
        npcOperate("Talk-to", npcIds) { interact ->
            val owner = pet
            if (owner == null || owner.index != interact.target.index) {
                message("This isn't your pet.")
                return@npcOperate
            }
            val def = definitions.forNpc(interact.target.id) ?: return@npcOperate
            if (def.isCatLike) {
                if (hasCatspeakAmulet() && isAdultCat(owner)) {
                    talkToCatWithAmulet(owner)
                } else {
                    talkToCatPlain(owner)
                }
            } else {
                talkToPet(def, owner)
            }
        }

        playerSpawn {
            val itemId = get("pet_active_item", "")
            if (itemId.isBlank()) return@playerSpawn
            val def = definitions.forItem(itemId) ?: return@playerSpawn
            set("pet_index", -1)
            variables.send("follower_details_name")
            variables.send("follower_details_chathead")
            variables.send("follower_details_chathead_animation")
            summonPet(def, itemId, restart = true)
        }
    }
}
