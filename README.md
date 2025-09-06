# RedisInsight
专注于Redis深度探索，从基础类型到集群配置，一条龙贯通

## 前言

Redis（Remote Dictionary Server）是一个开源的**内存数据库**，以高性能和灵活的数据结构著称。它通常用作缓存、消息队列或实时数据处理系统，支持持久化到磁盘，保证数据安全。

### 核心特点

1. **内存存储**：数据主要存在内存中，读写速度极快（微秒级）。
2. **丰富的数据结构**：支持字符串（String）、哈希（Hash）、列表（List）、集合（Set）、有序集合（ZSet）等。
3. **持久化**：通过 RDB（快照）和 AOF（日志）两种方式将数据保存到磁盘。
4. **高并发**：单线程模型避免锁竞争，利用 I/O 多路复用处理高并发请求。
5. **多功能**：支持事务、发布/订阅、Lua 脚本、过期键等特性。

### 常见用途

- **缓存**：加速热点数据访问（如网页缓存）。
- **会话存储**：存储用户登录状态（如 Session）。
- **排行榜**：利用 ZSet 实现实时排名。
- **消息队列**：通过 List 或 Stream 实现异步任务。

### 简单示例

```bash
# 写入数据
SET user:1 "Alice"
# 读取数据
GET user:1# 返回 "Alice"
# 设置过期时间（秒）
EXPIRE user:1 60
```

### 对比传统数据库

- **优势**：性能极高，适合实时场景。
- **局限**：内存成本高，不适合存储超大规模冷数据。

Redis 适合需要快速读写和灵活数据模型的场景，但需权衡内存开销与持久化需求。

## 基础数据结构与命令

### String（字符串）类型

#### **简单介绍**

String 类型是 Redis 最常用的数据类型，其内部基于 K，V 存储，Key 是字符串，Value 可以是字符串、数字或二进制数据。几乎适配大部分的业务场景，但同时也需要注意下大 Value 的问题。

#### 简单操作示例

```shell
# 设值/取值
SET key value  # 存储（如 SET user:1 "Alice"）
SETNX key value # 如果不存在则存储
GET key        # 获取（如 GET user:1 → "Alice"） **(进阶)**
GETSET key value # 替换并返回旧值（适合原子替换）**(进阶)**
# 数字增减
INCR key      # +1（如 INCR views）
DECRBY key 5  # 自定义减量
INCRBYFLOAT key increment # 浮点数原子增减 **(进阶)**
# 批量操作
MSET k1 v1 k2 v2  # 批量设值
MGET k1 k2        # 批量获取
MSETNX key1 value1 key2 value2 # 仅当所有键不存在时批量设值 **(进阶)**
# 过期控制
SETEX key 10 value  # 10秒后过期
TTL key             # 查看剩余时间
# 位操作
SETBIT key 0 1  # 设置第0位为1（如签到）
GETBIT key 0    # 获取位值
```

#### Java 代码对应示例

- 接口封装

```java
_/**
 * 缓存提供者，接口抽象专注于缓存
 *
__ * _**@author **_王玉涛
__ * _**@version **_1.0
__ * _**@since **_2025/8/8
 */
public interface StringCacheProvider {

    /**
     * 设置缓存
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 缓存value
     */
    void setString(String key, String value);

    /**
     * 设置缓存, 当key不存在时才设置
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 缓存value
__     * _**@return **_是否设置成功
     */
    Boolean setStringWhenNotExists(String key, String value);

    /**
     * 设置缓存, 当key不存在时才设置, 默认过期时间单位是秒
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 缓存value
__     * _**@param **_expire 过期时间
__     * _**@return **_是否设置成功
     */
    Boolean setStringWhenNotExists(String key, String value, long expire);

    /**
     * 获取并设置缓存, 默认返回旧值
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 缓存value
__     * _**@return **_旧值
     */
    String getAndSetString(String key, String value);

    /**
     * 带过期时间的设置缓存, 默认为秒
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 缓存value
__     * _**@param **_expire 过期时间
     */
    void setString(String key, String value, long expire);

    /**
     * 获取缓存的过期时间
     *
__     * _**@param **_key 缓存key
__     * _**@return **_过期时间
     */
    long ttlKey(String key);

    /**
     * 获取缓存, 如果不存在则返回空字符串
     *
__     * _**@param **_key 缓存key
__     * _**@return **_缓存value
     */
    String getString(String key);

    /**
     * 自增缓存，无需get/set更加高效
     *
__     * _**@param **_key 缓存key
     */
    void incrString(String key);

    /**
     * 自定义量自增缓存，无需get/set更加高效
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 自增数量
     */
    void incrString(String key, long value);

    /**
     * 自定义量自增缓存，无需get/set更加高效
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 自增数量
     */
    void incrString(String key, double value);

    /**
     * 自减缓存，无需get/set更加高效
     *
__     * _**@param **_key 缓存key
     */
    void decrString(String key);

    /**
     * 自定义量自减缓存，无需get/set更加高效
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 自减数量
     */
    void decrString(String key, long value);

    /**
     * 批量设置缓存
     *
__     * _**@param **_map 缓存map
     */
    void batchSetString(Map<String, String> map);

    /**
     * 批量设置缓存
     *
__     * _**@param **_keysAndValues 缓存key和value
     */
    void batchSetString(String... keysAndValues);

    /**
     * 当缓存不存在时，批量设置缓存
     *
__     * _**@param **_map 缓存map
     */
    void batchSetWhenNotExists(Map<String, String> map);

    /**
     * 设置缓存的bit位
     *
__     * _**@param **_key    缓存key
__     * _**@param **_offset bit位
__     * _**@param **_value  bit值
     */
    void setBitString(String key, long offset, boolean value);

    /**
     * 获取缓存的bit位
     *
__     * _**@param **_key    缓存key
__     * _**@param **_offset bit位
__     * _**@return **_bit值
     */
__    Boolean getBitString(String key, long offset);_
    
    _/**_
    _ * 获取缓存的过期时间(s)_
    _ *_
    _ * _**@param **_key 缓存key_
    _ * _**@return **_过期时间_
    _ */_
    Long getExpireTime(String _key_);_

    /**
     * 删除缓存
     *
__     * _**@param **_key 缓存key
     */
    void delete(String key);
__}_
```

#### 一些注意事项

- 避免 String 类型过大（超过 1MB），因为会**单线程阻塞**（耗时增加，阻塞单线程）; **网络延迟增大**；内存碎片频繁分片，降低内存利用率
- INCR/DECRBY 命令要求必须是数字或者可以被转化为数字的字符串，不然会报错，Java 后端使用的时候需要进行错误异常处理。
- INCR/DECRBY 命令要比手动 set/get 更加高效，更适合数字的增减场景，并且通过原子性操作也保证了高并发情况下的资源安全。

#### **为什么 String 类型过大会有这么大影响？**

- Redis 内部以页为单位存储，而一页的单位很小（与操作系统有关，大部分为 4KB）
- Redis 内部的页是虚拟空间的连续地址（逻辑连续），其页跳转逻辑由操作系统维护
- Redis 查询的时候，先在 CPU 缓存中进行
- 而当 String 类型较小的时候（例如 <10KB），直接在 CPU 内部即可完成所有查询，效率极高
- 而当 String 类型越大（>10KB），CPU 缓存无法容纳，命中率降低，会涉及到查询页表，导致平均查询效率降低，越大效率降低越明显
- Redis 的单线程模型又放大了这一效应，大 String 会阻塞后续所有的请求，因此 String 类型不建议过大，10KB 以内是效率巅峰。

#### **为什么在特定场景下，更推荐使用高级指令保证原子性？**

- 在 Redis 的单线程模型中，可以将该线程视作指令执行队列
- 在指令进入 Redis 内的过程中，原子性的指令会被打包一次性进入队列，保证中间不会被其他的指令插入
- 因此在 Redis 的执行过程中，原子性操作会一次性执行完，不会被其他指令插入
- 例如使用 INCR 进行数字增加，原操作需要 GET 再 SET，期间会出现其他命令插入，导致出现数据被篡改等风险，但是直接使用 INCR，即可避免数据篡改风险

#### **其他比较高级的玩法（Redisson）**

##### 分布式互斥锁

在单节点 JVM 内部，可以通过 synchronized 实现加锁，其性能也会比基于 Redis 的互斥锁性能更高，但是当出现跨 JVM/跨主机互斥时候，就需要分布式互斥锁来实现资源保护。

简单实现

```shell
SETNX lock:order 1  # 抢锁（1=成功，0=失败）
EXPIRE lock:order 10  # 必须设置过期，防死锁
```

1. 加锁（原子操作）

```shell
SET lock_key unique_value NX PX 30000  # 唯一值+不存在才设置+30秒自动过期
```

- unique_value（如 UUID）：确保只有锁持有者可解锁
- NX：仅当 lock_key 不存在时设值（防重复加锁）
- PX 30000：锁自动过期（防死锁）

1. 解锁（Lua 脚本保证原子性）

```lua
if redis.call("GET", KEYS[1]) == ARGV[1] then
    return redis.call("DEL", KEYS[1])  # 值匹配才删除
else
    return 0
end
```

1. 总结
   虽然手动实现互斥锁很简单，但是更推荐使用 Redisson 成熟中间键，其机制更全，更推荐在生产环境使用。
2. Redisson 直接使用

- 接口封装

```java
_/**_
_ * 分布式锁操作接口提供，专注于高级功能提供_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/9_
_ */_
public interface _DistributedLockProvider _{

    _/**_
_     * 尝试获取锁_
_     * _**@param **_key 锁标识_
_     * _**@param **_waitTime 最大等待时间(ms)_
_     * _**@param **_holdTime 锁持有时间(ms)_
_     * _**@return **_是否获锁成功_
_     */_
_    _boolean tryLock(String _key_, long _waitTime_, long _holdTime_) throws InterruptedException;

    _/**_
_     * 释放锁_
_     * _**@param **_key 锁标识_
_     */_
_    _void unlock(String _key_);
}
```

使用示例

```java
_/**_
_ * 核心业务类，用来进行对应接口的测试_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/9_
_ */_
@Slf4j
public class CoreService {

   @Resource
   private _DistributedLockProvider _lockProvider;

    _/**_
_     * 测试分布式锁_
_     */_
_    _public void testDistributedLock() {
        try {
            if (lockProvider.tryLock("test", 5000, 5000)) {
                _// 业务代码_
_            _}
        } catch (InterruptedException e) {
            Thread._currentThread_().interrupt();
            _log_.error("获取锁失败！");
        } finally {
            lockProvider.unlock("test");
        }
    }
}
```

注意：本文档倾向于面向 Redis 的底层讲解，因此针对于 Redis 的高级封装 Redisson 只提供封装好的代码，可以直接使用，有机会会为 Redisson 单独进行讲述。

---

##### RBucket 简单 K、V 操作

RBucket 是基于 Redis 的 String 类型进行的封装，其提供了以下高级功能

- 原子操作：如 compareAndSet（CAS）、getAndSet
- 对象序列化：自动处理 Java 对象 ↔ Redis String 的转换（支持多种编解码器）
- 过期控制：支持 expire、expireAt 等动态设置 TTL
- 监听器：支持监听 Key 的删除/过期事件（如 addListener）
- 批量操作：与 Redisson 其他组件（如 RMap、RList）无缝协作

#### 总结

Redisson 属于比较重量级的客户端，如果没有分布式锁等高级特性的需要，尽量不引用，对于轻量化需求的应用，可以基于 Redis 进行手动封装。

### Bitmap 类型

#### 介绍

Bitmap（位图）并不是单独的数据类型，而是基于 Redis 的 String **类型**实现的二进制数组，其本质依旧是字符串，但是仅由 0/1 构成。其空间消耗极小（1 亿统计约 12MB）、查询高效 `O(1)`、即使是 O(n)的 `BITCOUNT` 操作，其优化也十分好，同时支持复杂的位运算。

_Bitmap 可以统计第 n 位的布尔状态_。使用业务话术来说，_其可以统计单位时间/空间内，第 n 位或者连续 n 位的是与否状态_。例如：用户某一天签到、统计用户连续签到天数、统计用户一个月内签到次数、标记用户 n 是否是 vip、布隆过滤器(判断某个东西是否存在) ......

#### 注意事项

1. _避免稀疏_：Redis 会根据设置的 offset，进行 0 值填充，如果 offset 极大，那么就会导致大部分的二进制位被浪费，Redis 会分配足够的内存确保可以覆盖。因此需要合理设置 offset 大小。
2. _定时监控_：尽管 bitmap 的空间消耗极小，但是 offset 的不合理设置，会可能导致 bitmap 突然膨胀，建议使用 `MEMORY USAGE key` 定时监控内存占用。
3. _大 Key 操作风险_：尽管 Redis 针对 `BITCOUNT` 这种 O(n)级别的操作进行了优化，但是依旧注意避免大 Key 的性能消耗风险，其依旧可能导致命令阻塞。
4. 其他的注意事项

<table>
<tr>
<td>场景<br/></td><td>Bitmap 优势<br/></td><td>替代方案<br/></td></tr>
<tr>
<td>每日签到统计<br/></td><td>极省内存（12MB/1亿用户）<br/></td><td>`Set`（内存占用高）<br/></td></tr>
<tr>
<td>实时在线状态<br/></td><td>毫秒级更新/查询<br/></td><td>`Hash`（存储成本高）<br/></td></tr>
<tr>
<td>用户特征过滤<br/></td><td>快速位运算组合条件<br/></td><td>`SINTER`（Set 交集）<br/></td></tr>
<tr>
<td>精确去重计数<br/></td><td>需配合布隆过滤器<br/></td><td>`HyperLogLog`（近似计数）<br/></td></tr>
</table>

#### 简单操作示例

```bash
# === 1. 用户签到场景 ===
# 初始化用户1000的2024年4月签到数据（0=未签，1=已签）
SETBIT sign:user:1000:202404 0 1  # 第1天签到
SETBIT sign:user:1000:202404 2 1  # 第3天签到
SETBIT sign:user:1000:202404 3 1  # 第4天签到
SETBIT sign:user:1000:202404 6 1  # 第7天签到

# 查询操作
GETBIT sign:user:1000:202404 3    # 检查第4天是否签到 → 返回1
BITCOUNT sign:user:1000:202404     # 统计总签到天数 → 返回4
BITPOS sign:user:1000:202404 1     # 查找首次签到日 → 返回0（第1天）

# 连续签到判断（检查第2~4天是否全签）
BITOP AND tmp_continuous sign:user:1000:202404 "\x1C"  # 0x1C = 00011100（二进制掩码）
BITCOUNT tmp_continuous  # 若返回3表示连续签到


# === 2. 在线状态监控 ===
# 记录设备在线状态（设备ID=5,7,9在线）
SETBIT online:devices:20240401 5 1
SETBIT online:devices:20240401 7 1
SETBIT online:devices:20240401 9 1

# 统计与查询
BITCOUNT online:devices:20240401  # 在线设备数 → 3
GETBIT online:devices:20240401 7   # 设备7是否在线 → 1


# === 3. 用户特征筛选 ===
# 标记用户特征（用户1000是VIP且活跃）
SETBIT user:vip 1000 1
SETBIT user:active 1000 1

# 组合筛选（VIP且活跃的用户）
BITOP AND vip_and_active user:vip user:active
GETBIT vip_and_active 1000  # 用户1000是否满足 → 1


# === 4. 内存与性能管理 ===
MEMORY USAGE sign:user:1000:202404  # 查看内存占用（字节）
DEL tmp_continuous  # 清理临时键


# === 5. 高级技巧 ===
# 分片存储（按月拆分签到数据）
SETBIT sign:user:1000:202404 0 1
SETBIT sign:user:1000:202405 1 1

# 跨Bitmap统计（合并两个月签到天数）
BITOP OR sign:user:1000:Q2 sign:user:1000:202404 sign:user:1000:202405
BITCOUNT sign:user:1000:Q2
```

#### Java 代码示例

- 接口提供

```java
_/**_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/23_
_ */_
public interface _BitMapCacheProvider _{

    _/**_
_     * 设置位图中指定偏移量的值_
_     *_
_     * _**@param **_key    位图的键_
_     * _**@param **_offset 偏移量_
_     * _**@param **_value  要设置的值（0 或 1）_
_     */_
_    _void setBit(String _key_, long _offset_, boolean _value_);

    _/**_
_     * 获取位图中指定偏移量的值_
_     *_
_     * _**@param **_key    位图的键_
_     * _**@param **_offset 偏移量_
_     * _**@return **_指定偏移量的值_
_     */_
_    _Boolean getBit(String key, long offset);

    _/**_
_     * 统计位图中被设置为1的位数_
_     *_
_     * _**@param **_key 位图的键_
_     * _**@return **_被设置为1的位数_
_     */_
_    _Long bitCount(String _key_);

    _/**_
_     * 查找位图中第一个被设置为指定值的位的位置_
_     *_
_     * _**@param **_key   位图的键_
_     * _**@param **_value 要查找的值（0 或 1）_
_     * _**@return **_第一个被设置为指定值的位的位置，如果不存在则返回-1_
_     */_
_    _Long bitPos(String _key_, boolean _value_);

    _/**_
_     * 对一个或多个位图执行按位操作，并将结果存储到目标位图中_
_     *_
_     * _**@param **_operation 操作类型（AND, OR, XOR, NOT）_
_     * _**@param **_destKey   目标位图的键_
_     * _**@param **_keys      源位图的键列表_
_     */_
_    _void bitOp(String _operation_, String _destKey_, String... _keys_);

    _/**_
_     * 获取位图的内存使用量（字节）_
_     *_
_     * _**@param **_key 位图的键_
_     * _**@return **_内存使用量（字节）_
_     */_
_    _Long memoryUsage(String _key_);

    _/**_
_     * 删除指定的位图键_
_     *_
_     * _**@param **_key 位图的键_
_     */_
_    _void delete(String _key_);
}
```

### List 类型

#### 介绍

简单介绍

List 是 Redis 的一种 有序、可重复 的线性数据结构，基于 双向链表 实现，支持在 头部（Left）和尾部（Right） 高效插入和删除元素。

核心特性

1. 有序性：元素按插入顺序存储，可通过索引访问。
2. 高效操作：

   - 头尾操作（LPUSH/RPUSH/LPOP/RPOP）时间复杂度 O(1)。
   - 中间操作（如 LINSERT/LREM）时间复杂度 O(N)。
3. 可重复：允许存储相同值的元素。

#### 简单操作示例

<table>
<tr>
<td>命令<br/></td><td>作用<br/></td><td>示例<br/></td></tr>
<tr>
<td>LPUSH key value<br/></td><td>头部插入元素<br/></td><td>LPUSH tasks "task1"<br/></td></tr>
<tr>
<td>RPUSH key value<br/></td><td>尾部插入元素 <br/></td><td>RPUSH tasks "task2"<br/></td></tr>
<tr>
<td>LPOP key<br/></td><td>头部弹出元素 <br/></td><td>LPOP tasks → "task1"<br/></td></tr>
<tr>
<td>RPOP key<br/></td><td>尾部弹出元素<br/></td><td>RPOP tasks → "task2"<br/></td></tr>
<tr>
<td>LRANGE key 0 -1<br/></td><td>获取列表所有元素 <br/></td><td> LRANGE tasks 0 -1<br/></td></tr>
<tr>
<td>LLEN key<br/></td><td>获取列表长度<br/></td><td>LLEN tasks → 2<br/></td></tr>
<tr>
<td>BLPOP key timeout<br/></td><td>阻塞式头部弹出（用于消息队列）<br/></td><td>BLPOP tasks 10（等待10秒）<br/></td></tr>
</table>

#### Java 代码示例

- 接口提供

```java
_/**_
_ * 专注于List类型的提供者，解耦操作细节_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/9_
_ */_
public interface _ListCacheProvider _{

    _/**_
_     * 从列表左侧（头部）插入一个元素_
_     *_
_     * _**@param **_key   缓存key，不能为空_
_     * _**@param **_value 要插入的值，不能为空_
_     */_
_    _void leftPush(String key, String value);

    _/**_
_     * 从列表右侧（尾部）插入一个元素_
_     *_
_     * _**@param **_key   缓存key，不能为空_
_     * _**@param **_value 要插入的值，不能为空_
_     */_
_    _void rightPush(String key, String value);

    _/**_
_     * 从列表左侧（头部）弹出一个元素，如果列表为空则返回null_
_     *_
_     * _**@param **_key 缓存key，不能为空_
_     * _**@return **_弹出的元素值，可能为null_
_     */_
_    _String leftPop(String _key_);

    _/**_
_     * 从列表右侧（尾部）弹出一个元素，如果列表为空则返回null_
_     *_
_     * _**@param **_key 缓存key，不能为空_
_     * _**@return **_弹出的元素值，可能为null_
_     */_
_    _String rightPop(String key);

    _/**_
_     * 获取指定key对应列表中的所有元素_
_     *_
_     * _**@param **_key 缓存key，不能为空_
_     * _**@return **_所有元素组成的列表，不会为null_
_     */_
_    _java.util._List_<String> getAll(String _key_);

    _/**_
_     * 获取指定key对应列表中指定范围的元素_
_     *_
_     * _**@param **_key   缓存key，不能为空_
_     * _**@param **_start 开始索引（包含），从0开始_
_     * _**@param **_end   结束索引（包含），-1表示最后一个元素_
_     * _**@return **_指定范围内的元素组成的列表，不会为null_
_     */_
_    _java.util._List_<String> getRange(String key, long start, long end);

    _/**_
_     * 获取指定key对应列表中指定索引位置的元素_
_     *_
_     * _**@param **_key   缓存key，不能为空_
_     * _**@param **_index 索引位置，支持负数（-1表示最后一个元素）_
_     * _**@return **_指定索引位置的元素值，如果不存在则返回空字符串_
_     */_
_    _String getIndex(String key, long index);

    _/**_
_     * 获取指定key对应列表中的元素个数_
_     *_
_     * _**@param **_key 缓存key，不能为空_
_     * _**@return **_列表元素个数，如果key不存在则返回0_
_     */_
_    _Long getLength(String key);

    _/**_
_     * 删除指定key对应的缓存_
_     *_
_     * _**@param **_key 缓存key，不能为空_
_     */_
_    _void delete(String key);
}
```

- 实现类提供，提供常用命令的封装操作

```java
_/**_
_ * 基于Redis实现的List类型操作，提供对Redis List数据结构的常用操作封装_
_ * <p>_
_ * 该类为常用工具类型，可根据业务需求随时扩展新的方法_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/9_
_ */_
@Slf4j
@Component
@RequiredArgsConstructor
public class ListCacheRedisProvider implements _ListCacheProvider _{

    private final StringRedisTemplate redisTemplate;

    _/**_
_     * 从列表左侧（头部）插入一个元素_
_     *_
_     * _**@param **_key   缓存key，不能为空_
_     * _**@param **_value 要插入的值，不能为空_
_     */_
_    _@Override
    public void leftPush(String key, String _value_) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            _log_.debug("左侧添加元素缓存 key={}, value={}", key, value);
        } catch (Exception e) {
            _log_.error("左侧添加元素缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    _/**_
_     * 从列表右侧（尾部）插入一个元素_
_     *_
_     * _**@param **_key   缓存key，不能为空_
_     * _**@param **_value 要插入的值，不能为空_
_     */_
_    _@Override
    public void rightPush(String key, String value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            _log_.debug("右侧添加元素缓存 key={}, value={}", key, value);
        } catch (Exception e) {
            _log_.error("右侧添加元素缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    _/**_
_     * 从列表左侧（头部）弹出一个元素，如果列表为空则返回null_
_     *_
_     * _**@param **_key 缓存key，不能为空_
_     * _**@return **_弹出的元素值，可能为null_
_     */_
_    _@Override
    public String leftPop(String key) {
        try {
            String value = redisTemplate.opsForList().leftPop(key);
            _log_.debug("左侧弹出元素缓存 key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            _log_.error("左侧弹出元素缓存失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 从列表右侧（尾部）弹出一个元素，如果列表为空则返回null_
_     *_
_     * _**@param **_key 缓存key，不能为空_
_     * _**@return **_弹出的元素值，可能为null_
_     */_
_    _@Override
    public String rightPop(String key) {
        try {
            String value = redisTemplate.opsForList().rightPop(key);
            _log_.debug("右侧弹出元素缓存 key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            _log_.error("右侧弹出元素缓存失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 获取指定key对应列表中的所有元素_
_     *_
_     * _**@param **_key 缓存key，不能为空_
_     * _**@return **_所有元素组成的列表，不会为null_
_     */_
_    _@Override
    public _List_<String> getAll(String key) {
        try {
            _List_<String> stringList = redisTemplate.opsForList().range(key, 0, -1);
            stringList = Objects._isNull_(stringList) ? Collections._emptyList_() : stringList;

            _checkSizeAndDebug_(key, stringList);
            return stringList;
        } catch (Exception e) {
            _log_.error("获取所有元素缓存失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 获取指定key对应列表中指定范围的元素_
_     *_
_     * _**@param **_key   缓存key，不能为空_
_     * _**@param **_start 开始索引（包含），从0开始_
_     * _**@param **_end   结束索引（包含），-1表示最后一个元素_
_     * _**@return **_指定范围内的元素组成的列表，不会为null_
_     */_
_    _@Override
    public _List_<String> getRange(String key, long start, long _end_) {
        try {
            _List_<String> stringList = redisTemplate.opsForList().range(key, start, end);
            stringList = Objects._isNull_(stringList) ? Collections._emptyList_() : stringList;

            _checkSizeAndDebug_(key, stringList);
            return stringList;
        } catch (Exception e) {
            _log_.error("获取范围元素缓存失败 key={}, start={}, end={}", key, start, end, e);
            throw e;
        }
    }

    _/**_
_     * 内部辅助方法：检查列表大小并在合理范围内打印调试日志_
_     *_
_     * _**@param **_key        缓存key_
_     * _**@param **_stringList 元素列表_
_     */_
_    _private static void checkSizeAndDebug(String key, _List_<String> stringList) {
        if (_stringList_.size() < 20) {
            _log_.debug("获取缓存 key={}, value={}", key, stringList);
        }
    }

    _/**_
_     * 获取指定key对应列表中指定索引位置的元素_
_     *_
_     * _**@param **_key   缓存key，不能为空_
_     * _**@param **_index 索引位置，支持负数（-1表示最后一个元素）_
_     * _**@return **_指定索引位置的元素值，如果不存在则返回空字符串_
_     */_
_    _@Override
    public String getIndex(String key, long index) {
        try {
            String value = redisTemplate.opsForList().index(key, index);
            _log_.debug("获取缓存 key={}, index={}, value={}", key, index, value);
            return Objects._isNull_(value) ? "" : value;
        } catch (Exception e) {
            _log_.error("获取索引元素缓存失败 key={}, index={}", key, index, e);
            throw e;
        }
    }
    
    _/**_
_     * 获取指定key对应列表中的元素个数_
_     *_
_     * _**@param **_key 缓存key，不能为空_
_     * _**@return **_列表元素个数，如果key不存在则返回0_
_     */_
_    _@Override
    public Long getLength(String key) {
        try {
            Long size = redisTemplate.opsForList().size(key);
            _log_.debug("获取缓存 key={}, size={}", key, size);
            return Objects._isNull_(size) ? 0L : size;
        } catch (Exception e) {
            _log_.error("获取列表长度缓存失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 删除指定key对应的缓存_
_     *_
_     * _**@param **_key 缓存key，不能为空_
_     */_
_    _@Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            _log_.debug("删除缓存 key={}", key);
        } catch (Exception e) {
            _log_.error("删除缓存失败 key={}", key, e);
            throw e;
        }
    }
}
```

#### 一些典型应用场景

1. 消息队列

   - 生产者 LPUSH 写入任务，消费者 RPOP（或 BLPOP）消费任务。
   - 替代方案：更推荐使用 Stream 类型（Redis 5.0+）
2. 最新动态列表

   - 存储用户最新 10 条动态：`LPUSH user:1:feeds "post123"` + `LTRIM user:1:feeds 0 9`
   - `LPUSH user:1:feeds "post123"`：保存新动态
   - `LTRIM user:1:feeds 0 9`：只保留前十条
3. 栈（Stack）或队列（Queue）

   - 栈：`LPUSH` + `LPOP`（后进先出）。
   - 队列：`LPUSH` + `RPOP`（先进先出）。

#### 注意事项

1. 避免存储超长列表（如百万级），中间操作（`LINDEX/LREM`）会变慢。
2. `BLPOP` 慎用，其会加上隐式锁，多客户端竞争需要进行串行操作
3. `LRANGE` 复杂度是 O(n)，列表越大性能越差
4. 大 List 会导致 AOF/RDB 持久化和主从同步变慢（本身操作会更加耗时）
5. List 的链表特征，导致其节点分散分配，有内存碎片，频繁增删导致内存**空洞**

   1. 即 List 频繁增删，而删除的内存碎片直接归还系统，系统合并内存碎片的速度 < 内存碎片化程度
   2. 当 Redis 频繁增删，会频繁申请内存，若系统来不及合并碎片，会导致 OOM（内存充足，却因为内存过于碎片化 + 申请内存块过大导致申请失败，触发 Linux 强制杀掉进程，如果杀掉 Redis 进程，会触发连锁反应，例如缓存 `雪崩`）
6. 消息队列场景，推荐使用 Stream 类型（随后会单独讲述）

#### 生产环境监控

List 的链表结构 + 高频增删，避免不了内存碎片化，而当内存碎片化程度过高，可能会导致一系列问题，例如缓存雪崩，因此十分有必要监控内存碎片化程度，当超过阈值后，需要进行干预，例如发送信息人工干预。

1. Redis 监控命令

```shell
127.0.0.1:6379> info memory
# Memory
used_memory:935334  # Redis当前使用的内存总量(字节) (重点)
used_memory_human:913.41K  # 人类可读的内存使用量
used_memory_rss:918694  # 操作系统角度看到的内存用量(字节) (重点)
used_memory_rss_human:897.16K  # 人类可读的RSS内存
used_memory_peak:1115003  # 内存使用峰值(字节) (重点)
used_memory_peak_human:1.06M  # 人类可读的内存峰值
used_memory_peak_perc:83.89%  # 当前内存占峰值的百分比 (重点)
used_memory_overhead:563439  # Redis系统开销内存(字节) (重点)
used_memory_startup:556886  # Redis启动时消耗的内存(字节)
used_memory_dataset:371895  # 实际数据占用的内存(字节) (重点)
used_memory_dataset_perc:98.27%  # 数据内存占比 (重点)
allocator_allocated:918694  # 分配器分配的内存
allocator_active:886950  # 分配器活跃内存
allocator_resident:886950  # 分配器常驻内存
total_system_memory:68429873152  # 系统总内存(字节) (重点)
total_system_memory_human:63.73G  # 人类可读的系统总内存
used_memory_lua:31744  # Lua引擎使用内存(字节)
used_memory_vm_eval:31744  # VM评估使用内存
used_memory_lua_human:31.00K  # 人类可读的Lua内存
used_memory_scripts_eval:0  # 脚本评估内存
number_of_cached_scripts:0  # 缓存的脚本数量
number_of_functions:0  # 函数数量
number_of_libraries:0  # 库数量
used_memory_vm_functions:32768  # VM函数内存
used_memory_vm_total:64512  # VM总内存
used_memory_vm_total_human:63.00K  # 人类可读的VM内存
used_memory_functions:213  # 函数内存
used_memory_scripts:213  # 脚本内存
used_memory_scripts_human:213B  # 人类可读的脚本内存
maxmemory:0  # 配置的最大内存限制(0表示无限制) (重点)
maxmemory_human:0B  # 人类可读的最大内存
maxmemory_policy:noeviction  # 内存淘汰策略 (重点)
allocator_frag_ratio:0.97  # 分配器碎片率
allocator_frag_bytes:18446744073709519872  # 分配器碎片字节数
allocator_rss_ratio:1.00  # 分配器RSS比率
allocator_rss_bytes:0  # 分配器RSS字节数
rss_overhead_ratio:1.04  # RSS开销比率
rss_overhead_bytes:31744  # RSS开销字节数
mem_fragmentation_ratio:1.00  # 内存碎片率 (重点)
mem_fragmentation_bytes:0  # 内存碎片字节数
mem_not_counted_for_evict:0  # 不计入淘汰的内存
mem_replication_backlog:0  # 复制积压缓冲区内存
mem_total_replication_buffers:0  # 总复制缓冲区内存
mem_clients_slaves:0  # 从节点客户端内存
mem_clients_normal:1788  # 普通客户端内存 (重点)
mem_cluster_links:0  # 集群链接内存
mem_aof_buffer:0  # AOF缓冲区内存
mem_allocator:libc  # 内存分配器类型
active_defrag_running:0  # 是否正在执行主动碎片整理
lazyfree_pending_objects:0  # 待延迟释放的对象数
lazyfreed_objects:0  # 已延迟释放的对象数
```

1. Java 代码集成示例

- 缓存接口提供

```shell
_/**_
_ * 缓存提供者，负责提供缓存的其他底层指令_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/10_
_ */_
public interface _CacheProvider _{

    _/**_
    _ * 获取缓存提供者内存信息_
    _ *_
    _ * _**@return **_缓存提供者内存信息键值对集合，如果获取失败则返回空Map_
    _ */_
    _ Map_<String, String> infoMemory();
}
```

- 测试代码提供

```typescript
_/**_
_ * Redis连接信息测试类_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/10_
_ */_
@Slf4j
@SpringBootTest
public class RedisConnectionTest {

    @Resource
    private _CacheProvider _cacheProvider;

    @Test
    public void testConnection() {
        _Map_<String, String> stringStringMap = cacheProvider.infoMemory();
        _log_.info("获取到内存信息: {}", stringStringMap);
    }
}
```

- 测试输出如下

```java
获取到内存信息: {maxmemory_human=0B, used_memory_scripts_eval=0, used_memory_vm_functions=32768, used_memory_peak_perc=90.78%, mem_replication_backlog=0, mem_total_replication_buffers=0, rss_overhead_ratio=1.02, mem_cluster_links=0, used_memory_scripts_human=213B, allocator_resident=1642056, mem_fragmentation_bytes=0, used_memory_dataset=409743, allocator_rss_bytes=0, allocator_rss_ratio=1.00, rss_overhead_bytes=31744, total_system_memory=68429873152, mem_aof_buffer=0, allocator_frag_bytes=18446744073709519872, mem_allocator=libc, used_memory_scripts=213, used_memory_peak=1794507, number_of_libraries=0, used_memory_vm_total_human=63.00K, used_memory_human=1.55M, maxmemory=0, used_memory_functions=213, total_system_memory_human=63.73G, used_memory_lua=31744, used_memory_startup=556886, lazyfree_pending_objects=0, used_memory_dataset_perc=38.22%, allocator_frag_ratio=0.98, used_memory_vm_eval=31744, allocator_active=1642056, mem_clients_normal=657606, used_memory_rss_human=1.60M, mem_fragmentation_ratio=1.00, number_of_cached_scripts=0, maxmemory_policy=noeviction, used_memory_lua_human=31.00K, used_memory_rss=1673800, mem_not_counted_for_evict=0, active_defrag_running=0, used_memory=1629000, mem_clients_slaves=0, allocator_allocated=1673800, number_of_functions=0, lazyfreed_objects=0, used_memory_peak_human=1.71M, used_memory_vm_total=64512, used_memory_overhead=1219257}
```

1. 生产环境监控完整提供

- 监控专用接口提供

