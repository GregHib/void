package content.skill.slayer

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class SlayerHelmet : Script {

    private val helmetParts = listOf("black_mask", "earmuffs", "face_mask", "nose_peg", "spiny_helmet")
    private val maskedEarmuffs = setOf("earmuffs", "face_mask")
    private val fullHelmetParts = listOf("slayer_helmet", "hexcrest", "focus_sight")

    init {
        itemOnItem("earmuffs", "face_mask") { _, _ ->
            replace(listOf("earmuffs", "face_mask"), listOf("masked_earmuffs"))
        }

        itemOption("Disassemble", "masked_earmuffs") {
            replace(listOf("masked_earmuffs"), listOf("earmuffs", "face_mask"))
        }

        for (from in helmetParts) {
            for (to in helmetParts) {
                if (from == to || (from in maskedEarmuffs && to in maskedEarmuffs)) {
                    continue
                }
                itemOnItem(from, to, bidirectional = false) { _, _ ->
                    assembleHelmet()
                }
            }
        }

        for (from in fullHelmetParts) {
            for (to in fullHelmetParts) {
                if (from == to) {
                    continue
                }
                itemOnItem(from, to, bidirectional = false) { _, _ ->
                    assembleFullHelmet()
                }
            }
        }

        itemOption("Disassemble", "slayer_helmet") {
            replace(listOf("slayer_helmet"), helmetParts)
        }

        itemOption("Disassemble", "full_slayer_helmet") {
            replace(listOf("full_slayer_helmet"), fullHelmetParts)
        }
    }

    private fun Player.assembleHelmet() {
        if (!contains("malevolent_masquerade")) {
            message("You don't know what to do with these parts. You should talk to an expert, perhaps they know how to assemble these parts.")
            return
        }
        if (!helmetParts.all { inventory.contains(it) }) {
            message("You need a nosepeg, face mask, earmuffs, spiny helmet and an uncharged black mask in your inventory in order to construct a fully-enhanced Slayer helmet.")
            return
        }
        if (!has(Skill.Crafting, 55, message = true)) {
            return
        }
        if (replace(helmetParts, listOf("slayer_helmet"))) {
            message("You combine all parts of the helmet.")
        }
    }

    private fun Player.assembleFullHelmet() {
        if (!fullHelmetParts.all { inventory.contains(it) }) {
            message("You need a slayer helmet, hexcrest and focus sight in your inventory in order to construct a full slayer helmet.")
            return
        }
        if (replace(fullHelmetParts, listOf("full_slayer_helmet"))) {
            message("You carefully modify the slayer helmet so the extra pieces fit.")
        }
    }

    private fun Player.replace(remove: List<String>, add: List<String>): Boolean {
        val success = inventory.transaction {
            for (item in remove) {
                remove(item)
            }
            for (item in add) {
                add(item)
            }
        }
        if (!success) {
            inventoryFull()
        }
        return success
    }
}
