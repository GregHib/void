package org

import org.redrune.GameServer
import org.redrune.util.OutLogger
import kotlin.system.exitProcess

fun main() {
    System.setOut(OutLogger(System.out))
    try {
        GameServer.run()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}