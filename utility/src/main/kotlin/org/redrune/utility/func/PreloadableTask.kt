package org.redrune.utility.func

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since April 13, 2020
 */
interface PreloadableTask {

    /**
     * The essentials for the task to be ran must be loaded in this method
     */
    fun preload()

    /**
     * The running of the task
     */
    fun run()
}