package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.castFamiliarSpecial
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.random

class Pyrelord : Script {
    init {
        npcOperate("Interact", "pyrelord_familiar") { (target) ->
            if (target != follower) {
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("What are we doing here?")
                    player<Happy>("Whatever I feel like doing.")
                    npc<Neutral>("I was summoned by a greater demon once you know.")
                    npc<Neutral>("He said we'd see the world...")
                    player<Happy>("What happened?")
                    npc<Neutral>("He was slain; it was hilarious!")
                }
                1 -> {
                    npc<Neutral>("I used to be feared across five planes...")
                    player<Happy>("Oh dear, now you're going to be sad all day!")
                    npc<Neutral>("At least I won't be the only one.")
                }
                2 -> {
                    npc<Neutral>("I could teach you to smite your enemies with flames.")
                    player<Happy>("You're not the only one: we have runes to do that.")
                    npc<Neutral>("Runes? Oh, that's so cute!")
                    player<Happy>("Cute?")
                    npc<Neutral>("Well, not cute so much as tragic. I could teach you to do it without runes.")
                    player<Happy>("Really?")
                    npc<Neutral>("No.")
                }
                3 -> {
                    npc<Neutral>("Have you never been on fire?")
                    player<Happy>("You say that like it's a bad thing.")
                    npc<Neutral>("Isn't it? It gives me the heebie-jeebies!")
                    player<Happy>("You're afraid of something?")
                    npc<Neutral>("Yes: I'm afraid of being you.")
                    player<Happy>("I don't think he likes me...")
                }
            }
        }

        // Immense Heat - the pyrelord's flames stand in for a furnace, letting the owner craft
        // gold jewellery on the spot. Item-target special through the scroll + points gate.
        itemOnNPCOperate("gold_bar", "pyrelord_familiar") { (npc) ->
            if (npc != follower) {
                return@itemOnNPCOperate
            }
            castFamiliarSpecial {
                anim("immense_heat")
                gfx("immense_heat")
                open("make_mould${if (World.members) "_slayer" else ""}")
                true
            }
        }

        // The pyrelord and forge regent act as portable fire sources - they burn logs without a
        // tinderbox, granting the log's xp plus a small bonus for the demon's help.
        for (familiar in listOf("pyrelord_familiar", "forge_regent_familiar")) {
            itemOnNPCOperate("*logs*", familiar) { (target, item) ->
                if (target != follower) {
                    message("That's not your familiar.")
                    return@itemOnNPCOperate
                }
                val row = Rows.getOrNull("firemaking.${item.id}")
                if (row == null) {
                    message("The ${target.def.name.lowercase()} only burns logs.")
                    return@itemOnNPCOperate
                }
                val level = row.int("level")
                if (!has(Skill.Firemaking, level, true)) {
                    return@itemOnNPCOperate
                }
                if (GameObjects.getLayer(tile, ObjectLayer.GROUND) != null) {
                    message("You can't light a fire here.")
                    return@itemOnNPCOperate
                }
                if (!inventory.remove(item.id)) {
                    return@itemOnNPCOperate
                }
                anim("light_fire")
                exp(Skill.Firemaking, row.int("xp") / 10.0 + 10)
                val colour = row.string("colour")
                val life = row.int("life")
                GameObjects.add("fire_$colour", tile, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0, ticks = life)
                FloorItems.add(tile, "ashes", revealTicks = life, disappearTicks = 60, owner = "")
                message("The ${target.def.name.lowercase()} breathes fire and the logs begin to burn.", ChatType.Filter)
            }
        }
    }
}
