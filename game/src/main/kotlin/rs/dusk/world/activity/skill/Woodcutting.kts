package rs.dusk.world.activity.skill

import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.action.action
import rs.dusk.engine.entity.character.contain.ContainerResult
import rs.dusk.engine.entity.character.contain.equipment
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerRequirement
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.lacks
import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.entity.character.update.visual.setAnimation
import rs.dusk.engine.entity.item.detail.ItemDetails
import rs.dusk.engine.entity.item.detail.contains
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.ObjectOption
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject

class WoodcuttingHatchet : PlayerRequirement {
    override fun onFailure(player: Player) {
        player.message("You need a hatchet to chop down this tree.")
    }

    override fun met(player: Player): Boolean {
        return player.inventory.contains("Bronze Hatchet")
    }

}

fun Player.has(skill: Skill, level: Int, message: String = "You need to have an ${skill.name} level of $level."): Boolean {
    if(levels.get(skill) < level) {
        message("You need to have an ${skill.name} level fo $level.")
        return false
    }
    return true
}

val decoder: ItemDecoder by inject()
val details: ItemDetails by inject()

val hatchet = WoodcuttingHatchet()

ObjectOption where { obj.def.name.toLowerCase().contains("tree") && option == "Chop" } then {
    chopTree(player, obj)
}

fun chopTree(player: Player, tree: GameObject) {
    player.action {
        val hatchet = getHatchet(player)
        while(canContinue(player)) {

            val log = decoder.get(1234)
            player.setAnimation(1234)
            delay(2)
            addLog(player, log, 1)
        }
    }
}

fun Hatchet.level(): Int {
    val detail = details.get(id)
    return decoder.get(detail.id).getParam(750L, 0)
}

fun getHatchet(player: Player) {
    val list = Hatchet.values().filter { player.inventory.contains(it.id) || player.equipment.contains(it.id) }
    val sorted = list.sortedBy { it.level() }
    println("Hatchets $sorted")
}
@Suppress("EnumEntryName")
enum class Hatchet {
    Bronze_Hatchet,
    Iron_Hatchet,
    Black_Hatchet,
    Dwarven_Army_Axe,
    Steel_Hatchet,
    Mithril_Hatchet,
    Adamant_Hatchet,
    Rune_Hatchet,
    Dragon_Hatchet,
    Sacred_Clay_Hatchet,
    Inferno_Adze,
    Novite_Hatchet,
    Bathus_Hatchet,
    Marmaros_Hatchet,
    Kratonite_Hatchet,
    Fractite_Hatchet,
    Zephyrium_Hatchet,
    Argonite_Hatchet,
    Katagon_Hatchet,
    Gorgonite_Hatchet,
    Promethium_Hatchet,
    Primal_Hatchet;

    val id: String = name.toLowerCase()
}

fun getLog(tree: GameObject) {
    tree.def.name
}

fun canContinue(player: Player): Boolean {
    if (player.lacks(hatchet)) {
        return false
    }

    if (!player.has(Skill.Woodcutting, 1)) {
        return false
    }

    return player.inventory.spaces > 0
}

fun addLog(player: Player, item: ItemDefinition, amount: Int) {
    if (player.inventory.add(item.id, amount)) {
        player.message("You get some ${item.name}.")
    } else {
        when(player.inventory.result) {
            ContainerResult.Full -> player.message("You don't have enough inventory space.")
            else -> {}
        }
    }
}