```java
_/**_
_ * 内存监控信息接口_
_ * 定义了内存监控指标的相关方法，用于监控和分析实例的内存使用情况_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/11_
_ */_
public interface _MemoryMetrics _{

    _/**_
_     * 查询已使用内存占比_
_     * _
_     * _**@return **_已使用内存的百分比_
_     */_
_    _int queryUsedMemoryPercent();

    _/**_
_     * 查询高精度的内存使用率_
_     * _
_     * _**@return **_高精度内存使用率_
_     */_
_    _double queryMemoryUsageRate();

    _/**_
_     * 计算内存碎片风险等级_
_     * _
_     * _**@return **_内存碎片风险等级_
_     */_
_    _double calculateFragmentationRiskLevel();

    _/**_
_     * 计算内存驱逐风险指标_
_     * _
_     * _**@return **_内存驱逐风险指标_
_     */_
_    _double calculateEvictionRiskIndicator();

    _/**_
_     * 显示所有指标信息_
_     */_
_    _void showAll();

    _/**_
_     * 监控告警_
_     * _
_     * _**@return **_告警信息_
_     */_
_    _String monitorAlerts();
}
```

### Stream 类型

#### 介绍

Redis 的 Stream 是用于持久化消息队列的数据结构，支持多消费者组、消息回溯和阻塞读取，类似 Kafka 但更轻量。核心命令：`XADD`（生产）、`XREAD`（消费）、`XGROUP`（组管理）。

Redis Stream 的底层实现基于紧凑的宏节点（`macro nodes`）结构，本质是内存中的**双向链表**，每个节点包含多个消息条目（节省内存），并通过**基数树（Radix Tree）**索引加速查找。消息持久化依赖 RDB/AOF 机制。

#### 与 List 的区别

<table>
<tr>
<td>对比项<br/></td><td>Stream<br/></td><td>List<br/></td></tr>
<tr>
<td>用途<br/></td><td>持久化消息队列（支持多消费者组、回溯消费）<br/></td><td>简单内存队列（FIFO）<br/></td></tr>
<tr>
<td>底层结构<br/></td><td>宏节点（双向链表）+ 基数树索引<br/></td><td>双向链表<br/></td></tr>
<tr>
<td>消费者模型<br/></td><td>支持多消费者组、独立消费、ACK确认<br/></td><td>无消费者组，消息弹出即删除<br/></td></tr>
<tr>
<td>消息回溯<br/></td><td>支持（通过消息ID）<br/></td><td>不支持（弹出后消失）<br/></td></tr>
<tr>
<td>阻塞消费<br/></td><td>支持多客户端独立阻塞（`XREAD`）<br/></td><td>全体阻塞（`BLPOP`）<br/></td></tr>
<tr>
<td>持久化<br/></td><td>消息默认持久化（RDB/AOF）<br/></td><td>依赖RDB/AOF（纯内存结构）<br/></td></tr>
<tr>
<td>典型命令<br/></td><td>`XADD`、`XREADGROUP`、`XACK`<br/></td><td>`LPUSH`、`RPOP`、`BLPOP`<br/></td></tr>
</table>

总结

1. Stream 的中间操作更快，因为有索引加速
2. 支持持久化
3. 支持多客户端独立阻塞操作
4. 功能强，支持多组、ACK、回溯

但是在大部分场景下 List 依旧是更优选，而在于消息队列场景下，针对可靠性要求更高的场景下，依旧更推荐 Redis Stream

#### 与 RabbitMQ 的区别

<table>
<tr>
<td>对比项<br/></td><td>Redis Stream<br/></td><td>RabbitMQ<br/></td></tr>
<tr>
<td>定位<br/></td><td>轻量级内存消息队列（Redis生态内）<br/></td><td>专业消息中间件（完整AMQP协议）<br/></td></tr>
<tr>
<td>性能<br/></td><td>超高吞吐（内存操作，10W+/秒）<br/></td><td>较低（持久化场景约1W+/秒）<br/></td></tr>
<tr>
<td>功能<br/></td><td>基础消息队列（多组/ACK/回溯）<br/></td><td>高级功能（路由、事务、死信队列等）<br/></td></tr>
<tr>
<td>可靠性<br/></td><td>依赖Redis持久化（可能丢消息）<br/></td><td>强持久化+确认机制（更可靠）<br/></td></tr>
<tr>
<td>扩展性<br/></td><td>无集群原生支持（依赖Redis分片）<br/></td><td>支持集群、联邦、插件扩展<br/></td></tr>
<tr>
<td>适用场景<br/></td><td>缓存级消息（如实时通知、短时任务）<br/></td><td>金融级可靠消息（如支付、订单同步）<br/></td></tr>
</table>

总结：

1. Stream 在轻量化场景下，性能更占优势
2. RabbitMQ 更适合对消息可靠性要求更高，以及高级功能场景中

#### 简单操作示例

```shell
# 1. 生产消息
XADD mystream * field1 value1 field2 value2  # `*` 自动生成消息ID，ID格式<时间戳>-<序号>其严格按照id顺序进行排序，如果新增加的id<上一个id，就会报错
# 2. 独立消费
XREAD COUNT 1 STREAMS mystream 0  # 从开头读1条（不推荐，因为不会弹出，可能导致重复消费，更推荐使用组内消费）    
BLOCK 5000 STREAMS mystream $  # 阻塞5秒读新消息
# 3. 消费者组
XGROUP CREATE mystream mygroup $  # 创建组（`$`表示从新消息开始）
XREADGROUP GROUP mygroup consumer1 COUNT 1 STREAMS mystream >  # 组内消费
XACK mystream mygroup message-id  # 确认消息
# 4. 管理
XLEN mystream            # 查看消息数
XRANGE mystream - +      # 列出所有消息
XDEL mystream message-id # 删除消息 (如果业务不强制要求，是不推荐使用的，更推荐手动设置消息队列最长消息限制，会自动弹出)
# 5. PENDING超时处理、消息队列配置
XGROUP SETID mystream mygroup 0 IDLE 5000  # 5秒未ACK自动释放消息（需Redis 6.2+）当消费者消费超时后，会从PENDING中取出，队列中再次可见，可以通过 > 符号再次获取，推荐为**平均消费时长 * 2**
XCLAIM mystream mygroup consumer2 5000 message-id  # 强制接管5秒未ACK的消息，当PENDING中消息过期后，会强制转到别的消费者中重新消费
XADD mystream MAXLEN 1000 * field value  # 保留最新1000条消息（自动淘汰旧消息）
```

#### 注意事项

1. 生产者没有生产组概念，只需要将消息投递到对应的队列即可，其性能瓶颈最终取决于 Redis 的单线程模型
2. 消费者有消费组的概念，其可以让多个消费者同时竞争同一个队列，其类似于 RabbitMQ 的多消费者线程提升吞吐量，但是其性能瓶颈最后依旧取决于 Redis 的单线程模型
3. 并不是消费者越多越好，相反，当消费者过多，因为核心切换线程上下文，反而会影响 Redis 的单线程性能，导致吞吐量降低。因此黄金节点就是比节点数略少（N-1），保证留有一个核心执行 Redis 的主线程或者其他的命令。
4. Stream 的消费顺序默认从旧到新，推荐直接使用_>_符号，其表示自动按照**顺序进行推进**,即忽略已经 ack 的信息，找到第一条未消费的信息
5. _$ _表示初始化队列信息，忽略从当前时间往前的所有历史信息，这是**不推荐**的，除非业务要求不再需要之前的信息。
6. 消费者的确认机制，是在消息消费成功后进行 ack，但是在拿到消息后，消费过程中间，如果再出现了读取消息，就会出现重复拿取同一条消息的情况，导致出现重复消费。但真的是这样吗？
7. 消费者组拿到消息后，消息会进入到该消费者的 Pending 中，而对应的信息会被隐藏，组内其他消费者不可见，因此直接使用_>_符号不会出现第六点提到的并发问题，可以放心使用。除非在 Pending 中超时。
8. 消费者组内的 PENDING，默认是无超时设置的，需要手动设置超时处理，设置参考上述命令

#### Java 代码示例

- 接口提供

```java
import java.util.HashMap;
import java.util._List_;
import java.util._Map_;

_/**_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/15_
_ */_
public interface _StreamCacheProvider _{

    _/**_
_     * 向指定的 Stream 中添加单个消息_
_     *_
_     * _**@param **_streamName 流名称，不能为空_
_     * _**@param **_key        消息键，不能为空_
_     * _**@param **_value      消息值，不能为空_
_     * _**@throws **_IllegalArgumentException 如果参数为空或无效_
_     * _**@throws **_RuntimeException         如果 Redis 操作失败_
_     */_
_    _void addMessage(String _streamName_, String _key_, String _value_);

    _/**_
_     * 向指定的 Stream 中添加多个键值对消息_
_     *_
_     * _**@param **_streamName 流名称，不能为空_
_     * _**@param **_map        消息键值对集合，不能为空_
_     * _**@throws **_IllegalArgumentException 如果参数为空或无效_
_     * _**@throws **_RuntimeException         如果 Redis 操作失败_
_     */_
_    _void addMessageMap(String streamName, java.util._Map_<String, String> map);

    _/**_
_     * 向指定的 Stream 中添加多个键值对消息（可变参数形式）_
_     * 参数必须成对出现，即键后必须跟对应的值_
_     *_
_     * _**@param **_streamName 流名称，不能为空_
_     * _**@param **_keyValue   键值对参数，必须为偶数个_
_     * _**@throws **_IllegalArgumentException 如果参数个数不是偶数或参数为空_
_     * _**@throws **_RuntimeException         如果 Redis 操作失败_
_     */_
_    _void addMessages(String streamName, String... keyValue);

    _/**_
_     * 读取指定 Stream 中的所有消息，并按消息ID组织返回_
_     *_
_     * _**@param **_streamName 消息队列名称，不能为空_
_     * _**@param **_clazzK     key类型，用于类型转换_
_     * _**@param **_clazzV     value类型，用于类型转换_
_     * _**@return **_包含所有消息的映射，key为消息ID，value为消息内容映射_
_     * _**@throws **_RuntimeException 如果 Redis 操作失败或类型转换异常_
_     */_
_    _<_K_, _V_> _Map_<String, HashMap<_K_, _V_>> readAll(String streamName, Class<_K_> clazzK, Class<_V_> clazzV);

    _/**_
_     * 读取指定数量的消息_
_     *_
_     * _**@param **_streamName 流名称_
_     * _**@param **_count      读取数量_
_     * _**@param **_clazzK     key类型_
_     * _**@param **_clazzV     value类型_
_     * _**@return **_消息列表_
_     */_
_    _<_K_, _V_> _List_<_Map_<_K_, _V_>> readMapCount(String streamName, int count, Class<_K_> clazzK, Class<_V_> clazzV);

    _/**_
_     * 阻塞式读取消息_
_     *_
_     * _**@param **_streamName 流名称_
_     * _**@param **_blockTime  阻塞等待时间（毫秒）_
_     * _**@param **_clazzK     key类型_
_     * _**@param **_clazzV     value类型_
_     * _**@return **_消息列表_
_     */_
_    _<_K_, _V_> _List_<_Map_<_K_, _V_>> readMapBlock(String streamName, long blockTime, Class<_K_> clazzK, Class<_V_> clazzV);

    _/**_
_     * 读取指定数量的消息并支持阻塞_
_     *_
_     * _**@param **_streamName 流名称_
_     * _**@param **_count      读取数量_
_     * _**@param **_blockTime  阻塞等待时间（毫秒）_
_     * _**@param **_clazzK     key类型_
_     * _**@param **_clazzV     value类型_
_     * _**@return **_消息列表_
_     */_
_    _<_K_, _V_> _List_<_Map_<_K_, _V_>> readMap(String streamName, int count, long blockTime, Class<_K_> clazzK, Class<_V_> clazzV);

    _/**_
_     * 创建消费者组_
_     *_
_     * _**@param **_streamName 流名称_
_     * _**@param **_groupName  消费者组名称_
_     */_
_    _void createGroup(String streamName, String groupName);

    _/**_
_     * 从消费者组中读取消息_
_     *_
_     * _**@param **_streamName   流名称_
_     * _**@param **_groupName    消费者组名称_
_     * _**@param **_consumerName 消费者名称_
_     * _**@param **_clazzK       key类型_
_     * _**@param **_clazzV       value类型_
_     * _**@return **_消息映射_
_     */_
_    _<_K_, _V_> _Map_<_K_, _V_> readMessage(String streamName, String groupName, String consumerName, Class<_K_> clazzK, Class<_V_> clazzV);

    _/**_
_     * 确认消息已处理_
_     *_
_     * _**@param **_streamName  流名称_
_     * _**@param **_groupName   消费者组名称_
_     * _**@param **_messageIds  消息ID列表_
_     */_
_    _void ackMessage(String streamName, String groupName, String... messageIds);
}
```

#### 总结

Redis 的 Stream 本质上是针对 List 类型进行的优化，其适合轻量级 + 高性能的消息队列场景。当然如果业务场景适合，更推荐使用 RabbitMQ 等 SpringBoot 直接集成的中间件，因为支持更好。而针对 Redis 的 Stream 类型操作则较为繁琐。

### Set/ZSet 类型

#### 介绍

Set 类型也是 Redis 中比较重要的数据结构，其底层采用哈希表/整数集合（intest）。当 Set 中只有整数，且数值范围少（int16）、数量也较少（一般为 512 个，可以进行配置）。Set 集合的特性是唯一，但是无序。而 ZSet 则是有序版本的 Set 类型，其通过 score 来保证 ZSet 的有序性，通过**跳表（类似二分查找）**实现快速定位。

#### 区别

<table>
<tr>
<td><br/></td><td>Set<br/></td><td>ZSet<br/></td></tr>
<tr>
<td>底层数据结构<br/></td><td>哈希表/整数集合(intest)<br/></td><td>哈希表/整数集合(intest)、跳表<br/></td></tr>
<tr>
<td>有序<br/></td><td>否<br/></td><td>是<br/></td></tr>
<tr>
<td>空间消耗<br/></td><td>低<br/></td><td>高<br/></td></tr>
<tr>
<td>查询效率<br/></td><td>单点查找O（1）<br/></td><td>单点查找O（1），范围查找O(logN)<br/></td></tr>
<tr>
<td>适合场景<br/></td><td>单独去重<br/></td><td>去重+排序（如排行榜）<br/></td></tr>
</table>

#### 简单操作示例

- Set 操作

```shell
# 添加元素（自动去重）
SADD fruits "apple" "banana" "orange" "apple"  # 返回 3（实际添加 3 个，重复的 "apple" 被忽略）

# 查询所有成员（无序）
SMEMBERS fruits  # 可能输出：1) "banana" 2) "orange" 3) "apple"

# 判断元素是否存在
SISMEMBER fruits "banana"  # 返回 1（存在）
SISMEMBER fruits "grape"   # 返回 0（不存在）

# 随机弹出一个元素
SPOP fruits  # 可能返回 "apple"（随机）

# 集合运算
SADD fruits2 "banana" "grape"
SINTER fruits fruits2  # 交集：返回 1) "banana"
SUNION fruits fruits2  # 并集：返回所有不重复元素
```

- ZSet 示例

```shell
# 添加元素（带分数 score）
ZADD leaderboard 100 "Alice" 85 "Bob" 95 "Charlie" 70 "David"

# 按 score 升序查询（默认）
ZRANGE leaderboard 0 -1 WITHSCORES  
# 输出：
# 1) "David"   2) "70"
# 3) "Bob"     4) "85"
# 5) "Charlie" 6) "95"
# 7) "Alice"   8) "100"

# 按 score 降序查询（排行榜场景）
ZREVRANGE leaderboard 0 -1 WITHSCORES  
# 输出：
# 1) "Alice"   2) "100"
# 3) "Charlie" 4) "95"
# 5) "Bob"     6) "85"
# 7) "David"   8) "70"

# 查询某成员的 score
ZSCORE leaderboard "Charlie"  # 返回 "95"

# 按 score 范围查询（80 <= score <= 99）
ZRANGEBYSCORE leaderboard 80 99 WITHSCORES  
# 输出：
# 1) "Bob"     2) "85"
# 3) "Charlie" 4) "95"

# 查询成员的排名（升序从 0 开始）
ZRANK leaderboard "Bob"  # 返回 1（升序排名第 2）
ZREVRANK leaderboard "Bob"  # 返回 2（降序排名第 3）
```

#### 注意事项

- Set/ZSet 类型和 List 类型有着类似的操作风险，频繁增删会导致系统内存整合速度赶不上碎片化速度，当 Redis 或者其他应用程序申请大块内存的时候，可能就会因为内存碎片化导致申请失败，触发 OOM 风险。
- Set/ZSet 的数据量过大时候（百万级），使用 `SMEMBERS`_(复杂度 O(N))_操作会导致阻塞 + 内存占用率暴涨（Redis 将所有的成员打包响应），解决方案就是拆分为多个小 Key。

#### Java 代码示例

- 接口封装

```java
_import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
__ * _**@author **_王玉涛
__ * _**@version **_1.0
__ * _**@since **_2025/8/23
 */
public interface SetCacheProvider {

    // Set 相关方法定义

    /**
     * 向指定的 Set 集合中添加一个或多个元素
     *
__     * _**@param **_key    Set 集合的键
__     * _**@param **_values 要添加的元素数组
     */
    void set(String key, List<String> values);

    /**
     * 批量设置多个 Set 集合的元素，参数格式为 key1, value1, key2, value2, ...
     *
__     * _**@param **_keyValues 键值对数组，必须为偶数个
__     * _**@throws **_IllegalArgumentException 当参数个数不是偶数时抛出
     */
    void set(String... keyValues);

    /**
     * 原子性地批量设置多个 Set 集合的元素，使用 Lua 脚本保证操作的原子性
     * 参数格式为 key1, value1, key2, value2, ...
     *
__     * _**@param **_keyValues 键值对数组，必须为偶数个
__     * _**@throws **_IllegalArgumentException 当参数个数不是偶数时抛出
     */
    void atomicitySet(String... keyValues);

    /**
     * 获取指定 Set 集合中的所有元素
     *
__     * _**@param **_key Set 集合的键
__     * _**@return **_Set 集合中的所有元素
     */
    Set<String> queryAll(String key);

    /**
     * 判断指定元素是否存在于 Set 集合中
     *
__     * _**@param **_key   Set 集合的键
__     * _**@param **_value 要检查的元素
__     * _**@return **_如果元素存在返回 true，否则返回 false
     */
    boolean isExist(String key, String value);

    /**
     * 随机弹出并移除 Set 集合中的一个元素
     *
__     * _**@param **_key Set 集合的键
__     * _**@return **_被弹出的元素，如果集合为空则返回 null
     */
    String randomPop(String key);

    /**
     * 获取两个 Set 集合的交集
     *
__     * _**@param **_key      第一个 Set 集合的键
__     * _**@param **_otherKey 第二个 Set 集合的键
__     * _**@return **_两个集合的交集
     */
    Set<String> interSection(String key, String otherKey);

    /**
     * 获取多个 Set 集合的交集
     *
__     * _**@param **_keys Set 集合键的集合
__     * _**@return **_多个集合的交集
     */
    Set<String> interSection(Collection<String> keys);

    /**
     * 获取多个 Set 集合的并集
     *
__     * _**@param **_keys Set 集合键的集合
__     * _**@return **_多个集合的并集
     */
    Set<String> unionSection(Collection<String> keys);

    /**
     * 获取两个 Set 集合的并集
     *
__     * _**@param **_key      第一个 Set 集合的键
__     * _**@param **_otherKey 第二个 Set 集合的键
__     * _**@return **_两个集合的并集
     */
    Set<String> unionSection(String key, String otherKey);

    /**
     * 从 Set 集合中移除指定元素
     *
__     * _**@param **_key   Set 集合的键
__     * _**@param **_value 要移除的元素
     */
    void remove(String key, String value);

    // ZSet 相关方法定义

    /**
     * 向有序集合中添加元素及其分数
     *
__     * _**@param **_key   有序集合的键
__     * _**@param **_value 要添加的元素
__     * _**@param **_score 元素的分数
     */
    void zAdd(String key, String value, double score);

    /**
     * 原子性地批量添加有序集合元素，使用 Lua 脚本保证操作的原子性
     * 参数格式为 key1, value1, score1, key2, value2, score2, ...
     *
__     * _**@param **_keyValues 键值对和分数数组，必须为3的倍数
__     * _**@throws **_IllegalArgumentException 当参数个数不是3的倍数时抛出
     */
    void zAtomicityAdd(String... keyValues);

    /**
     * 批量添加有序集合元素
     * 参数格式为 key1, value1, score1, key2, value2, score2, ...
     *
__     * _**@param **_keyValues 键值对和分数数组，必须为3的倍数
__     * _**@throws **_IllegalArgumentException 当参数个数不是3的倍数时抛出
     */
    void zAdd(String... keyValues);

    /**
     * 从有序集合中移除一个或多个元素
     *
__     * _**@param **_key    有序集合的键
__     * _**@param **_values 要移除的元素数组
     */
    void zRemove(String key, Object... values);

    /**
     * 获取有序集合中指定范围的元素（按分数从小到大）
     *
__     * _**@param **_key   有序集合的键
__     * _**@param **_start 起始索引
__     * _**@param **_end   结束索引
__     * _**@return **_指定范围内的元素集合
     */
    Set<String> zRange(String key, long start, long end);

    /**
     * 获取有序集合中指定范围的元素（按分数从大到小）
     *
__     * _**@param **_key   有序集合的键
__     * _**@param **_start 起始索引
__     * _**@param **_end   结束索引
__     * _**@return **_指定范围内的元素集合
     */
    Set<String> zRevRange(String key, long start, long end);

    /**
     * 获取有序集合中指定元素的分数
     *
__     * _**@param **_key   有序集合的键
__     * _**@param **_value 元素值
__     * _**@return **_元素的分数，如果元素不存在则返回 null
     */
    Double zScore(String key, Object value);

    /**
     * 获取有序集合中指定元素的排名（按分数从小到大）
     *
__     * _**@param **_key   有序集合的键
__     * _**@param **_value 元素值
__     * _**@return **_元素的排名，如果元素不存在则返回 null
     */
    Long zRank(String key, Object value);

    /**
     * 获取有序集合中指定元素的排名（按分数从大到小）
     *
__     * _**@param **_key   有序集合的键
__     * _**@param **_value 元素值
__     * _**@return **_元素的排名，如果元素不存在则返回 null
     */
    Long zRevRank(String key, Object value);

    /**
     * 获取有序集合的元素个数
     *
__     * _**@param **_key 有序集合的键
__     * _**@return **_集合中的元素个数
     */
    Long zCard(String key);

    /**
     * 获取有序集合中指定分数范围内的元素
     *
__     * _**@param **_key 有序集合的键
__     * _**@param **_min 最小分数（包含）
__     * _**@param **_max 最大分数（包含）
__     * _**@return **_指定分数范围内的元素集合
     */
    Set<String> zRangeByScore(String key, double min, double max);

    /**
     * 统计有序集合中指定分数范围内的元素数量
     *
__     * _**@param **_key 有序集合的键
__     * _**@param **_min 最小分数（包含）
__     * _**@param **_max 最大分数（包含）
__     * _**@return **_指定分数范围内的元素数量
     */
    Long zCount(String key, double min, double max);
__}_
```

### Hash 类型

#### 介绍

Redis 的 Hash 类型的存储结构为：key -> {field1: value1, field2: value2 .....}，其底层使用的是 ziplist（压缩列表）或 hashtable（哈希表），当字段较少时会使用压缩列表优化，存储更紧凑。该类型适合存储结构化数据，支持字段级操作。

#### 注意事项

1. **避免存储过大的 Hash 结构**：如果 Hash 的字段过多（万级），执行 `HGETALL`、`HKEYS` 等操作会返回大量数据，可能导致 Redis **阻塞**或者**网络传输压力增大**。建议 Hash 结构根据业务进行拆分，或者使用 `HSCAN` 增量遍历，避免一次性获取所有数据。
2. **不支持嵌套数据操作**：Hash 的 value 只能是 String 类型，无法嵌套其他的 Hash 或者 List 结构，可以存储的时候序列化为 String 类型进行操作，但是更建议根据业务进行合理选择，避免嵌套数据结构。
3. **建议控制单个 Hash 的大小**：让 Redis 可以直接使用压缩列表进行优化，更省内存，也可通过 `redis.conf` 调整 `hash-max-ziplist-entries` 和 `hash-max-ziplist-value` 参数。
4. 其他注意事项

<table>
<tr>
<td>注意事项<br/></td><td>潜在问题<br/></td><td>解决方案<br/></td></tr>
<tr>
<td>大 Hash<br/></td><td>阻塞、网络压力<br/></td><td>拆分为小 Hash，用 `HSCAN` 遍历<br/></td></tr>
<tr>
<td>无嵌套结构<br/></td><td>无法存储复杂对象<br/></td><td>改用 JSON 或多级 Key<br/></td></tr>
<tr>
<td>内存优化<br/></td><td>内存占用高<br/></td><td>控制字段数和值大小，优先用 ziplist<br/></td></tr>
<tr>
<td>原子性局限<br/></td><td>非原子操作<br/></td><td>用 Lua 脚本或 `MULTI`/`EXEC`<br/></td></tr>
<tr>
<td>字段级 TTL<br/></td><td>无法针对字段单独过期<br/></td><td>改用 String 或 Sorted Set 管理<br/></td></tr>
<tr>
<td>大 Key 风险<br/></td><td>内存/性能问题<br/></td><td>监控并拆分大 Hash<br/></td></tr>
</table>

#### 简单操作示例

```shell
# Redis Hash 操作命令示例

# 1. 基本操作
# 设置哈希表 user:1001 的多个字段
HSET user:1001 name "Alice" age 25 email "alice@example.com"
# 获取哈希表 user:1001 中 name 字段的值
HGET user:1001 name
# 删除哈希表 user:1001 中的 email 字段
HDEL user:1001 email
# 检查哈希表 user:1001 中 age 字段是否存在
HEXISTS user:1001 age
# 获取哈希表 user:1001 中的字段数量
HLEN user:1001

# 2. 批量操作
# 批量设置哈希表 user:1002 的多个字段
HSET user:1002 name "Bob" age 30 city "New York"
# 批量获取哈希表 user:1002 中多个字段的值
HMGET user:1002 name age city
# 获取哈希表 user:1002 中所有字段和值（注意大Key风险）
HGETALL user:1002
# 获取哈希表 user:1002 中所有字段名（注意大Key风险）
HKEYS user:1002
# 获取哈希表 user:1002 中所有字段值（注意大Key风险）
HVALS user:1002

# 3. 数字操作
# 将哈希表 user:1002 中 age 字段的值增加1
HINCRBY user:1002 age 1
# 将哈希表 user:1002 中 score 字段的值增加0.5
HINCRBYFLOAT user:1002 score 0.5

# 4. 高级操作
# 当哈希表 user:1002 中 name 字段不存在时才设置值
HSETNX user:1002 name "Tom"
# 获取哈希表 user:1002 中 name 字段值的长度
HSTRLEN user:1002 name
# 增量迭代哈希表 user:1002 中的字段，每次返回10个
HSCAN user:1002 0 COUNT 10
```

#### Java 代码示例

- 接口提供

```java
_/**_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/23_
_ */_
public interface _HashCacheProvider _{

    _/**_
_     * 设置哈希表中指定字段的值_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@param **_value 字段值_
_     */_
_    _void set(String key, String field, String value);

    _/**_
_     * 获取哈希表中指定字段的值_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@return **_字段值，如果不存在则返回空字符串_
_     */_
_    _String get(String key, String field);

    _/**_
_     * 删除哈希表中一个或多个字段_
_     *_
_     * _**@param **_key    哈希表的键_
_     * _**@param **_fields 要删除的字段名数组_
_     * _**@return **_被成功删除的字段数量_
_     */_
_    _Long del(String key, String... fields);

    _/**_
_     * 检查哈希表中指定字段是否存在_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@return **_如果字段存在返回true，否则返回false_
_     */_
_    _Boolean isExists(String key, String field);

    _/**_
_     * 获取哈希表中字段的数量_
_     *_
_     * _**@param **_key 哈希表的键_
_     * _**@return **_字段数量_
_     */_
_    _Long getLength(String key);

    _/**_
_     * 批量设置哈希表中的多个字段_
_     *_
_     * _**@param **_key 哈希表的键_
_     * _**@param **_map 包含字段和值的映射_
_     */_
_    _void set(String key, java.util._Map_<String, String> _map_);

    _/**_
_     * 批量获取哈希表中多个字段的值_
_     *_
_     * _**@param **_key    哈希表的键_
_     * _**@param **_fields 字段名数组_
_     * _**@return **_字段值列表_
_     */_
_    _java.util._List_<String> get(String _key_, String... _fields_);

    _/**_
_     * 获取哈希表中所有的字段和值 (警惕大Key风险)_
_     *_
_     * _**@param **_key 哈希表的键_
_     * _**@return **_包含所有字段和值的映射_
_     */_
_    _java.util._Map_<String, String> getAll(String _key_);

    _/**_
_     * 通过扫描的方式分批获取哈希表中所有的字段和值，避免一次性加载大量数据导致内存问题_
_     *_
_     * _**@param **_key       哈希表的键_
_     * _**@param **_batchSize 每次扫描的批次大小_
_     * _**@return **_包含所有字段和值的映射_
_     */_
_    _java.util._Map_<String, String> scanAll(String key, int batchSize);

    _/**_
_     * 获取哈希表中所有的字段名 (警惕大Key风险)_
_     *_
_     * _**@param **_key 哈希表的键_
_     * _**@return **_字段名集合_
_     */_
_    _java.util._Set_<String> getKeys(String key);

    _/**_
_     * 获取哈希表中所有的字段值 (警惕大Key风险)_
_     *_
_     * _**@param **_key 哈希表的键_
_     * _**@return **_字段值集合_
_     */_
_    _java.util._List_<String> getValues(String key);

    _/**_
_     * 将哈希表中指定字段的值增加指定整数_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@param **_delta 增加的数值_
_     * _**@return **_增加后的值_
_     */_
_    _Long incrBy(String key, String field, long delta);

    _/**_
_     * 将哈希表中指定字段的值增加指定浮点数_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@param **_delta 增加的浮点数_
_     * _**@return **_增加后的值_
_     */_
_    _Double incrByFloat(String key, String field, double delta);

    _/**_
_     * 当哈希表中指定字段不存在时才设置值_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@param **_value 字段值_
_     * _**@return **_如果设置成功返回true，如果字段已存在返回false_
_     */_
_    _boolean setIfAbsent(String key, String field, String value);

    _/**_
_     * 获取哈希表中指定字段值的长度_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@return **_字段值的长度_
_     */_
_    _Long queryFieldLen(String key, String field);
}
```

## 持久化机制（RDB/AOF）

### 介绍

Redis 是内存数据库，服务重启后容易丢失数据，为了持久化数据，Redis 提供了两种方式，将数据持久化到磁盘，方便重启 Redis 的时候恢复宕机前的数据。

### RDB

#### 介绍

RDB（Redis DataBase）是 Redis 的**快照式持久化**机制，其核心特点如下

- **定时保存**：通过配置（例如：`save 900 1`）或者手动触发，将内存数据全量备份到二进制文件 `.rdb` 中。
- **性能高**：子进程异步生成快照，不阻塞主进程。
- **恢复快**：直接加载 `.rdb` 文件到内存即可，比 AOF 更快。
- **局限**：最后一次快照之后的数据可能会丢失，不能保证数据百分百恢复。

#### 开启 RDB

1. _配置文件_：在 `redis.conf` 文件中设置 `save` 规则
   - 我们平时重启 Redis，并不会丢失所有数据，是因为 Redis 默认开启了 RDB，而 Redis 默认配置如下。

   ```bash
   ```

save 3600 1    # 1 小时至少 1 次修改
save 300 100   # 5 分钟至少 100 次修改
save 60 10000  # 1 分钟至少 1 万次修改

```
	- 默认规则下已经足够，平衡性能与数据修改。如果有需要，可以参考以下业务场景进行修改。
	```bash
# ======高性能场景(可容忍丢数据)========
save ""         # 关闭RDB，纯用AOF，_如果出现save ""，则会禁用所有的save规则,或者注释掉所有的save规则。_

# ======混合持久化========
aof-use-rdb-preamble yes  # AOF包含RDB头，兼顾速度与安全

# ======生产环境=========
save 900 1      # 15分钟至少1次修改
save 300 10     # 5分钟至少10次修改
save 60 10000   # 1分钟至少1万次修改
```

```
- 多条指令独立执行，只要满足其中一条就会触发RDB。
```

2. _手动触发_：手动运行 `save` 命令，阻塞式保存，但是注意事项如下：
   - 大数据量下 `save` 命令，会_阻塞其他操作_，生产环境慎用。
   - 更推荐使用 `bgsave`，后台异步生成快照，_不会阻塞主线程_（大数据量仅瞬间延迟）

#### 总结

1. 默认情况下，默认配置的 RDB 完全够用。如果有特殊需求，可以按需对 save 进行微调。
2. 如果业务要求 100% 数据可靠，则更推荐使用 AOF。
3. 超高频写入场景可微调 `save` 参数或增加混合持久化。

### AOF

#### 介绍

AOF（Append Only File）：记录所有_写操作_的日志文件，重启时根据命令恢复数据。

- 优点：数据安全（可以根据需求配置为 `每秒同步` 或者 `始终同步`）
- 缺点：文件比 RDB 大，恢复较慢。
- 可配置 `aof-use-rdb-preamble yes` 开启混合持久化。

#### 开启 AOF

1. **配置文件**：`redis.conf` 文件中设置 `appendonly yes`。
2. **同步策略（可选）**：

   ```bash
   ```

appendfsync always   # 每个命令刷盘（最安全，性能最低）
appendfsync everysec # 每秒刷盘（默认推荐）
appendfsync no       # 由系统决定（最快，可能丢数据）

```

3. **重启生效**：`redis-server redis.conf`。

4. **二者共存**：开启后自动生成 .aof 文件，替代RDB需关闭 save 规则 ⚠️，RDB默认不会覆盖，而是二者共存，定时RDB生成快照+AOF持续跟进，但是**恢复会优先采用AOF**（因为记录更完整）。

#### 注意事项

1. **文件膨胀**：AOF文件会无限制增长，而针对一个key的重复覆盖指令会完整记录下来，因此需要定时使用`BGREWRITEAOF`进行日志压缩（去除无效指令），或者`auto-aof-rewrite-percentage`配置进行自动压缩。
	1. `auto-aof-rewrite-percentage`指令：该指令是指定距离上次重写后<u>文件大小增长</u>百分之几来进行自动压缩。例如`auto-aof-rewrite-percentage 100`则表示距离上次重写后，文件大小增长100%自动触发日志压缩。例如上次重写后文件大小为100MB，则此次触发阈值为200MB。
	2. 而针对该指令的首次压缩阈值，即首次启动后aof文件为空时，第一次压缩阈值，是根据`auto-aof-rewrite-min-size`指定的。
	3. Redis的conf文件中，二者都有指定的默认值
		```bash
auto-aof-rewrite-percentage 100  # 比上次重写增长100%
auto-aof-rewrite-min-size 64mb   # AOF文件至少64MB才触发
# 与 auto-aof-rewrite-percentage 100 共同生效：需同时满足「比基准大小增长100%」+「≥64MB」✂️
```

