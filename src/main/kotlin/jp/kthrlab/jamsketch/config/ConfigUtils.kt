package jp.kthrlab.jamsketch.config

import java.io.File
import java.util.*
import kotlin.reflect.full.declaredMembers

fun getDeclaredMember(className: String, memberName: String) : Any? {
    val kClass = Class.forName(className).kotlin
    val member = kClass.declaredMembers.find {
        it.name == memberName
    }
    return member?.call()
}

fun getAppDataDirectory(): File {
    val userHome = System.getProperty("user.home")
    val osName = System.getProperty("os.name").lowercase(Locale.getDefault())

    return when {
        osName.contains("win") -> {
            File(System.getenv("APPDATA"))
        }
        osName.contains("mac") -> {
            File(userHome, "Library/Application Support")
        }
        osName.contains("nux") -> {
            File(userHome, ".config")
        }
        else -> {
            File(userHome)
        }
    }
}

fun makeParentDirs(file: File) {
    val parentDir = file.parentFile
    if (!parentDir.exists()) { parentDir.mkdirs() }
    if (!parentDir.parentFile.exists()) { makeParentDirs(parentDir) }
}
