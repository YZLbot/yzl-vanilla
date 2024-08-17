package top.tbpdt.vanilla.utils

/**
 * 冷却时间计时器
 *
 * @author Takeoff0518
 * @param cdTime 冷却时间 (ms)
 */
class CDTimer(private val cdTime: Long) {
    private var lastCalledTime = 0L

    /**
     * 检查冷却时间
     *
     * >宜群！宣群！不择手段的宣群！要强杀群星！
     * >——时间之外的往事(188056008)
     *
     * @return 第一次调用时，返回 `-1`。
     *
     * 在后续的调用中，如果超过冷却时间，返回 `-1`，否则返回剩余时间的秒数。
     *
     * @See top.tbpdt.utils.CaveUtils.checkInterval
     */
    fun tick(): Long {
        val currentTime = System.currentTimeMillis()
        val isFirst = lastCalledTime == 0L
        return if (isFirst) {
            lastCalledTime = currentTime
            -1
        } else {
            val elapsedTime = currentTime - lastCalledTime
            if (elapsedTime < cdTime) {
                (cdTime / 1000 - elapsedTime / 1000)
            } else {
                lastCalledTime = currentTime
                -1
            }
        }
    }
}