2. **性能损耗**：AOF 若开启 `appendfsync always`，会导致吞吐量显著降低，生产环境通常不推荐。因为每次写入都会触发刷盘，导致 Redis 必须阻塞等待刷盘完成才可继续写入，导致性能大幅度下降。通常默认的 `appendfsync everysec` 已经足够使用。而 `appendfsync no` 则由系统决定，可能导致丢数据、突然阻塞等情况，**适合高吞吐量，且数据已经存储到数据库的场景下**。
3. **恢复慢**：AOF 重放命令会比 RDB 加载慢，超大数据量需测试回复时间。
4. 以上 1、3 点可通过**混合持久化进行**缓解。

#### 总结

1. AOF 适合**保障数据高可靠性**场景，其会持续跟进指令日志，并在重启后作为高优先级进行数据恢复。
2. AOF 文件会无限制膨胀，因此需要配合日志压缩进行缓解，而 Redis 已经配置了默认策略，大部分场景下足够使用。
3. AOF 文件的刷盘策略推荐采用默认场景（每秒刷盘），平衡了性能与数据安全性，若需要高吞吐量，且业务允许数据丢失（例如已经持久化到数据库中），则可以关闭刷盘策略，由系统自行决定。
4. 通常开启 AOF 后，RDB 可以直接关闭，让 AOF 代替 RDB 作为持久化策略。或者可以开启混合持久化，兼顾效率与安全性。

### RDB、AOF 混合持久化

#### 介绍

混合持久化是 Redis 4.0+ 版本支持的一种持久化策略，结合**快照的效率以及日志的高可靠性优势**，让数据快速恢复的同时，通过 AOF 文件的增量命令，提升数据的可靠性。

混合持久化，其工作流程如下：

1. RDB 的 save 规则单独存在，依旧会存在 rdb 文件，但是其并不参与混合持久化。
2. 混合持久化规则绑定 AOF 重写规则。
3. 当触发 AOF 重写时，会把内存数据以 RDB（快照形式）写入新的 AOF 文件头部的同时，后续的**增量命令**会持续写入 AOF 文件尾部。
4. 随后原子替换旧 AOF 文件，只留下新的 AOF 文件。
5. 总结：**AOF 重写 → RDB 头 + 增量 AOF → 替换旧 AOF**

#### 优势

1. **恢复快**：直接加载 RDB 快照部分，再重放增量 AOF。
2. **空间省**：避免纯 AOF 的冗余命令。

#### 开启混合存储

```bash
appendonly yes                  # 启用AOF
aof-use-rdb-preamble yes        # 开启混合模式
# 注意：须确保AOF开启，否则混合存储不生效。
```

#### 总结

1. 混合存储模式，须确保 Redis 版本为 4.0+。
2. 混合存储模式，在绝大部分的业务场景下都完全足够使用，如果针对数据超敏感业务，再更换刷盘策略，牺牲一定性能换取数据可靠性。
3. 可以通过 `INFO Persistence` 监控持久化状态，其输出如下

   ```shell
   ```

# Persistence

loading:0                      # 是否正在加载持久化文件（0=否，1=是）
async_loading:0                # 是否异步加载持久化文件（0=否，1=是）
current_cow_peak:0             # 当前写时复制内存峰值（Copy-On-Write，单位：字节）
current_cow_size:0             # 当前写时复制内存使用量（单位：字节）
current_cow_size_age:0         # 当前写时复制内存使用时长（单位：秒）
current_fork_perc:0.00         # 当前 fork 操作的 CPU 消耗百分比
current_save_keys_processed:0   # 当前 RDB 保存已处理的键数量
current_save_keys_total:0      # 当前 RDB 保存需要处理的总键数量

# RDB 持久化状态

rdb_changes_since_last_save:0  # 自上次 RDB 保存后的数据修改次数
rdb_bgsave_in_progress:0       # 是否有后台 RDB 保存进行中（0=否，1=是）
rdb_last_save_time:1755963988  # 上次 RDB 保存成功的时间戳（Unix 时间）
rdb_last_bgsave_status:ok      # 上次后台 RDB 保存状态（ok=成功，err=失败）
rdb_last_bgsave_time_sec:0     # 上次后台 RDB 保存耗时（秒）
rdb_current_bgsave_time_sec:-1 # 当前后台 RDB 保存已耗时（-1=未运行）
rdb_saves:2                    # RDB 保存成功总次数
rdb_last_cow_size:0            # 上次 RDB 保存时的写时复制内存使用量
rdb_last_load_keys_expired:0   # 上次加载 RDB 时过期的键数量
rdb_last_load_keys_loaded:93   # 上次加载 RDB 时成功加载的键数量

# AOF 持久化状态（aof_enabled=0 表示未启用）

aof_enabled:0                  # AOF 是否启用（0=否，1=是）
aof_rewrite_in_progress:0      # 是否有 AOF 重写进行中（0=否，1=是）
aof_rewrite_scheduled:0        # 是否有 AOF 重写计划中（0=否，1=是）
aof_last_rewrite_time_sec:-1   # 上次 AOF 重写耗时（秒，-1=未发生过）
aof_current_rewrite_time_sec:-1# 当前 AOF 重写已耗时（-1=未运行）
aof_last_bgrewrite_status:ok   # 上次后台 AOF 重写状态（ok=成功，err=失败）
aof_rewrites:0                 # AOF 重写成功总次数
aof_rewrites_consecutive_failures:0  # AOF 重写连续失败次数
aof_last_write_status:ok       # 上次 AOF 写入状态（ok=成功，err=失败）
aof_last_cow_size:0            # 上次 AOF 重写时的写时复制内存使用量

# 模块相关状态

module_fork_in_progress:0      # 是否有模块 fork 操作进行中（0=否，1=是）
module_fork_last_cow_size:0    # 上次模块 fork 时的写时复制内存使用量

```

## Redis高并发与性能优化

### 高并发介绍

#### Redis如何做到应对高并发？

1. **单线程无锁模型**：针对高并发场景，即多线程针对同一个资源的写操作竞争的场景。Redis使用了很简单但是很实用的方式，通过单线程模型，强制所有操作串行化。避免了写操作竞争。
	- 为什么单线程无锁模型反而在内存操作中更占优势？
	1. 在内存操作中，锁的持有与释放都需要**消耗性能**，而单线程模型**天然无锁竞争**；资源可以更多的放在读写操作中。**平衡了易用与性能**，因此在大部分场景下，反而比多线程+锁资源竞争更具有优势。
	2. <u>单线程绑定了单核</u>，无上下文切换开销，可以全力跑满一个CPU核心。
	3. 单线程模型可以**避免多线程的复杂性与隐性bug**，在大部分场景下，其性能足以应对。

2. **网络IO多路复用**：针对多网络请求，Redis采用单线程监听+事件驱动模型，其对应了Redis单主线程模型，不挤不抢，同时保证避免多线程下的资源竞争+线程调度造成的性能开销。

3. **纯内存操作**：所有的读写操作在内存中操作，避免与磁盘IO操作，同时异步持久化操作，不阻塞主线程，让性能发挥到极致。

#### Redis又有什么缺陷？

1. **大Key问题**：尽管单线程模型+纯内存操作，平衡了性能与稳定性。但是单线程意味着绑定单核，当遇到一次性操作大量数据时候，就会发生比较严重的阻塞问题。因为单CPU核心无法容纳大量的数据，就会涉及到查表+跳转+放入缓存等操作，Key越大，性能下降越明显。就会导致短暂的阻塞问题。

2. **OOM风险**：Redis针对List、Set、Hash等可以进行单字段删除的数据类型时，采用删除立刻归还内存的操作，这也就导致了高频的删改操作，会快速归还大量的内存碎片。导致系统合并无法赶上内存碎片化的速度。当Redis再次申请大块内存时候，系统即使内存碎片总和足够，也会拒绝请求，进而触发OOM风险。可能引发缓存雪崩等情况。

3. **持久化风险**：`RDB`可能会丢失部分数据，而`AOF`的恢复较慢，尽管可以采用混合操作进行缓冲，但是依旧有着持久化风险。

4. **集群短板**：单线程模型下，Redis的横向扩展依赖多实例，且跨节点事务/聚合操作较弱。

5. **适用场景限制**：Redis更擅长**缓存/高频读写**，不适合过度复杂的计算/持久化存储等操作。

### 性能优化

#### 默认配置有什么局限？

1. **内存不限制**：Redis配置默认`maxmemory=0`，无上限的内存容易触发OOM导致进程被杀。

2. **持久化不完全可靠**：RDB的三个默认save规则，虽然默认情况下够用，但是依旧会丢失部分数据，不完全可靠，且AOF默认关闭。

3. **网络抗压能力弱**：`tcp-backlog=511`，表示**内核等待处理的连接队列长度**最大为511，其包括网络**请求阶段+数据处理**阶段（已完成握手），实际可以处理的**数量低于511**。超过511的部分会**直接拒绝连接**。生产环境下应该进行适当的调整，<u>建议1024+</u>；`timeout=0`，意味着客户端（如Java应用建立的连接池），会**持续占有连接资源**，如果客户端崩溃，导致无法正常关闭，就会持续占有连接资源，直到资源描述符`maxclient`耗尽，后续的客户端连接请求都会被拒绝。生产环境下，建议`timeout=300`秒，自动清理长时间空闲的连接或者异常崩溃的僵尸连接。而**客户端则会在第一次请求中优先发送连接建立请求**，无须担心连接断开后服务无法使用。但是**频繁的连接创建会导致性能损耗**，因此建议平衡超时时长。

4. **无密码+无IP限制风险**：Redis默认配置无密码。且`protected-mode=no`，这会导致所有IP都可以无密码直接访问。因此生产环境需要改为yes，并通过`requirepass 密码`指定密码，`bind [IP1 IP2.....]`绑定允许的IP，无bind会默认指定127.0.0.1，有bind后127.0.0.1会失效，因此bind指定也需要显式指定127.0.0.1。同时避免指定0.0.0.0，这会允许所有的IP连接。

5. **集群节点需要同步**：Redis集群节点，每个节点的`redis.conf`需要单独调优，且密码、bind指定IP需要**所有节点同步**，否则集群握手会失效。

#### 需要修改的配置都有哪些？

1. **maxmemory****最大内存**：若服务器中只有单Redis，推荐设置为系统内存的`70%~80%`，为系统留足内存。若额外有SpringBoot，根据JVM参数`-Xmx`进行调整。例如：系统内存16G，`-Xmx4G`，则Redis推荐设置为8~10G（留给系统2~4G），`Redis内存 = 总内存 - JVM堆 - 系统预留`。根据`free -h`动态调整。

2. **maxmemory-policy****内存淘汰策略**：Redis默认的内存淘汰策略是：`noeviction`，内存满时拒绝写入，生产环境不推荐使用，容易触发OOM；`allkeys-lru`无差别删除所有的key，按照LRU策略（优先淘汰最久未访问的key），适合纯缓存场景。`volatile-lru` 只删**有过期时间的key**，适合有持久化需求的业务场景。
	- 需要额外注意的是，当触发内存淘汰策略时候，针对大Key会有以下特殊情况：
		- 对于List、Hash、Set等集合类型，大Key会导致大量的内存碎片释放，进而有触发OOM风险，因此需要定时执行`MEMORY PURGE`（Redis 4.0+）清理内存碎片，此过程会占CPU，因此更推荐通过定时任务，在低峰期手动触发该命令进行整理。或者监控`mem_fragmentation_ratio > 1.5`时触发整理。
		- 大Key可能因为访问频率更高，导致LRU无法直接淘汰该大Key，导致内部大部分数据滞留。
		- 如果要根治该情况，需要设置合理的数据模型，避免超大Key的产生。并进行定时监控，及时处理异常情况。

3. **appendonly yes**** 开启AOF**：对持久化有需求的业务场景，Redis默认的ROF策略无法保证数据可靠性，因此需要手动开启AOF，在Redis 4.0+版本以上，更推荐开启**RDB+AOF混合策略**，`aof-use-rdb-preamble yes`。其他的默认配置（例如压缩策略、刷盘策略）大部分情况下都足够使用。

4. **tcp-backlog、timeout****提高网络抗压能力**：默认配置下，最大等待队列是511，且没有连接超时踢出策略，可能导致请求拒绝、连接耗尽。因此推荐提高最大请求等待队列大小，并设置timeout超时踢出机制。推荐配置：`tcp-backlog=1024`、`timeout=30`。

5. **protected-mode****开启保护机制**：默认情况下该机制是关闭的，就会导致所有的IP无需密码即可访问，因此很容易受到攻击。生产环境下推荐`requirepass 密码`指定密码，`bind [IP1 IP2.....]`绑定允许的IP。

#### 配置文件示例

```bash
# 内存与淘汰策略
maxmemory 8GB # 限制内存大小为8G
maxmemory-policy allkeys-lru # 无差别淘汰，适合纯内存场景，如果有持久化需求，推荐换为volatile-lru，只删除有过期时间的key

# 持久化（高可靠场景）
appendonly yes # 开启AOF模式
aof-use-rdb-preamble yes # 开启RDB + AOF混合策略

# 网络优化 
tcp-backlog 1024 # 调整内核等待队列最大长度为1024+
timeout 30 # 调整超时踢出时间为30s

# 安全加固
protected-mode yes # 开启保护模式，禁止所有IP无需密码访问
requirepass YourStrongPassword # 指定密码
bind 127.0.0.1 内网可信IP # 绑定可信IP，若无该配置默认是本机，若指定则必须显性指定127.0.0.1，禁止指定0.0.0.0放行所有IP！
```

## 集群与高可用

### 介绍

1. 单机 Redis 因其单线程模型，容易出现读写瓶颈。
2. 而对应的提升性能的方式是主从复制：通过读写分离，实现读性能的大幅度提升。但是如果主节点宕机，主从复制可能会直接无法使用。
3. 对此可以使用哨兵集群：当 Redis 的主节点宕机，视情况更换主节点。
4. 但是哨兵集群需要考虑_脑裂风险_，因为哨兵集群通过**心跳机制 + 投票机制**实现主节点降级 + 选择从节点升级，但是如果**网络发生错误**，哨兵集群**误认为** Redis 宕机，选择新的主节点，又因为**网络错误无法通知主节点降级**，就会出现**两个主节点**。写操作会分配到两个主节点上，导致数据**无法统一且无法恢复**。
5. 针对这种风险，需配置 `min-slaves-to-write 1`，表示一个主节点至少需要一个从节点才允许写入（防止数据丢失），因为脑裂发生时候，主节点会失去足够的从节点，会**自动拒绝写入**。同时配置 `quorum`（判定主节点失效的最低投票数），如 `quorum=2` 需至少 2 个哨兵同意。公式：`quorum=2哨兵数/2 + 1`，例如 3 个哨兵组成的哨兵集群，设置为 2。防止网络抖动导致的单哨兵误报。
6. 而当内存上限、写操作出现瓶颈后，才考虑使用集群 Redis：通过多节点 Redis，形成集群，可以提升 Redis 的内存上限、写操作、读操作等多种性能。
7. 但随之而来的就是复杂度的大幅度提升。因此根据业务场景选择合适的策略，非必要不使用集群。

### 主从复制

#### 介绍

主从复制是 Redis 提高读性能的主流方式之一，其由多个 Redis 实例组成。首次同步时，主节点**通过 RDB 快照**，将数据同步到从节点中。从节点会**先删除数据**后同步主节点数据。后续的**增量同步**直接应用主节点的**写命令**即可。

当从节点断开后重新恢复，会向主节点发送自己的复制偏移量（repl_backlog）发送给主节点。主节点会对比**自身偏移量**与**复制缓冲区**范围。当偏移量在复制缓冲区范围内，会进行增量同步，反之则重新进行 RDB 全量同步。

从节点数据始终被主节点覆盖，从节点只做被动同步。

#### 如何进行主从复制？

1. **配置从节点**：在从节点中 `redis.conf` 文件中配置：`replicaof <主节点IP> <主节点端口>`，Redis 5.0+ 也可以使用 `REPLICAOF ` 命令实现相同效果，但是仅在运行期间生效，重启后会失效。（注意：旧版为 `slaveof`）。如果主节点配置了 `requirepass` 指定了密码，那么从节点也需要通过 `masterauth` 指定**主节点相同的密码**，反之若主节点没有配置，那么从节点也无需配置。
2. **激活首次同步**：配置好文件后，通过重启即可激活首次同步；或者可以通过 `REPLICAOF` 命令立刻生效，同时通过 `CONFIG REWRITE` 将改动持久化到配置文件中，无需重启即可激活。
3. **配置主节点密码**：虽然主节点可以直接忽略 `requirepass`，让从节点直接连接，但是生产环境依旧**建议配置认证密码**。

#### 一主一从示例

1. 当前环境采用 windows 版本简单示例。
2. 主节点：修改 `redis.windows.conf` 文件

```bash
port 7001 # 主节点启动端口，原配置：port 6379
protected-mode yes # 开启保护模式，设置密码与限制端口
requirepass abc123321 # 设置密码，原配置为注释内容
bind 127.0.0.1 # 绑定ip
```

1. 从节点：修改 `redis.windows.conf` 文件

```bash
port 7002 # 从节点启动端口，原配置：port 6379
protected-mode yes # 开启保护模式，设置密码与限制端口
requirepass abc123321 # 设置密码，原配置为注释内容
replicaof 127.0.0.1 7001 # 指定主节点IP、端口
masterauth abc123321 # 指定主节点密码，必须和主节点的requirepass指定密码保持一致
```

1. 启动，显示以下日志即成功

```shell
**259:M 25 Aug 2025 14:09:41.082 * Ready to accept connections**
**259:M 25 Aug 2025 14:09:41.519 * Replica 127.0.0.1:7002 asks for synchronization**
**259:M 25 Aug 2025 14:09:41.519 * Full resync requested by replica 127.0.0.1:7002**
**259:M 25 Aug 2025 14:09:41.519 * Replication backlog created, my new replication IDs are 'ea00028778729f5d3047d21de7a91d7261a744c2' and '0000000000000000000000000000000000000000'**
**259:M 25 Aug 2025 14:09:41.519 * Delay next BGSAVE for diskless SYNC**
```

1. 在主节点测试写入数据，从节点跟随同步，且从节点拒绝写操作，说明主从复制成功。

#### 一主多从示例

> 1. 主节点：修改 `redis.windows.conf` 文件
>
> ```bash
> ```

port 7001 # 主节点启动端口，原配置：port 6379
protected-mode yes # 开启保护模式，设置密码与限制端口
requirepass abc123321 # 设置密码，原配置为注释内容
bind 127.0.0.1 # 绑定 ip

```
> 1. 从节点1：修改`redis.windows.conf`文件
> ```bash
port 7002 # 从节点启动端口，原配置：port 6379
protected-mode yes # 开启保护模式，设置密码与限制端口
requirepass abc123321 # 设置密码，原配置为注释内容
replicaof 127.0.0.1 7001 # 指定主节点IP、端口
masterauth abc123321 # 指定主节点密码，必须和主节点的requirepass指定密码保持一致
```

1. 从节点 2：修改 `redis.windows.conf` 文件

```bash
port 7003 # 从节点启动端口，原配置：port 6379
protected-mode yes # 开启保护模式，设置密码与限制端口
requirepass abc123321 # 设置密码，原配置为注释内容
replicaof 127.0.0.1 7001 # 指定主节点IP、端口
masterauth abc123321 # 指定主节点密码，必须和主节点的requirepass指定密码保持一致
```

1. 其他的从节点，都可以沿用上述从节点复制，几乎无需改动
2. 从节点启动后，出现如下配置表示同步成功

```bash
1820:S 25 Aug 2025 14:23:00.469 * Connecting to MASTER 127.0.0.1:7001
1820:S 25 Aug 2025 14:23:00.473 * MASTER <-> REPLICA sync started
1820:S 25 Aug 2025 14:23:00.480 * Non blocking connect for SYNC fired the event.
1820:S 25 Aug 2025 14:23:00.485 * Master replied to PING, replication can continue...
1820:S 25 Aug 2025 14:23:00.486 * Partial resynchronization not possible (no cached master)
1820:S 25 Aug 2025 14:23:05.975 * Full resync from master: ea00028778729f5d3047d21de7a91d7261a744c2:1165
1820:S 25 Aug 2025 14:23:06.001 * MASTER <-> REPLICA sync: receiving streamed RDB from master with EOF to disk
1820:S 25 Aug 2025 14:23:06.001 * MASTER <-> REPLICA sync: Flushing old data
1820:S 25 Aug 2025 14:23:06.001 * MASTER <-> REPLICA sync: Loading DB in memory
1820:S 25 Aug 2025 14:23:06.006 * Loading RDB produced by version 7.0.12
1820:S 25 Aug 2025 14:23:06.007 * RDB age 0 seconds
1820:S 25 Aug 2025 14:23:06.007 * RDB memory usage when created 0.88 Mb
1820:S 25 Aug 2025 14:23:06.007 * Done loading RDB, keys loaded: 1, keys expired: 0.
1820:S 25 Aug 2025 14:23:06.007 * MASTER <-> REPLICA sync: Finished with success
1820:S 25 Aug 2025 14:23:06.010 * Creating AOF incr file temp-appendonly.aof.incr on background rewrite
1820:S 25 Aug 2025 14:23:06.025 * Background append only file rewriting started by pid 1821
1821:C 25 Aug 2025 14:23:06.031 * Successfully created the temporary AOF base file temp-rewriteaof-bg-1821.aof
1821:C 25 Aug 2025 14:23:06.031 * Fork CoW for AOF rewrite: current 0 MB, peak 0 MB, average 0 MB
1820:S 25 Aug 2025 14:23:06.112 * Background AOF rewrite terminated with success
1820:S 25 Aug 2025 14:23:06.116 * Successfully renamed the temporary AOF base file temp-rewriteaof-bg-1821.aof into appendonly.aof.3.base.rdb
1820:S 25 Aug 2025 14:23:06.118 * Successfully renamed the temporary AOF incr file temp-appendonly.aof.incr into appendonly.aof.3.incr.aof
1820:S 25 Aug 2025 14:23:06.124 * Removing the history file appendonly.aof.2.incr.aof in the background
1820:S 25 Aug 2025 14:23:06.125 * Removing the history file appendonly.aof.2.base.rdb in the background
1820:S 25 Aug 2025 14:23:06.141 * Background AOF rewrite finished successfully
```

#### 从-从-主-从-从级联复制示例

- 在之前的从-主-从基础上改动

> 1. 主节点：修改 `redis.windows.conf` 文件
>
> ```bash
> ```

port 7001 # 主节点启动端口，原配置：port 6379
protected-mode yes # 开启保护模式，设置密码与限制端口
requirepass abc123321 # 设置密码，原配置为注释内容
bind 127.0.0.1 # 绑定 ip

```
> 1. 从节点1：修改`redis.windows.conf`文件
> ```bash
port 7002 # 从节点启动端口，原配置：port 6379
protected-mode yes # 开启保护模式，设置密码与限制端口
requirepass abc123321 # 设置密码，原配置为注释内容
replicaof 127.0.0.1 7001 # 指定主节点IP、端口
masterauth abc123321 # 指定主节点密码，必须和主节点的requirepass指定密码保持一致
```

> 1. 从节点 2：修改 `redis.windows.conf` 文件
>
> ```bash
> ```

port 7003 # 从节点启动端口，原配置：port 6379
protected-mode yes # 开启保护模式，设置密码与限制端口
requirepass abc123321 # 设置密码，原配置为注释内容
replicaof 127.0.0.1 7001 # 指定主节点 IP、端口
masterauth abc123321 # 指定主节点密码，必须和主节点的 requirepass 指定密码保持一致

```

- 从从节点1：修改`redis.windows.conf`文件（7004绑定7002）

```bash
port 7004 # 从节点启动端口，原配置：port 6379
protected-mode yes # 开启保护模式，设置密码与限制端口
requirepass abc123321 # 设置密码，原配置为注释内容
replicaof 127.0.0.1 7002 # 指定主节点IP、端口
masterauth abc123321 # 指定主节点密码，必须和主节点的requirepass指定密码保持一致
```

- 从从节点 2：修改 `redis.windows.conf` 文件（7005 绑定 7003）

```bash
port 7005 # 从节点启动端口，原配置：port 6379
protected-mode yes # 开启保护模式，设置密码与限制端口
requirepass abc123321 # 设置密码，原配置为注释内容
replicaof 127.0.0.1 7003 # 指定主节点IP、端口
masterauth abc123321 # 指定主节点密码，必须和主节点的requirepass指定密码保持一致
```

- 从从节点出现以下输出，即成功

```bash
771:S 25 Aug 2025 14:33:22.375 * Ready to accept connections
771:S 25 Aug 2025 14:33:23.352 * **Connecting to MASTER 127.0.0.1:7003 # 成功将7003作为主节点**
771:S 25 Aug 2025 14:33:23.353 * MASTER <-> REPLICA sync started
771:S 25 Aug 2025 14:33:23.354 * Non blocking connect for SYNC fired the event.
771:S 25 Aug 2025 14:33:23.354 * Master replied to PING, replication can continue...
771:S 25 Aug 2025 14:33:23.355 * Partial resynchronization not possible (no cached master)
771:S 25 Aug 2025 14:33:28.519 * Full resync from master: ea00028778729f5d3047d21de7a91d7261a744c2:1963
771:S 25 Aug 2025 14:33:28.535 * MASTER <-> REPLICA sync: receiving streamed RDB from master with EOF to disk
771:S 25 Aug 2025 14:33:28.536 * MASTER <-> REPLICA sync: Flushing old data
771:S 25 Aug 2025 14:33:28.537 * MASTER <-> REPLICA sync: Loading DB in memory
771:S 25 Aug 2025 14:33:28.544 * Loading RDB produced by version 7.0.12
771:S 25 Aug 2025 14:33:28.544 * RDB age 0 seconds
771:S 25 Aug 2025 14:33:28.545 * RDB memory usage when created 0.81 Mb
771:S 25 Aug 2025 14:33:28.547 * Done loading RDB, keys loaded: 1, keys expired: 0.
771:S 25 Aug 2025 14:33:28.548 * MASTER <-> REPLICA sync: Finished with success
771:S 25 Aug 2025 14:33:28.550 * Creating AOF incr file temp-appendonly.aof.incr on background rewrite
771:S 25 Aug 2025 14:33:28.564 * Background append only file rewriting started by pid 772
772:C 25 Aug 2025 14:33:28.569 * Successfully created the temporary AOF base file temp-rewriteaof-bg-772.aof
772:C 25 Aug 2025 14:33:28.569 * Fork CoW for AOF rewrite: current 0 MB, peak 0 MB, average 0 MB
771:S 25 Aug 2025 14:33:28.671 * Background AOF rewrite terminated with success
771:S 25 Aug 2025 14:33:28.675 * Successfully renamed the temporary AOF base file temp-rewriteaof-bg-772.aof into appendonly.aof.4.base.rdb
771:S 25 Aug 2025 14:33:28.676 * Successfully renamed the temporary AOF incr file temp-appendonly.aof.incr into appendonly.aof.4.incr.aof
771:S 25 Aug 2025 14:33:28.681 * Removing the history file appendonly.aof.3.incr.aof in the background
771:S 25 Aug 2025 14:33:28.681 * Removing the history file appendonly.aof.3.base.rdb in the background
771:S 25 Aug 2025 14:33:28.684 * Background AOF rewrite finished successfully
```

#### 注意

1. 当前主从复制，仅仅是主从节点数据同步。
2. 连接主节点后无法直接实现读写分离，依旧需要中间件实现读写分离（哨兵/Proxy）自动路由读写。
3. 如果要保证高可用性，建议集成哨兵集群。
4. 一主多从、级联复制本质上就是从节点对齐主节点的密码配置，同时注意允许对应的 ip 进行连接。
5. 级联复制会增加同步延迟，因此生产环境下慎用。

### 哨兵集群

#### 介绍

哨兵（Sentinel）是 Redis 自带的高可用组件，集成在 Redis 的源码中，使用 `redis-sentinel` 或者 `redis-server --sentinel` 即可启动。其可以实现以下核心功能：

同时需要注意以下几点：

总结："**主从 + 自动选主**"的故障转移系统

#### 如何实现哨兵集群？

1. 准备好从-主-从配置（5.2.4 已给出示例）
2. 注意：哨兵模式下，即使是主库，也必须配置 `masterauth ` 指定密码。不然主库变为从库重启后，会因为缺失密码不断重试！
3. 启动所有 Redis 实例
4. 配置哨兵（每个节点创建 `sentinel.conf`）

```bash
port 26379  # 🌟 哨兵端口
daemonize yes  # 🌟 后台运行
pidfile /var/run/redis-sentinel.pid  # 进程ID文件

# 监控主库（名称、IP、端口、quorum数）
sentinel monitor mymaster <主库IP> 6379 2  # 🌟 核心配置

# 认证
sentinel auth-pass mymaster <密码>  # 🌟 主库密码（若需认证）
sentinel auth-user mymaster <用户名>  # 📌 Redis 6.0+ 用户名认证

# 超时与故障判定
sentinel down-after-milliseconds mymaster 5000  # 🌟 5秒无响应=宕机
sentinel failover-timeout mymaster 10000  # 🌟 故障转移超时（10秒）
sentinel parallel-syncs mymaster 1  # 🌟 从库同步并发数（默认1）

# 从节点限制
sentinel min-replicas-to-write 1  # 📌 至少1个从节点才允许写（防数据丢失）
sentinel min-replicas-max-lag 10  # 📌 从库延迟≤10秒才算健康

# 日志与通知
logfile "/var/log/redis/sentinel.log"  # 🌟 日志路径
syslog-ident sentinel  # 📌 系统日志标识
sentinel notification-script mymaster /path/to/alert.sh  # 📌 故障时触发脚本
sentinel client-reconfig-script mymaster /path/to/notify.sh  # 📌 切换时通知客户端

# 网络与安全
bind <内网IP>  # 🌟 绑定内网IP
protected-mode yes  # 🌟 禁止外网访问（除非配置bind）
dir /tmp  # 📌 哨兵工作目录（存储状态）

# 高级配置
sentinel deny-scripts-reconfig yes  # 📌 禁止运行时修改脚本
sentinel resolve-hostnames no  # 📌 禁用DNS解析（防网络问题）
sentinel announce-hostnames no  # 📌 禁用主机名广播（用IP）
```

1. 启动哨兵：`redis-server sentinel.conf --sentinel`
2. 验证：`redis-cli -p 26379 sentinel masters`
3. 关键点：奇数节点、相同监控名称、足够 quorum 值（法定人数，达到这个值才能更换主节点）。生产环境建议 3 节点以上。

#### 哨兵集群搭建

1. 准备好从-主-从配置（5.2.4 已给出示例）
2. 启动所有 Redis 实例
3. 配置主节点哨兵

```bash
port 27001
daemonize yes  # 🌟 后台运行
sentinel monitor master-slave-test 127.0.0.1 7001 2  # 🌟 核心配置
sentinel auth-pass master-slave-test abc123321  # 🌟 主库密码abc123321（若需认证）
sentinel down-after-milliseconds master-slave-test 5000  # 🌟 5秒无响应=宕机
sentinel failover-timeout master-slave-test 10000  # 🌟 故障转移超时（10秒）
sentinel parallel-syncs master-slave-test 1  # 🌟 从库同步并发数（默认1）
bind 127.0.0.1  # 🌟 绑定内网IP
protected-mode yes  # 🌟 禁止外网访问（除非配置bind）
logfile "sentinel-27001.log" # 指定输出日志，这里是在dir指定的工作目录下输出的
```

1. 配置从节点哨兵（除了部署地址，其余基本相同）

```bash
port 27002
daemonize yes  # 🌟 后台运行
sentinel monitor master-slave-test 127.0.0.1 7001 2  # 🌟 核心配置
sentinel auth-pass master-slave-test abc123321  # 🌟 主库密码abc123321（若需认证）
sentinel down-after-milliseconds master-slave-test 5000  # 🌟 5秒无响应=宕机
sentinel failover-timeout master-slave-test 10000  # 🌟 故障转移超时（10秒）
sentinel parallel-syncs master-slave-test 1  # 🌟 从库同步并发数（默认1）
bind 127.0.0.1  # 🌟 绑定内网IP
protected-mode yes  # 🌟 禁止外网访问（除非配置bind）
logfile "sentinel-27002.log" # 指定输出日志，这里是在dir指定的工作目录下输出的
```

```bash
port 27003
daemonize yes  # 🌟 后台运行
sentinel monitor master-slave-test 127.0.0.1 7001 2  # 🌟 核心配置
sentinel auth-pass master-slave-test abc123321  # 🌟 主库密码abc123321（若需认证）
sentinel down-after-milliseconds master-slave-test 5000  # 🌟 5秒无响应=宕机
sentinel failover-timeout master-slave-test 10000  # 🌟 故障转移超时（10秒）
sentinel parallel-syncs master-slave-test 1  # 🌟 从库同步并发数（默认1）
bind 127.0.0.1  # 🌟 绑定内网IP
protected-mode yes  # 🌟 禁止外网访问（除非配置bind）
logfile "sentinel-27003.log" # 指定输出日志，这里是在dir指定的工作目录下输出的
```

1. 启动哨兵：`redis-server sentinel.conf --sentinel`
2. 注意：可能出现以下错误

```bash
D:\Redis\redis-master-7001>redis-server sentinel.conf --sentinel

*** FATAL CONFIG FILE ERROR (Redis 7.0.12) ***
Reading the configuration file, at line 55
>>> 'dir /tmp'
No such file or directory
```

1. 更换目录为当前系统已有的固定目录即可

```bash
dir "/Redis/redis-sentinel-7001"
```

1. 验证哨兵：`redis-cli -p 27001 sentinel masters` （视情况更换端口号），若有以下输出表示输出成功

```bash
D:\Redis\redis-master-7001>redis-cli -p 27001 sentinel masters
1)  1) "name"
    2) "master-slave-test"
    3) "ip"
    4) "127.0.0.1"
    5) "port"
    6) "7001"
    7) "runid"
    8) "0c7d35fd94a473e0bdf317e1516a5a6334d3051a"
    9) "flags"
   10) "master"
   11) "link-pending-commands"
   12) "0"
   13) "link-refcount"
   14) "1"
   15) "last-ping-sent"
   16) "0"
   17) "last-ok-ping-reply"
   18) "881"
   19) "last-ping-reply"
   20) "881"
   21) "down-after-milliseconds"
   22) "5000"
   23) "info-refresh"
   24) "5357"
   25) "role-reported"
   26) "master"
   27) "role-reported-time"
   28) "15556"
   29) "config-epoch"
   30) "0"
   31) "num-slaves"
   32) "0"
   33) "num-other-sentinels"
   34) "0"
   35) "quorum"
   36) "2"
   37) "failover-timeout"
   38) "10000"
   39) "parallel-syncs"
   40) "1"
2)  1) "name"
    2) "mymaster"
    3) "ip"
    4) "127.0.0.1"
    5) "port"
    6) "6379"
    7) "runid"
    8) ""
    9) "flags"
   10) "master"
   11) "link-pending-commands"
   12) "15"
   13) "link-refcount"
   14) "1"
   15) "last-ping-sent"
   16) "15556"
   17) "last-ok-ping-reply"
   18) "15556"
   19) "last-ping-reply"
   20) "15556"
   21) "down-after-milliseconds"
   22) "30000"
   23) "info-refresh"
   24) "0"
   25) "role-reported"
   26) "master"
   27) "role-reported-time"
   28) "15556"
   29) "config-epoch"
   30) "0"
   31) "num-slaves"
   32) "0"
   33) "num-other-sentinels"
   34) "0"
   35) "quorum"
   36) "2"
   37) "failover-timeout"
   38) "180000"
   39) "parallel-syncs"
   40) "1"
```

1. 当前的哨兵集群为后台启动，Redis 中没有直接关闭哨兵的指令，因此需要手动查询哨兵的进程，并杀死对应的进程即可。

