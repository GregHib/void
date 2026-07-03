package content.skill.summoning

import content.entity.gfx.areaGfx
import content.entity.player.bank.bank
import content.entity.proj.shoot
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.canFit
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.type.random

/**
 * Utility familiar specials: send-the-familiar-to-fight, call/ambush, ground-drop foragers, and
 * banking. Combat-engage moves register as [FamiliarSpecialMoves.npc]; the rest are instant casts.
 * Pack Yak's Winter Storage picks an inventory item, so it is wired with `itemOnNPCApproach`.
 */
class FamiliarUtilitySpecials : Script {

    private val grimyHerbs = listOf(
        "grimy_guam",
        "grimy_marrentill",
        "grimy_tarromin",
        "grimy_harralander",
        "grimy_ranarr",
        "grimy_irit",
        "grimy_avantoe",
        "grimy_kwuarm",
    )
    private val fruit = listOf("orange", "banana", "lemon", "lime", "papaya_fruit")
    private val rawFish = listOf("raw_trout", "raw_salmon", "raw_cod", "raw_pike")

    init {
        // Goad / Pester - the special simply sends the familiar at the target (with a Hunter feel).
        FamiliarSpecialMoves.npc("spirit_graahk_familiar", "spirit_mosquito_familiar") { target ->
            if (!familiarCanSpecial(target)) {
                return@npc false
            }
            commandFamiliarAttack(target, silent = true)
            true
        }

        // Ambush - teleport the familiar to the owner, ready to strike.
        FamiliarSpecialMoves.instant("spirit_kyatt_familiar") {
            callFollower()
            true
        }

        // Call to Arms - the Void familiars teleport their owner to the Void Knights' Outpost.
        FamiliarSpecialMoves.instant(
            "void_ravager_familiar",
            "void_shifter_familiar",
            "void_spinner_familiar",
            "void_torcher_familiar",
        ) {
            queue("call_to_arms", 1) {
                anim("call_to_arms")
                gfx("call_to_arms")
            }
            queue("call_to_arms_land", 3) {
                tele(2659, 2658, 0)
                anim("call_to_arms_land")
                gfx("call_to_arms_land")
            }
            true
        }

        // Cheese Feast - the albino rat produces 4 cheese into its own inventory
        FamiliarSpecialMoves.instant("albino_rat_familiar") {
            ensureBeastOfBurdenInventory()
            val free = beastOfBurdenCapacity - beastOfBurden.items.count { it.isNotEmpty() }
            if (free < 4) {
                message("Your familiar is too full to collect items.")
                return@instant false
            }
            familiarSelfSpecial(anim = "cheese_feast", sourceGfx = "cheese_feast") {
                beastOfBurden.add("cheese", 4)
                if (interfaces.contains("beast_of_burden")) {
                    syncBeastOfBurdenInterface()
                }
            }
        }

        // Egg Spawn - the spirit spider scatters red spider eggs onto free tiles around the player
        FamiliarSpecialMoves.instant("spirit_spider_familiar") {
            val steps: StepValidator = get()
            val eggTiles = tile.spiral(1).asSequence()
                .filter { it != tile && steps.canFit(it, collision, 1, blockMove) }
                .toList()
                .shuffled()
                .take(random.nextInt(5) + 1)
            familiarSelfSpecial(anim = "egg_spawn") {
                for (eggTile in eggTiles) {
                    areaGfx("egg_spawn", eggTile)
                }
                queue("egg_spawn", 1) {
                    for (eggTile in eggTiles) {
                        FloorItems.add(eggTile, "red_spiders_eggs", revealTicks = 120, disappearTicks = 30, owner = this)
                    }
                }
            }
        }

        // Generate Compost - cast on an empty compost bin to fill it with compost (10% supercompost).
        FamiliarSpecialMoves.obj("compost_mound_familiar") { obj ->
            if (!obj.id.startsWith("farming_compost_bin_")) {
                message("This scroll can only be used on an empty compost bin.")
                return@obj false
            }
            val variable = obj.id.removePrefix("farming_")
            if (get(variable, "empty") != "empty") {
                message("This scroll can only be used on an empty compost bin.")
                return@obj false
            }
            val familiar = follower ?: return@obj false
            familiar.face(obj.tile)
            familiar.anim("generate_compost")
            familiar.gfx("generate_compost")
            val flight = familiar.shoot("generate_compost_proj", obj.tile)
            queue("generate_compost", CLIENT_TICKS.toTicks(flight)) {
                areaGfx("generate_compost_bin", obj.tile)
                val superCompost = random.nextInt(10) == 0
                this[variable] = if (superCompost) "supercompost_15" else "compost_15"
            }
            true
        }

        // Herbcall - drop a random grimy herb at the player's feet.
        FamiliarSpecialMoves.instant("macaw_familiar") {
            dropForage(grimyHerbs[random.nextInt(grimyHerbs.size)])
        }

        // Fruitfall - drop a papaya plus a few random fruits.
        FamiliarSpecialMoves.instant("fruit_bat_familiar") {
            dropForage("papaya_fruit")
            repeat(random.nextInt(4)) {
                dropForage(fruit[random.nextInt(fruit.size)])
            }
            true
        }

        // Fish Rain - drop one or two random raw fish.
        FamiliarSpecialMoves.instant("ibis_familiar") {
            repeat(random.nextInt(2) + 1) {
                dropForage(rawFish[random.nextInt(rawFish.size)])
            }
            true
        }

        // Essence Shipment - bank all rune and pure essence carried.
        FamiliarSpecialMoves.instant("abyssal_titan_familiar") {
            val moved = bankAll("rune_essence") + bankAll("pure_essence")
            if (moved == 0) {
                message("You have no essence for your familiar to bank.")
                return@instant false
            }
            message("Your familiar banks your essence.")
            true
        }

        // Winter Storage - bank the item the player uses on the pack yak.
        itemOnNPCApproach("*", "pack_yak_familiar") { (npc, item) ->
            if (npc != follower) {
                return@itemOnNPCApproach
            }
            castFamiliarSpecial {
                if (!inventory.contains(item.id)) {
                    return@castFamiliarSpecial false
                }
                inventory.remove(item.id, 1)
                bank.add(item.id, 1)
                true
            }
        }
    }

    /** Drops [id] on the player's tile (it despawns after ~3 minutes), counting as a successful cast. */
    private fun Player.dropForage(id: String): Boolean {
        FloorItems.add(tile, id, disappearTicks = 300, owner = this)
        return true
    }

    /** Moves every [id] from the inventory to the bank, returning how many were moved. */
    private fun Player.bankAll(id: String): Int {
        val count = inventory.count(id)
        if (count > 0) {
            inventory.remove(id, count)
            bank.add(id, count)
        }
        return count
    }
}
