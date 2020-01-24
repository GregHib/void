package org.redrune.network.packet

/**
 * The possible headers of a packet
 */
enum class PacketHeader {
    RAW,
    FIXED,
    VARIABLE_BYTE,
    VARIABLE_SHORT
}