#### 验证是否生效

1. 模拟主 Redis 宕机： `redis-cli -p 7001 -a abc123321 shutdown`
2. 查看日志：

```bash
136:X 25 Aug 2025 15:22:32.834 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
136:X 25 Aug 2025 15:22:32.834 # Redis version=7.0.12, bits=64, commit=00000000, modified=0, pid=136, just started
136:X 25 Aug 2025 15:22:32.835 # Configuration loaded
136:X 25 Aug 2025 15:22:32.836 * monotonic clock: POSIX clock_gettime
136:X 25 Aug 2025 15:22:32.838 * Running mode=sentinel, port=27001.
136:X 25 Aug 2025 15:22:32.839 # Sentinel ID is 78a8fd27814374a1dc5f05155f0d77617e940617
136:X 25 Aug 2025 15:22:32.839 # +monitor master mymaster 127.0.0.1 6379 quorum 2
136:X 25 Aug 2025 15:22:32.839 # +monitor master master-slave-test 127.0.0.1 7001 quorum 2
136:X 25 Aug 2025 15:23:02.837 # +sdown master mymaster 127.0.0.1 6379
136:X 25 Aug 2025 15:27:14.109 * +slave slave 127.0.0.1:7002 127.0.0.1 7002 @ master-slave-test 127.0.0.1 7001
136:X 25 Aug 2025 15:27:14.124 # WARNING: Sentinel was not able to save the new configuration on disk!!!: Permission denied
136:X 25 Aug 2025 15:28:04.280 * +slave slave 127.0.0.1:7003 127.0.0.1 7003 @ master-slave-test 127.0.0.1 7001
136:X 25 Aug 2025 15:28:04.296 # WARNING: Sentinel was not able to save the new configuration on disk!!!: Permission denied
136:X 25 Aug 2025 15:31:59.279 # +sdown master master-slave-test 127.0.0.1 7001
```

1. 问题：哨兵节点因为缺失权限，导致配置文件写入失败。
2. 原因：哨兵监视到主节点宕机后会执行一下步骤：

   1. **选主**：哨兵投票从健康从库中选新主（优先优先级高、数据新的）。
   2. **提主**：新主执行 REPLICAOF no one 脱离从库身份。
   3. **切从**：其他从库执行 REPLICAOF < 新主 IP> 端口。
   4. **通知**：哨兵更新** ****sentinel.conf** 并通知客户端新主地址。
   5. 问题就出现在第四步，更改 sentinel.conf 文件出现权限错误。
3. 修改：在父目录或者对应的 sentinel.conf 文件中，赋予 Users 组写入权限。
4. 重新启动主 Redis，再次手动触发 Redis 宕机
5. 日志输出：

```bash
976:X 25 Aug 2025 16:01:22.570 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
976:X 25 Aug 2025 16:01:22.570 # Redis version=7.0.12, bits=64, commit=00000000, modified=0, pid=976, just started
976:X 25 Aug 2025 16:01:22.570 # Configuration loaded
976:X 25 Aug 2025 16:01:22.571 * monotonic clock: POSIX clock_gettime
976:X 25 Aug 2025 16:01:22.574 * Running mode=sentinel, port=27001.
976:X 25 Aug 2025 16:01:22.574 # Sentinel ID is 78a8fd27814374a1dc5f05155f0d77617e940617
976:X 25 Aug 2025 16:01:22.575 # +monitor master master-slave-test 127.0.0.1 7001 quorum 2
976:X 25 Aug 2025 16:02:28.124 # +sdown master master-slave-test 127.0.0.1 7001
```

1. 现象：三哨兵节点都触发了主观宕机，但是未触发重新选主流程.
2. 查询问题：

```bash
# 查从库状态：  
redis-cli -p 27001 sentinel replicas master-slave-test  
# 查哨兵集群通信：  
redis-cli -p 27001 sentinel sentinels master-slave-test  

D:\Redis\redis-master-7001>redis-cli -p 27001 sentinel replicas master-slave-test
1)  1) "name"
    2) "127.0.0.1:7003"
    3) "ip"
    4) "127.0.0.1"
    5) "port"
    6) "7003"
    7) "runid"
    8) "7dc54480f912f715390117fa66a0d351f3b90cf5"
    9) "flags"
   10) "slave"
   11) "link-pending-commands"
   12) "0"
   13) "link-refcount"
   14) "1"
   15) "last-ping-sent"
   16) "0"
   17) "last-ok-ping-reply"
   18) "169"
   19) "last-ping-reply"
   20) "169"
   21) "down-after-milliseconds"
   22) "5000"
   23) "info-refresh"
   24) "169"
   25) "role-reported"
   26) "slave"
   27) "role-reported-time"
   28) "258479"
   29) "master-link-down-time"
   30) "198000"
   31) "master-link-status"
   32) "err"
   33) "master-host"
   34) "127.0.0.1"
   35) "master-port"
   36) "7001"
   37) "slave-priority"
   38) "100"
   39) "slave-repl-offset"
   40) "25476"
   41) "replica-announced"
   42) "1"
2)  1) "name"
    2) "127.0.0.1:7002"
    3) "ip"
    4) "127.0.0.1"
    5) "port"
    6) "7002"
    7) "runid"
    8) "e93f91e4baba2806d8dcf50b33c7b18e7b25aca4"
    9) "flags"
   10) "slave"
   11) "link-pending-commands"
   12) "0"
   13) "link-refcount"
   14) "1"
   15) "last-ping-sent"
   16) "0"
   17) "last-ok-ping-reply"
   18) "168"
   19) "last-ping-reply"
   20) "168"
   21) "down-after-milliseconds"
   22) "5000"
   23) "info-refresh"
   24) "168"
   25) "role-reported"
   26) "slave"
   27) "role-reported-time"
   28) "258479"
   29) "master-link-down-time"
   30) "198000"
   31) "master-link-status"
   32) "err"
   33) "master-host"
   34) "127.0.0.1"
   35) "master-port"
   36) "7001"
   37) "slave-priority"
   38) "100"
   39) "slave-repl-offset"
   40) "25476"
   41) "replica-announced"
   42) "1"

D:\Redis\redis-master-7001>redis-cli -p 27001 sentinel sentinels master-slave-test
(empty array)

D:\Redis\redis-master-7001>
```

1. 原因以及解决方案： 哨兵集群未形成，是触发选主失败的根本原因！
   1. 关键问题：
      1. 从库状态正常：
         - 两个从库 `7002` 和 `7003` 均被哨兵发现（`flags=slave`），优先级 `100`（可参与选主），数据偏移量一致（`25476`）。
      2. 致命缺陷：

      ```bash
      ```

redis-cli -p 27001 sentinel sentinels master-slave-test   (empty array)
→ 仅 1 个哨兵在运行（未组成 3 哨兵集群），无法满足 quorum=2 的投票条件，因此无法达成客观宕机（+odown），更无法触发选主。

```
	2. 查询日志
	```bash
61:X 25 Aug 2025 16:38:12.101 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
61:X 25 Aug 2025 16:38:12.102 # Redis version=7.0.12, bits=64, commit=00000000, modified=0, pid=61, just started
61:X 25 Aug 2025 16:38:12.102 # Configuration loaded
61:X 25 Aug 2025 16:38:12.103 * monotonic clock: POSIX clock_gettime
61:X 25 Aug 2025 16:38:12.105 * Running mode=sentinel, port=27001.
61:X 25 Aug 2025 16:38:12.106 # Sentinel ID is **78a8fd27814374a1dc5f05155f0d77617e940617**
61:X 25 Aug 2025 16:38:12.106 # +monitor master master-slave-test 127.0.0.1 7001 quorum 2
```

```
```bash
```

63:X 25 Aug 2025 16:38:15.698 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
63:X 25 Aug 2025 16:38:15.698 # Redis version=7.0.12, bits=64, commit=00000000, modified=0, pid=63, just started
63:X 25 Aug 2025 16:38:15.698 # Configuration loaded
63:X 25 Aug 2025 16:38:15.699 * monotonic clock: POSIX clock_gettime
63:X 25 Aug 2025 16:38:15.702 * Running mode=sentinel, port=27002.
63:X 25 Aug 2025 16:38:15.702 # Sentinel ID is **78a8fd27814374a1dc5f05155f0d77617e940617**
63:X 25 Aug 2025 16:38:15.702 # +monitor master master-slave-test 127.0.0.1 7001 quorum 2

```
	```bash
61:X 25 Aug 2025 16:38:12.101 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
61:X 25 Aug 2025 16:38:12.102 # Redis version=7.0.12, bits=64, commit=00000000, modified=0, pid=61, just started
61:X 25 Aug 2025 16:38:12.102 # Configuration loaded
61:X 25 Aug 2025 16:38:12.103 * monotonic clock: POSIX clock_gettime
61:X 25 Aug 2025 16:38:12.105 * Running mode=sentinel, port=27001.
61:X 25 Aug 2025 16:38:12.106 # Sentinel ID is **78a8fd27814374a1dc5f05155f0d77617e940617**
61:X 25 Aug 2025 16:38:12.106 # +monitor master master-slave-test 127.0.0.1 7001 quorum 2
```

```
1. 分析：我们发现所有的哨兵ID都相同，这会导致哨兵之间无法互认，这是导致问题的根本原因。
2. 而经过查询，对应的 **sentinel myid**配置项完全相同，这就导致了日志启动中出现了完全相同的Sentinel Id，根本原因就是复制的配置文件conf，没有修改该配置项。
3. 修改Sentinel Id，保证三个不相同即可。
```

2. 经过上述修改后，再次查询主库信息，输出正常

```json
D:\Redis\redis-master-7001> redis-cli -p 27001 sentinel sentinels master-slave-test
1)  1) "name"
    2) "78a8fd27814374a1dc5f05155f0d77617e940619"
    3) "ip"
    4) "127.0.0.1"
    5) "port"
    6) "27003"
    7) "runid"
    8) "78a8fd27814374a1dc5f05155f0d77617e940619"
    9) "flags"
   10) "sentinel"
   11) "link-pending-commands"
   12) "0"
   13) "link-refcount"
   14) "1"
   15) "last-ping-sent"
   16) "0"
   17) "last-ok-ping-reply"
   18) "491"
   19) "last-ping-reply"
   20) "491"
   21) "down-after-milliseconds"
   22) "5000"
   23) "last-hello-message"
   24) "1018"
   25) "voted-leader"
   26) "?"
   27) "voted-leader-epoch"
   28) "0"
2)  1) "name"
    2) "78a8fd27814374a1dc5f05155f0d77617e940618"
    3) "ip"
    4) "127.0.0.1"
    5) "port"
    6) "27002"
    7) "runid"
    8) "78a8fd27814374a1dc5f05155f0d77617e940618"
    9) "flags"
   10) "sentinel"
   11) "link-pending-commands"
   12) "0"
   13) "link-refcount"
   14) "1"
   15) "last-ping-sent"
   16) "0"
   17) "last-ok-ping-reply"
   18) "972"
   19) "last-ping-reply"
   20) "972"
   21) "down-after-milliseconds"
   22) "5000"
   23) "last-hello-message"
   24) "299"
   25) "voted-leader"
   26) "?"
   27) "voted-leader-epoch"
   28) "0"

D:\Redis\redis-master-7001>
```

1. 再次手动宕机 7001 主机测试，日志输出：

```bash
75:X 25 Aug 2025 16:48:08.209 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
75:X 25 Aug 2025 16:48:08.210 # Redis version=7.0.12, bits=64, commit=00000000, modified=0, pid=75, just started
75:X 25 Aug 2025 16:48:08.210 # Configuration loaded
75:X 25 Aug 2025 16:48:08.211 * monotonic clock: POSIX clock_gettime
75:X 25 Aug 2025 16:48:08.213 * Running mode=sentinel, port=27001.
75:X 25 Aug 2025 16:48:08.214 # Sentinel ID is 78a8fd27814374a1dc5f05155f0d77617e940617
75:X 25 Aug 2025 16:48:08.214 # +monitor master master-slave-test 127.0.0.1 7001 quorum 2
75:X 25 Aug 2025 16:48:08.940 * +sentinel sentinel 78a8fd27814374a1dc5f05155f0d77617e940618 127.0.0.1 27002 @ master-slave-test 127.0.0.1 7001
75:X 25 Aug 2025 16:48:08.946 # WARNING: Sentinel was not able to save the new configuration on disk!!!: Permission denied
75:X 25 Aug 2025 16:48:10.145 * +sentinel sentinel 78a8fd27814374a1dc5f05155f0d77617e940619 127.0.0.1 27003 @ master-slave-test 127.0.0.1 7001
75:X 25 Aug 2025 16:48:10.149 # WARNING: Sentinel was not able to save the new configuration on disk!!!: Permission denied
75:X 25 Aug 2025 16:50:29.762 # +sdown master master-slave-test 127.0.0.1 7001
75:X 25 Aug 2025 16:50:29.848 # +odown master master-slave-test 127.0.0.1 7001 #quorum 2/2
75:X 25 Aug 2025 16:50:29.852 # +new-epoch 1
```

1. 现象：成功更换 7003 为主机，但是因为权限问题依旧无法保存配置。即使以管理员身份运行也无法成功，其可能是 Windows 系统权限问题，但是后续也经过多次测试，不同的 Redis 主节点宕机都不受影响，因此可以忽略这种错误。
2. 总结：Windows 权限玄学问题，当前测试不影响，在 Linux 系统运行应该不会有这种错误，可以暂时掠过，后续有机会再改正。

### docker 使用 Btinami 镜像，构建哨兵集群

#### 介绍

docker 是我们经常使用的容器化部署工具，而 bitnami 又为 Redis 的 docker 镜像提供了大量的开箱即用的配置，接下来，我们将使用 bitnami 镜像，一步一步进行集群搭建，并做到发生故障稳定转移 + 恢复故障稳定运行。

#### 初始 yml 文件

```yaml
version: '3.8'
services:
  # Redis 主节点配置
  redis-master:
    image: bitnami/redis:8.2.1  # 使用 Redis 8.2.1 官方镜像
    container_name: redis-master  # 容器名称
    environment:
      - REDIS_REPLICATION_MODE=master  # 设置为 master 节点
      **# 坑点1：一定要在主节点也声明对应的主节点密码，故障转移后，其会作为从节点，如果不进行声明，就会导致旧主节点无法连接新主节点**
      - REDIS_MASTER_PASSWORD=abc123321  # 主节点密码（用于从节点连接）
      - REDIS_PASSWORD=abc123321  # Redis 访问密码
    networks:
      - redis-network  # 加入 redis-network 网络
    ports:
      - "7380:6379"  # 主机7380端口映射到容器6379端口
    volumes:
      - redis-master-data:/bitnami/redis/data  # 数据持久化卷

  # Redis 从节点1配置
  redis-slave1:
    image: bitnami/redis:8.2.1
    container_name: redis-slave1
    environment:
      - REDIS_REPLICATION_MODE=slave  # 设置为 slave 节点
      - REDIS_MASTER_HOST=172.31.195.154  # 主节点IP
      - REDIS_MASTER_PORT_NUMBER=7380  # 主节点端口
      - REDIS_MASTER_PASSWORD=abc123321  # 主节点密码
      - REDIS_PASSWORD=abc123321  # Redis 访问密码
    depends_on:
      - redis-master  # 依赖主节点
    networks:
      - redis-network
    ports:
      - "7381:6379"  # 主机7381端口映射到容器6379端口
    volumes:
      - redis-slave1-data:/bitnami/redis/data

  # Redis 从节点2配置
  redis-slave2:
    image: bitnami/redis:8.2.1
    container_name: redis-slave2
    environment:
      - REDIS_REPLICATION_MODE=slave
      - REDIS_MASTER_HOST=172.31.195.154
      - REDIS_MASTER_PORT_NUMBER=7380
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_PASSWORD=abc123321
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "7382:6379"  # 主机7382端口映射到容器6379端口
    volumes:
      - redis-slave2-data:/bitnami/redis/data

  # Sentinel 哨兵节点1配置
  sentinel1:
    image: bitnami/redis-sentinel:8.2.1  # 使用 Redis Sentinel 镜像
    container_name: sentinel1
    environment:
      - REDIS_MASTER_HOST=172.31.195.154  # 监控的主节点IP
      - REDIS_MASTER_PORT_NUMBER=7380  # 主节点端口
      - REDIS_MASTER_PASSWORD=abc123321  # 主节点密码（用于监控）
      - REDIS_SENTINEL_PASSWORD=abc123321  # 哨兵集群通信密码
      - REDIS_SENTINEL_QUORUM=2  # 故障判定仲裁数
      - REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS=5000  # 5秒不可达判定主观下线
      - REDIS_SENTINEL_FAILOVER_TIMEOUT=20000  # 故障转移超时时间(毫秒)
    depends_on:
      - redis-master
      - redis-slave1
      - redis-slave2
    networks:
      - redis-network
    ports:
      - "26380:26379"  # 主机26380端口映射到容器26379端口
    volumes:
      - sentinel1-data:/bitnami/redis/data  # 哨兵数据持久化

  # Sentinel 哨兵节点2配置
  sentinel2:
    image: bitnami/redis-sentinel:8.2.1
    container_name: sentinel2
    environment:
      - REDIS_MASTER_HOST=172.31.195.154
      - REDIS_MASTER_PORT_NUMBER=7380
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_SENTINEL_PASSWORD=abc123321
      - REDIS_SENTINEL_QUORUM=2
      - REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS=5000
      - REDIS_SENTINEL_FAILOVER_TIMEOUT=20000
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "26381:26379"  # 主机26381端口映射到容器26379端口
    volumes:
      - sentinel2-data:/bitnami/redis/data

  # Sentinel 哨兵节点3配置
  sentinel3:
    image: bitnami/redis-sentinel:8.2.1
    container_name: sentinel3
    environment:
      - REDIS_MASTER_HOST=172.31.195.154
      - REDIS_MASTER_PORT_NUMBER=7380
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_SENTINEL_PASSWORD=abc123321
      - REDIS_SENTINEL_QUORUM=2
      - REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS=5000
      - REDIS_SENTINEL_FAILOVER_TIMEOUT=20000
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "26382:26379"  # 主机26382端口映射到容器26379端口
    volumes:
      - sentinel3-data:/bitnami/redis/data

# 自定义网络配置
networks:
  redis-network:
    driver: bridge  # 使用 bridge 网络驱动

# 数据卷配置
volumes:
  redis-master-data:  # 主节点数据卷
  redis-slave1-data:  # 从节点1数据卷
  redis-slave2-data:  # 从节点2数据卷
  sentinel1-data:  # 哨兵1数据卷
  sentinel2-data:  # 哨兵2数据卷
  sentinel3-data:  # 哨兵3数据卷
```

现象：该 yml 文件可以正常启动所有镜像，但是依旧会有一些问题：

1. 从节点使用端口号异常
   - 我们查看 Sentinel 节点对应的日志，发现输出如下

   ```bash
   ```

1:X 26 Aug 2025 05:59:52.773 * oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
1:X 26 Aug 2025 05:59:52.773 * Redis version=8.2.1, bits=64, commit=00000000, modified=1, pid=1, just started
1:X 26 Aug 2025 05:59:52.773 * Configuration loaded
1:X 26 Aug 2025 05:59:52.773 * monotonic clock: POSIX clock_gettime
1:X 26 Aug 2025 05:59:52.774 * Running mode=sentinel, port=26379.
1:X 26 Aug 2025 05:59:52.777 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 05:59:52.777 * Sentinel ID is 972e7ea9dad2f0b197844a3d60b5af1bd05a1278
1:X 26 Aug 2025 05:59:52.777 # +monitor master mymaster 172.31.195.154 7380 quorum 2
1:X 26 Aug 2025 05:59:52.778 * +slave slave 172.25.0.4:7380 172.25.0.4 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 05:59:52.781 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 05:59:54.756 * +sentinel sentinel 9b50ce03c9894f728b9f33c1c24c4eaaa83be54a 172.25.0.3 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 05:59:54.762 * Sentinel new configuration saved on disk

```
	- 注意第九行输出，对应的从节点（IP忽略，这是docker内部网络IP，无需在意），我们发现端口都用了7380，其和主节点对外暴露的端口一致。
	- 也就是说，主从节点绑定时候，也是使用的该IP、端口，这是第一点异常。

2. 从节点网络不可达
	- 随后Sentinel节点日志又输出了如下内容
	```bash
1:X 26 Aug 2025 05:59:52.778 * +slave slave 172.25.0.4:7380 172.25.0.4 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 05:59:52.781 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 05:59:54.756 * +sentinel sentinel 9b50ce03c9894f728b9f33c1c24c4eaaa83be54a 172.25.0.3 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 05:59:54.762 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 05:59:55.011 * +sentinel sentinel 412ec4c2f879050efa1f6f274ca49e27d7952a03 172.25.0.7 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 05:59:55.015 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 05:59:57.791 # +sdown slave 172.25.0.4:7380 172.25.0.4 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:00:02.813 * +slave slave 172.25.0.6:7380 172.25.0.6 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:00:02.815 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:00:07.831 # +sdown slave 172.25.0.6:7380 172.25.0.6 7380 @ mymaster 172.31.195.154 7380
```

```
- 注意时间，两个从节点从开始识别出来(+slave slave .....)，到主观下线(+sdown slave.....)，其中间全部间隔5s，而这也是我们手动设置的主观判断下限的时间。
- 也就是说，从一开始，这两个端口就是不可达状态，Sentinel会定时向所有节点都发送心跳检测，也就是说，从节点从一开始就是不可达状态。我们可以初步推断：**第一点异常出现的错误的端口，导致了从节点的端口的错误识别，进而导致了从节点不可达。**
```

3. 进一步确认
   - 为进一步确认，我们进入 Sentinel 容器内部，以 Sentinel 视角，向该节点的地址发送请求，查看是否能响应

   ```bash
   ```

docker exec -it sentinel1 redis-cli -h 172.25.0.4 -p 7380 ping

```
	- 我们发现无法连接，我们再次更换为docker实际上使用的端口（6379）
	```bash
docker exec -it <sentinel_container> redis-cli -h 172.25.0.4 -p 6379 ping
```

```
- 发现可以响应，问题确定：**端口识别出错，导致了Sentinel无法识别对应的从节点。**
```

#### 针对性改进

我们根据 5.4.2 的排查，发现了是端口问题导致的错误，我们将 yml 文件进行进一步改动，统一 Redis 实际使用的端口，文件如下（去除其他未改动地方）

```yaml
# Redis 主节点
  redis-master:
    image: bitnami/redis:8.2.1
    container_name: redis-master
    environment:
      - REDIS_REPLICATION_MODE=master
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_PASSWORD=abc123321
      - REDIS_PORT_NUMBER=7380 # 注意：一定要把端口号变为主机对外暴露端口，因为sentinel会错误从节点的端口也是主Redis所在端口。导致持续的端口
    networks:
      - redis-network
    ports:
      - "7380:7380"
    volumes:
      - redis-master-data:/bitnami/redis/data

  # Redis 从节点 1
  redis-slave1:
    image: bitnami/redis:8.2.1
    container_name: redis-slave1
    environment:
      - REDIS_REPLICATION_MODE=slave
      - REDIS_MASTER_HOST=172.31.195.154
      - REDIS_MASTER_PORT_NUMBER=7380
      - REDIS_PORT_NUMBER=7380 # 注意：一定要把端口号变为主机对外暴露端口，因为sentinel会错误从节点的端口也是主Redis所在端口。导致持续的端口不可达
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_PASSWORD=abc123321
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "7381:7380"
    volumes:
      - redis-slave1-data:/bitnami/redis/data

  # Redis 从节点 2
  redis-slave2:
    image: bitnami/redis:8.2.1
    container_name: redis-slave2
    environment:
      - REDIS_REPLICATION_MODE=slave
      - REDIS_MASTER_HOST=172.31.195.154
      - REDIS_MASTER_PORT_NUMBER=7380
      - REDIS_PORT_NUMBER=7380 # 注意：一定要把端口号变为主机对外暴露端口，因为sentinel会错误从节点的端口也是主Redis所在端口。导致持续的端口不可达
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_PASSWORD=abc123321
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "7382:7380"
    volumes:
      - redis-slave2-data:/bitnami/redis/data
```

1. 经过上述改动，Sentinel 集群成功识别到对应的从节点，并建立稳定的心跳机制。日志输出如下（重点标红）

```bash
1:X 26 Aug 2025 06:06:50.366 * oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
1:X 26 Aug 2025 06:06:50.366 * Redis version=8.2.1, bits=64, commit=00000000, modified=1, pid=1, just started
1:X 26 Aug 2025 06:06:50.366 * Configuration loaded
1:X 26 Aug 2025 06:06:50.367 * monotonic clock: POSIX clock_gettime
1:X 26 Aug 2025 06:06:50.367 * Running mode=sentinel, port=26379.
1:X 26 Aug 2025 06:06:50.370 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:06:50.370 * Sentinel ID is 5d1689878435f07dfc20d977e1259d539e80e6f8
1:X 26 Aug 2025 06:06:50.370 # +monitor master mymaster 172.31.195.154 7380 quorum 2
1:X 26 Aug 2025 06:06:50.370 * +slave slave 192.168.208.5:7380 192.168.208.5 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:06:50.375 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:06:52.165 * +sentinel sentinel 498a1b0c0c5a00e8e485dd0f76f4c14836dc6689 192.168.208.4 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:06:52.169 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:06:52.285 * +sentinel sentinel 35583c332fa08e3c1983b577606d261a6848f6c3 192.168.208.6 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:06:52.287 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:07:00.423 * +slave slave 192.168.208.3:7380 192.168.208.3 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:07:00.425 * Sentinel new configuration saved on disk
```

1. 随后我们开始测试故障测试，停止主节点 docker 容器，发现了第二个异常，日志输出如下（重点标红）
   ```bash
   ```

1:X 26 Aug 2025 06:08:10.999 # +sdown master mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:08:11.088 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:08:11.088 # +new-epoch 1
1:X 26 Aug 2025 06:08:11.091 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:08:11.091 # +vote-for-leader 498a1b0c0c5a00e8e485dd0f76f4c14836dc6689 1
1:X 26 Aug 2025 06:08:11.579 # +config-update-from sentinel 498a1b0c0c5a00e8e485dd0f76f4c14836dc6689 192.168.208.4 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:08:11.579 # +switch-master mymaster 172.31.195.154 7380 192.168.208.5 7380
1:X 26 Aug 2025 06:08:11.580 * +slave slave 192.168.208.3:7380 192.168.208.3 7380 @ mymaster 192.168.208.5 7380
1:X 26 Aug 2025 06:08:11.580 * +slave slave 172.31.195.154:7380 172.31.195.154 7380 @ mymaster 192.168.208.5 7380
1:X 26 Aug 2025 06:08:11.589 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:08:16.654 # +sdown slave 172.31.195.154:7380 172.31.195.154 7380 @ mymaster 192.168.208.5 7380
1:X 26 Aug 2025 06:09:27.616 # -sdown slave 172.31.195.154:7380 172.31.195.154 7380 @ mymaster 192.168.208.5 7380
1:X 26 Aug 2025 06:09:42.605 * +slave slave 192.168.208.2:6379 192.168.208.2 6379 @ mymaster 192.168.208.5 7380
1:X 26 Aug 2025 06:09:42.607 * Sentinel new configuration saved on disk

```
	- 这里有几个关键的节点：
		- 06:08:10.999：主节点主观宕机
		- 06:08:11：触发新选主过程
		- 06:09:27：旧主节点恢复，作为从节点运行。
		- 06:09:42：新增从节点，192.168.208.2:6379
	- 在这里就出现问题了，明明是旧主节点恢复，按理说，06:09:42节点，出现的应该是192.168.208.2:7380，但是这里却是默认的6379，也就是说，从节点向主节点声明又出现了问题。

2. 我们针对这里进行翻阅资料，发现我们遗漏了一个关键信息：`replica-announce-port` ，该配置项就是主动声明主从复制使用的端口，默认是6379，最后导致了这个错误。我们又翻阅了Bitnami官方镜像，发现了有对应的配置项：
	<table>
<tr>
<td>配置项<br/></td><td>详细描述<br/></td><td>中文大致意思<br/></td><td>默认值<br/></td></tr>
<tr>
<td>`REDIS_REPLICATION_MODE`<br/></td><td>Redis replication mode (values: master, slave)<br/></td><td>主从模式<br/></td><td>`nil`<br/></td></tr>
<tr>
<td>`REDIS_REPLICA_IP`<br/></td><td>The replication announce ip<br/></td><td>声明主从模式使用的ip<br/></td><td>`nil`<br/></td></tr>
<tr>
<td>`REDIS_REPLICA_PORT`<br/></td><td>The replication announce port<br/></td><td>声明主从模式使用的port<br/></td><td>`nil`<br/></td></tr>
</table>
	- 因此我们可以声明`REDIS_REPLICA_PORT=7380`，表示使用的就是该端口

#### 再次改进
我们针对5.4.3再次改进后，最终形成了如下的yml文件

```yaml
version: '3.8'
services:
  # Redis 主节点
  redis-master:
    image: bitnami/redis:8.2.1
    container_name: redis-master
    environment:
      - REDIS_REPLICATION_MODE=master
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_PASSWORD=abc123321
      - REDIS_REPLICA_PORT=7380 # 注意，这个表示上报给主节点使用的端口，如果不进行声明，会默认是6379，之前的从节点也是因为这个错误！
      - REDIS_PORT_NUMBER=7380 # 注意：一定要把端口号变为主机对外暴露端口，因为sentinel会错误从节点的端口也是主Redis所在端口。导致持续的端口
    networks:
      - redis-network
    ports:
      - "7380:7380"
    volumes:
      - redis-master-data:/bitnami/redis/data

  # Redis 从节点 1
  redis-slave1:
    image: bitnami/redis:8.2.1
    container_name: redis-slave1
    environment:
      - REDIS_REPLICATION_MODE=slave
      - REDIS_MASTER_HOST=172.31.195.154
      - REDIS_MASTER_PORT_NUMBER=7380
      - REDIS_REPLICA_PORT=7380 # 注意，这个表示上报给主节点使用的端口，如果不进行声明，会默认是6379，之前的从节点也是因为这个错误！
      - REDIS_PORT_NUMBER=7380 # 注意：一定要把端口号变为主机对外暴露端口，因为sentinel会错误从节点的端口也是主Redis所在端口。导致持续的端口不可达
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_PASSWORD=abc123321
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "7381:7380"
    volumes:
      - redis-slave1-data:/bitnami/redis/data

  # Redis 从节点 2
  redis-slave2:
    image: bitnami/redis:8.2.1
    container_name: redis-slave2
    environment:
      - REDIS_REPLICATION_MODE=slave
      - REDIS_MASTER_HOST=172.31.195.154
      - REDIS_MASTER_PORT_NUMBER=7380
      - REDIS_REPLICA_PORT=7380 # 注意，这个表示上报给主节点使用的端口，如果不进行声明，会默认是6379，之前的从节点也是因为这个错误！
      - REDIS_PORT_NUMBER=7380 # 注意：一定要把端口号变为主机对外暴露端口，因为sentinel会错误从节点的端口也是主Redis所在端口。导致持续的端口不可达
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_PASSWORD=abc123321
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "7382:7380"
    volumes:
      - redis-slave2-data:/bitnami/redis/data

  # Sentinel 节点 1
  sentinel1:
    image: bitnami/redis-sentinel:8.2.1
    container_name: sentinel1
    environment:
      - REDIS_MASTER_HOST=172.31.195.154         # 监控的主库容器名
      - REDIS_MASTER_PORT_NUMBER=7380   # 主库端口
      - REDIS_MASTER_PASSWORD=abc123321        # 主库密码（用于检查主库状态）
      - REDIS_SENTINEL_PASSWORD=abc123321       # 哨兵集群内部通信密码（防止未授权访问）
      - REDIS_SENTINEL_QUORUM=2
      - REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS=5000
      - REDIS_SENTINEL_FAILOVER_TIMEOUT=20000
    depends_on:
      - redis-master
      - redis-slave1
      - redis-slave2
    networks:
      - redis-network
    ports:
      - "26380:26379"
    volumes:
      - sentinel1-data:/bitnami/redis/data

  # Sentinel 节点 2
  sentinel2:
    image: bitnami/redis-sentinel:8.2.1
    container_name: sentinel2
    environment:
      - REDIS_MASTER_HOST=172.31.195.154         # 监控的主库容器名
      - REDIS_MASTER_PORT_NUMBER=7380   # 主库端口
      - REDIS_MASTER_PASSWORD=abc123321        # 主库密码（用于检查主库状态）
      - REDIS_SENTINEL_PASSWORD=abc123321       # 哨兵集群内部通信密码（防止未授权访问）
      - REDIS_SENTINEL_QUORUM=2
      - REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS=5000
      - REDIS_SENTINEL_FAILOVER_TIMEOUT=20000
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "26381:26379"
    volumes:
      - sentinel2-data:/bitnami/redis/data

  # Sentinel 节点 3
  sentinel3:
    image: bitnami/redis-sentinel:8.2.1
    container_name: sentinel3
    environment:
      - REDIS_MASTER_HOST=172.31.195.154         # 监控的主库容器名
      - REDIS_MASTER_PORT_NUMBER=7380   # 主库端口
      - REDIS_MASTER_PASSWORD=abc123321        # 主库密码（用于检查主库状态）
      - REDIS_SENTINEL_PASSWORD=abc123321       # 哨兵集群内部通信密码（防止未授权访问）
      - REDIS_SENTINEL_QUORUM=2
      - REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS=5000
      - REDIS_SENTINEL_FAILOVER_TIMEOUT=20000
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "26382:26379"
    volumes:
      - sentinel3-data:/bitnami/redis/data

networks:
  redis-network:
    driver: bridge

volumes:
  redis-master-data:
  redis-slave1-data:
  redis-slave2-data:
  sentinel1-data:
  sentinel2-data:
  sentinel3-data:
```

- 我们经过日志观察，输出如下

