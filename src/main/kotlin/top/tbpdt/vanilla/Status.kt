package top.tbpdt.vanilla

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.*
import top.tbpdt.utils.MessageUtils.isCommand
import top.tbpdt.vanilla.utils.StatusRecorder.querySendAndReceive
import top.tbpdt.vanilla.utils.StatusRecorder.updateReceive
import top.tbpdt.vanilla.utils.StatusRecorder.updateSend
import java.sql.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * @author Takeoff0518
 */
object Status : SimpleListenerHost() {
    var sendCount: Int = 0
    var receiveCount: Int = 0

    @EventHandler(priority = EventPriority.LOW)
    fun GroupMessagePostSendEvent.onCount() {
        sendCount++
    }

    @EventHandler(priority = EventPriority.LOW)
    fun FriendMessagePostSendEvent.onCount() {
        sendCount++
    }

    @EventHandler(priority = EventPriority.LOW)
    fun GroupMessageEvent.onCount() {
        receiveCount++
    }

    @EventHandler(priority = EventPriority.LOW)
    fun FriendMessageEvent.onCount() {
        receiveCount++
    }

    @EventHandler(priority = EventPriority.LOW)
    suspend fun MessageEvent.onCommand() {
        if (!message.isCommand("status")) {
            return
        }
        val date = LocalDate.now()
        update()
        var result: Pair<Int, Int>
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val text = StringBuilder().append("近七日 (日期: 收 / 发)：\n")
        for (i in 6L downTo 0L) {
            val queryDate = date.minusDays(i)
            result = querySendAndReceive(Date.valueOf(queryDate))
            text.append("${queryDate.format(formatter)}: ${result.second} / ${result.first}\n")
        }
        subject.sendMessage(text.toString())
    }

    fun update() {
        val date = LocalDate.now()
        updateReceive(Date.valueOf(date), receiveCount)
        updateSend(Date.valueOf(date), sendCount)
        receiveCount = 0
        sendCount = 0
    }

    /**
     * 每个整点执行任务
     */
    fun scheduleHourlyTask(task: () -> Unit) {
        val timer = Timer()
        val now = Calendar.getInstance()
        now.set(Calendar.MINUTE, 59)
        now.set(Calendar.SECOND, 59)
        now.set(Calendar.MILLISECOND, 500)
        if (now.timeInMillis < System.currentTimeMillis()) {
            now.add(Calendar.HOUR_OF_DAY, 1)
        }
        val delay = now.timeInMillis - System.currentTimeMillis()
        val period: Long = 60 * 60 * 1000
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                task()
            }
        }, delay, period)
    }

}
