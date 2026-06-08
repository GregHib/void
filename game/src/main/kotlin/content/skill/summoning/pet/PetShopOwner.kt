package content.skill.summoning.pet

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.suspend.pauseInt

private const val PUPPY_PRICE = 500
private const val SHARD_PRICE = 25

private val logger = InlineLogger()

private data class Breed(val option: String, val petId: String, val puppyItem: String)

/** Dog breeds sold by the pet shop, ordered to match the iface 668 ("Pick a puppy") component indices. */
private fun dogBreeds(): List<Breed> = Tables.get("dog_breeds").rows().map {
    Breed(it.string("option"), it.string("pet_id"), it.item("puppy_item"))
}

private fun Player.alreadyHasDog(): Boolean {
    val active = get("pet_active_item", "")
    val breeds = dogBreeds()
    if (active.isNotBlank() && breeds.any { active.startsWith(it.petId) }) return true
    return breeds.any { breed ->
        inventory.contains(breed.puppyItem) || inventory.contains(breed.petId)
    }
}

class PetShopOwner : Script {

    init {
        npcOperate("Talk-to", "pet_shop_owner_yanille,pet_shop_owner_taverley") { interact ->
            mainMenu(interact.target)
        }

        npcOperate("Trade", "pet_shop_owner_yanille,pet_shop_owner_taverley") {
            openShop("pet_shop")
        }

        npcOperate("Sell-shards", "pet_shop_owner_yanille,pet_shop_owner_taverley") { interact ->
            player<Quiz>("Are you interested in buying spirit shards?")
            spiritShards(interact.target)
        }

        // iface 668 may emit either a continue-dialogue packet or a regular
        // interface-option packet depending on how the cache wires up each
        // button, so register both dispatch paths against the same resume.
        continueDialogue("dialogue_pick_a_puppy:*") { id ->
            val index = dogBreeds().indexOfFirst { it.petId == id.substringAfter(":") }
            if (index >= 0) (suspension as? Suspension.IntEntry)?.resume(index)
        }
        interfaceOption(id = "dialogue_pick_a_puppy:*") { option ->
            val index = dogBreeds().indexOfFirst { it.petId == option.component }
            if (index >= 0) (suspension as? Suspension.IntEntry)?.resume(index)
        }
    }

    private suspend fun Player.mainMenu(owner: NPC) {
        choice {
            option<Quiz>("Can I see your shop, please?") {
                npc<Happy>(owner.id, "Of course!")
                openShop("pet_shop")
            }
            option<Quiz>("How much is that puppy in the window?") {
                puppyTree(owner)
            }
            option<Quiz>("So, what sorts of pets are available?") {
                availablePets(owner)
            }
            option<Quiz>("Are you interested in buying spirit shards?") {
                spiritShards(owner)
            }
        }
    }

    private suspend fun Player.puppyTree(owner: NPC) {
        if (alreadyHasDog()) {
            npc<Neutral>(owner.id, "You asked me that when you last picked up a dog, and I can't sell you another.")
            return
        }
        if (inventory.isFull()) {
            npc<Neutral>(owner.id, "Where are you going to put it, on your head? You can't buy a puppy unless you have space to hold it.")
            player<Neutral>("Good point, I'll go bank some items.")
            return
        }
        npc<Happy>(owner.id, "The one with the waggly tail?")
        if (!open("dialogue_pick_a_puppy")) return
        val index = pauseInt()
        close("dialogue_pick_a_puppy")
        val breed = dogBreeds().getOrNull(index) ?: return
        buyPuppy(owner, breed)
    }

    private suspend fun Player.buyPuppy(owner: NPC, breed: Breed) {
        player<Quiz>("No, the ${breed.option.lowercase()}.")
        npc<Neutral>(owner.id, "$PUPPY_PRICE gold.")
        player<Quiz>("Isn't that a little steep?")
        npc<Pleased>(
            owner.id,
            "Well, if we gave them away for free then people would just buy them and dump them without a care. " +
                "Dogs are a big responsibility and should be cared for. If a person is unwilling to invest $PUPPY_PRICE coins, " +
                "then they don't deserve to have the puppy in the first place. So, do you still want one?",
        )
        choice {
            option<Happy>("Okay, I'll take the ${breed.option}.") {
                completePuppySale(owner, breed)
            }
            option<Neutral>("No thanks.")
        }
    }