```bash
1:X 26 Aug 2025 06:23:13.663 * oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
1:X 26 Aug 2025 06:23:13.663 * Redis version=8.2.1, bits=64, commit=00000000, modified=1, pid=1, just started
1:X 26 Aug 2025 06:23:13.663 * Configuration loaded
1:X 26 Aug 2025 06:23:13.663 * monotonic clock: POSIX clock_gettime
1:X 26 Aug 2025 06:23:13.663 * Running mode=sentinel, port=26379.
1:X 26 Aug 2025 06:23:13.665 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:23:13.665 * Sentinel ID is c2a0a3d535edc4debb11ad3382e1745ace8fda22
1:X 26 Aug 2025 06:23:13.665 # +monitor master mymaster 172.31.195.154 7380 quorum 2
1:X 26 Aug 2025 06:23:13.666 * +slave slave 172.25.0.3:7380 172.25.0.3 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:13.668 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:23:15.554 * +sentinel sentinel e05e4b0da2f2e35b907aec24b81cc24e76a69dfc 172.25.0.5 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:15.557 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:23:15.587 * +sentinel sentinel 8787ae0b32e6a6a8b579f0698407733906326cb1 172.25.0.6 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:15.590 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:23:23.676 * +slave slave 172.25.0.4:7380 172.25.0.4 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:23.678 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:23:34.815 # +sdown master mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:34.868 # +odown master mymaster 172.31.195.154 7380 #quorum 2/2
1:X 26 Aug 2025 06:23:34.868 # +new-epoch 1
1:X 26 Aug 2025 06:23:34.868 # +try-failover master mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:34.871 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:23:34.871 # +vote-for-leader c2a0a3d535edc4debb11ad3382e1745ace8fda22 1
1:X 26 Aug 2025 06:23:34.877 * 8787ae0b32e6a6a8b579f0698407733906326cb1 voted for c2a0a3d535edc4debb11ad3382e1745ace8fda22 1
1:X 26 Aug 2025 06:23:34.878 * e05e4b0da2f2e35b907aec24b81cc24e76a69dfc voted for c2a0a3d535edc4debb11ad3382e1745ace8fda22 1
1:X 26 Aug 2025 06:23:34.943 # +elected-leader master mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:34.943 # +failover-state-select-slave master mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:34.996 # +selected-slave slave 172.25.0.3:7380 172.25.0.3 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:34.996 * +failover-state-send-slaveof-noone slave 172.25.0.3:7380 172.25.0.3 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:35.048 * +failover-state-wait-promotion slave 172.25.0.3:7380 172.25.0.3 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:35.880 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:23:35.880 # +promoted-slave slave 172.25.0.3:7380 172.25.0.3 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:35.880 # +failover-state-reconf-slaves master mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:35.948 * +slave-reconf-sent slave 172.25.0.4:7380 172.25.0.4 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:36.885 * +slave-reconf-inprog slave 172.25.0.4:7380 172.25.0.4 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:36.885 * +slave-reconf-done slave 172.25.0.4:7380 172.25.0.4 7380 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:36.947 # +failover-end master mymaster 172.31.195.154 7380
1:X 26 Aug 2025 06:23:36.947 # +switch-master mymaster 172.31.195.154 7380 172.25.0.3 7380
1:X 26 Aug 2025 06:23:36.947 * +slave slave 172.25.0.4:7380 172.25.0.4 7380 @ mymaster 172.25.0.3 7380
1:X 26 Aug 2025 06:23:36.947 * +slave slave 172.31.195.154:7380 172.31.195.154 7380 @ mymaster 172.25.0.3 7380
1:X 26 Aug 2025 06:23:36.951 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 06:23:41.954 # +sdown slave 172.31.195.154:7380 172.31.195.154 7380 @ mymaster 172.25.0.3 7380
1:X 26 Aug 2025 06:24:32.977 # -sdown slave 172.31.195.154:7380 172.31.195.154 7380 @ mymaster 172.25.0.3 7380
1:X 26 Aug 2025 06:24:42.945 * +convert-to-slave slave 172.31.195.154:7380 172.31.195.154 7380 @ mymaster 172.25.0.3 7380
1:X 26 Aug 2025 06:24:47.767 * +slave slave 172.25.0.2:7380 172.25.0.2 7380 @ mymaster 172.25.0.3 7380
1:X 26 Aug 2025 06:24:47.769 * Sentinel new configuration saved on disk
```

- 最后哨兵集群稳定运行，故障恢复也保持稳定！

#### 复盘 + 优化性改进

1. 我们通过本地部署 +docker 使用 bitnami 镜像进行部署，发现了两个方案的复杂度接近，而 bitnami 镜像提供了更加方便的配置方式。因此我们更推荐使用 docker 进行容器化部署。
2. 坑点 1：哨兵集群下，主节点、从节点需要统一声明主节点密码，否则当旧主节点重新上线时候，作为从节点旧无法连接到新主节点上。
3. 坑点 2：docker 部署的主节点进行的主从复制，如果不进行声明，就会默认认为从节点也会以 docker 暴露的端口（本质上是 Sentinel 监视的主端口导致的）为连接端口，导致 Sentinel 无法到达从节点端口。
4. 坑点 3：如果不声明主从复制模式下的端口，主节点下线再作为从节点上线后，哨兵集群就会误使用 6379 端口作为从节点端口。
5. 我们经过以上复盘，发现是可以直接指定主从模式下，使用的 ip+ 端口的，那么我们也可以直接声明对应的 ip+ 端口，直接使用服务器内网地址，而不是使用 docker 网络，这样可以大幅度降低复杂度。

```yaml
version: '3.8'
services:
  # Redis 主节点
  redis-master:
    image: bitnami/redis:8.2.1
    container_name: redis-master
    environment:
      - REDIS_REPLICATION_MODE=master
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_PASSWORD=abc123321
      - REDIS_REPLICA_IP=172.31.195.154
      - REDIS_REPLICA_PORT=7380 # 这里声明使用的端口号是7380
    networks:
      - redis-network
    ports:
      - "7380:6379"
    volumes:
      - redis-master-data:/bitnami/redis/data

  # Redis 从节点 1
  redis-slave1:
    image: bitnami/redis:8.2.1
    container_name: redis-slave1
    environment:
      - REDIS_REPLICATION_MODE=slave
      - REDIS_MASTER_HOST=172.31.195.154
      - REDIS_MASTER_PORT_NUMBER=7380
      - REDIS_REPLICA_IP=172.31.195.154
      - REDIS_REPLICA_PORT=7381 # 这里声明使用的端口号是7381
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_PASSWORD=abc123321
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "7381:6379"
    volumes:
      - redis-slave1-data:/bitnami/redis/data

  # Redis 从节点 2
  redis-slave2:
    image: bitnami/redis:8.2.1
    container_name: redis-slave2
    environment:
      - REDIS_REPLICATION_MODE=slave
      - REDIS_MASTER_HOST=172.31.195.154
      - REDIS_MASTER_PORT_NUMBER=7380
      - REDIS_REPLICA_IP=172.31.195.154
      - REDIS_REPLICA_PORT=7382 # 这里声明使用的端口号是7382
      - REDIS_MASTER_PASSWORD=abc123321
      - REDIS_PASSWORD=abc123321
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "7382:6379"
    volumes:
      - redis-slave2-data:/bitnami/redis/data

  # Sentinel 节点 1
  sentinel1:
    image: bitnami/redis-sentinel:8.2.1
    container_name: sentinel1
    environment:
      - REDIS_MASTER_HOST=172.31.195.154         # 监控的主库容器名
      - REDIS_MASTER_PORT_NUMBER=7380   # 主库端口
      - REDIS_MASTER_PASSWORD=abc123321        # 主库密码（用于检查主库状态）
      - REDIS_SENTINEL_PASSWORD=abc123321       # 哨兵集群内部通信密码（防止未授权访问）
      - REDIS_SENTINEL_QUORUM=2
      - REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS=5000
      - REDIS_SENTINEL_FAILOVER_TIMEOUT=20000
    depends_on:
      - redis-master
      - redis-slave1
      - redis-slave2
    networks:
      - redis-network
    ports:
      - "26380:26379"
    volumes:
      - sentinel1-data:/bitnami/redis/data

  # Sentinel 节点 2
  sentinel2:
    image: bitnami/redis-sentinel:8.2.1
    container_name: sentinel2
    environment:
      - REDIS_MASTER_HOST=172.31.195.154         # 监控的主库容器名
      - REDIS_MASTER_PORT_NUMBER=7380   # 主库端口
      - REDIS_MASTER_PASSWORD=abc123321        # 主库密码（用于检查主库状态）
      - REDIS_SENTINEL_PASSWORD=abc123321       # 哨兵集群内部通信密码（防止未授权访问）
      - REDIS_SENTINEL_QUORUM=2
      - REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS=5000
      - REDIS_SENTINEL_FAILOVER_TIMEOUT=20000
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "26381:26379"
    volumes:
      - sentinel2-data:/bitnami/redis/data

  # Sentinel 节点 3
  sentinel3:
    image: bitnami/redis-sentinel:8.2.1
    container_name: sentinel3
    environment:
      - REDIS_MASTER_HOST=172.31.195.154         # 监控的主库容器名
      - REDIS_MASTER_PORT_NUMBER=7380   # 主库端口
      - REDIS_MASTER_PASSWORD=abc123321        # 主库密码（用于检查主库状态）
      - REDIS_SENTINEL_PASSWORD=abc123321       # 哨兵集群内部通信密码（防止未授权访问）
      - REDIS_SENTINEL_QUORUM=2
      - REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS=5000
      - REDIS_SENTINEL_FAILOVER_TIMEOUT=20000
    depends_on:
      - redis-master
    networks:
      - redis-network
    ports:
      - "26382:26379"
    volumes:
      - sentinel3-data:/bitnami/redis/data

networks:
  redis-network:
    driver: bridge

volumes:
  redis-master-data:
  redis-slave1-data:
  redis-slave2-data:
  sentinel1-data:
  sentinel2-data:
  sentinel3-data:
```

改进点：我们声明主从复制模式下，对应的 ip 是内网地址，端口是 docker 部署的端口。同时修改回内部部署端口为默认 6379 端口，这样无需再进行冗余声明。

1. 查看日志输出
   ```shell
   ```

1:X 26 Aug 2025 07:19:47.710 * oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
1:X 26 Aug 2025 07:19:47.710 * Redis version=8.2.1, bits=64, commit=00000000, modified=1, pid=1, just started
1:X 26 Aug 2025 07:19:47.710 * Configuration loaded
1:X 26 Aug 2025 07:19:47.710 * monotonic clock: POSIX clock_gettime
1:X 26 Aug 2025 07:19:47.711 * Running mode=sentinel, port=26379.
1:X 26 Aug 2025 07:19:47.714 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 07:19:47.714 * Sentinel ID is 53d66aaf482dce915c6dfcc4f0a53aa363156295
1:X 26 Aug 2025 07:19:47.714 # +monitor master mymaster 172.31.195.154 7380 quorum 2
1:X 26 Aug 2025 07:19:47.714 * +slave slave 172.31.195.154:7381 172.31.195.154 7381 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 07:19:47.716 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 07:19:47.716 * +slave slave 172.31.195.154:7382 172.31.195.154 7382 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 07:19:47.718 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 07:19:49.578 * +sentinel sentinel d37def6906f901aa8f7c1ef28dd642c885c12261 172.28.0.5 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 07:19:49.581 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 07:19:49.644 * +sentinel sentinel 673387919088b23b1f7c4c1f6d5b6fb85ac78922 172.28.0.6 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 07:19:49.646 * Sentinel new configuration saved on disk

```
	- 我们发现地址成功变为内网地址，说明配置正常
	- 我们尝试让主机宕机并再次恢复
	```bash
1:X 26 Aug 2025 07:23:43.355 # +sdown master mymaster 172.31.195.154 7380
1:X 26 Aug 2025 07:23:43.446 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 07:23:43.446 # +new-epoch 1
1:X 26 Aug 2025 07:23:43.451 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 07:23:43.451 # +vote-for-leader 673387919088b23b1f7c4c1f6d5b6fb85ac78922 1
1:X 26 Aug 2025 07:23:44.489 # +odown master mymaster 172.31.195.154 7380 #quorum 3/2
1:X 26 Aug 2025 07:23:44.489 * Next failover delay: I will not start a failover before Tue Aug 26 07:24:23 2025
1:X 26 Aug 2025 07:23:44.578 # +config-update-from sentinel 673387919088b23b1f7c4c1f6d5b6fb85ac78922 172.28.0.6 26379 @ mymaster 172.31.195.154 7380
1:X 26 Aug 2025 07:23:44.578 # +switch-master mymaster 172.31.195.154 7380 172.31.195.154 7381
1:X 26 Aug 2025 07:23:44.579 * +slave slave 172.31.195.154:7382 172.31.195.154 7382 @ mymaster 172.31.195.154 7381
1:X 26 Aug 2025 07:23:44.579 * +slave slave 172.31.195.154:7380 172.31.195.154 7380 @ mymaster 172.31.195.154 7381
1:X 26 Aug 2025 07:23:44.582 * Sentinel new configuration saved on disk
1:X 26 Aug 2025 07:23:49.606 # +sdown slave 172.31.195.154:7380 172.31.195.154 7380 @ mymaster 172.31.195.154 7381
1:X 26 Aug 2025 07:24:35.415 # -sdown slave 172.31.195.154:7380 172.31.195.154 7380 @ mymaster 172.31.195.154 7381
```

```
- 故障转移完成，故障恢复也正常。
- 我们也查看了旧节点的日志，发现其作为从节点，输出一切正常，说明优化性改进圆满完成~！
```

### Redis Cluster 集群

#### 介绍

Redis Cluster 集群，是 Redis 官方提供的分布式方案，通过数据分片和主从复制实现高可用、可横向扩展的集群。

上述的哨兵集群 + 读写分离中，写操作主要是在主机中进行，且内存上限受限于单机方案。其本质上依旧是单机 Redis。当出现写性能瓶颈/单机内存上限瓶颈时候，就需要考虑换成 Redis Cluster 集群方案，横向拓展 Redis 的写性能以及突破单机的内存上限瓶颈。

Redis Cluster 与哨兵集群的主要区别

<table>
<tr>
<td><br/></td><td>哨兵集群<br/></td><td>Redis Cluster<br/></td></tr>
<tr>
<td>核心目标<br/></td><td>让读写分离**实现高可用**<br/></td><td>实现**高可用**的同时，突破单机**写性能、内存上限**瓶颈<br/></td></tr>
<tr>
<td>数据分布<br/></td><td>单机全量数据<br/></td><td>数据分片到多节点（16384槽）<br/></td></tr>
<tr>
<td>写性能<br/></td><td>受限于主节点<br/></td><td>分散到多个节点，吞吐量提升明显<br/></td></tr>
<tr>
<td>适用场景<br/></td><td>小数据量，提升读性能，高可用<br/></td><td>大数据量/高并发，突破单机瓶颈<br/></td></tr>
<tr>
<td>本质<br/></td><td>读写分离的高可用版本<br/></td><td>读写分离的分布式版本<br/></td></tr>
</table>

#### 简单讲述

1. 分片机制
   🔢 分片机制

   - 哈希槽计算：数据通过 `CRC16(key) % 16384` 计算哈希槽，分散到不同主节点。
   - CRC16 算法：
     - ✅ 轻量快速：比 MD5/SHA 更高效
     - ✅ 稳定均匀：相同 key 固定槽位，不同 key 均匀分布
   - 16384 槽位设计（2^14）：
     - 数据分布均匀：槽位足够多，避免热点
     - 元数据精简：仅需 2KB 存储集群状态
     - 性价比最优：Redis 作者验证的黄金值
   - 位运算优化：
     - `CRC16(key) % 16384` ≡ `CRC16(key) & 0x3FFF`
     - ⚡ 性能提升 10 倍 +（CPU 位操作替代取模）
       🎯 高级分片控制
   - 哈希标签（Hash Tag）：
     - 语法：`{user}:order:100`
     - 强制仅计算 `{ }` 内内容（如 `user`），确保相同前缀的 key 落入同一槽位
     - 📌 适用场景：事务/跨 key 操作需数据共置
   - 槽位分配策略：
     - 默认均分：如 3 主节点 ≈ 5461 槽/节点
     - 手动调整：通过 `CLUSTER ADDSLOTS` 等命令灵活迁移槽位
     - 🔧 应对数据倾斜：自定义槽位与节点映射关系
       总结：Redis Cluster 通过智能分片 + 弹性槽位管理，实现高性能分布式存储。
2. Gossip 协议
   ![](static/PsrKbo5rwo5VbmxsXJdc1McNnnf.png)
   🔄 节点自管理

   - 去中心化通信：节点间通过 Gossip 协议互相探测状态，自动维护集群拓扑。
   - 全自动化：故障检测、状态同步、主从切换等均由集群自主完成，无需人工介入。
     👨💻 开发人员关注点

    <table>

<tr>
<td>阶段<br/></td><td>操作<br/></td><td>说明<br/></td></tr>
<tr>
<td>初始配置<br/></td><td>定义节点关系<br/></td><td>指定主从角色、槽位分配（如 `redis-cli --cluster create`）<br/></td></tr>
<tr>
<td>异常监控<br/></td><td>监听告警事件<br/></td><td>关注 `CLUSTERDOWN`、主从切换等关键日志<br/></td></tr>
<tr>
<td>集群扩容<br/></td><td>增减节点<br/></td><td>触发槽位迁移（如 `redis-cli --cluster reshard`）<br/></td></tr>
</table>
	⚙️ 设计本质
	- 零干预高可用：基于去中心化架构，实现故障自愈与数据一致性。
	- 最终一致性：状态信息通过随机传播（Gossip）最终覆盖全集群。
	> 📌 注：人工仅需处理初始部署和容量规划，运行时故障由集群自动处理。
> 底层依旧是主从复制，从节点只复制主节点并分担读压力，并且也负责通信中的状态广播。

3. 高可用
   🔄 故障自动转移
   - 主从架构：每个主节点（Master）配置 1~N 个从节点（Replica），确保数据冗余。
   - 自动接管：当主节点宕机，从节点自动晋升为新主节点，接管其负责的槽位。
     ⚙️ 核心流程

   1. 故障检测（Gossip 协议）
      - 节点间互相探测，若主节点无响应，标记为 `PFAIL`（疑似下线）。
      - 多数节点确认后，升级为 `FAIL`（确认下线）。
   2. 从节点晋升
      - 从节点检测到主节点 `FAIL` 后，发起选举（若配置允许）。
      - 自动接管槽位，继续提供服务。
        🆚 对比哨兵模式

    <table>

<tr>
<td>特性<br/></td><td>Redis Cluster<br/></td><td>哨兵模式（Sentinel）<br/></td></tr>
<tr>
<td>故障检测<br/></td><td>内置 Gossip 协议<br/></td><td>依赖独立哨兵进程<br/></td></tr>
<tr>
<td>故障转移<br/></td><td>从节点自动接管<br/></td><td>哨兵触发主从切换<br/></td></tr>
<tr>
<td>架构复杂度<br/></td><td>去中心化，无单点<br/></td><td>哨兵需独立部署<br/></td></tr>
</table>
	🎯 设计优势
	- 零外部依赖：Cluster 自身完成故障检测与恢复，无需额外组件（如哨兵）。
	- 快速恢复：从节点自动接管，减少人工干预时间。
	- 去中心化：无单点故障，集群自管理。
	> 📌 本质：Redis Cluster 的高可用机制是内置自愈能力，比哨兵模式更轻量、更自动化。
	本质：**去中心化设计让集群自愈**。

4. 客户端重定向

   - 若请求的 key 不在当前节点，返回 MOVED 指令引导客户端访问正确节点
   - 示例流程：
     - 客户端问节点 A：“user:123 在哪？”
     - 节点 A 检查发现该 key 属于节点 B → 返回（示例）：`MOVED 1234 192.168.1.2:6379`
     - 客户端更新本地缓存，下次直连节点 B
   - 客户端会缓存 key → 节点 的映射关系（基于 MOVED 响应），后续请求直接命中，无需重复计算。
   - 缓存优化流程
     - 首次请求：若路由错误，服务端返回 MOVED 并携带正确节点信息。
     - 客户端缓存：记录 CRC16(key) → 目标节点，下次直连。
     - 缓存失效：若节点拓扑变更（如扩容），服务端返回 ASK 指令强制更新缓存。
       本质：用缓存避免每次请求都计算槽位，性能提升关键。
5. 弹性扩缩容

   - 动态迁移哈希槽（如添加节点时）
   - 扩缩容时，Redis Cluster 会按策略重新分配槽位：
     1. 扩容：
        - 新节点加入后，从现有节点迁移部分槽位到新节点（如 `redis-cli --cluster reshard`）。
     2. 缩容：
        - 节点下线前，需先将其负责的槽位手动迁移到其他节点。
     3. 数据迁移：
        - 槽位迁移时，数据会原子性转移，过程中仍可正常访问（通过 `ASK` 临时重定向）。
          本质：槽位是流动的，节点是弹性的。

一句话：分布式数据存储 + 智能路由 + 自愈能力。

#### Cluster 集群创建

1. 介绍

我们使用 Bitnami 提供的 Redis Cluster 镜像，直接进行镜像创建。

1. 初始配置文件提供

```yaml
version: '3.8'  # 使用 Docker Compose 版本 3.8

services:
  # Redis Cluster Node 0 (Master) - 主节点服务定义
  redis-node-0:
    image: bitnami/redis-cluster:8.2.1  # 使用 bitnami 提供的 Redis 集群镜像，版本 8.2.1
    container_name: redis-node-0  # 容器名称
    
    # 环境变量配置
    environment:
      # 安全配置
      - REDIS_PASSWORD=abc123321  # Redis 默认用户密码
      - REDIS_MASTER_PASSWORD=abc123321  # 主节点密码（用于主从复制）

      # 集群拓扑配置
      - REDIS_CLUSTER_REPLICAS=1  # 每个主节点的副本数（1表示每个主节点有1个从节点）
      - REDIS_NODES=172.31.195.154:8380,172.31.195.154:8381,172.31.195.154:8382,172.31.195.154:8383,172.31.195.154:8384,172.31.195.154:8385  # 集群所有节点地址列表

      # 网络声明
      - REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154  # 集群节点对外宣告的IP地址
      - REDIS_CLUSTER_DYNAMIC_IPS=no  # 禁用动态IP（使用固定IP）
      - REDIS_CLUSTER_ANNOUNCE_PORT=8380  # 当前节点对外宣告的端口
      - REDIS_CLUSTER_ANNOUNCE_BUS_PORT=16380  # 当前节点总线端口（用于集群节点间通信）

      # 节点角色配置
      - REDIS_CLUSTER_CREATOR=yes  # 指定此节点为集群创建者
      - REDIS_REPLICATION_MODE=master  # 节点角色为主节点
      - REDIS_REPLICA_IP=172.31.195.154  # 副本节点IP（用于主从复制）
      - REDIS_REPLICA_PORT=8380  # 副本节点端口（用于主从复制）

    # 网络配置
    networks:
      - redis-cluster-net  # 连接到名为 redis-cluster-net 的网络

    # 端口映射
    ports:
      - "8380:6379"  # 将宿主机的8380端口映射到容器的6379端口（Redis服务端口）
      - "16380:16379"  # 将宿主机的16380端口映射到容器的16379端口（集群总线端口）

    # 数据卷配置
    volumes:
      - redis_data_0:/bitnami/redis/data  # 将名为 redis_data_0 的卷挂载到容器内的数据目录

  # Redis Cluster Node 1 (Master)
  redis-node-1:
    image: bitnami/redis-cluster:8.2.1
    container_name: redis-node-1
    environment:
      # 安全配置（同节点0）
      - REDIS_PASSWORD=abc123321
      - REDIS_MASTER_PASSWORD=abc123321

      # 集群拓扑配置（同节点0）
      - REDIS_CLUSTER_REPLICAS=1
      - REDIS_NODES=172.31.195.154:8380,172.31.195.154:8381,172.31.195.154:8382,172.31.195.154:8383,172.31.195.154:8384,172.31.195.154:8385

      # 网络声明（端口不同）
      - REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154
      - REDIS_CLUSTER_DYNAMIC_IPS=no
      - REDIS_CLUSTER_ANNOUNCE_PORT=8381
      - REDIS_CLUSTER_ANNOUNCE_BUS_PORT=16381

      # 节点角色配置
      - REDIS_REPLICATION_MODE=master
      - REDIS_REPLICA_IP=172.31.195.154
      - REDIS_REPLICA_PORT=8381
    networks:
      - redis-cluster-net
    ports:
      - "8381:6379"
      - "16381:16379"
    volumes:
      - redis_data_1:/bitnami/redis/data

  # Redis Cluster Node 2 (Master)
  redis-node-2:
    image: bitnami/redis-cluster:8.2.1
    container_name: redis-node-2
    environment:
      # 安全配置（同节点0）
      - REDIS_PASSWORD=abc123321
      - REDIS_MASTER_PASSWORD=abc123321

      # 集群拓扑配置（同节点0）
      - REDIS_CLUSTER_REPLICAS=1
      - REDIS_NODES=172.31.195.154:8380,172.31.195.154:8381,172.31.195.154:8382,172.31.195.154:8383,172.31.195.154:8384,172.31.195.154:8385

      # 网络声明（端口不同）
      - REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154
      - REDIS_CLUSTER_DYNAMIC_IPS=no
      - REDIS_CLUSTER_ANNOUNCE_PORT=8382
      - REDIS_CLUSTER_ANNOUNCE_BUS_PORT=16382

      # 节点角色配置
      - REDIS_REPLICATION_MODE=master
      - REDIS_REPLICA_IP=172.31.195.154
      - REDIS_REPLICA_PORT=8382
    networks:
      - redis-cluster-net
    ports:
      - "8382:6379"
      - "16382:16379"
    volumes:
      - redis_data_2:/bitnami/redis/data

  # Redis Cluster Node 3 (Slave)
  redis-node-3:
    image: bitnami/redis-cluster:8.2.1
    container_name: redis-node-3
    environment:
      # 安全配置（同节点0）
      - REDIS_PASSWORD=abc123321
      - REDIS_MASTER_PASSWORD=abc123321

      # 集群拓扑配置（同节点0）
      - REDIS_CLUSTER_REPLICAS=1
      - REDIS_NODES=172.31.195.154:8380,172.31.195.154:8381,172.31.195.154:8382,172.31.195.154:8383,172.31.195.154:8384,172.31.195.154:8385

      # 网络声明（端口不同）
      - REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154
      - REDIS_CLUSTER_DYNAMIC_IPS=no
      - REDIS_CLUSTER_ANNOUNCE_PORT=8383
      - REDIS_CLUSTER_ANNOUNCE_BUS_PORT=16383

      # 从节点特殊配置
      - REDIS_REPLICATION_MODE=slave
      - REDIS_MASTER_HOST=172.31.195.154  # 主节点IP
      - REDIS_MASTER_PORT_NUMBER=8380     # 主节点端口
      - REDIS_REPLICA_IP=172.31.195.154
      - REDIS_REPLICA_PORT=8383
    networks:
      - redis-cluster-net
    ports:
      - "8383:6379"
      - "16383:16379"
    volumes:
      - redis_data_3:/bitnami/redis/data

  # Redis Cluster Node 4 (Slave)
  redis-node-4:
    image: bitnami/redis-cluster:8.2.1
    container_name: redis-node-4
    environment:
      # 安全配置（同节点0）
      - REDIS_PASSWORD=abc123321
      - REDIS_MASTER_PASSWORD=abc123321

      # 集群拓扑配置（同节点0）
      - REDIS_CLUSTER_REPLICAS=1
      - REDIS_NODES=172.31.195.154:8380,172.31.195.154:8381,172.31.195.154:8382,172.31.195.154:8383,172.31.195.154:8384,172.31.195.154:8385

      # 网络声明（端口不同）
      - REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154
      - REDIS_CLUSTER_DYNAMIC_IPS=no
      - REDIS_CLUSTER_ANNOUNCE_PORT=8384
      - REDIS_CLUSTER_ANNOUNCE_BUS_PORT=16384

      # 从节点特殊配置
      - REDIS_REPLICATION_MODE=slave
      - REDIS_MASTER_HOST=172.31.195.154  # 主节点IP
      - REDIS_MASTER_PORT_NUMBER=8381     # 主节点端口
      - REDIS_REPLICA_IP=172.31.195.154
      - REDIS_REPLICA_PORT=8384
    networks:
      - redis-cluster-net
    ports:
      - "8384:6379"
      - "16384:16379"
    volumes:
      - redis_data_4:/bitnami/redis/data

  # Redis Cluster Node 5 (Slave)
  redis-node-5:
    image: bitnami/redis-cluster:8.2.1
    container_name: redis-node-5
    environment:
      # 安全配置（同节点0）
      - REDIS_PASSWORD=abc123321
      - REDIS_MASTER_PASSWORD=abc123321

      # 集群拓扑配置（同节点0）
      - REDIS_CLUSTER_REPLICAS=1
      - REDIS_NODES=172.31.195.154:8380,172.31.195.154:8381,172.31.195.154:8382,172.31.195.154:8383,172.31.195.154:8384,172.31.195.154:8385

      # 网络声明（端口不同）
      - REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154
      - REDIS_CLUSTER_DYNAMIC_IPS=no
      - REDIS_CLUSTER_ANNOUNCE_PORT=8385
      - REDIS_CLUSTER_ANNOUNCE_BUS_PORT=16385

      # 从节点特殊配置
      - REDIS_REPLICATION_MODE=slave
      - REDIS_MASTER_HOST=172.31.195.154  # 主节点IP
      - REDIS_MASTER_PORT_NUMBER=8382     # 主节点端口
      - REDIS_REPLICA_IP=172.31.195.154
      - REDIS_REPLICA_PORT=8385
    networks:
      - redis-cluster-net
    ports:
      - "8385:6379"
      - "16385:16379"
    volumes:
      - redis_data_5:/bitnami/redis/data

networks:
  redis-cluster-net:
    driver: bridge

volumes:
  redis_data_0:
  redis_data_1:
  redis_data_2:
  redis_data_3:
  redis_data_4:
  redis_data_5:
```

1. 我们查看日志，发现并没有集群创建成功的日志

```shell
38:C 27 Aug 2025 07:33:41.731 * oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
38:C 27 Aug 2025 07:33:41.731 * Redis version=8.2.1, bits=64, commit=00000000, modified=1, pid=38, just started
38:C 27 Aug 2025 07:33:41.731 * Configuration loaded
38:M 27 Aug 2025 07:33:41.731 * monotonic clock: POSIX clock_gettime
38:M 27 Aug 2025 07:33:41.732 * Running mode=cluster, port=6379.
38:M 27 Aug 2025 07:33:41.732 * No cluster configuration found, I'm 49b7536adea222b1347d0d825543be126d8faf04
38:M 27 Aug 2025 07:33:41.734 * Server initialized
38:M 27 Aug 2025 07:33:41.735 * Creating AOF base file appendonly.aof.1.base.rdb on server start
38:M 27 Aug 2025 07:33:41.736 * Creating AOF incr file appendonly.aof.1.incr.aof on server start
38:M 27 Aug 2025 07:33:41.736 * Ready to accept connections tcp
```

1. 我们执行手动创建，发现如下问题

```shell
[root@iZbp1acrrz4yaqtlhtoo39Z ~]# redis-cli --cluster create \
>   172.31.195.154:8380 \
>   172.31.195.154:8381 \
>   172.31.195.154:8382 \
>   172.31.195.154:8383 \
>   172.31.195.154:8384 \
>   172.31.195.154:8385 \
>   --cluster-replicas 1 \
>   -a abc123321 \
>   --cluster-yes  # 强制覆盖
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
>>> Performing hash slots allocation on 6 nodes...
Master[0] -> Slots 0 - 5460
Master[1] -> Slots 5461 - 10922
Master[2] -> Slots 10923 - 16383 # 表示主节点创建成功
Adding replica 172.31.195.154:8384 to 172.31.195.154:8380
Adding replica 172.31.195.154:8385 to 172.31.195.154:8381
Adding replica 172.31.195.154:8383 to 172.31.195.154:8382 # 表示主从节点创建成功
>>> Trying to optimize slaves allocation for anti-affinity
[WARNING] Some slaves are in the same host as their master # 警告信息，说明从节点在一个主机中
M: 49b7536adea222b1347d0d825543be126d8faf04 172.31.195.154:8380
   slots:[0-5460] (5461 slots) master
M: e486ba1749c3e581a81fcc842b0e57887be55740 172.31.195.154:8381
   slots:[5461-10922] (5462 slots) master
M: 9f002a85688dfaf5f90454476b1352d30dafdacb 172.31.195.154:8382
   slots:[10923-16383] (5461 slots) master
S: f275ef0d1de1ef7a8bf66c442f0106b3d01770c7 172.31.195.154:8383
   replicates 49b7536adea222b1347d0d825543be126d8faf04
S: fbc2587ae3f6f4bfe17853b8c3363d13495e4a52 172.31.195.154:8384
   replicates e486ba1749c3e581a81fcc842b0e57887be55740
S: 1dc33998b9440e71bfc79d0c684d6afb87be35d3 172.31.195.154:8385
   replicates 9f002a85688dfaf5f90454476b1352d30dafdacb # 表示槽位分配完毕，主从节点均正常
>>> Nodes configuration updated # 在这里发生了问题，经过了很长时间加载都无法响应
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join
.....................................................................................................................................................
```

1. 我们再次查看 docker 日志，发现如下信息

```shell
38:M 27 Aug 2025 07:33:41.736 * Ready to accept connections tcp
Node 172.31.195.154:8380 not ready, waiting for all the nodes to be ready...
Node 172.31.195.154:8380 not ready, waiting for all the nodes to be ready...
...... # 该信息重复出现，
```

1. 这个报错信息表示节点之间握手不成功，但是之前的主从配置均无问题，表示问题很有可能出现在了总线(16380~16385)中
2. 我们针对总线进行问题排查

   1. 查询所有节点是否使用宿主机 IP
   2. 执行：`redis-cli -a abc123321 -p 8380 config get cluster-announce-*`

   ```shell
   ```

[root@iZbp1acrrz4yaqtlhtoo39Z ~]# redis-cli -a abc123321 -p 8380 config get cluster-announce-*
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.

1) "cluster-announce-human-nodename"
2) ""
3) "cluster-announce-hostname"
4) ""
5) "cluster-announce-port"
6) "8380"
7) "cluster-announce-tls-port"
8) "0"
9) "cluster-announce-bus-port"
10) "16380" # 对应的端口无异常
11) "cluster-announce-ip"
12) "172.31.195.154" # 该配置无异常
    [root@iZbp1acrrz4yaqtlhtoo39Z ~]#

```
	1. 针对其他的节点都执行这个命令，配置均无问题，说明使用的集群总线地址没有问题

3. 我们测试总线联通性
	1. 执行`telnet 172.31.195.154 16380`
	```shell
[root@iZbp1acrrz4yaqtlhtoo39Z ~]# telnet 172.31.195.154 16380
Trying 172.31.195.154...
Connected to 172.31.195.154.
Escape character is '^]'. # 表示该端口联通
```

```
1. 针对其他的端口测试，均无连通性问题
```

4. 我们尝试手动触发节点之间握手流程，查看集群中使用的总线连接地址是什么
   1. 清理所有残留的集群连接情况

   ```shell
   ```

# 进入每个节点执行重置

for port in {8380..8385}; do
redis-cli -a abc123321 -p $port flushall
redis-cli -a abc123321 -p $port cluster reset
done

# 所有节点均返回 OK

```
	1. 手动添加对应的节点
	```shell
for port in {8381..8385}; do
  redis-cli -a abc123321 -p 8380 cluster meet 172.31.195.154 $port
done
# 所有节点均返回OK
```

```
1. 查看节点连接信息
```shell
```

redis-cli -a abc123321 -p 8380 cluster nodes

0bace74d99b3343f66f630b999f8569ebe91f88a 172.31.195.154:8385@18385 handshake - 0 0 0 disconnected
49b7536adea222b1347d0d825543be126d8faf04 172.31.195.154:8380@16380 myself,master - 0 0 1 connected
fd229b575a0becdcf6fb3cc2ba842f098973c9d5 172.31.195.154:8383@18383 handshake - 0 0 0 disconnected
7a658f4345f57521cba7408aabd24a8e030b76bf 172.31.195.154:8384@18384 handshake - 0 0 0 disconnected

```
	1. 我们发现，依旧没有成功建立连接信息，主要原因是对应的**总线端口异常**，说明我们声明的`REDIS_CLUSTER_ANNOUNCE_BUS_PORT`配置项没有生效。
	2. 但是我们第`7.b`中已经展示过，对应的总线端口映射是没有问题的。

5. 我们针对这个问题，进行分析
	1. 首先`REDIS_CLUSTER_ANNOUNCE_BUS_PORT`究竟是什么？
	2. 该配置项是本节点自己声明的使用总线端口，如果不声明，默认是声明端口+10000
	3. 也就是说，创建节点并不知道这个配置，因此不管是采用`--cluster create`、`cluster meet`,如果不显式指定端口，就都会采用默认的+10000端口。这也就导致我们后续的集群创建失败。

6. 我们针对这个猜想，进行实验
	```bash
