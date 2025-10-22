package top.tbpdt.vanilla

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import top.tbpdt.utils.AccountUtils
import top.tbpdt.utils.MessageUtils.getPlainText
import top.tbpdt.utils.MessageUtils.parseCommand
import top.tbpdt.vanilla.utils.CDTimer

/**
 * @author Takeoff0518
 */
object RussianRoulette : SimpleListenerHost() {

    val coolDownTimer = CDTimer(2000)

    data class RouletteProgress(
//        val groupId: Long,
        val totalNum: Int, // 总弹数
        var realNum: Int, // 会打死人的弹数
        val income: MutableMap<Long, Int>, // 收益
        val eliminated: MutableSet<Long>, // 被淘汰群员 QQ 号
        val bulletList: List<Boolean>, // 子弹序列，真弹为 true，空弹为 false
        var bulletNow: Int // 游戏进度
    )

    val gameProgress: MutableMap<Long, RouletteProgress> = mutableMapOf() // k=群号, v=游戏进度

    fun generateBulletList(total: Int, real: Int): List<Boolean> {
        require(real <= total) { "total 不能超过列表大小" }
        return MutableList(total) { index -> index < real }
            .apply { shuffle() }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun GroupMessageEvent.startHandle() {
        if (message.getPlainText().trim().startsWith("装弹")) {
            if (gameProgress.containsKey(group.id)) {
                group.sendMessage(message.quote() + "游戏还未结束，请勿重复操作~")
                return
            }
            val arguments = message.getPlainText().removePrefix("装弹").trim().parseCommand()
            if (arguments.isEmpty()) {
                gameProgress[group.id] =
                    RouletteProgress(7, 1, mutableMapOf(), mutableSetOf(), generateBulletList(7, 1), -1)
                group.sendMessage(message.quote() + "装弹成功！总弹数 7 弹，真弹数 1 弹~\n请发送 开枪 加入 游戏~")
                return
            }
            if (arguments.size == 1 || arguments.size > 2) {
                group.sendMessage(message.quote() + "参数个数不匹配，应为 [弹夹容量] [真弹数量]！")
                return
            }
            if (arguments[0].toIntOrNull() == null || arguments[1].toIntOrNull() == null) {
                group.sendMessage(message.quote() + "参数类型不匹配，应为 [整数] [整数]！")
                return
            }
            val total = arguments[0].toInt()
            val real = arguments[1].toInt()
            if (total !in 1..20) {
                group.sendMessage(message.quote() + "自定义装弹数总数最多 20 个，最少 1 个。请检查参数后再开始游戏~")
                return
            }
            if (real <= 0) {
                group.sendMessage(message.quote() + "咋想的，真弹数比 1 还小还咋玩……请检查参数后再开始游戏~")
                return
            }
            if (real >= total) {
                group.sendMessage(message.quote() + "我去，这么多真弹数？想集体暴毙？！请检查参数后再开始游戏~")
                return
            }
            gameProgress[group.id] = RouletteProgress(
                total, real, mutableMapOf(), mutableSetOf(), generateBulletList(total, real), -1
            )
            group.sendMessage(message.quote() + "装弹成功！总弹数 $total 个，真弹数 $real 个~\n请发送 开枪 加入游戏~")
        }
    }

    fun RouletteProgress.shot(): Boolean {
        bulletNow++
        if (bulletList[bulletNow]) {
            realNum--
            return true
        }
        return false
    }

    fun updateIncome(incomeList: MutableMap<Long, Int>) {
        for (i in incomeList) {
            if (AccountUtils.addMoney(i.key, i.value)) {
                AccountUtils.updateMoney(i.key, 0)
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun GroupMessageEvent.gameHandle() {
        if (message.getPlainText().trim() == "开枪") {
            if (!gameProgress.containsKey(group.id)) {
                group.sendMessage(message.quote() + "该群暂未开启游戏，请发送 装弹 来填充弹夹~")
                return
            }
            val processNow = gameProgress[group.id]!!
            if (sender.id in processNow.eliminated) {
                group.sendMessage(message.quote() + "你已经中弹一次了，想学安倍身中两弹？我看你还是老老实实等待游戏结束吧！")
                return
            }
            val timeLeft = coolDownTimer.tick()
            if (timeLeft != -1L) {
                group.sendMessage(message.quote() + "冷却中，剩余 $timeLeft 秒~")
                return
            }
            val gameMessage = StringBuilder()
            val bulletCount = processNow.bulletList.drop(processNow.bulletNow).count { it }
            if (!processNow.income.containsKey(sender.id)) {
                processNow.income[sender.id] = 0
            }
            if (!processNow.shot()) { // 空枪
                val incomeDelta = (1..7).random() * (processNow.bulletNow + 1)
                gameMessage.append("有惊无险，这是一个空枪！(+${incomeDelta}li)\n")
                processNow.income[sender.id]?.let { processNow.income[sender.id] = it + incomeDelta }
                gameMessage.append("剩余总弹数: ${processNow.totalNum - processNow.realNum - 1}\n")
                gameMessage.append("剩余真弹数: ${processNow.realNum}\n")
            } else { // 炸膛了(bushi)
                processNow.eliminated.add(sender.id)
                val incomeDelta = (1..7).random() * (processNow.bulletNow + 1)
                gameMessage.append(
                    "boom！你送走了你自己！(=${incomeDelta}li)\n"
                )
                processNow.income[sender.id] = -incomeDelta
                gameMessage.append("剩余总弹数: ${processNow.totalNum - processNow.realNum - 1}\n")
                gameMessage.append("剩余真弹数: ${processNow.realNum}\n")
            }
            if (bulletCount == processNow.realNum) { // 必死，游戏结束
                gameMessage.append("弹夹已无空弹，游戏结束！\n")
                updateIncome(processNow.income)
                gameProgress.remove(group.id)
            } else if (processNow.realNum == 1) { // 最后一颗
                gameMessage.append("诶嘿嘿~最后一个倒霉蛋是谁呢？")
            }
            if (processNow.realNum == 0) { // 空弹夹，游戏结束
                gameMessage.append("弹夹已无真弹，游戏结束！\n")
                updateIncome(processNow.income)
                gameProgress.remove(group.id)
            }
            gameMessage.append("\n战绩：\n")
            for (i in processNow.income.toSortedMap()) {
                gameMessage.append("${i.value} [mirai:at:${i.key}]\n")
            }
            group.sendMessage(gameMessage.toString())
        }
    }
}