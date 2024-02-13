package top.tbpdt.utils

import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import top.tbpdt.configer.GlobalConfig

/**
 * @author Takeoff0518
 */
object MessageUtils {
    fun MessageChain.getPlainText(): String {
        var result = ""
        for (i in listIterator()) if (i is PlainText) result += i.content
        return result
    }

    fun String.encodeToMiraiCode(initiative: User, passive: User): String {
        return replace("%主动%", "[mirai:at:${initiative.id}]")
            .replace("%被动%", "[mirai:at:${passive.id}]")
    }

    fun String.encodeToMiraiCode(user: User, isInitiative: Boolean): String {
        return if (isInitiative) replace("%主动%", "[mirai:at:${user.id}]")
        else replace("%被动%", "[mirai:at:${user.id}]")
    }

    fun MessageChain.isCommand(prefix: String): Boolean {
        val serializedCode = this.serializeToMiraiCode()
        if (serializedCode.isEmpty() || GlobalConfig.commandPrefix != serializedCode.firstOrNull()) {
            return false
        }
        return serializedCode.removePrefix(GlobalConfig.commandPrefix.toString()).trim().startsWith(prefix)
    }

    fun MessageChain.getRemovedPrefixCommand(prefix: String): String {
        if (!this.isCommand(prefix)) return ""
        return this.serializeToMiraiCode().removePrefix(GlobalConfig.commandPrefix + prefix).trim()
    }

    fun String.parseCommand(): List<String> {
        val parts = mutableListOf<String>()
        var currentPart = ""
        var inQuotes = false
        for (char in this) {
            when {
                char.isWhitespace() && !inQuotes ->
                    if (currentPart.isNotEmpty()) {
                        parts.add(currentPart)
                        currentPart = ""
                    }
                char == '"' -> inQuotes = !inQuotes
                else -> currentPart += char
            }
        }
        if (currentPart.isNotEmpty()) {
            parts.add(currentPart)
        }
        return parts
    }

    fun GroupMessageEvent.checkMutePermission(): Boolean {
        return sender.permission.level < group.botPermission.level
    }
}