    private suspend fun Player.completePuppySale(owner: NPC, breed: Breed) {
        if (inventory.isFull()) {
            npc<Neutral>(owner.id, "Where are you going to put it, on your head? You can't buy a puppy unless you have space to hold it.")
            player<Neutral>("Good point, I'll go bank some items.")
            return
        }
        if (!inventory.contains("coins", PUPPY_PRICE)) {
            npc<Neutral>(owner.id, "You don't seem to have $PUPPY_PRICE coins on you. Come back when you do!")
            return
        }
        if (!inventory.remove("coins", PUPPY_PRICE)) return
        if (!inventory.add(breed.puppyItem)) {
            // Roll back the coins if we somehow can't fit the puppy.
            if (!inventory.add("coins", PUPPY_PRICE)) {
                logger.warn { "Failed to refund $PUPPY_PRICE coins to $accountName after puppy add failed" }
                message("Something went wrong; please contact a member of staff.")
            }
            return
        }
        npc<Happy>(owner.id, "There you go! I hope you two get on.")
    }

    private suspend fun Player.availablePets(owner: NPC) {
        npc<Happy>(owner.id, "Well, here we sell dogs, but we also have supplies for any other creatures you might want to raise.")
        player<Quiz>("Such as?")
        npc<Happy>(
            owner.id,
            "Well, we sell nuts. Those can be used to feed squirrels. If you want to capture a squirrel, you'll need to use the nuts " +
                "on the trap you set, as the little scamps won't be fooled by anything else.",
        )
        player<Happy>("I'll bear that in mind!")
        npc<Happy>(
            owner.id,
            "There are also a number of birds that live in the woodlands of the world. If you can find their eggs then you can use " +
                "the incubator over there to hatch it. So long as you are the first thing they see out of the shell, they will follow " +
                "you anywhere. After that, you just need to feed the chick ground fishing bait until it's old enough to eat it solid.",
        )
        player<Pleased>("I'll make sure to keep an eye on them if I go anywhere dangerous.")
        npc<Happy>(
            owner.id,
            "There are also a number of fabulous and exotic lizards in Karamja. Some can be caught easily in a box trap, while others " +
                "will need to be raised from an egg.",
        )
        player<Quiz>("Will the incubator work for them, too?")
        npc<Happy>(owner.id, "Of course! I'll keep an eye on all the eggs you put in there, so they will never end up hard-boiled.")
        player<Pleased>("Thank goodness!")
        npc<Happy>(
            owner.id,
            "The geckos of Karamja are quite easy to trap, like raccoons. Both will investigate a trap happily without any special bait. " +
                "Monkeys are a different story, however!",
        )
        player<Quiz>("What do you mean?")
        npc<Pleased>(
            owner.id,
            "Well, they are clever little things and can easily get out of a box trap, unless they are stuck. The easiest way to do that " +
                "is to put a banana into the workings. They will hang on tight, and never let go, even when the trap closes!",
        )
        player<Happy>("Thanks a lot, you've been very helpful!")
        npc<Happy>(owner.id, "It's always a pleasure to help a fellow animal-lover. Come back and visit soon.")
    }

    private suspend fun Player.spiritShards(owner: NPC) {
        npc<Happy>(owner.id, "I certainly am. Lots of them, too!")
        val held = inventory.count("spirit_shards")
        if (held <= 0) {
            player<Neutral>("Thanks, I'll bear that in mind.")
            return
        }
        val requested = intEntry("How many will you sell? ($SHARD_PRICE coins each, you have $held)")
        if (requested <= 0) return
        val toSell = requested.coerceAtMost(held)
        if (!inventory.remove("spirit_shards", toSell)) return
        val payout = toSell * SHARD_PRICE
        if (!inventory.add("coins", payout)) {
            // Out of room; refund.
            if (!inventory.add("spirit_shards", toSell)) {
                logger.warn { "Failed to refund $toSell spirit shards to $accountName after coin payout failed" }
                message("Something went wrong; please contact a member of staff.")
                return
            }
            message("You don't have enough room in your inventory.")
            return
        }
        statement("You sell $toSell spirit shard${if (toSell == 1) "" else "s"} for $payout coins.")
    }
}
