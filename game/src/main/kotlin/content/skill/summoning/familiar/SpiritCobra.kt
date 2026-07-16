package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.FamiliarSpecialMoves
import content.skill.summoning.castFamiliarSpecial
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

/** The eggs Ophidian Incubation can hatch into their spirit cockatrice-family counterparts. */
private val INCUBATION_EGGS = mapOf(
    "egg" to "cockatrice_egg",
    "birds_egg_green" to "guthatrice_egg",
    "birds_egg_blue" to "saratrice_egg",
    "birds_egg_red" to "zamatrice_egg",
    "penguin_egg" to "pengatrice_egg",
    "raven_egg" to "coraxatrice_egg",
    "vulture_egg" to "vulatrice_egg",
)

class SpiritCobra : Script {
    init {
        // A plain click on the cast button has no item to work on - point at the real triggers.
        FamiliarSpecialMoves.instant("spirit_cobra_familiar") {
            message("To cast Ophidian Incubation, use the Cast option or an egg on the spirit cobra.")
            false
        }

        // Ophidian Incubation - the spirit cobra transmutes an egg into the cockatrice-family egg of
        // the matching god bird. Cast on an egg, or use the egg on the familiar - both run through
        // the scroll + points gate.
        FamiliarSpecialMoves.item("spirit_cobra_familiar") { item -> incubate(item.id) }

        for (egg in INCUBATION_EGGS.keys) {
            itemOnNPCOperate(egg, "spirit_cobra_familiar*") { (npc, item) ->
                if (npc != follower) {
                    return@itemOnNPCOperate
                }
                castFamiliarSpecial { incubate(item.id) }
            }
        }

        npcOperate("Interact", "spirit_cobra_familiar") {
            if (equipped(EquipSlot.Ring).id in setOf("ring_of_charos", "ring_of_charos_a", "ring_of_charos_ai")) {
                npc<Neutral>("You are under my power!")
                player<Happy>("No, you are under my power!")
                npc<Neutral>("No, you are under my power!")
                player<Happy>("No, my power is greater!")
                npc<Neutral>("Your power is the greater...")
                player<Happy>("Your powers are no match for mine!")
                npc<Neutral>("You are convinced you have won this argument...")
                player<Happy>("I won the argument...yay!")
                npc<Neutral>("*Manic serpentine laughter*")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Do we have to do thissss right now?")
                    player<Happy>("Yes, I'm afraid so.")
                    npc<Neutral>("You are under my sssspell...")
                    player<Happy>("I will do as you ask...")
                    npc<Neutral>("Do we have to do thissss right now?")
                    player<Happy>("Not at all, I had just finished!")
                }
                1 -> {
                    npc<Neutral>("You are feeling ssssleepy...")
                    player<Happy>("I am feeling sssso ssssleepy...")
                    npc<Neutral>("You will bring me lotssss of sssstuff!")
                    player<Happy>("What ssssort of sssstuff?")
                    npc<Neutral>("What ssssort of sssstuff have you got?")
                    player<Happy>("All kindsss of sssstuff.")
                    npc<Neutral>("Then just keep bringing sssstuff until I'm ssssatissssfied!")
                }
                2 -> {
                    npc<Neutral>("I'm bored, do ssssomething to entertain me...")
                    player<Happy>("Errr, I'm not here to entertain you, you know.")
                    npc<Neutral>("You will do as I assssk...")
                    player<Happy>("Your will is my command...")
                    npc<Neutral>("I'm bored, do ssssomething to entertain me...")
                    player<Happy>("I'll dance for you!")
                }
                3 -> {
                    npc<Neutral>("I am king of the world!")
                    player<Happy>("You know, I think there is a law against snakes being the king.")
                    npc<Neutral>("My will is your command...")
                    player<Happy>("I am yours to command...")
                    npc<Neutral>("I am king of the world!")
                    player<Happy>("All hail King Serpentor!")
                }
            }
        }
    }

    /** The Ophidian Incubation effect: transmute a god-bird egg into its cockatrice counterpart. */
    private fun Player.incubate(itemId: String): Boolean {
        val product = INCUBATION_EGGS[itemId]
        if (product == null) {
            message("Your spirit cobra can only incubate eggs.")
            return false
        }
        if (!inventory.replace(itemId, product)) {
            return false
        }
        val cobra = follower ?: return false
        cobra.anim("ophidian_incubation")
        cobra.gfx("ophidian_incubation")
        message("Your spirit cobra incubates the egg with its gaze.", ChatType.Filter)
        return true
    }
}
