package top.tbpdt.vanilla.utils

import top.tbpdt.utils.DBUtils
import top.tbpdt.vanilla.PluginMain
import java.sql.Date

/**
 * @author Takeoff0518
 */

// replaced by SQLite
//object StatusRecorder: AutoSavePluginData("StatusRecorder") {
//
//    val receiveCount: MutableMap<String, Int> by value(mutableMapOf())
//
//    val sendCount: MutableMap<String, Int> by value(mutableMapOf())
//}

object StatusRecorder {
    fun createTable() {
        DBUtils.connectToDB().use { connection ->
            DBUtils.createTable(
                connection,
                """
                    CREATE TABLE IF NOT EXISTS status (
                    date DATE,
                    send INT,
                    receive INT,
                    sign INT,
                    nudge INT,
                    PRIMARY KEY (date)
                    )
                    """
            )
        }
        PluginMain.logger.info("已尝试创建数据表 status")
    }

    fun updateSend(date: Date, delta: Int) {
        val query = """
            INSERT INTO status (date, send, receive, sign, nudge)
            VALUES (?, ?, 0, 0, 0)
            ON CONFLICT(date) DO UPDATE SET
            send = send + excluded.send;
        """

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setDate(1, date)
                preparedStatement.setInt(2, delta)

                preparedStatement.executeUpdate()
            }
        }
    }

    fun updateReceive(date: Date, delta: Int) {
        val query = """
            INSERT INTO status (date, send, receive, sign, nudge)
            VALUES (?, 0, ?, 0, 0)
            ON CONFLICT(date) DO UPDATE SET
            receive = receive + excluded.receive;
        """

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setInt(1, delta)
                preparedStatement.setDate(2, date)

                preparedStatement.executeUpdate()
            }
        }
    }

    fun updateNudge(date: Date) {
        val query = """
            INSERT INTO status (date, send, receive, sign, nudge)
            VALUES (?, 0, 0, 0, 1)
            ON CONFLICT(date) DO UPDATE SET
            send = send + 1;
        """

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setDate(1, date)

                preparedStatement.executeUpdate()
            }
        }
    }

    fun updateSign(date: Date) {
        val query = """
            INSERT INTO status (date, send, receive, sign, nudge)
            VALUES (?, 0, 0, 1, 0)
            ON CONFLICT(date) DO UPDATE SET
            sign = sign + 1;
        """

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setDate(1, date)

                preparedStatement.executeUpdate()
            }
        }
    }

    fun querySendAndReceive(date: Date): Pair<Int, Int> {
        val query = "SELECT send, receive FROM status WHERE date = ?"
        var send = 0
        var receive = 0

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setDate(1, date)
                val resultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    send = resultSet.getInt("send")
                    receive = resultSet.getInt("receive")
                }
            }
        }
        return Pair(send, receive)
    }

    fun querySign(date: Date): Int {
        val query = "SELECT sign FROM status WHERE date = ?"
        var sign = 0

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setDate(1, date)
                val resultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    sign = resultSet.getInt("sign")
                }
            }
        }
        return sign
    }

    fun queryNudge(date: Date): Int {
        val query = "SELECT nudge FROM status WHERE date = ?"
        var nudge = 0

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setDate(1, date)
                val resultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    nudge = resultSet.getInt("nudge")
                }
            }
        }
        return nudge
    }
}
