package top.tbpdt.vanilla

import io.github.ollama4j.OllamaAPI
import kotlinx.coroutines.delay
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import top.tbpdt.utils.AccountUtils
import top.tbpdt.utils.MessageUtils.getRemovedPrefixCommand
import top.tbpdt.utils.MessageUtils.isCommand
import top.tbpdt.vanilla.configer.OllamaConfig
import top.tbpdt.vanilla.utils.CDTimer

/**
 * @author Takeoff0518
 */
object Ollama : SimpleListenerHost() {

    private val cdTimer = CDTimer(OllamaConfig.cdTime)
    private val limitTimer = CDTimer(OllamaConfig.timeLimit)
    var isOccupied = false

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun GroupMessageEvent.handle() {
        if (!message.isCommand("ask")) {
            return
        }
        if (isOccupied) {
            group.sendMessage("目前服务器正在生成内容哦，请稍后再试~")
            return
        }
        if (!AccountUtils.addMoney(sender.id, -OllamaConfig.askCost)) {
            group.sendMessage("所需 li 不足哦~ (${AccountUtils.queryMoney(sender.id)} < ${OllamaConfig.askCost})")
            return
        }
        val prompt = message.getRemovedPrefixCommand("ask")
        if (prompt.isEmpty()) {
            group.sendMessage(message.quote() + "你好像什么都没问诶…")
            return
        }
        val cdTime = cdTimer.tick()
        if (cdTime != -1L) {
            group.sendMessage(message.quote() + "冷却中，剩余 $cdTime 秒")
            return
        }
        val ollamaAPI = OllamaAPI(OllamaConfig.ollamaHost)
        ollamaAPI.setRequestTimeoutSeconds(60)
        if (!ollamaAPI.ping()) {
            group.sendMessage(message.quote() + "唔姆，失去了与 Ollama 服务器的联系…请联系一下饲养员吧~")
            return
        }
        if (OllamaConfig.timeLimit == -1L) {
            limitTimer.lastCalledTime = 0L
            limitTimer.tick()
        }
        isOccupied = true
        group.sendMessage(
            message.quote() + "即将通过 ${OllamaConfig.modelName} 生成内容… (-${OllamaConfig.askCost} li)\n" +
                    "轮询间隔：${OllamaConfig.pollIntervalMilliseconds} ms"
        )
        val streamer = ollamaAPI.generateAsync(OllamaConfig.modelName, prompt, false)
        while (true) {
            val tokens = streamer.stream.poll()
            if (tokens.isNotEmpty()) {
                group.sendMessage(tokens)
            }
            if (!streamer.isAlive) {
                isOccupied = false
                break
            }
            if (OllamaConfig.timeLimit != -1L && limitTimer.tick() == -1L) {
                group.sendMessage("时间超限，已自动截断~")
                streamer.interrupt()
                break
            }
            delay(OllamaConfig.pollIntervalMilliseconds)
        }
        if (OllamaConfig.timeLimit != -1L) {
            cdTimer.lastCalledTime = 0L
            cdTimer.tick()
        }
//        group.sendMessage(streamer.completeResponse)
    }
}