[root@iZbp1acrrz4yaqtlhtoo39Z ~]# redis-cli -a abc123321 -p 8380 cluster meet 172.31.195.154 8381 16381
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
OK
[root@iZbp1acrrz4yaqtlhtoo39Z ~]# redis-cli -a abc123321 -p 8380 cluster nodes
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
49b7536adea222b1347d0d825543be126d8faf04** ****172.31.195.154:8380@16380** myself,master - 0 0 1 connected
e486ba1749c3e581a81fcc842b0e57887be55740 **172.31.195.154:8381@16381** master - 0 1756282662905 2 connected
```

```
1. 我们发现，显式指定总线端口后，连接成功，因此我们可以确认，之前Bitnami创建指令没有完成，**本质上是没有强制指定对应端口，从而采用默认的端口**。导致了后续一系列的错误。
2. 我们查看对应的配置文件，发现确实是有这样的问题
```yaml
```

REDIS_NODES: 172.31.195.154:8380,172.31.195.154:8381,172.31.195.154:8382,172.31.195.154:8383,172.31.195.154:8384,172.31.195.154:8385

```
	1. Bitnami启动的时候，会自动执行`--cluster create`命令，会读取REDIS_NODES列表，也就是说，其本质上的命令是这样的
	```shell
redis-cli --cluster create \
  172.31.195.154:8380 \
  172.31.195.154:8381 \
  172.31.195.154:8382 \
  172.31.195.154:8383 \
  172.31.195.154:8384 \
  172.31.195.154:8385 \
  --cluster-replicas 1 \
  -a abc123321 \
  --cluster-yes
```

7. 我们进行修改，显式指定总线端口
   ```yaml
   ```

REDIS_NODES=172.31.195.154:8380@16830,172.31.195.154:8381@16831,172.31.195.154:8382@16832,172.31.195.154:8383@16833,172.31.195.154:8384@16834,172.31.195.154:8385@16835

```
	1. 我们再次启动试验，其对应的指令是
	```yaml
redis-cli --cluster create \
  172.31.195.154:8380@16830 \
  172.31.195.154:8381@16831 \
  172.31.195.154:8382@16832 \
  172.31.195.154:8383@16833 \
  172.31.195.154:8384@16834 \
  172.31.195.154:8385@16835 \
  --cluster-replicas 1 \
  -a abc123321 \
  --cluster-yes
```

8. 我们随后进行了多次测试，发现 Bitnami 并不支持 172.31.195.154:8380@16830 这类格式，经过多次调整，我们最终去除了密码等配置，在保持极简配置的前提下，实现了集群的基本部署
   ```yaml
   ```

version: "2"

services:
redis-cluster-node-0:
image: docker.io/bitnami/redis-cluster:8.2.1
ports:
- 9001:9001
- 19001:19001
restart: always
environment:
- 'REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154'
- 'REDIS_PORT_NUMBER=9001'
- 'REDIS_CLUSTER_DYNAMIC_IPS=no'
- 'ALLOW_EMPTY_PASSWORD=yes'
- 'REDIS_NODES=172.31.195.154:9001 172.31.195.154:9002 172.31.195.154:9003 172.31.195.154:9004 172.31.195.154:9005 172.31.195.154:9006'

redis-cluster-node-1:
image: docker.io/bitnami/redis-cluster:8.2.1
ports:
- 9002:9002
- 19002:19002
restart: always
environment:
- 'REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154'
- 'REDIS_PORT_NUMBER=9002'
- 'REDIS_CLUSTER_DYNAMIC_IPS=no'
- 'ALLOW_EMPTY_PASSWORD=yes'
- 'REDIS_NODES=172.31.195.154:9001 172.31.195.154:9002 172.31.195.154:9003 172.31.195.154:9004 172.31.195.154:9005 172.31.195.154:9006'

redis-cluster-node-2:
image: docker.io/bitnami/redis-cluster:8.2.1
ports:
- 9003:9003
- 19003:19003
restart: always
environment:
- 'REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154'
- 'REDIS_PORT_NUMBER=9003'
- 'REDIS_CLUSTER_DYNAMIC_IPS=no'
- 'ALLOW_EMPTY_PASSWORD=yes'
- 'REDIS_NODES=172.31.195.154:9001 172.31.195.154:9002 172.31.195.154:9003 172.31.195.154:9004 172.31.195.154:9005 172.31.195.154:9006'

redis-cluster-node-3:
image: docker.io/bitnami/redis-cluster:8.2.1
ports:
- 9004:9004
- 19004:19004
restart: always
environment:
- 'REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154'
- 'REDIS_PORT_NUMBER=9004'
- 'REDIS_CLUSTER_DYNAMIC_IPS=no'
- 'ALLOW_EMPTY_PASSWORD=yes'
- 'REDIS_NODES=172.31.195.154:9001 172.31.195.154:9002 172.31.195.154:9003 172.31.195.154:9004 172.31.195.154:9005 172.31.195.154:9006'

redis-cluster-node-4:
image: docker.io/bitnami/redis-cluster:8.2.1
ports:
- 9005:9005
- 19005:19005
restart: always
environment:
- 'REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154'
- 'REDIS_PORT_NUMBER=9005'
- 'REDIS_CLUSTER_DYNAMIC_IPS=no'
- 'ALLOW_EMPTY_PASSWORD=yes'
- 'REDIS_NODES=172.31.195.154:9001 172.31.195.154:9002 172.31.195.154:9003 172.31.195.154:9004 172.31.195.154:9005 172.31.195.154:9006'

redis-cluster-node-5:
image: docker.io/bitnami/redis-cluster:8.2.1
ports:
- 9006:9006
- 19006:19006
restart: always
depends_on:
- redis-cluster-node-0
- redis-cluster-node-1
- redis-cluster-node-2
- redis-cluster-node-3
- redis-cluster-node-4
environment:
- 'REDIS_CLUSTER_ANNOUNCE_IP=172.31.195.154'
- 'REDIS_PORT_NUMBER=9006'
- 'REDIS_CLUSTER_DYNAMIC_IPS=no'
- 'ALLOW_EMPTY_PASSWORD=yes'
- 'REDIS_CLUSTER_REPLICAS=1'
- 'REDIS_CLUSTER_CREATOR=yes'
- 'REDIS_NODES=172.31.195.154:9001 172.31.195.154:9002 172.31.195.154:9003 172.31.195.154:9004 172.31.195.154:9005 172.31.195.154:9006'

```

### 总结

Redis本身集成了很多提升性能（主从复制/Cluster集群）、 可靠性（哨兵集群）的方式，根据不同的业务场景需求，进行合理选择即可。

1. 正常情况下
	1. 纯缓存场景，Redis单机+RDB
	2. 有持久化需求，Redis单机+AOF持久化
	3. 有持久化需求，且对恢复数据速度有需求，Redis单机+RDB、AOF混合持久化

2. 对读性能有更高需求/可靠性保障，选择Redis主从复制+哨兵集群。

3. 对写性能有更高需求/突破单机内存限制，选择Redis Cluster集群。

## 实战场景与应用案例

### 介绍

Redis作为内存型数据库，其可以做到的不仅仅是缓存数据。还有诸如分布式锁、布隆过滤器等高阶实战技巧，

这一章节将围绕分布式锁、布隆过滤器等多种场景进行讲解，并提供可以直接落地的代码示例。

### 分布式锁

#### 介绍

并发场景下，多线程会针对同一个资源进行写操作，这就会涉及到资源竞争问题。如果不进行限制，单资源会被反复修改，导致数据错乱等严重的后果。而针对于单体单机部署应用，JVM内部提供的锁可以为资源加锁，从而让多线程串行执行，保证数据可靠性。

但是当场景涉及到跨实例，例如单体多机部署，JVM内部锁就仅限于当前单机应用，因此我们需要一个分布式锁，实现跨实例加锁、释放锁等操作。

而Redis的`setnx`命令，就符合当前的场景（如果存在，就返回false，如果不存在，则设置值并返回true），换成业务场景下，就是"该资源已被持有(已设置值)，则拒绝请求（返回false），该资源未被持有（没有设置值），则允许请求（返回true）"。

#### 简单集成

我们利用该指令的特性，在SpringBoot即可简单集成获取锁、释放锁等操作。

- 接口提供

```java
_/**_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/28_
_ */_
public interface _LockProvider _{

    _/**_
_     * 尝试获取分布式锁_
_     *_
_     * _**@param **_key 锁的键名，用于唯一标识一把锁_
_     * _**@param **_value 锁的值，通常为请求标识，用于验证锁的拥有者_
_     * _**@param **_expire 锁的过期时间（秒），防止死锁_
_     * _**@return **_true-获取锁成功，false-获取锁失败_
_     */_
_    _boolean tryLock(String key, String value, long expire);

    _/**_
_     * 释放分布式锁_
_     *_
_     * _**@param **_key 锁的键名_
_     * _**@param **_value 锁的值，用于验证锁的拥有者_
_     * _**@return **_true-释放锁成功，false-释放锁失败_
_     */_
_    _boolean releaseLock(String key, String value);
}
```

- 接口实现

```java
_/**_
_ * Redis分布式锁提供者实现类_
_ * _
_ * 基于Redis的SETNX命令实现分布式锁机制，提供获取锁和释放锁的功能。_
_ * 锁的过期时间可配置，防止死锁情况发生。_
_ * _
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/28_
_ */_
@Slf4j
@Component
@RequiredArgsConstructor
public class LockRedisProvider implements _LockProvider _{
    
    _/**_
_     * Redis操作模板类，用于执行Redis命令_
_     */_
_    _private final StringRedisTemplate redisTemplate;
    
    _/**_
_     * 尝试获取分布式锁_
_     * _
_     * 使用Redis的SETNX命令（setIfAbsent）实现加锁操作，_
_     * 只有当key不存在时才能设置成功，从而保证锁的互斥性。_
_     * _
_     * _**@param **_key 锁的键名，用于唯一标识一把锁_
_     * _**@param **_value 锁的值，通常为请求标识，用于验证锁的拥有者_
_     * _**@param **_expire 锁的过期时间（秒），防止死锁_
_     * _**@return **_true-获取锁成功，false-获取锁失败_
_     */_
_    _@Override
    public boolean tryLock(String key, String value, long expire) {
        try {
            _log_.debug("尝试获取锁: key={}, value={}, expire={}s", key, value, expire);
            _// 使用SETNX命令尝试设置键值对，如果key不存在则设置成功返回true_
_            _Boolean tryLock = redisTemplate.opsForValue().setIfAbsent(key, value, expire, _TimeUnit_._SECONDS_);

            _// 判断是否成功获取锁_
_            _boolean isLocked = Boolean._TRUE_.equals(tryLock);
            if (isLocked) {
                _log_.debug("获取锁成功: key={}, value={}, expire={}s", _key_, _value_, _expire_);
            } else {
                _log_.debug("获取锁失败: key={}, value={}, expire={}s", _key_, _value_, _expire_);
            }
            return isLocked;
        } catch (Exception _e_) {
            _log_.error("获取锁失败: key={}, value={}, expire={}s", key, value, expire, e);
            return false;
        }
    }
    
    _/**_
_     * 释放分布式锁_
_     * _
_     * 在释放锁之前会验证当前锁的值是否与请求标识一致，_
_     * 防止误释放其他请求持有的锁。_
_     * _
_     * _**@param **_key 锁的键名_
_     * _**@param **_value 锁的值，用于验证锁的拥有者_
_     * _**@return **_true-释放锁成功，false-释放锁失败_
_     */_
_    _@Override
    public boolean releaseLock(String key, String value) {
        try {
            _log_.debug("尝试释放锁: key={}, value={}", key, value);
            _// 获取当前锁的值_
_            _String currentValue = redisTemplate.opsForValue().get(key);
            _// 验证请求标识与锁的值是否一致_
_            _if (value.equals(currentValue)) {
                _log_.debug("释放锁成功: key={}, value={}", key, value);
                _// 删除键以释放锁_
_                _redisTemplate.delete(key);
                return true;
            }
            _log_.debug("释放锁失败: key={}, value={}", key, value);
            return false;
        } catch (Exception e) {
            _log_.error("释放锁失败: key={}, value={}", key, value, e);
            return false;
        }
    }
}
```

#### Redisson 客户端简单集成

我们在 6.2.2 中进行了简单的手动实现，适合在简单的环境中使用，但是无法实现锁自动续期、自动尝试获取锁等高级特性，而 Redisson 客户端则为我们集成了这些高级特性，我们只需要集成依赖并进行配置，即可直接使用。其底层依旧是基于 Redis 进行实现。

- 依赖集成

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>3.23.4</version>
</dependency>
```

- 配置项配置

```java
_/**_
_ * Redisson配置类，用于连接到Redis_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/9_
_ */_
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedissonConfig {
    _/**_
_     * Redis主机地址_
_     */_
_    _private String host;
    _/**_
_     * Redis端口号_
_     */_
_    _private String port;

    _/**_
_     * Redis数据库索引_
_     */_
_    _private int database;

    _/**_
_     * 创建Redisson配置对象_
_     *_
_     * _**@return **_Redisson配置对象_
_     */_
_    _@Bean
    public Config redissonClientConfig() {
        String address = "redis://" + host + ":" + port;

        Config config = new Config();
        config.useSingleServer().setAddress(address);
        config.useSingleServer().setDatabase(database);
        _log_.info("Redisson连接地址：{}", address);
        _log_.info("Redisson连接数据库：{}", database);
        return config;
    }

    _/**_
_     * 创建Redisson客户端对象_
_     *_
_     * _**@return **_Redisson客户端对象_
_     */_
_    _@Bean
    public _RedissonClient _redissonClient() {
        _RedissonClient _redissonClient = Redisson._create_(redissonClientConfig());
        _log_.info("Redisson连接成功！");
        return redissonClient;
    }
}
```

- 接口集成

```java
_/**_
_ * 分布式锁操作接口提供，专注于高级功能提供_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/9_
_ */_
public interface _DistributedLockProvider _{

    _/**_
_     * 尝试获取锁_
_     * _**@param **_key 锁标识_
_     * _**@param **_waitTime 最大等待时间(ms)_
_     * _**@param **_holdTime 锁持有时间(ms)_
_     * _**@return **_是否获锁成功_
_     */_
_    _boolean tryLock(String key, long waitTime, long holdTime) throws InterruptedException;

    _/**_
_     * 释放锁_
_     * _**@param **_key 锁标识_
_     */_
_    _void unlock(String key);
}
```

- 实现类提供

```java
_/**_
_ * 分布式锁操作，基于Redisson底层实现类_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/9_
_ */_
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockRedissonProvider implements _DistributedLockProvider _{

    private final _RedissonClient _redissonClient;

    _/**_
_     * 尝试获取分布式锁_
_     *_
_     * _**@param **_key      锁标识，用于唯一标识一个锁资源_
_     * _**@param **_waitTime 获取锁的最大等待时间，超过该时间则放弃获取锁_
_     * _**@param **_holdTime 锁的持有时间，即锁自动释放的时间_
_     * _**@return **_是否成功获取锁，true表示成功，false表示失败_
_     * _**@throws **_InterruptedException 当线程在等待锁的过程中被中断时抛出_
_     */_
_    _@Override
    public boolean tryLock(String key, long waitTime, long holdTime) throws InterruptedException {
        _// 通过Redisson客户端获取指定key的可重入锁实例_
_        RLock _lock = redissonClient.getLock(key);

        _log_.debug("尝试获取锁 key={}, holdTime={}ms", key, holdTime);
        boolean tryLock = false;
        try {
            _// 尝试获取锁，最多等待waitTime毫秒，持有锁holdTime毫秒后自动释放_
_            _tryLock = lock.tryLock(waitTime, holdTime, _TimeUnit_._MILLISECONDS_);
        } catch (InterruptedException e) {
            _// 恢复中断状态，以便调用栈上层能正确处理中断信号_
_            _Thread._currentThread_().interrupt();
            _log_.error("获取锁出现异常! key={}", key, e);
            _// 包装为运行时异常抛出，避免强制调用方处理检查型异常_
_            _throw new IllegalStateException("获取锁失败: " + e.getMessage(), e);
        }

        if (!tryLock) {
            _log_.warn("获取锁超时或失败！key={}", key);
        } else {
            _log_.debug("获取锁成功！key={}, holdTime={}ms", key, holdTime);
        }
        return tryLock;
    }

    _/**_
_     * 安全地释放分布式锁_
_     * 只有当前持有锁的线程才能释放锁，防止误释放其他线程持有的锁_
_     *_
_     * _**@param **_key 锁标识，用于唯一标识要释放的锁资源_
_     */_
_    _@Override
    public void unlock(String key) {
        _log_.debug("准备释放锁, key={}", key);
        _// 获取与key关联的锁实例_
_        RLock _lock = redissonClient.getLock(key);
        _// 检查锁是否被任何线程持有，并且是否由当前线程持有_
_        _if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
            _log_.debug("成功释放锁！key={}", key);
        } else {
            _log_.warn("未持有该锁或锁已释放，无需执行解锁操作。key={}", key);
        }
    }
}
```

### 布隆过滤器

#### 介绍

布隆过滤器是**空间利用率极高的概率型数据结构**，用于判断一个元素**是否可能存在**于该集合中，或者**一定不存在**于该集合中。

其底层采用**位数组存储**，本质上是二进制数据结构。并通过**多个哈希函数**，将一个元素进行多次计算，计算出多个位置，并在**多个位置同时变为 1**。

当查询该元素时候，进行相同的哈希操作，对多个位置进行逐一验证，当有一个位置为 0，说明一定不存在。而全部为 1，则**可能存在（因为哈希冲突）**。

该特性，适合于允许少量误判、性能、空间使用敏感的业务场景。适合垃圾邮件过滤、缓存穿透防护、大规模去重。利用很小的空间，过滤掉"不可能存在的数据"。

#### 简单集成

Redis 原生并不支持布隆过滤器，需要额外安装插件，官方推荐安装 Redis Bloom，启动时候添加该插件即可使用该功能。

