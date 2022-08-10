package top.iseason.bukkit.sakuracdk.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.configuration.ConfigurationSection
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.config.annotations.Comment
import top.iseason.bukkit.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkit.bukkittemplate.config.annotations.Key
import top.iseason.bukkit.bukkittemplate.debug.SimpleLogger
import top.iseason.bukkit.bukkittemplate.debug.info
import top.iseason.bukkit.bukkittemplate.dependency.DependencyDownloader
import top.iseason.bukkit.sakuracdk.SakuraCDK
import top.iseason.bukkit.sakuracdk.data.*
import java.io.File
import java.sql.SQLException

@FilePath("config.yml")
object Config : SimpleYAMLConfig() {
    lateinit var mysql: Database

    @Key
    @Comment("数据库类型: 支持 MySQL、MariaDB、SQLite、H2、Oracle、PostgreSQL、SQLServer")
    var dbType = "H2"

    @Key
    @Comment("数据库地址")
    var url = File(SakuraCDK.javaPlugin.dataFolder, "database").absoluteFile.toString()

    @Key
    @Comment("", "数据库名")
    var dbName = "database"


    @Key
    @Comment("", "数据库用户名，如果有的话")

    var user = "test"

    @Key
    @Comment("", "数据库密码，如果有的话")
    var password = "password"

    @Key
    @Comment("", "随机cdk模板，X将被替换为随机字符")
    var cdkTemplate = "XXXX-XXXXXXXX"

    override val onLoaded: ConfigurationSection.() -> Unit = {
//        println("loaded")
    }

    override val onSaved: (ConfigurationSection.() -> Unit) = {
//        println("saved")
    }
    private var ds: HikariDataSource? = null

    var isConnected = false
        private set

    fun closeDB() {
        try {
            ds?.close()
            TransactionManager.closeAndUnregister(mysql)
        } catch (_: Exception) {
        }
    }

    fun reConnectedDB() {
        isConnected = false
        info("&6数据库链接中...")
        try {
            closeDB()
            val dd = DependencyDownloader()
            dd.repositories.clear()
            dd.addRepository("https://maven.aliyun.com/repository/public")
            val config = when (dbType) {
                "SQLite" -> HikariConfig().apply {
                    dd.downloadDependency("org.xerial:sqlite-jdbc:3.39.2.0")
                    jdbcUrl = "jdbc:sqlite:$url"
                    driverClassName = "org.sqlite.JDBC"
                }

                "H2" -> HikariConfig().apply {
                    dd.downloadDependency("com.h2database:h2:2.1.214")
                    jdbcUrl = "jdbc:h2:$url;TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0"
                    driverClassName = "org.h2.Driver"
                }

                "PostgreSQL" -> HikariConfig().apply {
                    dd.downloadDependency("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.9")
                    jdbcUrl = "jdbc:postgresql://$url"
                    driverClassName = "com.impossibl.postgres.jdbc.PGDriver"
                    username = user
                    password = Config.password
                }

                "Oracle" -> HikariConfig().apply {
                    dd.downloadDependency("com.oracle.database.jdbc:ojdbc8:21.6.0.0.1")
                    jdbcUrl = "dbc:oracle:thin:@//$url"
                    driverClassName = "oracle.jdbc.OracleDriver"
                    username = user
                    password = Config.password
                }

                "SQLServer" -> HikariConfig().apply {
                    dd.downloadDependency("com.microsoft.sqlserver:mssql-jdbc:10.2.1.jre8")
                    jdbcUrl = "jdbc:sqlserver://$url"
                    driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
                    username = user
                    password = Config.password
                }

                "MySQL", "MariaDB" -> HikariConfig().apply {
                    dd.downloadDependency("mysql:mysql-connector-java:8.0.30")
                    jdbcUrl = "jdbc:mysql://$url?charactorEncoding=utf-8mb4"
                    driverClassName = "com.mysql.cj.jdbc.Driver"
                    username = user
                    password = Config.password
                }

                else -> throw Exception("错误的数据库类型!")
            }
            with(config) {
                maximumPoolSize = 10
                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                poolName = "SakuraCDK"
            }
            ds = HikariDataSource(config)
            mysql = Database.connect(ds!!)
            transaction {
                if (SimpleLogger.isDebug) addLogger(StdOutSqlLogger)
                if (!dbType.equals("sqlite", true)) {
                    val schema = Schema(dbName)
                    SchemaUtils.createSchema(schema)
                    SchemaUtils.setSchema(schema)
                }
                SchemaUtils.create(Kits, Records, NormalCDKs, RandomCDKs, CDKs)
            }
            isConnected = true
            info("&a数据库链接成功!")
        } catch (e: Exception) {
            info("&c数据库链接失败!")
        } catch (e: SQLException) {
            info("&c数据库链接失败!")
        }

    }

    fun reload() {
        if (isConnected) {
            closeDB()
            CDKsYml.onDisable()
        }
        reConnectedDB()
    }

}