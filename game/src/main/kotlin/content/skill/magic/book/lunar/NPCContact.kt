package content.skill.magic.book.lunar

import content.area.kandarin.ardougne.larryHideAndSeekChat
import content.area.wilderness.abyss.darkMageChat
import content.skill.magic.spell.removeSpellItems
import content.skill.slayer.master.slayerMasterChat
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.network.login.protocol.encode.npcDialogueHead
import world.gregs.voidps.type.random

class NPCContact : Script {

    private val hidden = listOf(
        "honest_jimmy",
        "bert",
        "advisor_ghrim",
        "spria",
        "lanthus",
        "achtryn",
        "cyrisus",
    )

    private val contactable = listOf(
        "turael",
        "mazchna",
        "vannaka",
        "chaeldar",
        "sumona",
        "duradel",
        "lapalok",
        "dark_mage",
        "larry",
    )

    init {
        interfaceOption("Cast", "lunar_spellbook:npc_contact") {
            if (hasClock("action_delay")) {
                return@interfaceOption
            }
            open("npc_contact")
        }

        interfaceOpened("npc_contact") {
            for (npc in hidden) {
                interfaces.sendVisibility("npc_contact", "${npc}_head", false)
                interfaces.sendVisibility("npc_contact", "${npc}_name", false)
            }
            // The cache reuses Murphy's slot; replace him with the Abyss's dark mage
            val component = InterfaceDefinitions.getComponent("npc_contact", "dark_mage_head")
            if (component != null) {
                client?.npcDialogueHead(component.id, NPCDefinitions.get("dark_mage").id)
            }
            interfaces.sendText("npc_contact", "dark_mage_name", "Dark mage")
        }

        interfaceOption("*", "npc_contact:*") {
            val npc = it.component.removeSuffix("_head").removeSuffix("_name")
            if (npc == "close") {
                close("npc_contact")
                return@interfaceOption
            }
            val contact = if (npc == "random") contactable[random.nextInt(contactable.size)] else npc
            if (contact !in contactable) {
                return@interfaceOption
            }
            close("npc_contact")
            if (!removeSpellItems("npc_contact")) {
                return@interfaceOption
            }
            start("action_delay", 2)
            anim("lunar_cast_charge")
            gfx("npc_contact")
            sound("npc_contact")
            exp(Skill.Magic, Tables.int("spells.npc_contact.xp") / 10.0)
            delay(2)
            contact(contact)
        }
    }

    private suspend fun Player.contact(id: String) {
        // Some contacts have no world spawn under their own id; Lapalok replaced
        // Duradel at Shilo Village and Larry only spawns as his Ardougne variants
        val candidates = when (id) {
            "duradel" -> listOf("duradel", "lapalok_shilo_village")
            "lapalok" -> listOf("lapalok_shilo_village", "duradel")
            "larry" -> listOf("larry_ardougne_normal", "larry_ardougne", "larry_ardougne_2")
            else -> listOf(id)
        }
        val npc = candidates.firstNotNullOfOrNull { candidate -> NPCs.firstOrNull { it.id == candidate } }
        if (npc == null) {
            message("${id.toSentenceCase()} is too busy to talk right now.")
            return
        }
        talkWith(npc)
        when (id) {
            // Lapalok stands in for Duradel and hands out his task list
            "lapalok" -> slayerMasterChat("duradel")
            "turael", "mazchna", "vannaka", "chaeldar", "sumona", "duradel" -> slayerMasterChat(id)
            "dark_mage" -> darkMageChat()
            "larry" -> larryHideAndSeekChat()
        }
    }
}
