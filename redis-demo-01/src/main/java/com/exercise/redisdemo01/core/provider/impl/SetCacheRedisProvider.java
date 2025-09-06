package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.provider.SetCacheProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Redis Set 集合缓存操作实现类
 * 提供对 Redis Set 数据结构的常用操作封装，包括添加、查询、删除、集合运算等
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SetCacheRedisProvider implements SetCacheProvider {

    private final StringRedisTemplate redisTemplate;

    /**
     * 向指定的 Set 集合中添加一个或多个元素
     *
     * @param key    Set 集合的键
     * @param values 要添加的元素数组
     */
    @Override
    public void set(String key, List<String> values) {
        try {
            String[] valuesArray = values.toArray(new String[0]);
            redisTemplate.opsForSet().add(key, valuesArray);
            log.debug("设置缓存成功 key={}, values={}", key, values);
        } catch (Exception e) {
            log.error("设置缓存失败 key={}, values={}", key, values, e);
            throw e;
        }
    }

    /**
     * 批量设置多个 Set 集合的元素，参数格式为 key1, value1, key2, value2, ...
     *
     * @param keyValues 键值对数组，必须为偶数个
     * @throws IllegalArgumentException 当参数个数不是偶数时抛出
     */
    @Override
    public void set(String... keyValues) {
        try {
            int length = keyValues.length;
            if (length % 2 != 0) {
                log.error("缓存键必须为偶数！");
                throw new IllegalArgumentException("参数个数必须为偶数");
            }
            for (int i = 0; i < length; i += 2) {
                redisTemplate.opsForSet().add(keyValues[i], keyValues[i + 1]);
                log.debug("设置缓存成功 key={}, value={}", keyValues[i], keyValues[i + 1]);
            }
        } catch (Exception e) {
            log.error("设置缓存失败 keyValues={}", keyValues, e);
            throw e;
        }
    }

    /**
     * 原子性地批量设置多个 Set 集合的元素，使用 Lua 脚本保证操作的原子性
     * 参数格式为 key1, value1, key2, value2, ...
     *
     * @param keyValues 键值对数组，必须为偶数个
     * @throws IllegalArgumentException 当参数个数不是偶数时抛出
     */
    @Override
    public void atomicitySet(String... keyValues) {
        try {
            int length = keyValues.length;
            if (length % 2 != 0) {
                log.error("缓存键数目必须为偶数！");
                throw new IllegalArgumentException("参数个数必须为偶数");
            }

            // 拆分参数：偶数位为集合名，奇数位为值
            List<String> keys = new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (int i = 0; i < keyValues.length; i += 2) {
                keys.add(keyValues[i]);
                values.add(keyValues[i + 1]);
            }
            Object[] valuesArray = values.toArray();

            // 构建 Lua 脚本
            String luaScript = "for i = 1, #KEYS do " +
                    "redis.call('SADD', KEYS[i], ARGV[i]) " +
                    "end";

            // 执行 Lua 脚本
            redisTemplate.execute(
                    new DefaultRedisScript<>(luaScript, String.class),
                    keys,
                    valuesArray
            );

            log.debug("原子性设置缓存成功 keyValues={}", (Object[]) keyValues);
        } catch (Exception e) {
            log.error("原子性设置缓存失败 keyValues={}", keyValues, e);
            throw e;
        }
    }

    /**
     * 获取指定 Set 集合中的所有元素
     *
     * @param key Set 集合的键
     * @return Set 集合中的所有元素
     */
    @Override
    public Set<String> queryAll(String key) {
        try {
            Set<String> values = redisTemplate.opsForSet().members(key);
            log.debug("查询缓存成功 key={}, values={}", key, values);
            return values;
        } catch (Exception e) {
            log.error("查询缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 判断指定元素是否存在于 Set 集合中
     *
     * @param key   Set 集合的键
     * @param value 要检查的元素
     * @return 如果元素存在返回 true，否则返回 false
     */
    @Override
    public boolean isExist(String key, String value) {
        try {
            Boolean exists = redisTemplate.opsForSet().isMember(key, value);
            log.debug("查询缓存成功 key={}, value={}, exists={}", key, value, exists);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("查询缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 随机弹出并移除 Set 集合中的一个元素
     *
     * @param key Set 集合的键
     * @return 被弹出的元素，如果集合为空则返回 null
     */
    @Override
    public String randomPop(String key) {
        try {
            String value = redisTemplate.opsForSet().pop(key);
            log.debug("随机弹出缓存成功 key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("随机弹出缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 获取两个 Set 集合的交集
     *
     * @param key      第一个 Set 集合的键
     * @param otherKey 第二个 Set 集合的键
     * @return 两个集合的交集
     */
    @Override
    public Set<String> interSection(String key, String otherKey) {
        try {
            Set<String> intersect = redisTemplate.opsForSet().intersect(key, otherKey);
            log.debug("集合交集成功 key={}, otherKey={}, values={}", key, otherKey, intersect);
            return intersect;
        } catch (Exception e) {
            log.error("集合交集失败 key={}, otherKey={}", key, otherKey, e);
            throw e;
        }
    }

    /**
     * 获取多个 Set 集合的交集
     *
     * @param keys Set 集合键的集合
     * @return 多个集合的交集
     */
    @Override
    public Set<String> interSection(Collection<String> keys) {
        try {
            Set<String> intersect = redisTemplate.opsForSet().intersect(keys);
            log.debug("集合交集成功 keys={}, values={}", keys, intersect);
            return intersect;
        } catch (Exception e) {
            log.error("集合交集失败 keys={}", keys, e);
            throw e;
        }
    }

    /**
     * 获取多个 Set 集合的并集
     *
     * @param keys Set 集合键的集合
     * @return 多个集合的并集
     */
    @Override
    public Set<String> unionSection(Collection<String> keys) {
        try {
            Set<String> union = redisTemplate.opsForSet().union(keys);
            log.debug("集合并集成功 keys={}, values={}", keys, union);
            return union;
        } catch (Exception e) {
            log.error("集合并集失败 keys={}", keys, e);
            throw e;
        }
    }

    /**
     * 获取两个 Set 集合的并集
     *
     * @param key      第一个 Set 集合的键
     * @param otherKey 第二个 Set 集合的键
     * @return 两个集合的并集
     */
    @Override
    public Set<String> unionSection(String key, String otherKey) {
        try {
            Set<String> union = redisTemplate.opsForSet().union(key, otherKey);
            log.debug("集合并集成功 key={}, otherKey={}, values={}", key, otherKey, union);
            return union;
        } catch (Exception e) {
            log.error("集合并集失败 key={}, otherKey={}", key, otherKey, e);
            throw e;
        }
    }

    /**
     * 从 Set 集合中移除指定元素
     *
     * @param key   Set 集合的键
     * @param value 要移除的元素
     */
    @Override
    public void remove(String key, String value) {
        try {
            redisTemplate.opsForSet().remove(key, value);
            log.debug("删除缓存成功 key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("删除缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    // ZSet 相关方法封装

    /**
     * 向有序集合中添加元素及其分数
     *
     * @param key   有序集合的键
     * @param value 要添加的元素
     * @param score 元素的分数
     */
    @Override
    public void zAdd(String key, String value, double score) {
        try {
            redisTemplate.opsForZSet().add(key, value, score);
            log.debug("ZSet 添加元素成功 key={}, value={}, score={}", key, value, score);
        } catch (Exception e) {
            log.error("ZSet 添加元素失败 key={}, value={}, score={}", key, value, score, e);
            throw e;
        }
    }

    /**
     * 原子性地批量添加有序集合元素，使用 Lua 脚本保证操作的原子性
     * 参数格式为 key1, value1, score1, key2, value2, score2, ...
     *
     * @param keyValues 键值对和分数数组，必须为3的倍数
     * @throws IllegalArgumentException 当参数个数不是3的倍数时抛出
     */
    @Override
    public void zAtomicityAdd(String... keyValues) {
        try {
            int length = keyValues.length;
            if (length % 3 != 0) {
                log.error("参数个数必须为3的倍数，key, value, score");
                throw new IllegalArgumentException("参数个数必须为3的倍数");
            }

            // 拆分参数：每三个参数为一组，分别是 key, value, score
            List<String> keys = new ArrayList<>();
            List<String> values = new ArrayList<>();
            List<String> scores = new ArrayList<>();

            for (int i = 0; i < length; i += 3) {
                keys.add(keyValues[i]);
                values.add(keyValues[i + 1]);
                scores.add(keyValues[i + 2]);
            }

            // 构建 Lua 脚本，原子性地添加 ZSet 元素
            String luaScript = "for i = 1, #KEYS do " +
                    "redis.call('ZADD', KEYS[i], ARGV[i * 2 - 1], ARGV[i * 2]) " +
                    "end";

            // 合并 values 和 scores 作为 ARGV 参数传递
            List<String> args = new ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                args.add(scores.get(i));
                args.add(values.get(i));
            }

            Object[] argsArray = args.toArray();
            // 执行 Lua 脚本
            redisTemplate.execute(
                    new DefaultRedisScript<>(luaScript, String.class),
                    keys,
                    argsArray
            );

            log.debug("ZSet 原子性添加元素成功 keyValues={}", (Object[]) keyValues);
        } catch (Exception e) {
            log.error("ZSet 原子性添加元素失败 keyValues={}", keyValues, e);
            throw e;
        }
    }

    /**
     * 批量添加有序集合元素
     * 参数格式为 key1, value1, score1, key2, value2, score2, ...
     *
     * @param keyValues 键值对和分数数组，必须为3的倍数
     * @throws IllegalArgumentException 当参数个数不是3的倍数时抛出
     */
    @Override
    public void zAdd(String... keyValues) {
        try {
            int length = keyValues.length;
            if (length % 3 != 0) {
                log.error("参数个数必须为3的倍数，格式为：key, value, score");
                throw new IllegalArgumentException("参数个数必须为3的倍数");
            }

            for (int i = 0; i < length; i += 3) {
                String key = keyValues[i];
                String value = keyValues[i + 1];
                double score = Double.parseDouble(keyValues[i + 2]);
                redisTemplate.opsForZSet().add(key, value, score);
                log.debug("ZSet添加元素成功 key={}, value={}, score={}", key, value, score);
            }
        } catch (Exception e) {
            log.error("ZSet 添加元素失败 keyValues={}", keyValues, e);
            throw e;
        }
    }

    /**
     * 从有序集合中移除一个或多个元素
     *
     * @param key    有序集合的键
     * @param values 要移除的元素数组
     */
    @Override
    public void zRemove(String key, Object... values) {
        try {
            redisTemplate.opsForZSet().remove(key, values);
            log.debug("ZSet 删除元素成功 key={}, values={}", key, values);
        } catch (Exception e) {
            log.error("ZSet 删除元素失败 key={}, values={}", key, values, e);
            throw e;
        }
    }

    /**
     * 获取有序集合中指定范围的元素（按分数从小到大）
     *
     * @param key   有序集合的键
     * @param start 起始索引
     * @param end   结束索引
     * @return 指定范围内的元素集合
     */
    @Override
    public Set<String> zRange(String key, long start, long end) {
        try {
            Set<String> values = redisTemplate.opsForZSet().range(key, start, end);
            log.debug("ZSet 范围查询成功 key={}, start={}, end={}, values={}", key, start, end, values);
            return values;
        } catch (Exception e) {
            log.error("ZSet 范围查询失败 key={}, start={}, end={}", key, start, end, e);
            throw e;
        }
    }

    /**
     * 获取有序集合中指定范围的元素（按分数从大到小）
     *
     * @param key   有序集合的键
     * @param start 起始索引
     * @param end   结束索引
     * @return 指定范围内的元素集合
     */
    @Override
    public Set<String> zRevRange(String key, long start, long end) {
        try {
            Set<String> values = redisTemplate.opsForZSet().reverseRange(key, start, end);
            log.debug("ZSet 倒序范围查询成功 key={}, start={}, end={}, values={}", key, start, end, values);
            return values;
        } catch (Exception e) {
            log.error("ZSet 倒序范围查询失败 key={}, start={}, end={}", key, start, end, e);
            throw e;
        }
    }

    /**
     * 获取有序集合中指定元素的分数
     *
     * @param key   有序集合的键
     * @param value 元素值
     * @return 元素的分数，如果元素不存在则返回 null
     */
    @Override
    public Double zScore(String key, Object value) {
        try {
            Double score = redisTemplate.opsForZSet().score(key, value);
            log.debug("ZSet 查询分数成功 key={}, value={}, score={}", key, value, score);
            return score;
        } catch (Exception e) {
            log.error("ZSet 查询分数失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 获取有序集合中指定元素的排名（按分数从小到大）
     *
     * @param key   有序集合的键
     * @param value 元素值
     * @return 元素的排名，如果元素不存在则返回 null
     */
    @Override
    public Long zRank(String key, Object value) {
        try {
            Long rank = redisTemplate.opsForZSet().rank(key, value);
            log.debug("ZSet 查询排名成功 key={}, value={}, rank={}", key, value, rank);
            return rank;
        } catch (Exception e) {
            log.error("ZSet 查询排名失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 获取有序集合中指定元素的排名（按分数从大到小）
     *
     * @param key   有序集合的键
     * @param value 元素值
     * @return 元素的排名，如果元素不存在则返回 null
     */
    @Override
    public Long zRevRank(String key, Object value) {
        try {
            Long rank = redisTemplate.opsForZSet().reverseRank(key, value);
            log.debug("ZSet 倒序查询排名成功 key={}, value={}, rank={}", key, value, rank);
            return rank;
        } catch (Exception e) {
            log.error("ZSet 倒序查询排名失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 获取有序集合的元素个数
     *
     * @param key 有序集合的键
     * @return 集合中的元素个数
     */
    @Override
    public Long zCard(String key) {
        try {
            Long size = redisTemplate.opsForZSet().zCard(key);
            log.debug("ZSet 查询大小成功 key={}, size={}", key, size);
            return size;
        } catch (Exception e) {
            log.error("ZSet 查询大小失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 获取有序集合中指定分数范围内的元素
     *
     * @param key 有序集合的键
     * @param min 最小分数（包含）
     * @param max 最大分数（包含）
     * @return 指定分数范围内的元素集合
     */
    @Override
    public Set<String> zRangeByScore(String key, double min, double max) {
        try {
            Set<String> values = redisTemplate.opsForZSet().rangeByScore(key, min, max);
            log.debug("ZSet 分数范围查询成功 key={}, min={}, max={}, values={}", key, min, max, values);
            return values;
        } catch (Exception e) {
            log.error("ZSet 分数范围查询失败 key={}, min={}, max={}", key, min, max, e);
            throw e;
        }
    }

    /**
     * 统计有序集合中指定分数范围内的元素数量
     *
     * @param key 有序集合的键
     * @param min 最小分数（包含）
     * @param max 最大分数（包含）
     * @return 指定分数范围内的元素数量
     */
    @Override
    public Long zCount(String key, double min, double max) {
        try {
            Long count = redisTemplate.opsForZSet().count(key, min, max);
            log.debug("ZSet 统计分数范围内元素数量成功 key={}, min={}, max={}, count={}", key, min, max, count);
            return count;
        } catch (Exception e) {
            log.error("ZSet 统计分数范围内元素数量失败 key={}, min={}, max={}", key, min, max, e);
            throw e;
        }
    }
}
