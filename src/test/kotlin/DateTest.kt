
import org.junit.Assert.assertEquals
import org.junit.Test
import top.tbpdt.utils.AccountUtils.getDaysBetweenDates
import java.sql.Date

/**
 * @author Takeoff0518
 */
class DateTest {
    @Test
    fun testGetDaysBetweenDates() {
        val date1 = Date(1707570582630) // 2024-02-10 21:09:42
        val date2 = Date(1707605556306) // 2024-02-11 06:52:36
        val result = getDaysBetweenDates(date1,date2)
        println(result)
        assertEquals(1, result)
    }
}