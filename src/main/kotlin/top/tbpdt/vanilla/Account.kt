package top.tbpdt.vanilla

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import top.tbpdt.configer.GlobalConfig
import top.tbpdt.utils.AccountUtils.addMoney
import top.tbpdt.utils.AccountUtils.getDaysBetweenDates
import top.tbpdt.utils.AccountUtils.initUserAccount
import top.tbpdt.utils.AccountUtils.isUserExist
import top.tbpdt.utils.AccountUtils.queryAccount
import top.tbpdt.utils.AccountUtils.queryExperienceRank
import top.tbpdt.utils.AccountUtils.queryMoneyRank
import top.tbpdt.utils.AccountUtils.queryNick
import top.tbpdt.utils.AccountUtils.sign
import top.tbpdt.utils.AccountUtils.updateExperience
import top.tbpdt.utils.AccountUtils.updateMoney
import top.tbpdt.utils.AccountUtils.updateNick
import top.tbpdt.utils.MessageUtils.getPlainText
import top.tbpdt.utils.MessageUtils.getRemovedPrefixCommand
import top.tbpdt.utils.MessageUtils.isCommand
import top.tbpdt.utils.MessageUtils.parseCommand
import top.tbpdt.vanilla.utils.StatusRecorder
import java.sql.Date
import java.time.LocalDate

/**
 * @author Takeoff0518
 */
object Account : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun GroupMessageEvent.handle() {
        if (message.isCommand("me")) {
            val userId = message.getRemovedPrefixCommand("me").toLongOrNull() ?: sender.id
            initUserAccount(userId, sender.nick)
            val userAccount = queryAccount(userId).first()
            val hitokoto: MyHitokoto = try {
                getHitokoto("d")
            } catch (e: Exception) {
                MyHitokoto(0, "", "[未获取到一言]", "", "", null, "", 0, 0, "", "", 0)
            }
            val result = "称呼：${userAccount.userNick}\n" +
                    "经验：${userAccount.experience}\n" +
                    "li：${userAccount.money} li\n" +
                    "与阿绫已经相识 ${
                        getDaysBetweenDates(
                            userAccount.encounterDate,
                            Date(System.currentTimeMillis())
                        )
                    } 天\n" +
                    "已累计签到 ${userAccount.totalSignDays} 天\n" +
                    "---------------\n" +
                    "${hitokoto.hitokoto}\n" +
                    "——${hitokoto.fromWho ?: ""}${if (hitokoto.from == hitokoto.fromWho) "" else "《" + hitokoto.from + "》"}"
            group.sendMessage(message.quote() + result)
        }
        if (message.isCommand("sign") || message.getPlainText().trim().startsWith("签到")) {
            if (group.id in GlobalConfig.signBlacklistGroups) {
                return
            }
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
            val hitokoto: MyHitokoto = try {
                getHitokoto("d")
            } catch (e: Exception) {
                MyHitokoto(0, "", "[未获取到一言]", "", "", null, "", 0, 0, "", "", 0)
            }
            StatusRecorder.updateSign(Date.valueOf(LocalDate.now()))
            val result =
                "叮咚~签到成功！你是今天第 ${StatusRecorder.querySign(Date.valueOf(LocalDate.now()))} 个签到的人~\n" +
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
        if (message.isCommand("rank money")) {
            var result = """
                |#  li    昵称(QQ)
                |
            """.trimMargin()
            val rank = queryMoneyRank()
            rank.forEachIndexed { index, element ->
                result += (index + 1).toString().padEnd(3) +
                        element.money.toString().padEnd(6) +
                        element.userNick +
                        "(" + element.userId + ")\n"
            }
            group.sendMessage(result)
        }
        if (message.isCommand("rank exp")) {
            var result = """
                |#  经验  昵称(QQ)
                |
            """.trimMargin()
            val rank = queryExperienceRank()
            rank.forEachIndexed { index, element ->
                result += (index + 1).toString().padEnd(3) +
                        element.experience.toString().padEnd(6) +
                        element.userNick +
                        "(" + element.userId + ")\n"
            }
            group.sendMessage(result)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun MessageEvent.adminHandle() {
        if (sender.id !in GlobalConfig.admin) return
        if (message.isCommand("money set")) {
            val arguments = message.getRemovedPrefixCommand("money set").parseCommand()
            if (arguments.size != 2) {
                subject.sendMessage("参数个数不匹配，应为 [被操作者QQ号] [钱数]！")
                return
            }
            val userId = arguments[0].toLongOrNull()
            val money = arguments[1].toIntOrNull()
            if (userId == null || money == null) {
                subject.sendMessage("参数类型不匹配，应为 [长整数] [整数]！")
                return
            }
            if (!isUserExist(sender.id)) {
                initUserAccount(userId, "[未指定]")
            }
            updateMoney(userId, money)
            subject.sendMessage("已为 $userId 修改钱数 为 $money li~")
        }
        if (message.isCommand("money add")) {
            val arguments = message.getRemovedPrefixCommand("money add").parseCommand()
            if (arguments.size != 2) {
                subject.sendMessage("参数个数不匹配，应为 [被操作者QQ号] [钱数增量]！")
                return
            }
            val userId = arguments[0].toLongOrNull()
            val delta = arguments[1].toIntOrNull()
            if (userId == null || delta == null) {
                subject.sendMessage("参数类型不匹配，应为 [长整数] [整数]！")
                return
            }
            if (!isUserExist(sender.id)) {
                initUserAccount(userId, "[未指定]")
            }
            addMoney(userId, delta)
            subject.sendMessage("已为 $userId 增减钱数 $delta li~")
        }
        if (message.isCommand("exp set")) {
            val arguments = message.getRemovedPrefixCommand("exp set").parseCommand()
            if (arguments.size != 2) {
                subject.sendMessage("参数个数不匹配，应为 [被操作者QQ号] [经验]！")
                return
            }
            val userId = arguments[0].toLongOrNull()
            val experience = arguments[1].toIntOrNull()
            if (userId == null || experience == null) {
                subject.sendMessage("参数类型不匹配，应为 [长整数] [整数]！")
                return
            }
            if (!isUserExist(sender.id)) {
                initUserAccount(userId, "[未指定]")
            }
            updateExperience(userId, experience)
            subject.sendMessage("已为 $userId 修改经验 为 $experience ~")
        }
    }

    /**
     * 修复缺失的昵称问题
     */
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun GroupMessageEvent.spyHandle() {
        if (!isUserExist(sender.id)) {
            initUserAccount(sender.id, senderName)
            return
        }
        if (queryNick(sender.id).trim() == "" || queryNick(sender.id).trim() == "[未指定]") {
            updateNick(sender.id, senderName)
        }
    }

}