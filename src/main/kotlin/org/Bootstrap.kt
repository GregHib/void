package org

import org.redrune.GameServer
import org.redrune.utility.functions.OutLogger

fun main() {
    System.setOut(OutLogger(System.out))
    GameServer.run()
}