package org.redrune.tools.func

import java.io.File
import java.io.IOException
import java.util.*

class FileFunc {

    companion object {

        fun getClasses(packageName: String): List<Any?>? {
             return try {
                 val classLoader = Thread.currentThread().contextClassLoader!!
                 val path = packageName.replace('.', '/')
                 val resources = classLoader.getResources(path)
                 val dirs: MutableList<File> = ArrayList()
                 while (resources.hasMoreElements()) {
                     val resource = resources.nextElement()
                     dirs.add(File(resource.file.replace("%20".toRegex(), " ")))
                 }
                 val classes: MutableList<Class<*>> = ArrayList()
                 for (directory in dirs) {
                    findClasses(directory, packageName)?.forEach { classes.add(it) }
                 }
                 val list: MutableList<Any?> = ArrayList()
                 for (clazz in classes) {
                     if (clazz.isAnnotation) {
                         continue
                     }
                     list.add(clazz.newInstance())
                 }
                 println("path=$path, resources=$resources, dirs=$dirs, classes=$classes, list=$list")
                 list
             } catch (e: IllegalAccessException) {
                 e.printStackTrace()
                 Collections.EMPTY_LIST
             } catch (e: InstantiationException) {
                 e.printStackTrace()
                 Collections.EMPTY_LIST
             } catch (e: IOException) {
                 e.printStackTrace()
                 Collections.EMPTY_LIST
             }
        }

        private fun findClasses(directory: File, packageName: String): MutableList<Class<*>>? {
            val classes: MutableList<Class<*>> = ArrayList()
            if (!directory.exists()) {
                return classes
            }
            val files = directory.listFiles() ?: return classes
            for (file in files) {
                if (file.name.contains("$")) {
                    continue
                }
                if (file.isDirectory) {
                    assert(!file.name.contains("."))
                    classes.addAll(findClasses(file, packageName + "." + file.name)!!)
                } else if (file.name.endsWith(".class")) {
                    try {
                        classes.add(Class.forName(packageName + '.' + file.name.substring(0, file.name.length - 6)))
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
            return classes
        }
    }
}