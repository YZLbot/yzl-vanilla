package top.tbpdt.vanilla

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.*
import top.tbpdt.utils.MessageUtils.isCommand
import top.tbpdt.vanilla.utils.StatusRecorder.receiveCount
import top.tbpdt.vanilla.utils.StatusRecorder.sendCount
import java.time.LocalDate
import java.util.*

/**
 * @author Takeoff0518
 */
object Status: SimpleListenerHost() {
    var sendGroupMsgCount: Int = 0
    var sendFriendMsgCount: Int = 0
    var receiveGroupMsgCount: Int = 0
    var receiveFriendMsgCount: Int = 0

    private fun addSendCount(){
        val date = LocalDate.now()
        val dateStr = "${date.year}-${date.monthValue}-${date.dayOfMonth}"
        sendCount[dateStr] = (sendCount[dateStr] ?: 0) + 1
    }

    private fun addReceiveCount(){
        val date = LocalDate.now()
        val dateStr = "${date.year}-${date.monthValue}-${date.dayOfMonth}"
        receiveCount[dateStr] = (receiveCount[dateStr] ?: 0) + 1
    }

    @EventHandler(priority = EventPriority.LOW)
    fun GroupMessagePostSendEvent.onCount(){
        addSendCount()
        sendGroupMsgCount ++
    }

    @EventHandler(priority = EventPriority.LOW)
    fun FriendMessagePostSendEvent.onCount(){
        addSendCount()
        sendFriendMsgCount ++
    }

    @EventHandler(priority = EventPriority.LOW)
    fun GroupMessageEvent.onCount(){
        addReceiveCount()
        receiveGroupMsgCount ++
    }

    @EventHandler(priority = EventPriority.LOW)
    fun FriendMessageEvent.onCount(){
        addReceiveCount()
        receiveFriendMsgCount ++
    }

    @EventHandler(priority = EventPriority.LOW)
    suspend fun MessageEvent.onCommand(){
        if(!message.isCommand("status")){
            return
        }
        val date = LocalDate.now()
        val dateStr = "${date.year}-${date.monthValue}-${date.dayOfMonth}"
        val text = "自服务端启动起：\n" +
                "群聊：收 $receiveGroupMsgCount / 发 $sendGroupMsgCount\n" +
                "好友：收 $receiveFriendMsgCount / 发 $sendFriendMsgCount\n" +
                "----------\n" +
                "今日：收 ${receiveCount[dateStr] ?: 0} / 发 ${sendCount[dateStr] ?: 0}"
        subject.sendMessage(text)
    }

    /**
     * 每个整点执行任务
     */
    fun scheduleHourlyTask(task: () -> Unit) {
        val timer = Timer()
        val now = Calendar.getInstance()
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
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
