# SakuraCDK

一款纯粹的CDK系统

## 使用场景

* 制作口令cdk礼包, 如 输入 xxx节快乐 领取礼包
* 制作随机cdk礼包, 如 输入 a56D-NI41H-XK51DG 领取礼包，可修改模板

## 特点

* 简单易用
* 支持跨服, 数据库类型 MySQL、MariaDB、SQLite、Oracle、PostgreSQL、SQLServer
* 发放命令与物品
* 全程异步运行，主线程0影响 (除了运行礼包命令)
* 自动重载配置，解放命令
* cdk\kit 支持限时限量
* 丰富的消息API

## 截图

查询cdk信息

![查询cdk信息](https://user-images.githubusercontent.com/65019366/216334442-94f8065b-13ee-4405-830f-8f064249ad36.png)

游戏内编辑礼包物品

![游戏内编辑礼包物品](https://user-images.githubusercontent.com/65019366/216334469-e7e70e57-78cc-4517-85b6-9c27836ca1c4.png)


## 使用方法

### 1. **安装**:

将插件放入服务端 plugins 文件夹中，重启服务器或者使用插件热重载

### 2. **配置**:

**数据库**

插件默认使用 Sqlite 数据库，在1.12.2以下的版本中可能报错，请改为其他数据库

更多信息请在`config.yml`中修改

所有配置自动重载，无需使用命令重载。 但如果自动重载失效也可使用
`cdkadmin reload` 命令重载

**cdk说明**
cdk由2部分构成: `cdk`、和`kit`

cdk 包括口令cdk和随机cdk2种, 每个随机cdk只能使用一次

kit包含物品或命令, 当玩家兑换cdk时将会给予对应的kit

一个kit可以被多个cdk重复使用

**先创建 kit**

在 kits.yml 中添加

~~~ yaml
# 是否自动更新
auto-Update: true
kits:
  # 取个名字，随意，不冲突即可
  test:
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
    # 给予玩家的物品, 通过cdkadmin edit [id] 添加
    itemStacks:
~~~

**如以上，也可通过命令创建**

~~~ text
/cdkadmin kit create [id] [过期时间]  创建礼包
/cdkadmin kit delete [id]  删除礼包
/cdkadmin kit edit [id]  编辑该礼包的物品
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

玩家输入 `/sakuracdk [cdk]`来兑换cdk

可在`config.yml` 中 修改 `sakuracdk` 和别名

该命令权限等于sakuracdk.命令名, 如: sakuracdk.sakuracdk

## 其他

命令可以通过输入`/cdkadmin `来查看更多管理命令

玩家默认有权限的命令只有/sakuracdk 一条

## 命令权限

命令权限为sakuracdk.节点名称

比如`/cdkadmin kit edit` 的权限为`sakuracdk.cdkadmin.kit.edit`

## 下载

Github: https://github.com/SakuraTown/SakuraCDK/releases

蓝奏: https://iseason.lanzouf.com/b00qn5cif  密码:6g2e
