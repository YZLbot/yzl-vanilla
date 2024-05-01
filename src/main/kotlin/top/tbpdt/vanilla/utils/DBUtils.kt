package top.tbpdt.utils

import top.tbpdt.vanilla.PluginMain.dataFolder
import top.tbpdt.vanilla.PluginMain.logger
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * @author Takeoff0518
 */
object DBUtils {
    private val dbPath = "${dataFolder}${File.separator}data.db"

    fun connectToDB(): Connection {
        val file = File(dbPath)
        if (!file.exists()) {
            createDB()
        }
        return DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    private fun createDB() {
        try {
            File(dbPath).createNewFile()
            getConnection().use { connection ->
                createTable(
                    connection,
                    """
                    CREATE TABLE IF NOT EXISTS cave_comments (
                        cave_id LONG,
                        text TEXT,
                        sender_id LONG,
                        sender_nick TEXT,
                        group_id LONG,
                        group_nick TEXT,
                        pick_count LONG,
                        date DATE,
                        PRIMARY KEY (cave_id)
                    )
                    """
                )

                createTable(
                    connection,
                    """
                    CREATE TABLE IF NOT EXISTS accounts (
                        user_id LONG,
                        user_nick TEXT,
                        encounter_date DATE,
                        last_sign_date DATE,
                        total_sign_days LONG,
                        continuous_sign_days LONG,
                        money LONG,
                        experience LONG,
                        PRIMARY KEY (user_id)
                    )
                    """
                )

                // name 包括文件后缀
                createTable(
                    connection,
                    """
                    CREATE TABLE IF NOT EXISTS cave_pics (
                        name TEXT,
                        url TEXT,
                        PRIMARY KEY (name)
                    )
                    """
                )
            }
        } catch (e: SQLException) {
            logger.error("数据库创建失败: $e")
        }
    }

    fun createTable(connection: Connection, createTableSQL: String) {
        connection.createStatement().use { statement ->
            statement.execute(createTableSQL)
        }
    }

    private fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    fun initCaveDB() {
        Class.forName("org.sqlite.JDBC")
        logger.info("数据库地址: $dbPath")
        connectToDB()
    }
}