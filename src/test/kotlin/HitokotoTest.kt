import kotlinx.coroutines.runBlocking
import org.junit.Test
import top.tbpdt.getHitokoto

/**
 * @author Takeoff0518
 */
class HitokotoTest {
    @Test
    fun getHitokotoTest() {
        runBlocking {
            val hitokoto = getHitokoto("d")
            println("“" + hitokoto.hitokoto + "”")
            println("——" + hitokoto.fromWho + "《" + hitokoto.from + "》")
        }
    }
}