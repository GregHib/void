package org.redrune.tools

class PCUtils {

    companion object {
        /**
         * The amount of processors available on the computer
         */
        @JvmStatic
        val PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors()

    }

}