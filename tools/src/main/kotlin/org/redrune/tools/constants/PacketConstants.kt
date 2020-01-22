package org.redrune.tools.constants

/**
 * Unchanging information about packets is held in this class
 */
class PacketConstants {

    companion object {
        /**
         * The array consisting of the expected lengths of incoming packets
         */
        val PACKET_LENGTHS = intArrayOf(255)

        /**
         * The time that makes a player inactive for logic packets
         */
        const val MAX_PACKETS_DECODER_PING_DELAY: Long = 30000

        /**
         * The maximum size of an incoming packet
         */
        const val RECEIVE_DATA_LIMIT = 7500
        /**
         * The maximum size of an outgoing packet
         */
        const val PACKET_SIZE_LIMIT = 7500

        init {
            for (id in 0..255) {
                PACKET_LENGTHS[id] = -4
            }
            PACKET_LENGTHS[64] = 8
            PACKET_LENGTHS[18] = 8
            PACKET_LENGTHS[25] = 8
            PACKET_LENGTHS[41] = -1
            PACKET_LENGTHS[14] = 3
            PACKET_LENGTHS[46] = 3
            PACKET_LENGTHS[87] = 6
            PACKET_LENGTHS[47] = 7
            PACKET_LENGTHS[57] = 3
            PACKET_LENGTHS[67] = 3
            PACKET_LENGTHS[91] = 8
            PACKET_LENGTHS[24] = 7
            PACKET_LENGTHS[73] = 16
            PACKET_LENGTHS[40] = 11
            PACKET_LENGTHS[36] = -1
            PACKET_LENGTHS[74] = -1
            PACKET_LENGTHS[31] = 3
            PACKET_LENGTHS[54] = 6
            PACKET_LENGTHS[12] = 5
            PACKET_LENGTHS[23] = 1
            PACKET_LENGTHS[9] = 3
            PACKET_LENGTHS[17] = -1
            PACKET_LENGTHS[44] = -1
            PACKET_LENGTHS[88] = -1
            PACKET_LENGTHS[42] = 15
            PACKET_LENGTHS[49] = 3
            PACKET_LENGTHS[21] = 15
            PACKET_LENGTHS[59] = -1
            PACKET_LENGTHS[37] = -1
            PACKET_LENGTHS[6] = 8
            PACKET_LENGTHS[55] = 7
            PACKET_LENGTHS[69] = 9
            PACKET_LENGTHS[26] = 16
            PACKET_LENGTHS[39] = 12
            PACKET_LENGTHS[71] = 4
            PACKET_LENGTHS[22] = 2
            PACKET_LENGTHS[32] = -1
            PACKET_LENGTHS[79] = -1
            PACKET_LENGTHS[89] = 4
            PACKET_LENGTHS[90] = -1
            PACKET_LENGTHS[15] = 4
            PACKET_LENGTHS[72] = -2
            PACKET_LENGTHS[20] = 8
            PACKET_LENGTHS[92] = 3
            PACKET_LENGTHS[82] = 3
            PACKET_LENGTHS[28] = 3
            PACKET_LENGTHS[81] = 8
            PACKET_LENGTHS[7] = -1
            PACKET_LENGTHS[4] = 8
            PACKET_LENGTHS[60] = -1
            PACKET_LENGTHS[13] = 2
            PACKET_LENGTHS[52] = 8
            PACKET_LENGTHS[65] = 11
            PACKET_LENGTHS[85] = 2
            PACKET_LENGTHS[86] = 7
            PACKET_LENGTHS[78] = -1
            PACKET_LENGTHS[83] = 18
            PACKET_LENGTHS[27] = 7
            PACKET_LENGTHS[2] = 7
            PACKET_LENGTHS[93] = 1
            PACKET_LENGTHS[70] = -1
            PACKET_LENGTHS[1] = -1
            PACKET_LENGTHS[8] = -1
            PACKET_LENGTHS[11] = 7
            PACKET_LENGTHS[0] = 9
            PACKET_LENGTHS[51] = -1
            PACKET_LENGTHS[5] = 4
            PACKET_LENGTHS[45] = 7
            PACKET_LENGTHS[75] = 4
            PACKET_LENGTHS[53] = 3
            PACKET_LENGTHS[33] = 0
            PACKET_LENGTHS[50] = 3
            PACKET_LENGTHS[76] = 7
            PACKET_LENGTHS[80] = -1
            PACKET_LENGTHS[77] = 3
            PACKET_LENGTHS[68] = -1
            PACKET_LENGTHS[43] = 3
            PACKET_LENGTHS[30] = -1
            PACKET_LENGTHS[19] = 3
            PACKET_LENGTHS[16] = 0
            PACKET_LENGTHS[34] = 4
            PACKET_LENGTHS[48] = 0
            PACKET_LENGTHS[56] = 0
            PACKET_LENGTHS[58] = 2
            PACKET_LENGTHS[10] = 8
            PACKET_LENGTHS[35] = 7
            PACKET_LENGTHS[84] = 6
            PACKET_LENGTHS[66] = 3
            PACKET_LENGTHS[61] = 8
            PACKET_LENGTHS[29] = -1
            PACKET_LENGTHS[62] = 3
            PACKET_LENGTHS[3] = 4
            PACKET_LENGTHS[63] = 4
            PACKET_LENGTHS[73] = 16
            PACKET_LENGTHS[38] = -1
        }

    }
}