1. 下载地址：[https://github.com/RedisBloom/RedisBloom/releases](https://github.com/RedisBloom/RedisBloom/releases)
2. 编译插件（会生成 `redisbloom.so` 文件）
3. 启动时候指定插件地址：`redis-server --loadmodule /path/to/redisbloom.so`
4. 注意：Windows 原生不支持，推荐使用 docker 等容器化在部署的时候依赖预装镜像

```bash
docker run -p 6379:6379 redislabs/rebloom
```

#### Redisson 客户端简单集成

1. 接口提供

```java
_/**_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/28_
_ */_
public interface _BloomFilterProvider _{
    
    _/**_
_     * 向布隆过滤器中添加单个元素_
_     * _
_     * _**@param **_key 布隆过滤器的键名_
_     * _**@param **_value 要添加的值_
_     * _**@return **_添加成功返回true，失败返回false_
_     */_
_    _boolean save(String key, String _value_);
    
    _/**_
_     * 向布隆过滤器中批量添加元素_
_     * _
_     * _**@param **_key 布隆过滤器的键名_
_     * _**@param **_values 要添加的值集合_
_     * _**@return **_添加成功返回true，失败返回false_
_     */_
_    _boolean save(String _key_, _Collection_<String> _values_);
    
    _/**_
_     * 判断布隆过滤器中是否存在指定元素_
_     * _
_     * _**@param **_key 布隆过滤器的键名_
_     * _**@param **_value 要查询的值_
_     * _**@return **_存在返回true，不存在返回false_
_     */_
_    _boolean contains(String _key_, String _value_);
    
    _/**_
_     * 判断布隆过滤器中是否存在指定的多个元素_
_     * _
_     * _**@param **_key 布隆过滤器的键名_
_     * _**@param **_values 要查询的值集合_
_     * _**@return **_所有元素都存在返回true，否则返回false_
_     */_
_    _boolean contains(String _key_, _Collection_<String> _values_);
}
```

1. 实现类提供

```java
_/**_
_ * Redisson布隆过滤器实现类_
_ * 提供基于Redisson的布隆过滤器操作，包括单个和批量的数据存储与查询功能_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/28_
_ */_
@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterRedissonProvider implements _BloomFilterProvider _{
    
    private final _RedissonClient _redissonClient;
    
    _/**_
_     * 向布隆过滤器中添加单个元素_
_     * _
_     * _**@param **_key 布隆过滤器的键名_
_     * _**@param **_value 要添加的值_
_     * _**@return **_添加成功返回true，失败返回false_
_     */_
_    _@Override
    public boolean save(String key, String value) {
        try {
            _log_.debug("向布隆过滤器中存储数据: key={}, value={}", key, value);
            boolean isSaved = redissonClient.getBloomFilter(key).add(value);
            if (!isSaved) {
                _log_.error("保存数据失败! key={}, value={}", key, value);
            } else {
                _log_.debug("保存数据成功! key={}, value={}", key, value);
            }
            return isSaved;
        } catch (Exception e) {
            _log_.error("保存数据出现异常！key={}, value={}", key, value, e);
            return false;
        }
    }
    
    _/**_
_     * 向布隆过滤器中批量添加元素_
_     * _
_     * _**@param **_key 布隆过滤器的键名_
_     * _**@param **_values 要添加的值集合_
_     * _**@return **_添加成功返回true，失败返回false_
_     */_
_    _@Override
    public boolean save(String _key_, _Collection_<String> values) {
        try {
            _log_.debug("向布隆过滤器中批量存储数据: key={}, values={}", key, values);
            boolean isSaved = redissonClient.getBloomFilter(key).add(values);
            if (!isSaved) {
                _log_.error("批量保存数据失败! key={}, values={}", _key_, _values_);
            } else {
                _log_.debug("批量保存数据成功! key={}, values={}", _key_, _values_);
            }
            return isSaved;
        } catch (Exception e) {
            _log_.error("批量保存数据出现异常！key={}, values={}", key, values, e);
            return false;
        }
    }
    
    _/**_
_     * 判断布隆过滤器中是否存在指定元素_
_     * _
_     * _**@param **_key 布隆过滤器的键名_
_     * _**@param **_value 要查询的值_
_     * _**@return **_存在返回true，不存在返回false_
_     */_
_    _@Override
    public boolean contains(String key, String value) {
        try {
            _log_.debug("从布隆过滤器中查询数据: key={}, value={}", key, value);
            boolean isExist = redissonClient.getBloomFilter(_key_).contains(_value_);
            if (!isExist) {
                _log_.error("查询数据失败! key={}, value={}", key, value);
            } else {
                _log_.debug("查询数据成功! key={}, value={}", key, value);
            }
            return isExist;
        } catch (Exception e) {
            _log_.error("查询数据出现异常！key={}, value={}", _key_, _value_, _e_);
            return false; 
        }
    }
    
    _/**_
_     * 判断布隆过滤器中是否存在指定的多个元素_
_     * _
_     * _**@param **_key 布隆过滤器的键名_
_     * _**@param **_values 要查询的值集合_
_     * _**@return **_所有元素都存在返回true，否则返回false_
_     */_
_    _@Override
    public boolean contains(String _key_, _Collection_<String> values) {
        try {
            _log_.debug("从布隆过滤器中批量查询数据: key={}, values={}", _key_, _values_);
            boolean isExist = redissonClient.getBloomFilter(_key_).contains(_values_);
            if (!isExist) {
                _log_.error("批量查询数据失败! key={}, values={}", key, values);
            } else {
                _log_.debug("批量查询数据成功! key={}, values={}", key, _values_);
            }
            return isExist;
        } catch (Exception e) {
            _log_.error("批量查询数据出现异常！key={}, values={}", key, values, e);
            return false;
        }
    }

    _/**_
_     * 查询布隆过滤器中元素数量_
_     *_
_     * _**@param **_key 布隆过滤器的键名_
_     * _**@return **_元素数量_
_     */_
_    _public long querySize(String _key_) {
        try {
            _log_.debug("查询布隆过滤器中元素数量: key={}", key);
            long count = redissonClient.getBloomFilter(key).count();
            _log_.debug("查询布隆过滤器中元素数量成功: key={}, count={}", key, count);
            return count;
        } catch (Exception e) {
            _log_.error("查询布隆过滤器中元素数量异常！: key={}", key, e);
            return -1;
        }
    }
}
```

#### 注意

1. Redisson 底层也是基于 Redis 客户端进行实现，因此使用布隆过滤器时候，需要安装布隆过滤器插件，否则运行时候会报错
2. 布隆过滤器本身是概率模型，并非百分百准确，但是对于判断不存在的业务场景已是足够。

### HyperLogLog

#### 介绍

HyperLogLog 是一种用于**基数估计**（即**统计不重复元素数量**）的概率算法，特点是**占用内存极小**（如 12KB 可统计上亿数据），但存在约 **0.81%** 的误差

1. 哈希分桶：数据通过哈希函数映射到多个桶（`register`）。
2. 记录极值：每个桶只保留哈希值的最长前导零位数。
3. 概率估算：通过调和均值公式计算基数。

#### 简单集成

1. 实现接口提供

```java
_/**_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/28_
_ */_
public interface _HyperLogLogProvider _{
    _/**_
_     * 向指定的 HyperLogLog 中添加元素_
_     *_
_     * _**@param **_key   HyperLogLog 的键名_
_     * _**@param **_value 要添加的元素值_
_     * _**@return **_如果至少有一个新元素被添加则返回 true，否则返回 false_
_     */_
_    _boolean save(String key, String value);

    _/**_
_     * 获取指定 HyperLogLog 中不重复元素的估计数量_
_     *_
_     * _**@param **_key HyperLogLog 的键名_
_     * _**@return **_不重复元素的估计数量_
_     */_
_    _long querySize(String key);
}
```

1. 实现类提供

```typescript
_/**_
_ * Redis HyperLogLog 数据结构操作实现类_
_ * _
_ * HyperLogLog 是一种概率数据结构，用于基数统计（即统计不重复元素的个数）。_
_ * 它的优点是内存占用固定且很小（通常12K左右），但存在一定的误差率（约0.81%）。_
_ * _
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/28_
_ */_
@Slf4j
@Component
@RequiredArgsConstructor
public class HyperLogLogRedisProvider implements _HyperLogLogProvider _{

    private final StringRedisTemplate stringRedisTemplate;

    _/**_
_     * 向指定的 HyperLogLog 中添加元素_
_     * _
_     * 使用 Redis 的 PFADD 命令实现，该命令将元素添加到 HyperLogLog 数据结构中。_
_     * 如果元素已经存在，则不会增加计数；如果添加了新元素，则返回 true。_
_     * _
_     * _**@param **_key   HyperLogLog 的键名_
_     * _**@param **_value 要添加的元素值_
_     * _**@return **_如果至少有一个新元素被添加则返回 true，否则返回 false_
_     */_
_    _@Override
    public boolean save(String _key_, String _value_) {
        Boolean result = stringRedisTemplate.opsForValue().getOperations().execute((_RedisCallback_<Boolean>) _connection _-> {
            Object executeResult = _connection_.execute("PFADD", key.getBytes(), value.getBytes());
            _// PFADD 命令返回 1 表示至少有一个元素被添加，0 表示所有元素都已存在_
_            // executeResult 通常是 Long 类型_
_            _if (executeResult instanceof Long _longResult_) {
                return _longResult _> 0;
            }
            return false;
        });
        _log_.debug("保存 HyperLogLog 成功 key={}, value={}, result={}", key, value, result);
        return result != null && result;
    }

    _/**_
_     * 获取指定 HyperLogLog 中不重复元素的估计数量_
_     * _
_     * 使用 Redis 的 PFCOUNT 命令实现，该命令返回 HyperLogLog 中不重复元素的近似数量。_
_     * 注意：由于是概率算法，返回值可能存在约0.81%的误差。_
_     * _
_     * _**@param **_key HyperLogLog 的键名_
_     * _**@return **_不重复元素的估计数量，如果发生异常则返回 0_
_     */_
_    _@Override
    public long querySize(String _key_) {
        try {
            Long result = stringRedisTemplate.opsForValue().getOperations().execute((_RedisCallback_<Long>) _connection _-> {
                Object executeResult = _connection_.execute("PFCOUNT", key.getBytes());
                _// PFCOUNT 命令返回 HyperLogLog 的元素数量_
_                _if (executeResult instanceof Long _longResult_) {
                    return _longResult_;
                }
                return 0L;
            });
            _log_.debug("查询 HyperLogLog 元素数量成功 key={}, result={}", _key_, result);
            return Objects._isNull_(result) ? 0L : result;
        } catch (Exception _e_) {
            _log_.error("查询 HyperLogLog 元素数量异常! key={}", _key_, _e_);
            return 0L;
        }
    }
}
```

1. Redisson 也有对应的集成，可以直接引用，这里不再进行过多封装。

## 接口实现类提供

### RedisKey 操作封装

```java
_/**_
_ * Redis键枚举类_
_ * 用于定义系统中所有Redis键的前缀和描述信息_
_ * 可以根据业务进行扩充、添加_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/23_
_ */_
@Getter
@AllArgsConstructor
public enum _RedisKey _{

    _/**_
_     * 用户模块根键_
_     */_
_    USER_("user:", "用户模块相关"),

    _/**_
_     * 用户信息键_
_     * 格式: user:info:{userId}_
_     */_
_    USER_INFO_("user:info:", "用户信息"),

    _/**_
_     * 用户token键_
_     * 格式: user:token:{tokenId}_
_     */_
_    USER_TOKEN_("user:token:", "用户token"),

    _/**_
_     * 用户角色键_
_     * 格式: user:role:{userId}_
_     */_
_    USER_ROLE_("user:role:", "用户角色"),

    _/**_
_     * 用户权限键_
_     * 格式: user:permission:{userId}_
_     */_
_    USER_PERMISSION_("user:permission:", "用户权限");

    _/**_
_     * Redis键前缀_
_     */_
_    _private final String keyPrefix;

    _/**_
_     * 键描述信息_
_     */_
_    _private final String description;

    _/**_
_     * 静态方法：合并多个键段为一个完整的Redis键_
_     *_
_     * _**@param **_keys 键段数组_
_     * _**@return **_合并后的Redis键字符串_
_     */_
_    _public static String ketMerging(String... keys) {
        StringBuilder keyBuilder = new StringBuilder();
        return _keyMerge_(keyBuilder, keys);
    }

    _/**_
_     * 实例方法：将当前枚举的前缀与传入的键段合并为完整Redis键_
_     *_
_     * _**@param **_keys 键段数组_
_     * _**@return **_完整的Redis键字符串_
_     */_
_    _public String keyAssembled(String... keys) {
        StringBuilder keyBuilder = new StringBuilder(this.keyPrefix);
        return _keyMerge_(keyBuilder, _keys_);
    }

    _/**_
_     * 私有工具方法：执行键段合并操作_
_     *_
_     * _**@param **_keyBuilder StringBuilder对象，用于构建键字符串_
_     * _**@param **_keys       键段数组_
_     * _**@return **_合并后的键字符串_
_     */_
_    _private static String keyMerge(StringBuilder keyBuilder, String[] keys) {
        int length = keys.length;
        for (int i = 0; i < length; i++) {
            keyBuilder.append(keys[i]);
            if (i != length - 1) {
                keyBuilder.append(":");
            }
        }
        return keyBuilder.toString();
    }
}
```

### String 类型：接口实现类提供

```java
_/**
 * 基于Redis实现的缓存提供者
 *
__ * _**@author **_王玉涛
__ * _**@version **_1.0
__ * _**@since **_2025/8/8
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StringCacheRedisProvider implements StringCacheProvider {

    /**
     * Redis模板
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * 设置缓存
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 缓存value
     */
    @Override
    public void setString(String key, String value) {
        try {
            checkSize(key, value);
            redisTemplate.opsForValue().set(key, value);
            log.debug("设置缓存成功 key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("直接设置缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 设置缓存, 当key不存在时才设置
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 缓存value
__     * _**@return **_是否设置成功
     */
    @Override
    public Boolean setStringWhenNotExists(String key, String value) {
        try {
            checkSize(key, value);
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
            log.debug("设置缓存成功 key={}, value={}, result={}", key, value, result);
            return result;
        } catch (Exception e) {
            logSetStringError(key, value, e);
            throw e;
        }
    }

    private static void logSetStringError(String key, String value, Exception e) {
        log.error("设置缓存失败 key={}, value={}", key, value, e);
    }

    /**
     * 设置缓存, 当key不存在时才设置, 默认过期时间单位是秒
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 缓存value
__     * _**@return **_是否设置成功
     */
    @Override
    public Boolean setStringWhenNotExists(String key, String value, long expire) {
        try {
            checkSize(key, value);
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
            log.debug("设置缓存成功 key={}, value={}, result={}, expire={}s", key, value, result, expire);
            return result;
        } catch (Exception e) {
            logSetStringError(key, value, e);
            throw e;
        }
    }

    /**
     * 获取并设置缓存, 默认返回旧值
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 缓存value
__     * _**@return **_旧值
     */
    @Override
    public String getAndSetString(String key, String value) {
        try {
            checkSize(key, value);
            String result = redisTemplate.opsForValue().getAndSet(key, value);
            log.debug("获取并设置缓存成功 key={}, value={}, result={}", key, value, result);
            return result;
        } catch (Exception e) {
            logSetStringError(key, value, e);
            throw e;
        }
    }

    /**
     * 检查缓存大小是否超过1MB
__     * _**@param **_key 缓存key
__     * _**@param **_value 缓存value
     */
    private static void checkSize(String key, String value) {
        if (value.length() > 1024 * 1024) {
            log.warn("value大小超过1MB，拒绝写入,key={},value={},size={}", key, value, value.length());
        }
    }

    /**
     * 带过期时间的设置缓存, 默认为秒
     *
__     * _**@param **_key   缓存key
__     * _**@param **_value 缓存value
__     * _**@param **_expire 过期时间
     */
    @Override
    public void setString(String key, String value, long expire) {
        try {
            checkSize(key, value);
            redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
            log.debug("设置带过期时间缓存成功 key={}, value={}, expire={}", key, value, expire);
        } catch (Exception e) {
            log.error("设置带过期时间缓存失败 key={}, value={}, expire={}", key, value, expire, e);
            throw e;
        }
    }

    /**
     * 获取缓存的过期时间
     *
__     * _**@param **_key 缓存key
__     * _**@return **_过期时间
     */
    @Override
    public long ttlKey(String key) {
        try {
            long expire = redisTemplate.getExpire(key);
            log.debug("获取缓存过期时间 key={}, expire={}", key, expire);
            return expire;
        } catch (Exception e) {
            log.error("获取缓存过期时间失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 获取缓存, 如果不存在则返回空字符串
     *
__     * _**@param **_key 缓存key
__     * _**@return **_缓存value
     */
    @Override
    public String getString(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            String result = Objects.isNull(value) ? "" : value;
            log.debug("获取缓存 key={}, value={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("获取缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 自增缓存，无需get/set更加高效
     *
__     * _**@param **_key 缓存key
     */
    @Override
    public void incrString(String key) {
        try {
            redisTemplate.opsForValue().increment(key);
            log.debug("自增缓存 key={}", key);
        } catch (Exception e) {
            log.error("自增缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 自定义量自增缓存，无需get/set更加高效
     *
__     * _**@param **_key 缓存key
__     * _**@param **_value 自增数量
     */
    @Override
    public void incrString(String key, long value) {
        try {
            redisTemplate.opsForValue().increment(key, value);
            log.debug("自定义量自增缓存 key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("自定义量自增缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 自定义量自增缓存，无需get/set更加高效
     *
__     * _**@param **_key 缓存key
__     * _**@param **_value 自增数量
     */
    @Override
    public void incrString(String key, double value) {
        try {
            redisTemplate.opsForValue().increment(key, value);
            log.debug("自定义量自增缓存(double) key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("自定义量自增缓存(double)失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 自减缓存，无需get/set更加高效
     *
__     * _**@param **_key 缓存key
     */
    @Override
    public void decrString(String key) {
        try {
            redisTemplate.opsForValue().decrement(key);
            log.debug("自减缓存 key={}", key);
        } catch (Exception e) {
            log.error("自减缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 自定义量自减缓存，无需get/set更加高效
     *
__     * _**@param **_key 缓存key
__     * _**@param **_value 自减数量
     */
    @Override
    public void decrString(String key, long value) {
        try {
            redisTemplate.opsForValue().decrement(key, value);
            log.debug("自定义量自减缓存 key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("自定义量自减缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 批量设置缓存
     *
__     * _**@param **_map 缓存map
     */
    @Override
    public void batchSetString(Map<String, String> map) {
        try {
            redisTemplate.opsForValue().multiSet(map);
            log.debug("批量设置缓存成功 map={}", map);
        } catch (Exception e) {
            log.error("批量设置缓存失败 map={}", map, e);
            throw e;
        }
    }

    /**
     * 批量设置缓存
     *
__     * _**@param **_keysAndValues 缓存key和value
     */
    @Override
    public void batchSetString(String... keysAndValues) {
        try {
            int length = keysAndValues.length;
            if (length % 2 != 0) {
                throw new IllegalArgumentException("参数长度必须为偶数");
            }
            HashMap<String, String> stringHashMap = new HashMap<>(length / 2);
            for (int i = 0; i < length; i += 2) {
                stringHashMap.put(keysAndValues[i], keysAndValues[i + 1]);
            }
            redisTemplate.opsForValue().multiSet(stringHashMap);
            log.debug("批量设置缓存成功 keysAndValues={}", (Object) keysAndValues);
        } catch (Exception e) {
            log.error("批量设置缓存失败 keysAndValues={}", (Object) keysAndValues, e);
            throw e;
        }
    }

    /**
     * 当缓存不存在时，批量设置缓存
     *
__     * _**@param **_map 缓存map
     */
    @Override
    public void batchSetWhenNotExists(Map<String, String> map) {
        try {
            redisTemplate.opsForValue().multiSetIfAbsent(map);
            log.debug("当缓存不存在，批量设置缓存成功 map={}", map);
        } catch (Exception e) {
            log.error("批量设置缓存失败 map={}", map, e);
            throw e;
        }
    }

    /**
     * 设置缓存的bit位
     *
__     * _**@param **_key 缓存key
__     * _**@param **_offset bit位
__     * _**@param **_value bit值
     */
    @Override
    public void setBitString(String key, long offset, boolean value) {
        try {
            redisTemplate.opsForValue().setBit(key, offset, value);
            log.debug("设置缓存bit位 key={}, offset={}, value={}", key, offset, value);
        } catch (Exception e) {
            log.error("设置缓存bit位失败 key={}, offset={}, value={}", key, offset, value, e);
            throw e;
        }
    }

    /**
     * 获取缓存的bit位
     *
__     * _**@param **_key 缓存key
__     * _**@param **_offset bit位
__     * _**@return **_bit值
     */
    @Override
    public Boolean getBitString(String key, long offset) {
        try {
            Boolean result = redisTemplate.opsForValue().getBit(key, offset);
            log.debug("获取缓存bit位 key={}, offset={}, result={}", key, offset, result);
            return result;
        } catch (Exception e) {
            log.error("获取缓存bit位失败 key={}, offset={}", key, offset, e);
            throw e;
        }
__    }_
    
    _/**_
    _ * 获取缓存的过期时间_
    _ *_
    _ * _**@param **_key 缓存key_
    _ * _**@return **_过期时间_
    _ */_
    @Override
    public Long getExpireTime(String key) {
        Long expire = null;
        try {
            expire = redisTemplate.getExpire(_key_);
            _log_.debug("获取缓存的过期时间 key={}, expire={}s", _key_, expire);
        } catch (Exception _e_) {
            _log_.error("获取缓存的过期时间失败 key={}", _key_, _e_);
        }
        return expire;
    }_

    /**
     * 删除缓存
     *
__     * _**@param **_key 缓存key
     */
    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("删除缓存 key={}", key);
        } catch (Exception e) {
            log.error("删除缓存失败 key={}", key, e);
            throw e;
        }
    }
__}_
```

### Redisson 分布式锁：接口实现类提供

```java
_/**
 * 分布式锁操作，基于Redisson底层实现类
 *
__ * _**@author **_王玉涛
__ * _**@version **_1.0
__ * _**@since **_2025/8/9
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockRedissonProvider implements DistributedLockProvider {

    private final RedissonClient redissonClient;

    /**
     * 尝试获取分布式锁
     *
__     * _**@param **_key      锁标识，用于唯一标识一个锁资源
__     * _**@param **_waitTime 获取锁的最大等待时间，超过该时间则放弃获取锁
__     * _**@param **_holdTime 锁的持有时间，即锁自动释放的时间
__     * _**@return **_是否成功获取锁，true表示成功，false表示失败
__     * _**@throws **_InterruptedException 当线程在等待锁的过程中被中断时抛出
     */
    @Override
    public boolean tryLock(String key, long waitTime, long holdTime) throws InterruptedException {
        // 通过Redisson客户端获取指定key的可重入锁实例
        RLock lock = redissonClient.getLock(key);

        log.debug("尝试获取锁 key={}, holdTime={}ms", key, holdTime);
        boolean tryLock = false;
        try {
            // 尝试获取锁，最多等待waitTime毫秒，持有锁holdTime毫秒后自动释放
            tryLock = lock.tryLock(waitTime, holdTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // 恢复中断状态，以便调用栈上层能正确处理中断信号
            Thread.currentThread().interrupt();
            log.error("获取锁出现异常! key={}", key, e);
            // 包装为运行时异常抛出，避免强制调用方处理检查型异常
            throw new IllegalStateException("获取锁失败: " + e.getMessage(), e);
        }

        if (!tryLock) {
            log.warn("获取锁超时或失败！key={}", key);
        } else {
            log.debug("获取锁成功！key={}, holdTime={}ms", key, holdTime);
        }
        return tryLock;
    }

    /**
     * 安全地释放分布式锁
     * 只有当前持有锁的线程才能释放锁，防止误释放其他线程持有的锁
     *
__     * _**@param **_key 锁标识，用于唯一标识要释放的锁资源
     */
    @Override
    public void unlock(String key) {
        log.debug("准备释放锁, key={}", key);
        // 获取与key关联的锁实例
        RLock lock = redissonClient.getLock(key);
        // 检查锁是否被任何线程持有，并且是否由当前线程持有
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("成功释放锁！key={}", key);
        } else {
            log.warn("未持有该锁或锁已释放，无需执行解锁操作。key={}", key);
        }
    }
__}_
```

### BitMap 类型：接口实现类提供

```java
_/**
__ * _**@author **_王玉涛
__ * _**@version **_1.0
__ * _**@since **_2025/8/23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BitMapCacheRedisProvider implements BitMapCacheProvider {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 设置位图中指定偏移量的值
     *
__     * _**@param **_key    位图的键
__     * _**@param **_offset 偏移量
__     * _**@param **_value  要设置的值（0 或 1）
     */
    @Override
    public void setBit(String key, long offset, boolean value) {
        try {
            stringRedisTemplate.opsForValue().setBit(key, offset, value);
            log.debug("设置位图成功 key={}, offset={}, value={}", key, offset, value);
        } catch (Exception e) {
            log.error("设置位图失败 key={}, offset={}, value={}", key, offset, value, e);
            throw e;
        }
    }

    /**
     * 获取位图中指定偏移量的值
     *
__     * _**@param **_key    位图的键
__     * _**@param **_offset 偏移量
__     * _**@return **_指定偏移量的值
     */
    @Override
    public Boolean getBit(String key, long offset) {
        try {
            Boolean value = stringRedisTemplate.opsForValue().getBit(key, offset);
            log.debug("获取位图值成功 key={}, offset={}, value={}", key, offset, value);
            return value;
        } catch (Exception e) {
            log.error("获取位图值失败 key={}, offset={}", key, offset, e);
            throw e;
        }
    }

    /**
     * 统计位图中被设置为1的位数
     *
__     * _**@param **_key 位图的键
__     * _**@return **_被设置为1的位数
     */
    @Override
    public Long bitCount(String key) {
        try {
            Long count = stringRedisTemplate.execute(connection -> connection.stringCommands().bitCount(key.getBytes()), true);
            log.debug("统计位图中1的个数成功 key={}, count={}", key, count);
            return count;
        } catch (Exception e) {
            log.error("统计位图中1的个数失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 查找位图中第一个被设置为指定值的位的位置
     *
__     * _**@param **_key   位图的键
__     * _**@param **_value 要查找的值（0 或 1）
__     * _**@return **_第一个被设置为指定值的位的位置，如果不存在则返回-1
     */
    @Override
    public Long bitPos(String key, boolean value) {
        try {
            Long pos = stringRedisTemplate.execute(connection -> connection.stringCommands().bitPos(key.getBytes(), value), true);
            log.debug("查找位图中第一个{}的位置成功 key={}, pos={}", value ? 1 : 0, key, pos);
            return pos;
        } catch (Exception e) {
            log.error("查找位图中第一个{}的位置失败 key={}", value ? 1 : 0, key, e);
            throw e;
        }
    }

    /**
     * 对一个或多个位图执行按位操作，并将结果存储到目标位图中
     *
__     * _**@param **_operation 操作类型（AND, OR, XOR, NOT）
__     * _**@param **_destKey   目标位图的键
__     * _**@param **_keys      源位图的键列表
     */
    @Override
    public void bitOp(String operation, String destKey, String... keys) {
        try {
            byte[][] keyBytes = new byte[keys.length][];
            for (int i = 0; i < keys.length; i++) {
                keyBytes[i] = keys[i].getBytes();
            }

            stringRedisTemplate.execute((RedisCallback<Object>) connection ->
                switch (operation.toUpperCase()) {
                    case "AND" ->
                            connection.stringCommands().bitOp(RedisStringCommands.BitOperation.AND, destKey.getBytes(), keyBytes);
                    case "OR" ->
                            connection.stringCommands().bitOp(RedisStringCommands.BitOperation.OR, destKey.getBytes(), keyBytes);
                    case "XOR" ->
                            connection.stringCommands().bitOp(RedisStringCommands.BitOperation.XOR, destKey.getBytes(), keyBytes);
                    case "NOT" -> {
                        if (keys.length != 1) {
                            throw new IllegalArgumentException("NOT操作只能接受一个源键");
                        }
                        yield connection.stringCommands().bitOp(RedisStringCommands.BitOperation.NOT, destKey.getBytes(), keyBytes[0]);
                    }
                    default -> throw new IllegalArgumentException("不支持的操作类型: " + operation);

            }, true);

            log.debug("位图操作成功 operation={}, destKey={}, keys={}", operation, destKey, keys);
        } catch (Exception e) {
            log.error("位图操作失败 operation={}, destKey={}, keys={}", operation, destKey, keys, e);
            throw e;
        }
    }

    /**
     * 获取位图的内存使用量（字节）
     *
__     * _**@param **_key 位图的键
__     * _**@return **_内存使用量（字节）
     */
    @Override
    public Long memoryUsage(String key) {
        try {
            byte[] serialize = stringRedisTemplate.getStringSerializer().serialize(key);
            Long memoryUsage = (Long) stringRedisTemplate.execute((RedisCallback<Object>) connection ->
                    connection.execute("MEMORY USAGE", serialize));

            log.debug("获取位图内存使用量成功 key={}, usage={} bytes", key, memoryUsage);
            return memoryUsage;
        } catch (Exception e) {
            log.error("获取位图内存使用量失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 删除指定的位图键
     *
__     * _**@param **_key 位图的键
     */
    @Override
    public void delete(String key) {
        try {
            stringRedisTemplate.delete(key);
            log.debug("删除位图成功 key={}", key);
        } catch (Exception e) {
            log.error("删除位图失败 key={}", key, e);
            throw e;
        }
    }
__}_
```

### List 类型：所有接口实现类提供

- 缓存实现类提供(注意：已经避免使用被淘汰的方法，转而使用更稳定的方法)

```java
_/**_
_ * 基于Redis实现的缓存提供操作类_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/10_
_ */_
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRedisProvider implements _CacheProvider _{

    private final StringRedisTemplate redisTemplate;

    _/**_
_     * 获取Redis内存信息_
_     * _
_     * _**@return **_Redis内存信息键值对集合，如果获取失败则返回空Map_
_     */_
_    _@Override
    public _Map_<String, String> infoMemory() {
        _log_.debug("开始获取Redis内存信息");
        
        try {
            Properties properties = redisTemplate.execute((_RedisCallback_<Properties>)
                    connection -> connection.serverCommands().info("memory")
            );
            
            if (Objects._isNull_(properties)) {
                _log_.warn("获取Redis内存信息为空");
                return Collections._emptyMap_();
            }
            
            _log_.debug("成功获取Redis内存信息，共{}条记录", properties.size());
            
            return properties.entrySet().stream()
                    .collect(
                            Collectors._toMap_(
                                    property -> (String) property.getKey(),
                                    _property _-> (String) property.getValue()
                            )
                    );
        } catch (Exception e) {
            _log_.error("获取Redis内存信息时发生异常", e);
            return Collections._emptyMap_();
        }
    }
}
```

- 监控专用类提供（针对重点指标进行封装，同时封装监控告警方法）

```java
_/**
 * Redis内存监控指标对象，封装了Redis内存使用的关键指标信息
 * 用于监控和分析Redis实例的内存使用情况，帮助进行性能调优和容量规划
 *
__ * _**@author **_王玉涛
__ * _**@version **_1.0
__ * _**@since **_2025/8/10
 */
@Slf4j
@Data
public class RedisMemoryMetrics implements MemoryMetrics {

    // 风险阈值常量
    private static final double FRAGMENTATION_RISK_THRESHOLD = 15.0;
    private static final double EVICTION_RISK_THRESHOLD = 20.0;
    private static final double MEM_FRAGMENTATION_RATIO_THRESHOLD = 1.5;
    private static final int MEMORY_PERCENT_THRESHOLD = 85;

    /**
     * Redis分配器分配的内存总量(以字节为单位)
     * 该值包括了所有数据、内部开销和碎片等的总和
     */
    private Long usedMemory;

    /**
     * Redis进程使用的物理内存总量(以字节为单位)
     * 包括Redis进程使用的全部物理内存，可能大于used_memory
     */
    private Long usedMemoryRss;

    /**
     * 内存碎片比率
     * 计算公式: used_memory_rss / used_memory
     * 正常情况下应接近1.0，过高表示存在内存碎片问题
     */
    private Double memFragmentationRatio;

    /**
     * Redis配置的最大内存限制(以字节为单位)
     * 当达到该限制时，Redis会根据max memory-policy策略进行数据淘汰
     */
    private Long maxMemory;

    /**
     * 因内存不足而被驱逐的键数量，如果说对应配置没有启动（例如内存满时候驱逐键，这里就没法进行启动，因此需要配置为0）
     * 反映了因内存限制而丢失的数据量，用于评估内存配置是否合理
     */
    private Long evictedKeys;

    /**
     * 数据集使用的内存量(以字节为单位)
     * 计算公式: used_memory - used_memory_startup
     * 表示实际存储数据所占用的内存量，不包括Redis内部开销
     */
    private Long usedMemoryDataset;

    /**
     * Redis启动时使用的内存量(以MB为单位)
     * 启动时Redis使用的内存量，用于计算已使用的内存量
     */
    private Long usedMemoryMb;

    /**
     * Redis配置的最大内存限制(以MB为单位)
     * 配置的Redis最大内存限制，用于计算已使用的内存占比
     */
    private Long maxMemoryMb;

    /**
     * Redis进程使用的物理内存总量(以MB为单位)
     * 测量Redis进程使用的物理内存，用于计算已使用的内存占比
     */
    private Long usedMemoryRssMb;

    /**
     * 数据集使用的内存量(以MB为单位)
     * 测量数据集使用的内存量，用于计算已使用的内存占比
     */
    private Long usedMemoryDatasetMb;

    /**
     * 内存使用率(精确到小数点后两位)
     * 用于风险控制等需要更高精度的场景
     */
    private Double memoryUsageRate;

    /**
     * 内存碎片率风险等级
     * 用于风险评估，值越高风险越大
     */
    private Double fragmentationRiskLevel;

    /**
     * 内存驱逐风险指标
     * 基于驱逐键数量计算的风险值，用于预测内存压力
     */
    private Double evictionRiskIndicator;

    // 使用DecimalFormat替代String.format提高性能
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public RedisMemoryMetrics(Map<String, String> infoMemory) {
        if (infoMemory == null || infoMemory.isEmpty()) {
            log.warn("传入的infoMemory为空，无法初始化Redis内存监控指标");
            return;
        }
        
        // 初始化基础内存指标
        this.usedMemory = parseLongValue(infoMemory.get("used_memory"));
        this.usedMemoryRss = parseLongValue(infoMemory.get("used_memory_rss"));
        this.maxMemory = parseLongValue(infoMemory.get("maxmemory"));
        this.evictedKeys = parseLongValue(
                Objects.isNull(infoMemory.get("evicted_keys")) ? "0" : infoMemory.get("evicted_keys")
        );
        this.usedMemoryDataset = parseLongValue(infoMemory.get("used_memory_dataset"));
        
        // 初始化内存比率指标
        this.memFragmentationRatio = parseDoubleValue(infoMemory.get("mem_fragmentation_ratio"));
        
        // 初始化MB单位的内存指标
        initUsedAndMaxMemory();
    }
    
    /**
     * 安全解析字符串为Long类型
     * 
__     * _**@param **_value 字符串值
__     * _**@return **_解析后的Long值，解析失败返回null
     */
    private Long parseLongValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("解析Long值失败: {}", value, e);
            return null;
        }
    }
    
    /**
     * 安全解析字符串为Double类型
     * 
__     * _**@param **_value 字符串值
__     * _**@return **_解析后的Double值，解析失败返回null
     */
    private Double parseDoubleValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("解析Double值失败: {}", value, e);
            return null;
        }
    }

    /**
     * 初始化已使用内存和最大内存
     */
    private void initUsedAndMaxMemory() {
        if (usedMemoryMb == null && usedMemory != null) {
            usedMemoryMb = usedMemory / (1024 * 1024);
        }
        if (maxMemoryMb == null && maxMemory != null) {
            maxMemoryMb = maxMemory / (1024 * 1024);
        }
        if (usedMemoryRssMb == null && usedMemoryRss != null) {
            usedMemoryRssMb = usedMemoryRss / (1024 * 1024);
        }
        if (usedMemoryDatasetMb == null && usedMemoryDataset != null) {
            usedMemoryDatasetMb = usedMemoryDataset / (1024 * 1024);
        }
    }

    /**
     * 查询已使用内存占比
     * 返回已使用内存的百分比，并打印日志进行监控
     *
__     * _**@return **_已使用内存的百分比
     */
    @Override
    public int queryUsedMemoryPercent() {
        if (usedMemory == null || usedMemory == 0 || maxMemory == null || maxMemory == 0) {
            log.warn("usedMemory或maxMemory为null或0(无限制)，无法计算已使用内存占比");
            return 0;
        }
        int percent = (int) (100L * usedMemory / maxMemory);
        if (percent > MEMORY_PERCENT_THRESHOLD) {
            log.warn("已使用内存占比过高: {}%", percent);
        } else {
            log.info("已使用内存占比正常: {}%", percent);
        }
        return percent;
    }

    /**
     * 查询高精度的内存使用率
     * 返回精确到小数点后两位的内存使用率
     *
__     * _**@return **_高精度内存使用率
     */
    @Override
    public double queryMemoryUsageRate() {
        if (memoryUsageRate != null) {
            return memoryUsageRate;
        }
        
        if (usedMemory == null || maxMemory == null || maxMemory == 0) {
            log.warn("usedMemory或maxMemory为null或0，无法计算内存使用率");
            return 0.0;
        }
        
        memoryUsageRate = Double.parseDouble(DECIMAL_FORMAT.format((double) usedMemory / maxMemory * 100));
        return memoryUsageRate;
    }

    /**
     * 计算内存碎片风险等级
     * 根据内存碎片比率计算风险等级，值越高风险越大
     *
__     * _**@return **_内存碎片风险等级
     */
    @Override
    public double calculateFragmentationRiskLevel() {
        if (fragmentationRiskLevel != null) {
            return fragmentationRiskLevel;
        }
        
        if (memFragmentationRatio == null) {
            log.warn("memFragmentationRatio为null，无法计算碎片风险等级");
            return 0.0;
        }
        // 简单的风险等级计算：碎片比率越高，风险等级越高
        fragmentationRiskLevel = Double.parseDouble(DECIMAL_FORMAT.format(memFragmentationRatio * 10));
        return fragmentationRiskLevel;
    }

    /**
     * 计算内存驱逐风险指标
     * 基于驱逐键数量计算风险值
     *
__     * _**@return **_内存驱逐风险指标
     */
    @Override
    public double calculateEvictionRiskIndicator() {
        if (evictionRiskIndicator != null) {
            return evictionRiskIndicator;
        }
        
        if (evictedKeys == null) {
            log.warn("evictedKeys为null，无法计算驱逐风险指标");
            return 0.0;
        }
        // 简单的风险指标计算：驱逐键数量越多，风险越高
        evictionRiskIndicator = Double.parseDouble(DECIMAL_FORMAT.format(Math.log10(evictedKeys + 1D) * 10));
        return evictionRiskIndicator;
    }

    /**
     * 显示所有指标信息
     */
    @Override
    public void showAll() {
        log.info("已使用内存: {}B, 转化为MB: {}MB", usedMemory, usedMemoryMb);
        log.info("最大可分配内存: {}B, 转化为MB: {}MB", maxMemory, maxMemoryMb);
        log.info("已使用内存占比: {}%", queryUsedMemoryPercent());
        log.info("内存碎片比率: {}", memFragmentationRatio);
        log.info("因内存不足而被驱逐的键数量: {}", evictedKeys);
        log.info("数据集使用的内存量: {}B, 转化为MB: {}MB", usedMemoryDataset, usedMemoryDatasetMb);
        log.info("Redis进程使用的物理内存总量: {}B, 转化为MB: {}MB", usedMemoryRss, usedMemoryRssMb);
        log.info("高精度内存使用率: {}%", queryMemoryUsageRate());
        log.info("内存碎片风险等级: {}", calculateFragmentationRiskLevel());
        log.info("内存驱逐风险指标: {}", calculateEvictionRiskIndicator());
    }

    /**
     * 监控告警
     * 返回Redis内存监控指标的告警信息
     *
__     * _**@return **_告警信息
     */
    @Override
    public String monitorAlerts() {
        // 创建StringBuilder用于收集告警信息
        StringBuilder sb = new StringBuilder();
        
        // 获取各项监控指标值
        int memoryPercent = queryUsedMemoryPercent();
        double fragmentationRisk = calculateFragmentationRiskLevel();
        double evictionRisk = calculateEvictionRiskIndicator();
        
        // 定义告警规则列表，每个规则包含判断条件和告警信息
        List<AlertRule> alertRules = Arrays.asList(
                // 内存使用率过高告警：当内存使用百分比超过阈值时触发
                new AlertRule(() -> memoryPercent > MEMORY_PERCENT_THRESHOLD, "内存使用率过高: " + memoryPercent),
                // 内存碎片风险告警：当内存碎片比率超过阈值时触发
                new AlertRule(() -> memFragmentationRatio > MEM_FRAGMENTATION_RATIO_THRESHOLD, "内存碎片风险: " + memFragmentationRatio),
                // 内存驱逐告警：当存在因内存不足被驱逐的键时触发
                new AlertRule(() -> evictedKeys > 0, "存在因内存不足被驱逐的键数量: " + evictedKeys),
                // 内存碎片风险等级过高告警：当碎片风险等级超过阈值时触发
                new AlertRule(() -> fragmentationRisk > FRAGMENTATION_RISK_THRESHOLD, "内存碎片风险等级过高: " + fragmentationRisk),
                // 内存驱逐风险等级过高告警：当驱逐风险等级超过阈值时触发
                new AlertRule(() -> evictionRisk > EVICTION_RISK_THRESHOLD, "内存驱逐风险等级过高: " + evictionRisk)
        );

        // 遍历告警规则，筛选出满足条件的规则并执行告警信息收集
        alertRules.stream()
                // 筛选触发告警的规则
                .filter(alertRule -> alertRule.getIsAlert().get())
                // 执行告警信息收集
                .forEach(alertRule -> alertRule.showAlert(sb));
        
        // 返回最终的告警信息字符串
        return sb.toString();
    }
__}_
```

- AlertRule 提供

```java
_/**_
_ * 简单规则+message的整合类_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/12_
_ */_
@Data
@AllArgsConstructor
public class AlertRule {
    _/**_
_     * 告警判断条件，通过Supplier函数式接口实现动态判断_
_     * 返回true表示触发告警，false表示不触发告警_
_     */_
_    _private _Supplier_<Boolean> isAlert;

    _/**_
_     * 告警信息内容，当告警条件满足时需要输出的信息_
_     */_
_    _private String message;

    _/**_
_     * 检查并展示告警信息_
_     * 当告警条件满足时，将告警信息追加到StringBuilder中_
_     * 如果StringBuilder中已有内容，则先添加逗号分隔符_
_     * _
_     * _**@param **_sb 用于收集告警信息的StringBuilder对象，不可为null_
_     */_
_    _public void showAlert(StringBuilder sb) {
        if (Boolean._TRUE_.equals(isAlert.get())) {
            sb.append(sb.isEmpty() ? "" : ",");
            sb.append(message);
        }
    }
}
```

- 配合上述测试代码进行集成

```java
_/**_
_ * Redis连接信息测试类_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/10_
_ */_
@Slf4j
@SpringBootTest
class RedisConnectionTest {

    @Resource
    private _CacheProvider _cacheProvider;

    @Test
    void testConnection() {
        _Map_<String, String> stringStringMap = cacheProvider.infoMemory();
        _log_.info("获取到内存信息: {}", stringStringMap);
    }

    @Test
    void testInfoMemory() {
        String monitorAlerts = cacheProvider.getMemoryMetrics().monitorAlerts();

        if (monitorAlerts.isEmpty()) {
            _log_.info("监控指标正常");
        } else {
            _log_.info(monitorAlerts);
        }
    }
}
```

### Stream 类型：接口实现类提供

- 实现类提供

```java
import com.exercise.redisdemo01.core.provider._StreamCacheProvider_;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

_/**_
_ * Redis Stream 消息队列提供者实现类_
_ * 用于操作 Redis Stream 数据结构，支持消息的添加、读取、确认等操作_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/19_
_ */_
@Slf4j
@Component
@RequiredArgsConstructor
public class StreamCacheRedisProvider implements _StreamCacheProvider _{

    private final StringRedisTemplate redisTemplate;

    _/**_
_     * 向指定的 Stream 中添加单个消息_
_     *_
_     * _**@param **_streamName 流名称，不能为空_
_     * _**@param **_key        消息键，不能为空_
_     * _**@param **_value      消息值，不能为空_
_     * _**@throws **_IllegalArgumentException 如果参数为空或无效_
_     * _**@throws **_RuntimeException         如果 Redis 操作失败_
_     */_
_    _@Override
    public void addMessage(String streamName, String key, String value) {
        try {
            _log_.debug("准备向 Stream [{}] 添加消息: key={}, value={}", _streamName_, _key_, _value_);
            RecordId recordId = redisTemplate.opsForStream().add(streamName, _Map_._of_(key, value));
            String messageId = Objects._isNull_(recordId) ? null : recordId.getValue();
            logInfoAddMessage(streamName, messageId);
        } catch (Exception e) {
            _log_.error("向 Stream [{}] 添加消息失败: key={}, value={}", streamName, key, value, e);
            throw new RuntimeException("添加消息到 Stream 失败", e);
        }
    }

    _/**_
_     * 向指定的 Stream 中添加多个键值对消息_
_     *_
_     * _**@param **_streamName 流名称，不能为空_
_     * _**@param **_map        消息键值对集合，不能为空_
_     * _**@throws **_IllegalArgumentException 如果参数为空或无效_
_     * _**@throws **_RuntimeException         如果 Redis 操作失败_
_     */_
_    _@Override
    public void addMessageMap(String streamName, _Map_<String, String> map) {
        try {
            _log_.debug("准备向 Stream [{}] 添加 {} 个消息键值对", streamName, map.size());
            RecordId recordId = redisTemplate.opsForStream().add(streamName, map);
            String messageId = Objects._isNull_(recordId) ? null : recordId.getValue();
            logInfoAddMessage(_streamName_, messageId);
        } catch (Exception e) {
            _log_.error("向 Stream [{}] 添加消息失败，map大小: {}", streamName, map.size(), e);
            throw new RuntimeException("添加消息到 Stream 失败", e);
        }
    }

    _/**_
_     * 添加消息成功后，记录日志信息_
_     *_
_     * _**@param **_streamName 流名称_
_     * _**@param **_messageId  消息ID_
_     */_
_    _private void logInfoAddMessage(String streamName, String messageId) {
        _log_.info("成功向 Stream [{}] 添加消息，消息ID: {}", streamName, messageId);
    }

    _/**_
_     * 向指定的 Stream 中添加多个键值对消息（可变参数形式）_
_     * 参数必须成对出现，即键后必须跟对应的值_
_     *_
_     * _**@param **_streamName 流名称，不能为空_
_     * _**@param **_keyValue   键值对参数，必须为偶数个_
_     * _**@throws **_IllegalArgumentException 如果参数个数不是偶数或参数为空_
_     * _**@throws **_RuntimeException         如果 Redis 操作失败_
_     */_
_    _@Override
    public void addMessages(String streamName, String... keyValue) {
        try {
            int length = keyValue.length;
            if (length % 2 != 0) {
                _log_.error("添加到消息队列的参数个数必须是偶数，当前个数: {}", length);
                throw new IllegalArgumentException("参数个数必须为偶数");
            }

            _log_.debug("准备向 Stream [{}] 添加 {} 个键值对", streamName, length / 2);
            HashMap<String, String> keyValueMap = new HashMap<>();
            for (int i = 0; i < length; i += 2) {
                keyValueMap.put(keyValue[i], keyValue[i + 1]);
            }

            RecordId recordId = redisTemplate.opsForStream().add(streamName, keyValueMap);
            String messageId = Objects._isNull_(recordId) ? null : recordId.getValue();
            _log_.info("成功向 Stream [{}] 添加消息，消息ID: {}，共 {} 个键值对", streamName, messageId, keyValueMap.size());
        } catch (Exception e) {
            _log_.error("向 Stream [{}] 添加消息失败", streamName, e);
            throw new RuntimeException("添加消息到 Stream 失败", e);
        }
    }

    _/**_
_     * 读取指定 Stream 中的所有消息，并按消息ID组织返回_
_     *_
_     * _**@param **_streamName 消息队列名称，不能为空_
_     * _**@param **_clazzK     key类型，用于类型转换_
_     * _**@param **_clazzV     value类型，用于类型转换_
_     * _**@return **_包含所有消息的映射，key为消息ID，value为消息内容映射_
_     * _**@throws **_RuntimeException 如果 Redis 操作失败或类型转换异常_
_     */_
_    _@Override
    public <_K_, _V_> _Map_<String, HashMap<_K_, _V_>> readAll(String streamName, Class<_K_> clazzK, Class<_V_> clazzV) {
        try {
            _log_.debug("开始读取 Stream [{}] 的所有消息", streamName);
            _List_<_MapRecord_<String, Object, Object>> mapRecords = redisTemplate.opsForStream().read(StreamOffset._fromStart_(streamName));
            logReadMessage(streamName, mapRecords);

            _Map_<String, HashMap<_K_, _V_>> messageMap = new HashMap<>();

            for (_MapRecord_<String, Object, Object> mapRecord : mapRecords) {
                String messageId = mapRecord.getId().getValue();
                _Map_<Object, Object> mapRecordValue = mapRecord.getValue();

                if (!mapRecordValue.isEmpty()) {
                    HashMap<_K_, _V_> bodyMap = new HashMap<>();
                    try {
                        mapRecordValue.entrySet().stream()
                                .map(entry -> {
                                    _K _key = clazzK.cast(entry.getKey());
                                    _V _value = clazzV.cast(entry.getValue());
                                    return new _AbstractMap_.SimpleEntry<>(key, value);
                                })
                                .forEach(entry -> bodyMap.put(entry.getKey(), entry.getValue()));

                        messageMap.put(messageId, bodyMap);
                    } catch (ClassCastException e) {
                        _log_.error("类型转换异常，messageId: {}, error: {}", messageId, e.getMessage(), e);
                    }
                }
            }
            _log_.debug("完成读取 Stream [{}] 的所有消息，共 {} 条消息", streamName, messageMap.size());
            return messageMap;
        } catch (Exception e) {
            _log_.error("读取 Stream [{}] 所有消息失败", streamName, e);
            throw new RuntimeException("读取 Stream 消息失败", e);
        }
    }

    _/**_
_     * 读取指定 Stream 中的消息，并转换为指定类型的映射_
_     *_
_     * _**@param **_streamName 流名称，不能为空_
_     * _**@param **_mapRecords 读取的 MapRecord 列表_
_     */_
_    _private void logReadMessage(String _streamName_, _List_<_MapRecord_<String, Object, Object>> _mapRecords_) {
        if (Objects._isNull_(_mapRecords_)) {
            _log_.info("未从 Stream [{}] 读取到消息记录", streamName);
        } else {
            _log_.info("从 Stream [{}] 读取到 {} 条消息记录", _streamName_, _mapRecords_.size());
        }
    }

    _/**_
_     * 根据配置读取 Stream 中的消息_
_     *_
_     * _**@param **_streamName  流名称_
_     * _**@param **_isCount     是否限制读取数量_
_     * _**@param **_count       读取数量上限_
_     * _**@param **_isBlock     是否阻塞读取_
_     * _**@param **_blockTime   阻塞等待时间（毫秒）_
_     * _**@param **_clazzK      key类型_
_     * _**@param **_clazzV      value类型_
_     * _**@return **_转换后的消息列表_
_     */_
_    _private <_K_, _V_> _List_<_Map_<_K_, _V_>> readMap(String _streamName_, boolean _isCount_, int _count_, boolean _isBlock_, long _blockTime_, Class<_K_> clazzK, Class<_V_> clazzV) {
        try {
            _log_.debug("开始读取 Stream [{}] 消息: isCount={}, count={}, isBlock={}, blockTime={}", 
                    _streamName_, _isCount_, _count_, _isBlock_, _blockTime_);

            StreamReadOptions streamReadOptions = _isCount _? StreamReadOptions._empty_().count(_count_) : StreamReadOptions._empty_();
            streamReadOptions = _isBlock _? streamReadOptions.block(Duration._ofMillis_(_blockTime_)) : streamReadOptions;
            StreamOffset<String> stringStreamOffset = StreamOffset._latest_(_streamName_);

            _List_<_MapRecord_<String, Object, Object>> mapRecords = redisTemplate.opsForStream().read(streamReadOptions, stringStreamOffset);
            logReadMessage(_streamName_, mapRecords);

            _List_<_Map_<_K_, _V_>> result = _castMapToList_(_clazzK_, _clazzV_, mapRecords);
            _log_.debug("完成读取 Stream [{}] 消息，转换后共 {} 条", _streamName_, result.size());
            return result;
        } catch (Exception _e_) {
            _log_.error("读取 Stream [{}] 消息失败: isCount={}, count={}, isBlock={}, blockTime={}", 
                    _streamName_, _isCount_, _count_, _isBlock_, _blockTime_, _e_);
            throw new RuntimeException("读取 Stream 消息失败", _e_);
        }
    }

    _/**_
_     * 将 Redis 返回的消息记录转换为指定类型的映射列表_
_     *_
_     * _**@param **_clazzK      key类型_
_     * _**@param **_clazzV      value类型_
_     * _**@param **_mapRecords  Redis 返回的消息记录列表_
_     * _**@return **_转换后的映射列表_
_     */_
_    _private static <_K_, _V_> _List_<_Map_<_K_, _V_>> castMapToList(Class<_K_> _clazzK_, Class<_V_> _clazzV_, _List_<_MapRecord_<String, Object, Object>> _mapRecords_) {
        _List_<_Map_<_K_, _V_>> messageMaps = new ArrayList<>();
        if (Objects._nonNull_(_mapRecords_)) {
            _log_.debug("开始转换 {} 条消息记录", _mapRecords_.size());
            for (_MapRecord_<String, Object, Object> mapRecord : _mapRecords_) {
                mapRecord.getValue().forEach((_key_, _value_) -> {
                    try {
                        HashMap<_K_, _V_> hashMap = new HashMap<>();
                        hashMap.put(clazzK.cast(_key_), clazzV.cast(_value_));
                        messageMaps.add(hashMap);
                    } catch (Exception _e_) {
                        _log_.error("类型转换错误！key类型: {}, value类型: {}, 实际key: {}, 实际value: {}", 
                                clazzK.getName(), clazzV.getName(), _key_, _value_, _e_);
                    }
                });
            }
            _log_.debug("完成转换，共 {} 条消息映射", messageMaps.size());
        }
        return messageMaps;
    }

    _/**_
_     * 读取指定数量的消息_
_     *_
_     * _**@param **_streamName 流名称_
_     * _**@param **_count      读取数量_
_     * _**@param **_clazzK     key类型_
_     * _**@param **_clazzV     value类型_
_     * _**@return **_消息列表_
_     */_
_    _@Override
    public <_K_, _V_> _List_<_Map_<_K_, _V_>> readMapCount(String _streamName_, int _count_, Class<_K_> _clazzK_, Class<_V_> _clazzV_) {
        _log_.debug("调用 readMapCount: streamName={}, count={}", _streamName_, _count_);
        return readMap(_streamName_, true, _count_, false, 0, _clazzK_, _clazzV_);
    }

    _/**_
_     * 阻塞式读取消息_
_     *_
_     * _**@param **_streamName 流名称_
_     * _**@param **_blockTime  阻塞等待时间（毫秒）_
_     * _**@param **_clazzK     key类型_
_     * _**@param **_clazzV     value类型_
_     * _**@return **_消息列表_
_     */_
_    _@Override
    public <_K_, _V_> _List_<_Map_<_K_, _V_>> readMapBlock(String _streamName_, long _blockTime_, Class<_K_> _clazzK_, Class<_V_> _clazzV_) {
        _log_.debug("调用 readMapBlock: streamName={}, blockTime={}", _streamName_, _blockTime_);
        return readMap(_streamName_, false, 0, true, _blockTime_, _clazzK_, _clazzV_);
    }

    _/**_
_     * 读取指定数量的消息并支持阻塞_
_     *_
_     * _**@param **_streamName 流名称_
_     * _**@param **_count      读取数量_
_     * _**@param **_blockTime  阻塞等待时间（毫秒）_
_     * _**@param **_clazzK     key类型_
_     * _**@param **_clazzV     value类型_
_     * _**@return **_消息列表_
_     */_
_    _@Override
    public <_K_, _V_> _List_<_Map_<_K_, _V_>> readMap(String _streamName_, int _count_, long _blockTime_, Class<_K_> _clazzK_, Class<_V_> _clazzV_) {
        _log_.debug("调用 readMap: streamName={}, count={}, blockTime={}", _streamName_, _count_, _blockTime_);
        return readMap(_streamName_, true, _count_, true, _blockTime_, _clazzK_, _clazzV_);
    }

    _/**_
_     * 创建消费者组_
_     *_
_     * _**@param **_streamName 流名称_
_     * _**@param **_groupName  消费者组名称_
_     */_
_    _@Override
    public void createGroup(String _streamName_, String _groupName_) {
        _log_.info("创建消费者组: streamName={}, groupName={}", _streamName_, _groupName_);
        try {
            redisTemplate.opsForStream().createGroup(_streamName_, _groupName_);
            _log_.info("成功创建消费者组: streamName={}, groupName={}", _streamName_, _groupName_);
        } catch (Exception _e_) {
            _log_.error("创建消费者组失败: streamName={}, groupName={}", _streamName_, _groupName_, _e_);
            throw new RuntimeException("创建消费者组失败", _e_);
        }
    }

    _/**_
_     * 从消费者组中读取消息_
_     *_
_     * _**@param **_streamName   流名称_
_     * _**@param **_groupName    消费者组名称_
_     * _**@param **_consumerName 消费者名称_
_     * _**@param **_clazzK       key类型_
_     * _**@param **_clazzV       value类型_
_     * _**@return **_消息映射_
_     */_
_    _@Override
    public <_K_, _V_> _Map_<_K_, _V_> readMessage(String _streamName_, String _groupName_, String _consumerName_, Class<_K_> _clazzK_, Class<_V_> _clazzV_) {
        try {
            _log_.debug("从消费者组读取消息: streamName={}, groupName={}, consumerName={}", _streamName_, _groupName_, _consumerName_);
            StreamReadOptions streamReadOptions = StreamReadOptions._empty_().count(1);
            Consumer consumer = Consumer._from_(_groupName_, _consumerName_);
            StreamOffset<String> streamOffset = StreamOffset._create_(_streamName_, ReadOffset._lastConsumed_());
            _List_<_MapRecord_<String, Object, Object>> mapRecords = redisTemplate.opsForStream().read(consumer, streamReadOptions, streamOffset);
            _log_.info("从消费者组读取到 {} 条消息记录", mapRecords.size());

            _List_<_Map_<_K_, _V_>> mapList = _castMapToList_(_clazzK_, _clazzV_, mapRecords);
            _Map_<_K_, _V_> result = mapList.isEmpty() ? Collections._emptyMap_() : mapList.get(0);
            _log_.debug("完成从消费者组读取消息，返回 {} 条消息", result.size());
            return result;
        } catch (Exception _e_) {
            _log_.error("从消费者组读取消息失败: streamName={}, groupName={}, consumerName={}", _streamName_, _groupName_, _consumerName_, _e_);
            throw new RuntimeException("从消费者组读取消息失败", _e_);
        }
    }

    _/**_
_     * 确认消息已处理_
_     *_
_     * _**@param **_streamName  流名称_
_     * _**@param **_groupName   消费者组名称_
_     * _**@param **_messageIds  消息ID列表_
_     */_
_    _@Override
    public void ackMessage(String _streamName_, String _groupName_, String... _messageIds_) {
        try {
            _log_.debug("确认消息已处理: streamName={}, groupName={}, messageIds={}", _streamName_, _groupName_, _messageIds_);
            redisTemplate.opsForStream().acknowledge(_streamName_, _groupName_, _messageIds_);
            _log_.info("成功确认 {} 条消息已处理: streamName={}, groupName={}", _messageIds_.length, _streamName_, _groupName_);
        } catch (Exception _e_) {
            _log_.error("确认消息已处理失败: streamName={}, groupName={}, messageIds={}", _streamName_, _groupName_, _messageIds_, _e_);
            throw new RuntimeException("确认消息已处理失败", _e_);
        }
    }
}
```

### Set/ZSet 类型：接口实现类提供

- 实现类提供

```java
import com.exercise.redisdemo01.core.provider._SetCacheProvider_;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;

_/**_
_ * Redis Set 集合缓存操作实现类_
_ * 提供对 Redis Set 数据结构的常用操作封装，包括添加、查询、删除、集合运算等_
_ *_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/23_
_ */_
@Slf4j
@Component
@RequiredArgsConstructor
public class SetCacheRedisProvider implements _SetCacheProvider _{

    private final StringRedisTemplate redisTemplate;

    _/**_
_     * 向指定的 Set 集合中添加一个或多个元素_
_     *_
_     * _**@param **_key    Set 集合的键_
_     * _**@param **_values 要添加的元素数组_
_     */_
_    _@Override
    public void set(String key, _List_<String> _values_) {
        try {
            String[] valuesArray = _values_.toArray(new String[0]);
            redisTemplate.opsForSet().add(key, valuesArray);
            _log_.debug("设置缓存成功 key={}, values={}", key, values);
        } catch (Exception e) {
            _log_.error("设置缓存失败 key={}, values={}", key, _values_, e);
            throw e;
        }
    }

    _/**_
_     * 批量设置多个 Set 集合的元素，参数格式为 key1, value1, key2, value2, ..._
_     *_
_     * _**@param **_keyValues 键值对数组，必须为偶数个_
_     * _**@throws **_IllegalArgumentException 当参数个数不是偶数时抛出_
_     */_
_    _@Override
    public void set(String... _keyValues_) {
        try {
            int length = keyValues.length;
            if (length % 2 != 0) {
                _log_.error("缓存键必须为偶数！");
                throw new IllegalArgumentException("参数个数必须为偶数");
            }
            for (int i = 0; i < length; i += 2) {
                redisTemplate.opsForSet().add(keyValues[i], keyValues[i + 1]);
                _log_.debug("设置缓存成功 key={}, value={}", _keyValues_[i], _keyValues_[i + 1]);
            }
        } catch (Exception e) {
            _log_.error("设置缓存失败 keyValues={}", keyValues, e);
            throw e;
        }
    }

    _/**_
_     * 原子性地批量设置多个 Set 集合的元素，使用 Lua 脚本保证操作的原子性_
_     * 参数格式为 key1, value1, key2, value2, ..._
_     *_
_     * _**@param **_keyValues 键值对数组，必须为偶数个_
_     * _**@throws **_IllegalArgumentException 当参数个数不是偶数时抛出_
_     */_
_    _@Override
    public void atomicitySet(String... keyValues) {
        try {
            int length = keyValues.length;
            if (length % 2 != 0) {
                _log_.error("缓存键数目必须为偶数！");
                throw new IllegalArgumentException("参数个数必须为偶数");
            }

            _// 拆分参数：偶数位为集合名，奇数位为值_
_            List_<String> keys = new ArrayList<>();
            _List_<String> values = new ArrayList<>();
            for (int i = 0; i < keyValues.length; i += 2) {
                keys.add(keyValues[i]);
                values.add(keyValues[i + 1]);
            }
            Object[] valuesArray = values.toArray();

            _// 构建 Lua 脚本_
_            _String luaScript = "for i = 1, #KEYS do " +
                    "redis.call('SADD', KEYS[i], ARGV[i]) " +
                    "end";

            _// 执行 Lua 脚本_
_            _redisTemplate.execute(
                    new DefaultRedisScript<>(luaScript, String.class),
                    keys,
                    valuesArray
            );

            _log_.debug("原子性设置缓存成功 keyValues={}", (Object[]) keyValues);
        } catch (Exception e) {
            _log_.error("原子性设置缓存失败 keyValues={}", keyValues, e);
            throw _e_;
        }
    }

    _/**_
_     * 获取指定 Set 集合中的所有元素_
_     *_
_     * _**@param **_key Set 集合的键_
_     * _**@return **_Set 集合中的所有元素_
_     */_
_    _@Override
    public _Set_<String> queryAll(String key) {
        try {
            _Set_<String> values = redisTemplate.opsForSet().members(_key_);
            _log_.debug("查询缓存成功 key={}, values={}", key, values);
            return values;
        } catch (Exception e) {
            _log_.error("查询缓存失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 判断指定元素是否存在于 Set 集合中_
_     *_
_     * _**@param **_key   Set 集合的键_
_     * _**@param **_value 要检查的元素_
_     * _**@return **_如果元素存在返回 true，否则返回 false_
_     */_
_    _@Override
    public boolean isExist(String key, String _value_) {
        try {
            Boolean exists = redisTemplate.opsForSet().isMember(_key_, _value_);
            _log_.debug("查询缓存成功 key={}, value={}, exists={}", _key_, _value_, exists);
            return Boolean._TRUE_.equals(exists);
        } catch (Exception _e_) {
            _log_.error("查询缓存失败 key={}, value={}", _key_, _value_, _e_);
            throw _e_;
        }
    }

    _/**_
_     * 随机弹出并移除 Set 集合中的一个元素_
_     *_
_     * _**@param **_key Set 集合的键_
_     * _**@return **_被弹出的元素，如果集合为空则返回 null_
_     */_
_    _@Override
    public String randomPop(String key) {
        try {
            String value = redisTemplate.opsForSet().pop(key);
            _log_.debug("随机弹出缓存成功 key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            _log_.error("随机弹出缓存失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 获取两个 Set 集合的交集_
_     *_
_     * _**@param **_key      第一个 Set 集合的键_
_     * _**@param **_otherKey 第二个 Set 集合的键_
_     * _**@return **_两个集合的交集_
_     */_
_    _@Override
    public _Set_<String> interSection(String _key_, String otherKey) {
        try {
            _Set_<String> intersect = redisTemplate.opsForSet().intersect(key, otherKey);
            _log_.debug("集合交集成功 key={}, otherKey={}, values={}", key, otherKey, intersect);
            return intersect;
        } catch (Exception e) {
            _log_.error("集合交集失败 key={}, otherKey={}", _key_, _otherKey_, _e_);
            throw e;
        }
    }

    _/**_
_     * 获取多个 Set 集合的交集_
_     *_
_     * _**@param **_keys Set 集合键的集合_
_     * _**@return **_多个集合的交集_
_     */_
_    _@Override
    public _Set_<String> interSection(_Collection_<String> keys) {
        try {
            _Set_<String> intersect = redisTemplate.opsForSet().intersect(keys);
            _log_.debug("集合交集成功 keys={}, values={}", keys, intersect);
            return intersect;
        } catch (Exception e) {
            _log_.error("集合交集失败 keys={}", keys, e);
            throw e;
        }
    }

    _/**_
_     * 获取多个 Set 集合的并集_
_     *_
_     * _**@param **_keys Set 集合键的集合_
_     * _**@return **_多个集合的并集_
_     */_
_    _@Override
    public _Set_<String> unionSection(_Collection_<String> keys) {
        try {
            _Set_<String> union = redisTemplate.opsForSet().union(keys);
            _log_.debug("集合并集成功 keys={}, values={}", keys, union);
            return union;
        } catch (Exception e) {
            _log_.error("集合并集失败 keys={}", keys, e);
            throw e;
        }
    }

    _/**_
_     * 获取两个 Set 集合的并集_
_     *_
_     * _**@param **_key      第一个 Set 集合的键_
_     * _**@param **_otherKey 第二个 Set 集合的键_
_     * _**@return **_两个集合的并集_
_     */_
_    _@Override
    public _Set_<String> unionSection(String _key_, String _otherKey_) {
        try {
            _Set_<String> union = redisTemplate.opsForSet().union(_key_, _otherKey_);
            _log_.debug("集合并集成功 key={}, otherKey={}, values={}", _key_, _otherKey_, union);
            return union;
        } catch (Exception _e_) {
            _log_.error("集合并集失败 key={}, otherKey={}", _key_, _otherKey_, _e_);
            throw e;
        }
    }

    _/**_
_     * 从 Set 集合中移除指定元素_
_     *_
_     * _**@param **_key   Set 集合的键_
_     * _**@param **_value 要移除的元素_
_     */_
_    _@Override
    public void remove(String _key_, String _value_) {
        try {
            redisTemplate.opsForSet().remove(_key_, _value_);
            _log_.debug("删除缓存成功 key={}, value={}", _key_, _value_);
        } catch (Exception _e_) {
            _log_.error("删除缓存失败 key={}, value={}", _key_, _value_, _e_);
            throw _e_;
        }
    }

    _// ZSet 相关方法封装_

_    /**_
_     * 向有序集合中添加元素及其分数_
_     *_
_     * _**@param **_key   有序集合的键_
_     * _**@param **_value 要添加的元素_
_     * _**@param **_score 元素的分数_
_     */_
_    _@Override
    public void zAdd(String key, String value, double score) {
        try {
            redisTemplate.opsForZSet().add(key, value, score);
            _log_.debug("ZSet 添加元素成功 key={}, value={}, score={}", key, value, score);
        } catch (Exception e) {
            _log_.error("ZSet 添加元素失败 key={}, value={}, score={}", key, value, score, e);
            throw e;
        }
    }

    _/**_
_     * 原子性地批量添加有序集合元素，使用 Lua 脚本保证操作的原子性_
_     * 参数格式为 key1, value1, score1, key2, value2, score2, ..._
_     *_
_     * _**@param **_keyValues 键值对和分数数组，必须为3的倍数_
_     * _**@throws **_IllegalArgumentException 当参数个数不是3的倍数时抛出_
_     */_
_    _@Override
    public void zAtomicityAdd(String... _keyValues_) {
        try {
            int length = keyValues.length;
            if (length % 3 != 0) {
                _log_.error("参数个数必须为3的倍数，key, value, score");
                throw new IllegalArgumentException("参数个数必须为3的倍数");
            }

            _// 拆分参数：每三个参数为一组，分别是 key, value, score_
_            List_<String> keys = new ArrayList<>();
            _List_<String> values = new ArrayList<>();
            _List_<String> scores = new ArrayList<>();

            for (int i = 0; i < length; i += 3) {
                keys.add(keyValues[i]);
                values.add(keyValues[i + 1]);
                scores.add(keyValues[i + 2]);
            }

            _// 构建 Lua 脚本，原子性地添加 ZSet 元素_
_            _String luaScript = "for i = 1, #KEYS do " +
                    "redis.call('ZADD', KEYS[i], ARGV[i * 2 - 1], ARGV[i * 2]) " +
                    "end";

            _// 合并 values 和 scores 作为 ARGV 参数传递_
_            List_<String> args = new ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                args.add(scores.get(i));
                args.add(values.get(i));
            }

            Object[] argsArray = args.toArray();
            _// 执行 Lua 脚本_
_            _redisTemplate.execute(
                    new DefaultRedisScript<>(luaScript, String.class),
                    keys,
                    argsArray
            );

            _log_.debug("ZSet 原子性添加元素成功 keyValues={}", (Object[]) _keyValues_);
        } catch (Exception e) {
            _log_.error("ZSet 原子性添加元素失败 keyValues={}", keyValues, e);
            throw e;
        }
    }

    _/**_
_     * 批量添加有序集合元素_
_     * 参数格式为 key1, value1, score1, key2, value2, score2, ..._
_     *_
_     * _**@param **_keyValues 键值对和分数数组，必须为3的倍数_
_     * _**@throws **_IllegalArgumentException 当参数个数不是3的倍数时抛出_
_     */_
_    _@Override
    public void zAdd(String... keyValues) {
        try {
            int length = keyValues.length;
            if (length % 3 != 0) {
                _log_.error("参数个数必须为3的倍数，格式为：key, value, score");
                throw new IllegalArgumentException("参数个数必须为3的倍数");
            }

            for (int i = 0; i < length; i += 3) {
                String key = keyValues[i];
                String value = keyValues[i + 1];
                double score = Double._parseDouble_(keyValues[i + 2]);
                redisTemplate.opsForZSet().add(key, value, score);
                _log_.debug("ZSet添加元素成功 key={}, value={}, score={}", key, value, score);
            }
        } catch (Exception e) {
            _log_.error("ZSet 添加元素失败 keyValues={}", keyValues, e);
            throw e;
        }
    }

    _/**_
_     * 从有序集合中移除一个或多个元素_
_     *_
_     * _**@param **_key    有序集合的键_
_     * _**@param **_values 要移除的元素数组_
_     */_
_    _@Override
    public void zRemove(String key, Object... values) {
        try {
            redisTemplate.opsForZSet().remove(key, values);
            _log_.debug("ZSet 删除元素成功 key={}, values={}", key, values);
        } catch (Exception e) {
            _log_.error("ZSet 删除元素失败 key={}, values={}", key, values, e);
            throw e;
        }
    }

    _/**_
_     * 获取有序集合中指定范围的元素（按分数从小到大）_
_     *_
_     * _**@param **_key   有序集合的键_
_     * _**@param **_start 起始索引_
_     * _**@param **_end   结束索引_
_     * _**@return **_指定范围内的元素集合_
_     */_
_    _@Override
    public _Set_<String> zRange(String key, long start, long end) {
        try {
            _Set_<String> values = redisTemplate.opsForZSet().range(key, start, end);
            _log_.debug("ZSet 范围查询成功 key={}, start={}, end={}, values={}", key, start, end, values);
            return values;
        } catch (Exception e) {
            _log_.error("ZSet 范围查询失败 key={}, start={}, end={}", key, start, end, e);
            throw e;
        }
    }

    _/**_
_     * 获取有序集合中指定范围的元素（按分数从大到小）_
_     *_
_     * _**@param **_key   有序集合的键_
_     * _**@param **_start 起始索引_
_     * _**@param **_end   结束索引_
_     * _**@return **_指定范围内的元素集合_
_     */_
_    _@Override
    public _Set_<String> zRevRange(String key, long start, long end) {
        try {
            _Set_<String> values = redisTemplate.opsForZSet().reverseRange(key, start, end);
            _log_.debug("ZSet 倒序范围查询成功 key={}, start={}, end={}, values={}", key, start, end, values);
            return values;
        } catch (Exception e) {
            _log_.error("ZSet 倒序范围查询失败 key={}, start={}, end={}", key, start, end, e);
            throw e;
        }
    }

    _/**_
_     * 获取有序集合中指定元素的分数_
_     *_
_     * _**@param **_key   有序集合的键_
_     * _**@param **_value 元素值_
_     * _**@return **_元素的分数，如果元素不存在则返回 null_
_     */_
_    _@Override
    public Double zScore(String key, Object value) {
        try {
            Double score = redisTemplate.opsForZSet().score(key, value);
            _log_.debug("ZSet 查询分数成功 key={}, value={}, score={}", key, value, score);
            return score;
        } catch (Exception e) {
            _log_.error("ZSet 查询分数失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    _/**_
_     * 获取有序集合中指定元素的排名（按分数从小到大）_
_     *_
_     * _**@param **_key   有序集合的键_
_     * _**@param **_value 元素值_
_     * _**@return **_元素的排名，如果元素不存在则返回 null_
_     */_
_    _@Override
    public Long zRank(String key, Object value) {
        try {
            Long rank = redisTemplate.opsForZSet().rank(key, value);
            _log_.debug("ZSet 查询排名成功 key={}, value={}, rank={}", key, value, rank);
            return rank;
        } catch (Exception e) {
            _log_.error("ZSet 查询排名失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    _/**_
_     * 获取有序集合中指定元素的排名（按分数从大到小）_
_     *_
_     * _**@param **_key   有序集合的键_
_     * _**@param **_value 元素值_
_     * _**@return **_元素的排名，如果元素不存在则返回 null_
_     */_
_    _@Override
    public Long zRevRank(String key, Object value) {
        try {
            Long rank = redisTemplate.opsForZSet().reverseRank(key, value);
            _log_.debug("ZSet 倒序查询排名成功 key={}, value={}, rank={}", key, value, rank);
            return rank;
        } catch (Exception e) {
            _log_.error("ZSet 倒序查询排名失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    _/**_
_     * 获取有序集合的元素个数_
_     *_
_     * _**@param **_key 有序集合的键_
_     * _**@return **_集合中的元素个数_
_     */_
_    _@Override
    public Long zCard(String key) {
        try {
            Long size = redisTemplate.opsForZSet().zCard(key);
            _log_.debug("ZSet 查询大小成功 key={}, size={}", key, size);
            return size;
        } catch (Exception e) {
            _log_.error("ZSet 查询大小失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 获取有序集合中指定分数范围内的元素_
_     *_
_     * _**@param **_key 有序集合的键_
_     * _**@param **_min 最小分数（包含）_
_     * _**@param **_max 最大分数（包含）_
_     * _**@return **_指定分数范围内的元素集合_
_     */_
_    _@Override
    public _Set_<String> zRangeByScore(String key, double min, double max) {
        try {
            _Set_<String> values = redisTemplate.opsForZSet().rangeByScore(key, min, max);
            _log_.debug("ZSet 分数范围查询成功 key={}, min={}, max={}, values={}", key, min, max, values);
            return values;
        } catch (Exception e) {
            _log_.error("ZSet 分数范围查询失败 key={}, min={}, max={}", key, min, max, e);
            throw e;
        }
    }

    _/**_
_     * 统计有序集合中指定分数范围内的元素数量_
_     *_
_     * _**@param **_key 有序集合的键_
_     * _**@param **_min 最小分数（包含）_
_     * _**@param **_max 最大分数（包含）_
_     * _**@return **_指定分数范围内的元素数量_
_     */_
_    _@Override
    public Long zCount(String key, double min, double max) {
        try {
            Long count = redisTemplate.opsForZSet().count(key, min, max);
            _log_.debug("ZSet 统计分数范围内元素数量成功 key={}, min={}, max={}, count={}", key, min, max, count);
            return count;
        } catch (Exception e) {
            _log_.error("ZSet 统计分数范围内元素数量失败 key={}, min={}, max={}", key, min, max, e);
            throw e;
        }
    }
}
```

### Hash 类型：接口实现类提供

```java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core._Cursor_;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

_/**_
_ * _**@author **_王玉涛_
_ * _**@version **_1.0_
_ * _**@since **_2025/8/23_
_ */_
@Slf4j
@Component
@RequiredArgsConstructor
public class HashCacheRedisProvider implements _HashCacheProvider _{

    private final StringRedisTemplate redisTemplate;

    _/**_
_     * 设置哈希表中指定字段的值_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@param **_value 字段值_
_     */_
_    _@Override
    public void set(String _key_, String _field_, String _value_) {
        try {
            redisTemplate.opsForHash().put(_key_, _field_, _value_);
            _log_.debug("设置哈希字段成功 key={}, field={}, value={}", key, field, value);
        } catch (Exception _e_) {
            _log_.error("设置哈希字段失败 key={}, field={}, value={}", _key_, field, value, e);
            throw e;
        }
    }

    _/**_
_     * 获取哈希表中指定字段的值_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@return **_字段值，如果不存在则返回空字符串_
_     */_
_    _@Override
    public String get(String _key_, String _field_) {
        try {
            Object value = redisTemplate.opsForHash().get(_key_, _field_);
            String result = value == null ? "" : value.toString();
            _log_.debug("获取哈希字段成功 key={}, field={}, value={}", _key_, _field_, result);
            return result;
        } catch (Exception _e_) {
            _log_.error("获取哈希字段失败 key={}, field={}", _key_, _field_, _e_);
            throw _e_;
        }
    }

    _/**_
_     * 删除哈希表中一个或多个字段_
_     *_
_     * _**@param **_key    哈希表的键_
_     * _**@param **_fields 要删除的字段名数组_
_     * _**@return **_被成功删除的字段数量_
_     */_
_    _@Override
    public Long del(String _key_, String... _fields_) {
        try {
            Long result = redisTemplate.opsForHash().delete(key, (Object[]) fields);
            _log_.debug("删除哈希字段成功 key={}, fields={}, deletedCount={}", key, fields, result);
            return result;
        } catch (Exception e) {
            _log_.error("删除哈希字段失败 key={}, fields={}", key, fields, e);
            throw e;
        }
    }

    _/**_
_     * 检查哈希表中指定字段是否存在_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@return **_如果字段存在返回true，否则返回false_
_     */_
_    _@Override
    public Boolean isExists(String key, String field) {
        try {
            Boolean exists = redisTemplate.opsForHash().hasKey(key, field);
            _log_.debug("检查哈希字段存在性 key={}, field={}, exists={}", key, field, exists);
            return exists;
        } catch (Exception e) {
            _log_.error("检查哈希字段存在性失败 key={}, field={}", key, field, e);
            throw e;
        }
    }

    _/**_
_     * 获取哈希表中字段的数量_
_     *_
_     * _**@param **_key 哈希表的键_
_     * _**@return **_字段数量_
_     */_
_    _@Override
    public Long getLength(String key) {
        try {
            Long size = redisTemplate.opsForHash().size(key);
            _log_.debug("获取哈希表字段数量 key={}, size={}", key, size);
            return size;
        } catch (Exception e) {
            _log_.error("获取哈希表字段数量失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 批量设置哈希表中的多个字段_
_     *_
_     * _**@param **_key 哈希表的键_
_     * _**@param **_map 包含字段和值的映射_
_     */_
_    _@Override
    public void set(String key, _Map_<String, String> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            _log_.debug("批量设置哈希字段成功 key={}, map={}", key, map);
        } catch (Exception e) {
            _log_.error("批量设置哈希字段失败 key={}, map={}", key, map, e);
            throw e;
        }
    }

    _/**_
_     * 批量获取哈希表中多个字段的值_
_     *_
_     * _**@param **_key    哈希表的键_
_     * _**@param **_fields 字段名数组_
_     * _**@return **_字段值列表_
_     */_
_    _@Override
    public _List_<String> get(String key, String... fields) {
        try {
            _List_<Object> values = redisTemplate.opsForHash().multiGet(key, java.util.Arrays._asList_(fields));
            _List_<String> result = new java.util.ArrayList<>();
            for (Object value : values) {
                result.add(value == null ? "" : value.toString());
            }
            _log_.debug("批量获取哈希字段成功 key={}, fields={}, values={}", key, fields, result);
            return result;
        } catch (Exception e) {
            _log_.error("批量获取哈希字段失败 key={}, fields={}", key, fields, e);
            throw e;
        }
    }

    _/**_
_     * 获取哈希表中所有的字段和值 (警惕大Key风险)_
_     *_
_     * _**@param **_key 哈希表的键_
_     * _**@return **_包含所有字段和值的映射_
_     */_
_    _@Override
    public _Map_<String, String> getAll(String key) {
        try {
            java.util._Map_<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            java.util._Map_<String, String> result = new java.util.HashMap<>();
            for (java.util._Map_._Entry_<Object, Object> entry : entries.entrySet()) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
            _log_.debug("获取哈希表所有字段成功 key={}, entries={}", key, result);
            return result;
        } catch (Exception e) {
            _log_.error("获取哈希表所有字段失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 通过扫描的方式分批获取哈希表中所有的字段和值，避免一次性加载大量数据导致内存问题_
_     *_
_     * _**@param **_key       哈希表的键_
_     * _**@param **_batchSize 每次扫描的批次大小_
_     * _**@return **_包含所有字段和值的映射_
_     */_
_    _@Override
    public _Map_<String, String> scanAll(String key, int batchSize) {
        _// 创建一个用于存储结果的Map_
_        Map_<String, String> result = new HashMap<>();
        try (
                _// 使用Redis的scan命令创建一个游标，用于分批扫描哈希表中的字段_
_                Cursor_<_Map_._Entry_<Object, Object>> cursor = redisTemplate.opsForHash()
                        .scan(_key_, ScanOptions._scanOptions_().count(batchSize).build())
        ) {
            _// 遍历游标中的每一个条目_
_            _while (cursor.hasNext()) {
                _// 获取下一个条目_
_                Map_._Entry_<Object, Object> entry = cursor.next();
                _// 将条目的键和值转换为字符串并存入结果Map中_
_                _result.put(entry.getKey().toString(), entry.getValue().toString());
            }
        } catch (Exception e) {
            _// 记录错误日志_
_            log_.error("扫描哈希表字段失败 key={}, batchSize={}", key, batchSize, e);
            _// 抛出运行时异常_
_            _throw e;
        }
        _// 返回扫描结果_
_        _return result;
    }

    _/**_
_     * 获取哈希表中所有的字段名 (警惕大Key风险)_
_     *_
_     * _**@param **_key 哈希表的键_
_     * _**@return **_字段名集合_
_     */_
_    _@Override
    public _Set_<String> getKeys(String key) {
        try {
            _Set_<Object> keys = redisTemplate.opsForHash().keys(key);
            _Set_<String> result = new HashSet<>();
            for (Object k : keys) {
                String keyString = (String) k;
                result.add(keyString);
            }
            _log_.debug("获取哈希表所有字段名成功 key={}, keys={}", key, result);
            return result;
        } catch (Exception e) {
            _log_.error("获取哈希表所有字段名失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 获取哈希表中所有的字段值 (警惕大Key风险)_
_     *_
_     * _**@param **_key 哈希表的键_
_     * _**@return **_字段值集合_
_     */_
_    _@Override
    public _List_<String> getValues(String key) {
        try {
            _Collection_<Object> values = redisTemplate.opsForHash().values(key);
            _List_<String> result = new ArrayList<>();
            for (Object value : values) {
                result.add(value == null ? "" : value.toString());
            }
            _log_.debug("获取哈希表所有字段值成功 key={}, values={}", key, result);
            return result;
        } catch (Exception e) {
            _log_.error("获取哈希表所有字段值失败 key={}", key, e);
            throw e;
        }
    }

    _/**_
_     * 将哈希表中指定字段的值增加指定整数_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@param **_delta 增加的数值_
_     * _**@return **_增加后的值_
_     */_
_    _@Override
    public Long incrBy(String key, String field, long delta) {
        try {
            Long result = redisTemplate.opsForHash().increment(key, field, delta);
            _log_.debug("哈希字段自增成功 key={}, field={}, delta={}, result={}", key, field, delta, result);
            return result;
        } catch (Exception e) {
            _log_.error("哈希字段自增失败 key={}, field={}, delta={}", key, field, delta, e);
            throw e;
        }
    }

    _/**_
_     * 将哈希表中指定字段的值增加指定浮点数_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@param **_delta 增加的浮点数_
_     * _**@return **_增加后的值_
_     */_
_    _@Override
    public Double incrByFloat(String key, String field, double delta) {
        try {
            Double result = redisTemplate.opsForHash().increment(key, field, delta);
            _log_.debug("哈希字段浮点自增成功 key={}, field={}, delta={}, result={}", key, field, delta, result);
            return result;
        } catch (Exception e) {
            _log_.error("哈希字段浮点自增失败 key={}, field={}, delta={}", key, field, delta, e);
            throw e;
        }
    }

    _/**_
_     * 当哈希表中指定字段不存在时才设置值_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@param **_value 字段值_
_     * _**@return **_如果设置成功返回true，如果字段已存在返回false_
_     */_
_    _@Override
    public boolean setIfAbsent(String key, String field, String value) {
        try {
            Boolean result = redisTemplate.opsForHash().putIfAbsent(key, field, value);
            _log_.debug("哈希字段不存在时设置成功 key={}, field={}, value={}, result={}", key, field, value, result);
            return result;
        } catch (Exception e) {
            _log_.error("哈希字段不存在时设置失败 key={}, field={}, value={}", key, field, value, e);
            throw e;
        }
    }

    _/**_
_     * 获取哈希表中指定字段值的长度_
_     *_
_     * _**@param **_key   哈希表的键_
_     * _**@param **_field 字段名_
_     * _**@return **_字段值的长度_
_     */_
_    _@Override
    public Long queryFieldLen(String key, String field) {
        try {
            Long length = redisTemplate.opsForHash().lengthOfValue(key, field);
            _log_.debug("获取哈希字段值长度成功 key={}, field={}, length={}", key, field, length);
            return length;
        } catch (Exception e) {
            _log_.error("获取哈希字段值长度失败 key={}, field={}", key, field, e);
            throw e;
        }
    }
}
```
