package top.tbpdt

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import top.tbpdt.utils.AccountUtils.getDaysBetweenDates
import top.tbpdt.utils.AccountUtils.initUserAccount
import top.tbpdt.utils.AccountUtils.queryAccount
import top.tbpdt.utils.AccountUtils.sign
import top.tbpdt.utils.AccountUtils.updateExperience
import top.tbpdt.utils.AccountUtils.updateMoney
import top.tbpdt.utils.AccountUtils.updateNick
import top.tbpdt.utils.MessageUtils.getPlainText
import top.tbpdt.utils.MessageUtils.getRemovedPrefixCommand
import top.tbpdt.utils.MessageUtils.isCommand
import java.sql.Date

/**
 * @author Takeoff0518
 */
object Account : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun GroupMessageEvent.handle() {
        if (message.isCommand("me")) {
            val userId = message.getRemovedPrefixCommand("me").toLongOrNull() ?: sender.id
            initUserAccount(userId, sender.nick)
            val userAccount = queryAccount(userId).first()
            val hitokoto = getHitokoto("d")
            val result = "称呼：${userAccount.userNick}\n" +
                    "经验：${userAccount.experience}\n" +
                    "li：${userAccount.money} li\n" +
                    "与阿绫已经相识 ${
                        getDaysBetweenDates(
                            userAccount.encounterDate,
                            Date(System.currentTimeMillis())
                        )
                    } 天\n" +
                    "已累计签到 ${userAccount.continuousSignDays} 天\n" +
                    "---------------\n" +
                    "${hitokoto.hitokoto}\n" +
                    "——${hitokoto.fromWho ?: ""}${if (hitokoto.from == hitokoto.fromWho) "" else "《" + hitokoto.from + "》"}"
            group.sendMessage(message.quote() + result)
        }
        if (message.isCommand("sign") || message.getPlainText().trim().startsWith("签到")) {
            initUserAccount(sender.id, sender.nick)
            val (totalSignDays, continuousSignDays) = sign(sender.id)
            if (totalSignDays == -1) {
                group.sendMessage(message.quote() + "你今天已经签到过了，请明天再来签到~")
                return
            }
            val deltaMoney = (20..40).random()
            val deltaExperience = (2..10).random()
            val userAccount = queryAccount(sender.id).first()
            updateMoney(sender.id, userAccount.money + deltaMoney)
            updateExperience(sender.id, userAccount.experience + deltaExperience)
            val hitokoto = getHitokoto("d")
            val result = "叮咚~签到成功！\n" +
                    "li：${userAccount.money + deltaMoney}(+$deltaMoney)\n" +
                    "经验：${userAccount.experience + deltaExperience}(+$deltaExperience)\n" +
                    "已连续签到 $continuousSignDays 天\n" +
                    "已累计签到 $totalSignDays 天\n" +
                    "---------------\n" +
                    "${hitokoto.hitokoto}\n" +
                    "——${hitokoto.fromWho ?: ""}${if (hitokoto.from == hitokoto.fromWho) "" else "《" + hitokoto.from + "》"}"
            group.sendMessage(message.quote() + result)
        }
        if (message.isCommand("nick")) {
            val nick = message.getRemovedPrefixCommand("nick")
            if (nick.isEmpty()) {
                group.sendMessage("唔……告诉我你叫什么吧~")
                return
            }
            updateNick(sender.id, nick)
            group.sendMessage(At(sender) + "好吧，现在阿绫叫你 $nick 啦~")
        }
    }
}