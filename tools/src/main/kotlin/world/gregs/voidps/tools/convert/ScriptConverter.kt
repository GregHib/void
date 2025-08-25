package world.gregs.voidps.tools.convert

import java.io.File

object ScriptConverter {

    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("C:\\Users\\Greg\\IdeaProjects\\void\\game\\src\\main\\kotlin\\content\\achievement\\TaskList.kts")

        val output = mutableListOf<String>()
        output.add("@file:Suppress(\"UnusedReceiverParameter\")")
        output.add("")

        fun dependency(name: String) {
            val import = "import $name"
            if (!output.contains(import)) {
                output.add(4, import)
            }
        }

        val lines = file.readLines()
        var depth = 0
        for (line in lines) {
            if (depth == 0 && line.startsWith("val")) {
                output.add("private $line")
                continue
            }
            if (line.startsWith("fun ")) {
                output.add("private $line")
                if (line.contains("{")) {
                    depth++
                }
                continue
            }
            if (depth != 0 && line.trim().startsWith("return@")) {
                output.add(line.substringBeforeLast("@"))
                continue
            }
            if (line.contains("{")) {
                depth++
            } else if (line.contains("}")) {
                depth--
            }
            when {
                line.startsWith("inventoryItem") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Option")
                    dependency("content.entity.player.inv.InventoryOption")
                    output.add("// $line")
                    output.add("@${line.replace("inventoryItem", "Option").substringBeforeLast(" {")}")
                    output.add("suspend fun InventoryOption.invOption() {")
                    depth++
                    continue
                }
                line.startsWith("objectOperate") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Option")
                    dependency("world.gregs.voidps.engine.entity.obj.ObjectOption")
                    output.add("// $line")
                    output.add("@${line.replace("objectOperate", "Option").substringBeforeLast(" {")}")
                    output.add("suspend fun ObjectOption<Player>.objOption() {")
                    depth++
                    continue
                }
                line.startsWith("npcCombatDamage") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Combat")
                    dependency("content.entity.combat.hit.CombatDamage")
                    output.add("// $line")
                    output.add("@${line.replace("npcCombatDamage", "Combat").substringBeforeLast(" {")}")
                    output.add("fun CombatDamage.cmbDamage(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("enterArea") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Area")
                    dependency("world.gregs.voidps.engine.entity.character.mode.move.AreaEntered")
                    output.add("// $line")
                    output.add("@${line.replace("enterArea", "Area").substringBeforeLast(" {")}")
                    output.add("suspend fun AreaEntered.enterArea() {")
                    depth++
                    continue
                }
                line.startsWith("exitArea") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Area")
                    dependency("world.gregs.voidps.engine.entity.character.mode.move.AreaExited")
                    output.add("// $line")
                    output.add("@${line.replace("exitArea", "Area").substringBeforeLast(" {")}")
                    output.add("suspend fun AreaExited.exitArea() {")
                    depth++
                    continue
                }
                line.startsWith("playerSpawn") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Spawn")
                    dependency("world.gregs.voidps.engine.entity.character.player.Player")
                    output.add("// $line")
                    output.add("@${line.replace("playerSpawn", "Spawn").substringBeforeLast(" {")}")
                    output.add("fun playerSpawn(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("npcSpawn") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Spawn")
                    dependency("world.gregs.voidps.engine.entity.character.npc.NPC")
                    output.add("// $line")
                    output.add("@${line.replace("npcSpawn", "Spawn").substringBeforeLast(" {")}")
                    output.add("fun npcSpawn(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("characterSpawn") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Spawn")
                    dependency("world.gregs.voidps.engine.entity.character.Character")
                    output.add("// $line")
                    output.add("@${line.replace("characterSpawn", "Spawn").substringBeforeLast(" {")}")
                    output.add("fun characterSpawn(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("objectSpawn") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Spawn")
                    dependency("world.gregs.voidps.engine.entity.obj.GameObject")
                    output.add("// $line")
                    output.add("@${line.replace("objectSpawn", "Spawn").substringBeforeLast(" {")}")
                    output.add("fun objectSpawn(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("worldSpawn") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Spawn")
                    dependency("world.gregs.voidps.engine.entity.World")
                    output.add("// $line")
                    output.add("@${line.replace("worldSpawn", "Spawn").substringBeforeLast(" {")}")
                    output.add("fun worldSpawn(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("inventoryUpdate") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Inventory")
                    dependency("world.gregs.voidps.engine.inv.InventoryUpdate")
                    dependency("world.gregs.voidps.engine.entity.character.player.Player")
                    output.add("// $line")
                    output.add("@${line.replace("inventoryUpdate", "Inventory").substringBeforeLast(" {")}")
                    output.add("fun InventoryUpdate.invUpdate(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("inventoryItem") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Option")
                    dependency("content.entity.player.inv.InventoryOption")
                    output.add("// $line")
                    output.add("@${line.replace("inventoryItem", "Option").substringBeforeLast(" {")}")
                    output.add("fun InventoryOption.click() {")
                    depth++
                    continue
                }
                line.startsWith("inventoryOption") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Option")
                    dependency("content.entity.player.inv.InventoryOption")
                    output.add("// $line")
                    output.add("@${line.replace("inventoryOption", "Option").substringBeforeLast(" {")}")
                    output.add("fun InventoryOption.click() {")
                    depth++
                    continue
                }
                line.startsWith("objTeleportTakeOff") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Option")
                    dependency("content.entity.obj.ObjectTeleport")
                    output.add("// $line")
                    output.add("@${line.replace("objTeleportTakeOff", "Option").substringBeforeLast(" {").replace(")", ", arrive = false)")}")
                    output.add("fun ObjectTeleport.teleportTakeOff() {")
                    depth++
                    continue
                }
                line.startsWith("objTeleportLand") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Option")
                    dependency("content.entity.obj.ObjectTeleport")
                    output.add("// $line")
                    output.add("@${line.replace("objTeleportLand", "Option").substringBeforeLast(" {")}")
                    output.add("fun ObjectTeleport.teleportLand() {")
                    depth++
                    continue
                }
                line.startsWith("itemAdded") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Option")
                    dependency("world.gregs.voidps.engine.inv.ItemAdded")
                    output.add("// $line")
                    output.add("@${line.replace("itemAdded", "Inventory").substringBeforeLast(" {")}")
                    output.add("fun ItemAdded.itemAdded(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("itemRemoved") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Option")
                    dependency("world.gregs.voidps.engine.inv.ItemRemoved")
                    output.add("// $line")
                    output.add("@${line.replace("itemRemoved", "Inventory").substringBeforeLast(" {")}")
                    output.add("fun ItemRemoved.itemRemoved(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("timerStop") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.timer.TimerStop")
                    output.add("// $line")
                    output.add("@${line.replace("timerStop", "On").substringBeforeLast(" {")}")
                    output.add("fun TimerStop.stopTimer(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("npcTimerStop") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.timer.TimerStop")
                    output.add("// $line")
                    output.add("@${line.replace("npcTimerStop", "On").substringBeforeLast(" {")}")
                    output.add("fun TimerStop.npcStopTimer(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("characterTimerStop") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.timer.TimerStop")
                    output.add("// $line")
                    output.add("@${line.replace("characterTimerStop", "On").substringBeforeLast(" {")}")
                    output.add("fun TimerStop.charStopTimer(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("timerStart") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.timer.TimerStart")
                    output.add("// $line")
                    output.add("@${line.replace("timerStart", "On").substringBeforeLast(" {")}")
                    output.add("fun TimerStart.startTimer(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("npcTimerStart") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.timer.TimerStart")
                    output.add("// $line")
                    output.add("@${line.replace("npcTimerStart", "On").substringBeforeLast(" {")}")
                    output.add("fun TimerStart.npcStartTimer(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("characterTimerStart") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.timer.TimerStart")
                    output.add("// $line")
                    output.add("@${line.replace("characterTimerStart", "On").substringBeforeLast(" {")}")
                    output.add("fun TimerStart.charStartTimer(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("timerTick") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.timer.TimerTick")
                    output.add("// $line")
                    output.add("@${line.replace("timerTick", "On").substringBeforeLast(" {")}")
                    output.add("fun TimerTick.startTimer(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("npcTimerTick") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.timer.TimerTick")
                    output.add("// $line")
                    output.add("@${line.replace("npcTimerTick", "On").substringBeforeLast(" {")}")
                    output.add("fun TimerTick.npcTickTimer(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("characterTimerTick") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.timer.TimerTick")
                    output.add("// $line")
                    output.add("@${line.replace("characterTimerTick", "On").substringBeforeLast(" {")}")
                    output.add("fun TimerTick.charTickTimer(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("inventoryChanged") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Inventory")
                    dependency("world.gregs.voidps.engine.inv.InventorySlotChanged")
                    output.add("// $line")
                    output.add("@${line.replace("inventoryChanged", "Inventory").substringBeforeLast(" {")}")
                    output.add("fun InventorySlotChanged.invChanged(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("variableSet") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Variable")
                    dependency("world.gregs.voidps.engine.client.variable.VariableSet")
                    output.add("// $line")
                    output.add("@${line.replace("variableSet", "Variable").substringBeforeLast(" {")}")
                    output.add("fun VariableSet.invChanged(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("playerDeath") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("content.entity.death.Death")
                    output.add("// $line")
                    output.add("@${line.replace("playerDeath", "On").substringBeforeLast(" {")}")
                    output.add("fun Death.playerDeath(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("npcDeath") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("content.entity.death.Death")
                    output.add("// $line")
                    output.add("@${line.replace("npcDeath", "On").substringBeforeLast(" {")}")
                    output.add("fun Death.npcDeath(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("characterDeath") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("content.entity.death.Death")
                    output.add("// $line")
                    output.add("@${line.replace("characterDeath", "On").substringBeforeLast(" {")}")
                    output.add("fun Death.characterDeath(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("maxLevelChange") -> {
                    dependency("world.gregs.voidps.engine.event.handle.LevelChange")
                    dependency("world.gregs.voidps.engine.entity.character.player.skill.level.MaxLevelChanged")
                    output.add("// $line")
                    output.add("@${line.replace("maxLevelChange", "LevelChange").substringBeforeLast(" {")}")
                    output.add("fun MaxLevelChanged.maxLevelChange(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("levelChange") -> {
                    dependency("world.gregs.voidps.engine.event.handle.LevelChange")
                    dependency("world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged")
                    output.add("// $line")
                    output.add("@${line.replace("levelChange", "LevelChange").substringBeforeLast(" {")}")
                    output.add("fun CurrentLevelChanged.levelChange(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("npcLevelChange") -> {
                    dependency("world.gregs.voidps.engine.event.handle.LevelChange")
                    dependency("world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged")
                    output.add("// $line")
                    output.add("@${line.replace("npcLevelChange", "LevelChange").substringBeforeLast(" {")}")
                    output.add("fun CurrentLevelChanged.npcLevelChange(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("characterLevelChange") -> {
                    dependency("world.gregs.voidps.engine.event.handle.LevelChange")
                    dependency("world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged")
                    output.add("// $line")
                    output.add("@${line.replace("characterLevelChange", "LevelChange").substringBeforeLast(" {")}")
                    output.add("fun CurrentLevelChanged.characterLevelChange(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("itemSold") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Handle")
                    dependency("content.entity.npc.shop.sell.SoldItem")
                    output.add("// $line")
                    output.add("@${line.replace("itemSold", "Handle").replace("(", "(\"item_sold\", ").substringBeforeLast(" {")}")
                    output.add("fun SoldItem.itemSold(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("prayerStart") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("content.skill.prayer.PrayerStart")
                    output.add("// $line")
                    output.add("@${line.replace("prayerStart", "On").substringBeforeLast(" {")}")
                    output.add("fun PrayerStart.prayerStart(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("prayerStop") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("content.skill.prayer.PrayerStop")
                    output.add("// $line")
                    output.add("@${line.replace("prayerStop", "On").substringBeforeLast(" {")}")
                    output.add("fun PrayerStop.prayerStop(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("combatAttack") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Combat")
                    dependency("content.entity.combat.hit.CombatAttack")
                    output.add("// $line")
                    output.add("@${line.replace("combatAttack", "Combat").substringBeforeLast(" {")}")
                    output.add("fun CombatAttack.combatAttack(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("npcCombatAttack") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Combat")
                    dependency("content.entity.combat.hit.CombatAttack")
                    output.add("// $line")
                    output.add("@${line.replace("npcCombatAttack", "Combat").substringBeforeLast(" {")}")
                    output.add("fun CombatAttack.npcCombatAttack(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("characterCombatAttack") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Combat")
                    dependency("content.entity.combat.hit.CombatAttack")
                    output.add("// $line")
                    output.add("@${line.replace("characterCombatAttack", "Combat").substringBeforeLast(" {")}")
                    output.add("fun CombatAttack.characterCombatAttack(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("shopOpen") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Handle")
                    dependency("content.entity.npc.shop.OpenShop")
                    output.add("// $line")
                    output.add("@${line.replace("shopOpen", "Handle").replace("(", "(\"open_shop\", ").substringBeforeLast(" {")}")
                    output.add("fun OpenShop.shopOpen(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("move") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Move")
                    dependency("world.gregs.voidps.engine.entity.character.mode.move.Moved")
                    output.add("// $line")
                    output.add("@${line.replace("move", "Move").substringBeforeLast(" {")}")
                    output.add("fun Moved<Player>.move(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("npcMove") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Move")
                    dependency("world.gregs.voidps.engine.entity.character.mode.move.Moved")
                    output.add("// $line")
                    output.add("@${line.replace("npcMove", "Move").substringBeforeLast(" {")}")
                    output.add("fun Moved<NPC>.npcMove(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("characterMove") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Move")
                    dependency("world.gregs.voidps.engine.entity.character.mode.move.Moved")
                    output.add("// $line")
                    output.add("@${line.replace("characterMove", "Move").substringBeforeLast(" {")}")
                    output.add("fun Moved<Character>.characterMove(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("interfaceOption") -> {
                    dependency("world.gregs.voidps.engine.event.handle.Option")
                    dependency("world.gregs.voidps.engine.client.ui.InterfaceOption")
                    output.add("// $line")
                    output.add("// TODO convert to component:id")
                    output.add("@${line.replace("interfaceOption", "Option").substringBeforeLast(" {")}")
                    output.add("fun InterfaceOption.interfaceOption() {")
                    depth++
                    continue
                }
                line.startsWith("interfaceOpen") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.client.ui.event.InterfaceOpened")
                    output.add("// $line")
                    output.add("@${line.replace("interfaceOpen", "On").substringBeforeLast(" {")}")
                    output.add("fun InterfaceOpened.interfaceOpen(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("interfaceClose") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.client.ui.event.InterfaceClosed")
                    output.add("// $line")
                    output.add("@${line.replace("interfaceClose", "On").substringBeforeLast(" {")}")
                    output.add("fun InterfaceClosed.interfaceClose(${params(line)}) {")
                    depth++
                    continue
                }
                line.startsWith("interfaceRefresh") -> {
                    dependency("world.gregs.voidps.engine.event.handle.On")
                    dependency("world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed")
                    output.add("// $line")
                    output.add("@${line.replace("interfaceRefresh", "On").substringBeforeLast(" {")}")
                    output.add("fun InterfaceRefreshed.interfaceRefresh(${params(line)}) {")
                    depth++
                    continue
                }
                else -> output.add(line)
            }
        }
        val outFile = file.parentFile.resolve(file.nameWithoutExtension + ".kt")
        println(outFile)
        outFile.writeText(output.joinToString("\n"))
    }

    private fun params(line: String): String {
        val substringAfter = line.substringAfterLast("{ ")
        return if (substringAfter.contains("player")) {
            "${substringAfter.removeSuffix("->")}${if (substringAfter.contains(":")) "" else ": Player"}"
        } else if (substringAfter.contains("npc")) {
            "${substringAfter.removeSuffix("->")}${if (substringAfter.contains(":")) "" else ": NPC"}"
        } else {
            "it: Player"
        }
    }
}