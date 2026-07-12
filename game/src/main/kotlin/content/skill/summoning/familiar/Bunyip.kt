package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.proj.shoot
import content.skill.summoning.FamiliarSpecialMoves
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.random

class Bunyip : Script {
    init {
        // A plain click on the cast button has no item to work on - point at the real trigger.
        FamiliarSpecialMoves.instant("bunyip_familiar") {
            message("To cast Swallow Whole, use the Cast option on a raw fish.")
            false
        }

        // Swallow Whole - the bunyip gulps down a raw fish the owner could cook, healing them for
        // the cooked fish's worth with no eat delay. Cast on a raw fish through the scroll +
        // points gate; using the fish directly on the bunyip transmutes it instead (below).
        FamiliarSpecialMoves.item("bunyip_familiar") { item -> swallowWhole(item) }

        // Using a raw fish on the bunyip transmutes it into water runes - one up to a tenth of the
        // cooked fish's healing - free of any scroll.
        itemOnNPCOperate("raw_*", "bunyip_familiar*") { (npc, item) ->
            if (npc != follower) {
                return@itemOnNPCOperate
            }
            val row = Rows.getOrNull("cooking.${item.id}")
            val cookedId = row?.item("cooked")?.ifEmpty { item.id.replace("raw", "cooked") }
            val heals: IntRange? = cookedId?.let { ItemDefinitions.getOrNull(it)?.getOrNull("heals") }
            if (heals == null) {
                message("Your bunyip only transmutes raw fish.")
                return@itemOnNPCOperate
            }
            if (!inventory.remove(item.id)) {
                return@itemOnNPCOperate
            }
            val runes = random.nextInt((heals.last / 10).coerceAtLeast(1)) + 1
            inventory.add("water_rune", runes)
            anim("bunyip_transmute")
            // The tossed fish flies from the player to the bunyip's mouth.
            follower?.let { bunyip -> shoot("bunyip_transmute_proj", bunyip) }
            message("Your bunyip transmutes the ${item.def.name.lowercase()} into some water runes.", ChatType.Filter)
        }

        npcOperate("Interact", "bunyip_familiar") {
            val fish = listOf(
                "raw_crayfish", "raw_shrimps", "raw_anchovies", "raw_sardine", "raw_giant_carp",
                "raw_herring", "raw_trout", "raw_salmon", "raw_pike", "raw_tuna", "raw_swordfish",
                "raw_lobster", "raw_mackerel", "raw_cod", "raw_bass", "leaping_trout", "raw_lava_eel",
                "leaping_salmon", "leaping_sturgeon", "raw_monkfish", "raw_shark", "raw_cavefish", "raw_rocktail",
            )
            if (fish.any { inventory.contains(it) }) {
                npc<Neutral>("I see you've got some fish there, mate.")
                player<Happy>("Yeah, but I might cook them up before I give them to you!")
                npc<Neutral>("Humans...always ruining good fishes.")
                player<Happy>("You know, some people prefer them cooked.")
                npc<Neutral>("Yeah. We call 'em freaks.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Where are we going and why is it not to the beach?")
                    player<Happy>("Well, we have a fair few places to go, but I suppose we could go to the beach if we get time.")
                    npc<Neutral>("Bonza! I'll get my board ready!")
                    player<Happy>("Well, even if we do go to the beach I don't know if we'll have time for that.")
                    npc<Neutral>("Awww, that's a drag...")
                }
                1 -> {
                    npc<Neutral>("Hey Bruce, can we go down to the beach t'day?")
                    player<Happy>("Well, I have a lot of things to do today but maybe later.")
                    npc<Neutral>("Bonza!")
                }
                2 -> {
                    npc<Neutral>("Pass me another bunch of shrimps, mate!")
                    player<Happy>("I don't know if I want any more water runes.")
                    npc<Neutral>("Righty, but I do know that I want some shrimps!")
                    player<Happy>("A fair point.")
                }
                3 -> {
                    npc<Neutral>("Sigh...")
                    player<Happy>("What's the matter?")
                    npc<Neutral>("I'm dryin' out in this sun, mate.")
                    player<Happy>("Well, what can I do to help?")
                    npc<Neutral>("Well, fish oil is bonza for the skin, ya know.")
                    player<Happy>("Oh, right, I think I see where this is going.")
                }
            }
        }
    }

    /** The Swallow Whole effect: gulp a cookable raw fish, healing the cooked value instantly. */
    private fun Player.swallowWhole(item: Item): Boolean {
        val row = Rows.getOrNull("cooking.${item.id}")
        if (row == null || !item.id.startsWith("raw_")) {
            message("Your bunyip only swallows raw fish.")
            return false
        }
        val cookedId = row.item("cooked").ifEmpty { item.id.replace("raw", "cooked") }
        val heals: IntRange? = ItemDefinitions.getOrNull(cookedId)?.getOrNull("heals")
        if (heals == null) {
            message("Your bunyip only swallows raw fish.")
            return false
        }
        if (!has(Skill.Cooking, row.int("level"), message = true)) {
            return false
        }
        if (!inventory.remove(item.id)) {
            return false
        }
        val bunyip = follower ?: return false
        bunyip.anim("swallow_whole")
        bunyip.gfx("swallow_whole")
        levels.restore(Skill.Constitution, heals.random())
        message("Your bunyip swallows the ${item.def.name.lowercase()} whole, and you feel reinvigorated.", ChatType.Filter)
        return true
    }
}
