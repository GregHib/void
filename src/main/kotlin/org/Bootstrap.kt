package org

import org.redrune.GameServer
import org.redrune.utility.functions.OutLogger
import java.lang.System.exit
import kotlin.system.exitProcess

fun main() {
    try {
        System.setOut(OutLogger(System.out))
        GameServer.run()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}