# SakuraCDK

一款简易的CDK系统

## 特点

* 简单易用
* 支持数据库 MySQL、MariaDB、SQLite、H2、Oracle、PostgreSQL、SQLServer
* 发放命令与物品
* 全程异步运行，主线程0影响 (除了运行礼包命令)
* 支持热重载

## 使用方法

### 1. **安装**:

将插件放入服务端 plugins 文件夹中，重启服务器或者使用插件热重载

### 2. **配置**:

**数据库**

插件默认使用H2 数据库，

更多信息请在`config.yml`中修改

所有配置自动重载，无需使用命令重载

**先创建 kit**

在 kits.yml 中添加

~~~ yaml
# 是否自动更新
auto-Update: true
kits:
  # 取个名字，随意，不冲突即可
  test:
    # 必须复制一份
    ==: top.iseason.bukkit.sakuracdk.data.KitYml
    # 识别id，不允许重复
    id: test
    # 创建日期，随意，无关紧要
    create: "2022-08-10T23:02:32.052"
    # 到期时间，到期后礼包不会再发放
    expires: "2022-09-10T23:02:32.056"
    # 执行命令，%player% 为玩家名称占位符
    # CMD: 前缀 表示控制台执行
    # OP: 前缀 表示玩家以op身份执行
    # 没有前缀 表示玩家自身执行
    commands:
      - CMD:gamemode survival %player%
      - OP:fly
      - spawn
    # 给予玩家的物品, 通过cdkadmin addItem [id] 添加
    itemStacks:
~~~

**如以上，也可通过命令创建**

~~~ text
/cdkadmin kit create [id] [过期时间]  创建礼包
/cdkadmin kit delete [id]  删除礼包
/cdkadmin kit addItem [id]  给礼包添加手上物品
/cdkadmin kit give [id] [player]  将礼包给予玩家,不会有记录
~~~

---

**创建 cdk**
打开cdk.yml文件

~~~ yaml
# 如果type是 normal 则该键将作为cdk
test:
  # normal 类型为 可重复多次使用的CDK，每人只能领取一次
  type: normal
  # 最大领取次数
  amount: 10
  # 过期时间
  expire: '2022-09-10T23:02:32.061'
  # 对应的礼包，可以多个
  kits:
  - test

# 如果type是 random 则对应random文件夹中的txt 随机cdk
# 通过命令 /cdkadmin randomCDK [id] [amount]  创建
# 命令中的id对应这个键
test2:
  type: random
  # 是否允许共一个玩家多次领取
  repeat: true
  # 过期时间
  expire: '2022-09-10T23:02:32.063'
  # 对应的礼包，可以多个
  kits:
  - test

~~~

看以上注释

---

**3. 完成配置**

由于跨服支持，本地修改的数据不会立即生效

需要使用命令: `/cdkadmin update <type> ` 将数据同步至数据库

其中 type:

* 为 `all` 时上传所有数据
* 为 `cdk` 时上传cdk.yml数据
* 为 `kit` 时上传kits.yml数据
* 为 `random` 时上传random文件夹下所有数据

命令` /cdkadmin download <type>  `将数据库的数据下载到本地

type同以上

## 用户使用

玩家输入 `/cdk [cdk]`来兑换cdk

## 其他

命令可以通过输入`/cdkadmin `来查看更多管理命令

玩家命令只有/cdk 一条
当命令冲突时可以使用
`/sakuracdk` 或 `/cdks`

## 命令权限

命令权限为sakuracdk.节点名称

比如`/cdkadmin kit addItem` 的权限为`sakuracdk.cdkadmin.kit.addItem`