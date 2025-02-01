package top.tbpdt.vanilla.configer

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * @author Takeoff0518
 */
object OllamaConfig : AutoSavePluginConfig("OllmaConfig") {
    @ValueDescription("Ollama 服务器地址")
    val ollamaHost: String by value("http://localhost:11434/")

    @ValueDescription("模型名")
    val modelName: String by value("deepseek-r1:1.5b")

    @ValueDescription("轮询间隔 (ms)\n轮询间隔越小，接收 token 的频率越高")
    val pollIntervalMilliseconds: Long by value(4000L)

    @ValueDescription("时限 (ms)\n超过该时限，输出会被强行终止\n值设置为 -1 可关闭时限")
    val timeLimit: Long by value(-1L)

    @ValueDescription("冷却时间 (ms)\n每次输出完成后的冷却时间。只有过了冷却时间以后才可发起下一次询问。\n值设置为 0 将无冷却时间")
    val cdTime: Long by value(0L)

    @ValueDescription("单次询问消耗经济 (li)")
    val askCost: Int by value(5)
}