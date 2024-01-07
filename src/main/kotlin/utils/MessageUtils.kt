package top.tbpdt.utils

import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText

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
}