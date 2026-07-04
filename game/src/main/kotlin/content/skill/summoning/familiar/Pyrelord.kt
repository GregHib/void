package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.FamiliarSpecialMoves
import content.skill.summoning.castFamiliarSpecial
import content.skill.summoning.follower
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.move.canTravel
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.entity.character.player.Player
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

        // A plain click on the cast button has no item to work on - point at the real triggers.
        FamiliarSpecialMoves.instant("pyrelord_familiar") {
            message("To cast Immense Heat, use the Cast option or a gold bar on the pyrelord.")
            false
        }

        // Immense Heat - the pyrelord's flames stand in for a furnace, letting the owner craft
        // gold jewellery on the spot. Cast on a gold bar, or use the bar on the familiar - both
        // run through the scroll + points gate.
        FamiliarSpecialMoves.item("pyrelord_familiar") { item -> immenseHeat(item.id) }

        itemOnNPCOperate("gold_bar", "pyrelord_familiar") { (npc, item) ->
            if (npc != follower) {
                return@itemOnNPCOperate
            }
            castFamiliarSpecial { immenseHeat(item.id) }
        }

        // The pyrelord and forge regent act as portable fire sources - the demon breathes fire over
        // the logs, lighting them beneath itself, granting the log's xp plus a bonus for its help.
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
                if (GameObjects.getLayer(target.tile, ObjectLayer.GROUND) != null) {
                    message("You can't light a fire here.")
                    return@itemOnNPCOperate
                }
                if (!inventory.remove(item.id)) {
                    return@itemOnNPCOperate
                }
                // The logs land at the familiar's feet first, as with a tinderbox, then it breathes
                // fire over them a moment later. Aborts, fireless, if they're gone by then.
                val logs = FloorItems.add(target.tile, item.id, disappearTicks = 300, owner = this)
                target.anim("familiar_light_fire")
                pause(2)
                if (!FloorItems.remove(logs)) {
                    return@itemOnNPCOperate
                }
                exp(Skill.Firemaking, row.int("xp") / 10.0 + 10)
                val colour = row.string("colour")
                val life = row.int("life")
                GameObjects.add("fire_$colour", target.tile, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0, ticks = life)
                FloorItems.add(target.tile, "ashes", revealTicks = life, disappearTicks = 60, owner = "")
                message("The ${target.def.name.lowercase()} breathes fire and the logs begin to burn.", ChatType.Filter)
                // The familiar steps off its new fire westward, as players do, then waits beside it
                // facing its owner - resuming the follow straight away would walk it right back onto
                // the fire, so it stands by (PauseMode, like the beaver's chop) until the owner moves.
                val steps: StepValidator = get()
                if (steps.canTravel(target, -1, 0)) {
                    val owner = this
                    target.queue("familiar_step_west") {
                        val dest = target.tile.addX(-1)
                        target.walkTo(dest)
                        var ticks = 0
                        while (target.tile != dest && ticks++ < 5) {
                            delay()
                        }
                        target.mode = PauseMode
                        target.watch(owner)
                        val stand = owner.tile
                        while (owner.tile == stand && owner.follower == target && target.mode is PauseMode) {
                            delay()
                        }
                        if (owner.follower == target && target.mode is PauseMode) {
                            target.mode = Follow(target, owner)
                        }
                    }
                }
            }
        }
    }

    /** The Immense Heat effect: melt a gold bar into the jewellery mould interface, no furnace needed. */
    private fun Player.immenseHeat(itemId: String): Boolean {
        if (itemId != "gold_bar") {
            message("The pyrelord can only work its heat on gold bars.")
            return false
        }
        anim("immense_heat")
        gfx("immense_heat")
        open("make_mould${if (World.members) "_slayer" else ""}")
        return true
    }
}
