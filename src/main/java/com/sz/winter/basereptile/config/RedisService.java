package com.sz.winter.basereptile.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis client service
 */
@Service
public class RedisService {

    @Value("${base.redis.redishost}")
    private static String redishost;

    @Value("${base.redis.redisPort}")
    private static Integer redisPort;

    @Value("${base.redis.redispassword}")
    private static String redispassword;

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    protected static JedisPool jedisPool;

    public RedisService() {
    }

    @PostConstruct
    public void initJedisPool(){
    	jedisPool=init();
    }

    public static Jedis getResource() {
//    	if(jedisPool==null)jedisPool = init();
        return jedisPool.getResource();
    }

    public static String get(String key) {
        String value = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            value = jedis.get(key);
        } catch (Exception e) {
            logger.error("get error.", e);
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return value;
    }

    public static Pipeline getPipeline() {
        return getResource().pipelined();
    }

    public static String set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getResource();
            return jedis.set(key, value);
        } catch (Exception e) {
            logger.error("set error.", e);
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return "0";
    }

    public static Long del(String... keys) {
        Jedis jedis = null;
        try {
            jedis = getResource();
            return jedis.del(keys);
        } catch (Exception e) {
            logger.error("del error.", e);
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return 0L;
    }

    /**
     * 通过key向指定的value值追加值
     *
     * @param key
     * @param str
     * @return 成功返回 添加后value的长度 失败 返回 添加的 value 的长度 异常返回0L
     */
    public static Long append(String key, String str) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.append(key, str);
        } catch (Exception e) {
            logger.error("append error", e);
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return 0L;
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return true OR false
     */
    public static Boolean exists(String key) {
        Boolean exists = false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            exists = jedis.exists(key);
        } catch (Exception e) {
            logger.error("exists error", e);
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return exists;
    }

    /**
     * 设置key value,如果key已经存在则返回0,nx==> not exist
     *
     * @param key
     * @param value
     * @return 成功返回1 如果存在返回 0 发生异常返回-1
     */
    public static Long setnx(String key, String value) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.setnx(key, value);
        } catch (Exception e) {
            logger.error("setnx error", e);
            result = -1L;
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return result;
    }

    /**
     * 设置key value并制定这个键值的有效期
     *
     * @param key
     * @param value
     * @param seconds 单位:秒
     * @return 成功返回OK 失败和异常返回null
     */
    public static String setex(String key, String value, int seconds) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.setex(key, seconds, value);
        } catch (Exception e) {
            logger.error("setex error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 设置key这个键值的有效期
     *
     * @param key
     * @param seconds 单位:秒
     * @return 成功返回1 失败和异常返回0
     */
    public static Long expire(String key, int seconds) {
        Long res = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.expire(key, seconds);
        } catch (Exception e) {
            logger.error("expire error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }
    
    /**
     * 获取key剩余失效时间
     *
     * @param key
     * @return seconds 单位:毫秒
     */
    public static Long pttl(String key) {
        Long res = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.pttl(key);
        } catch (Exception e) {
            logger.error("expire error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key 和offset 从指定的位置开始将原先value替换 下标从0开始,offset表示从offset下标开始替换
     * 如果替换的字符串长度过小则会这样 example: value : bigsea@zto.cn str : abc 从下标7开始替换 则结果为
     * RES : bigsea.abc.cn
     *
     * @param key
     * @param str
     * @param offset 下标位置
     * @return 返回替换后 value 的长度
     */
    public static Long setrange(String key, String str, int offset) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.setrange(key, offset, str);
        } catch (Exception e) {
            logger.error("setrange error", e);
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return result;
    }

    /**
     * 通过批量的key获取批量的value
     *
     * @param keys string数组 也可以是一个key
     * @return 成功返回value的集合, 失败返回null的集合 ,异常返回空
     */
    public static List<String> mget(String... keys) {
        List<String> values = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            values = jedis.mget(keys);
        } catch (Exception e) {
            logger.error("mget error", e);
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return values;
    }

    /**
     * 批量的设置key:value,可以一个 example: obj.mset(new
     * String[]{"key2","value1","key2","value2"})
     *
     * @param keysvalues
     * @return 成功返回OK 失败 异常 返回 null
     */
    public static String mset(String... keysvalues) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.mset(keysvalues);
        } catch (Exception e) {
            logger.error("mset error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 批量的设置key:value,可以一个,如果key已经存在则会失败,操作会回滚 example: obj.msetnx(new
     * String[]{"key2","value1","key2","value2"})
     *
     * @param keysvalues
     * @return 成功返回1 失败返回0
     */
    public static Long msetnx(String... keysvalues) {
        Long res = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.msetnx(keysvalues);
        } catch (Exception e) {
            logger.error("msetnx error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 设置key的值,并返回一个旧值
     *
     * @param key
     * @param value
     * @return 旧值 如果key不存在 则返回null
     */
    public static String getset(String key, String value) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.getSet(key, value);
        } catch (Exception e) {
            logger.error("getset error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过下标 和key 获取指定下标位置的 value
     *
     * @param key
     * @param startOffset 开始位置 从0 开始 负数表示从右边开始截取
     * @param endOffset
     * @return 如果没有返回null
     */
    public static String getrange(String key, int startOffset, int endOffset) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.getrange(key, startOffset, endOffset);
        } catch (Exception e) {
            logger.error("getrange error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key 对value进行加值+1操作,当value不是int类型时会返回错误,当key不存在是则value为1
     *
     * @param key
     * @return 加值后的结果
     */
    public static Long incr(String key) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.incr(key);
        } catch (Exception e) {
            logger.error("incr error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key给指定的value加值,如果key不存在,则这是value为该值
     *
     * @param key
     * @param integer
     * @return
     */
    public static Long incrBy(String key, Long integer) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.incrBy(key, integer);
        } catch (Exception e) {
            logger.error("incrBy error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key给指定的value加值,如果key不存在,则这是value为该值
     *
     * @param key
     * @param incrvalue
     * @return
     */
    public static Double incrByfloat(String key, Double incrvalue) {
        Double res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.incrByFloat(key, incrvalue);
        } catch (Exception e) {
            logger.error("incrBy error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 对key的值做减减操作,如果key不存在,则设置key为-1
     *
     * @param key
     * @return
     */
    public static Long decr(String key) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.decr(key);
        } catch (Exception e) {
            logger.error("decr error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 减去指定的值
     *
     * @param key
     * @param integer
     * @return
     */
    public static Long decrBy(String key, Long integer) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.decrBy(key, integer);
        } catch (Exception e) {
            logger.error("decrBy error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取value值的长度
     *
     * @param key
     * @return 失败返回null
     */
    public static Long serlen(String key) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.strlen(key);
        } catch (Exception e) {
            logger.error(" serlen error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key给field设置指定的值,如果key不存在,则先创建
     *
     * @param key
     * @param field 字段
     * @param value
     * @return 如果存在返回0 异常返回null
     */
    public static Long hset(String key, String field, String value) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hset(key, field, value);
        } catch (Exception e) {
            logger.error(" hset error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key给field设置指定的值,如果key不存在则先创建,如果field已经存在,返回0
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public static Long hsetnx(String key, String field, String value) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hsetnx(key, field, value);
        } catch (Exception e) {
            logger.error(" hsetnx error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key同时设置 hash的多个field
     *
     * @param key
     * @param hash
     * @return 返回OK 异常返回null
     */
    public static String hmset(String key, Map<String, String> hash) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hmset(key, hash);
        } catch (Exception e) {
            logger.error(" hmset error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key 和 field 获取指定的 value
     *
     * @param key
     * @param field
     * @return 没有返回null
     */
    public static String hget(String key, String field) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hget(key, field);
        } catch (Exception e) {
            logger.error(" hget error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key 和 fields 获取指定的value 如果没有对应的value则返回null
     *
     * @param key
     * @param fields 可以使 一个String 也可以是 String数组
     * @return
     */
    public static List<String> hmget(String key, String... fields) {
        List<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hmget(key, fields);
        } catch (Exception e) {
            logger.error(" hmget error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key给指定的field的value加上给定的值
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public static Long hincrby(String key, String field, Long value) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hincrBy(key, field, value);
        } catch (Exception e) {
            logger.error(" hincrby error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key和field判断是否有指定的value存在
     *
     * @param key
     * @param field
     * @return
     */
    public static Boolean hexists(String key, String field) {
        Boolean res = false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hexists(key, field);
        } catch (Exception e) {
            logger.error(" hexists error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回field的数量
     *
     * @param key
     * @return
     */
    public static Long hlen(String key) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hlen(key);
        } catch (Exception e) {
            logger.error("hlen  error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;

    }

    /**
     * 通过key 删除指定的 field
     *
     * @param key
     * @param fields 可以是 一个 field 也可以是 一个数组
     * @return
     */
    public static Long hdel(String key, String... fields) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hdel(key, fields);
        } catch (Exception e) {
            logger.error(" hdel error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回所有的field
     *
     * @param key
     * @return
     */
    public static Set<String> hkeys(String key) {
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hkeys(key);
        } catch (Exception e) {
            logger.error(" hkeys error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回所有和key有关的value
     *
     * @param key
     * @return
     */
    public static List<String> hvals(String key) {
        List<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hvals(key);
        } catch (Exception e) {
            logger.error(" hvals error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取所有的field和value
     *
     * @param key
     * @return
     */
    public static Map<String, String> hgetall(String key) {
        Map<String, String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.hgetAll(key);
        } catch (Exception e) {
            logger.error(" hgetall error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key向list头部添加字符串
     * </p>
     *
     * @param key
     * @param strs 可以使一个string 也可以使string数组
     * @return 返回list的value个数
     */
    public static Long lpush(String key, String... strs) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.lpush(key, strs);
        } catch (Exception e) {
            logger.error(" lpush error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key向list尾部添加字符串
     * </p>
     *
     * @param key
     * @param strs 可以使一个string 也可以使string数组
     * @return 返回list的value个数
     */
    public static Long rpush(String key, String... strs) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.rpush(key, strs);
        } catch (Exception e) {
            logger.error(" rpush error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key设置list指定下标位置的value
     * </p>
     * <p>
     * 如果下标超过list里面value的个数则报错
     * </p>
     *
     * @param key
     * @param index 从0开始
     * @param value
     * @return 成功返回OK
     */
    public static String lset(String key, Long index, String value) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.lset(key, index, value);
        } catch (Exception e) {
            logger.error(" lset error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key从对应的list中删除指定的count个 和 value相同的元素
     * </p>
     *
     * @param key
     * @param count 当count为0时删除全部
     * @param value
     * @return 返回被删除的个数
     */
    public static Long lrem(String key, long count, String value) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.lrem(key, count, value);
        } catch (Exception e) {
            logger.error(" lrem error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key保留list中从strat下标开始到end下标结束的value值
     * </p>
     *
     * @param key
     * @param start
     * @param end
     * @return 成功返回OK
     */
    public static String ltrim(String key, long start, long end) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.ltrim(key, start, end);
        } catch (Exception e) {
            logger.error(" ltrim error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key从list的头部删除一个value,并返回该value
     * </p>
     *
     * @param key
     * @return
     */
    public static String lpop(String key) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.lpop(key);
        } catch (Exception e) {
            logger.error(" lpop error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key从list尾部删除一个value,并返回该元素
     * </p>
     *
     * @param key
     * @return
     */
    public static String rpop(String key) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.rpop(key);
        } catch (Exception e) {
            logger.error(" rpop error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key从一个list的尾部删除一个value并添加到另一个list的头部,并返回该value
     * </p>
     * <p>
     * 如果第一个list为空或者不存在则返回null
     * </p>
     *
     * @param srckey
     * @param dstkey
     * @return
     */
    public static String rpoplpush(String srckey, String dstkey) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.rpoplpush(srckey, dstkey);
        } catch (Exception e) {
            logger.error(" rpoplpush error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key获取list中指定下标位置的value
     * </p>
     *
     * @param key
     * @param index
     * @return 如果没有返回null
     */
    public static String lindex(String key, long index) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.lindex(key, index);
        } catch (Exception e) {
            logger.error(" lindex error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key返回list的长度
     * </p>
     *
     * @param key
     * @return
     */
    public static Long llen(String key) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.llen(key);
        } catch (Exception e) {
            logger.error(" llen error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key获取list指定下标位置的value
     * </p>
     * <p>
     * 如果start 为 0 end 为 -1 则返回全部的list中的value
     * </p>
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static List<String> lrange(String key, long start, long end) {
        List<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.lrange(key, start, end);
        } catch (Exception e) {
            logger.error(" lrange error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key向指定的set中添加value
     * </p>
     *
     * @param key
     * @param members 可以是一个String 也可以是一个String数组
     * @return 添加成功的个数
     */
    public static Long sadd(String key, String... members) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.sadd(key, members);
        } catch (Exception e) {
            logger.error(" sadd error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key删除set中对应的value值
     * </p>
     *
     * @param key
     * @param members 可以是一个String 也可以是一个String数组
     * @return 删除的个数
     */
    public static Long srem(String key, String... members) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.srem(key, members);
        } catch (Exception e) {
            logger.error(" srem error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key随机删除一个set中的value并返回该值
     * </p>
     *
     * @param key
     * @return
     */
    public static String spop(String key) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.spop(key);
        } catch (Exception e) {
            logger.error(" spop error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key获取set中的差集
     * </p>
     * <p>
     * 以第一个set为标准
     * </p>
     *
     * @param keys 可以使一个string 则返回set中所有的value 也可以是string数组
     * @return
     */
    public static Set<String> sdiff(String... keys) {
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.sdiff(keys);
        } catch (Exception e) {
            logger.error(" sdiff error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key获取set中的差集并存入到另一个key中
     * </p>
     * <p>
     * 以第一个set为标准
     * </p>
     *
     * @param dstkey 差集存入的key
     * @param keys   可以使一个string 则返回set中所有的value 也可以是string数组
     * @return
     */
    public static Long sdiffstore(String dstkey, String... keys) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.sdiffstore(dstkey, keys);
        } catch (Exception e) {
            logger.error(" sdiffstore error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key获取指定set中的交集
     * </p>
     *
     * @param keys 可以使一个string 也可以是一个string数组
     * @return
     */
    public static Set<String> sinter(String... keys) {
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.sinter(keys);
        } catch (Exception e) {
            logger.error(" sinter error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key获取指定set中的交集 并将结果存入新的set中
     * </p>
     *
     * @param dstkey
     * @param keys   可以使一个string 也可以是一个string数组
     * @return
     */
    public static Long sinterstore(String dstkey, String... keys) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.sinterstore(dstkey, keys);
        } catch (Exception e) {
            logger.error(" sinterstore error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key返回所有set的并集
     * </p>
     *
     * @param keys 可以使一个string 也可以是一个string数组
     * @return
     */
    public static Set<String> sunion(String... keys) {
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.sunion(keys);
        } catch (Exception e) {
            logger.error(" sunion error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key返回所有set的并集,并存入到新的set中
     * </p>
     *
     * @param dstkey
     * @param keys   可以使一个string 也可以是一个string数组
     * @return
     */
    public static Long sunionstore(String dstkey, String... keys) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.sunionstore(dstkey, keys);
        } catch (Exception e) {
            logger.error(" sunionstore error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key将set中的value移除并添加到第二个set中
     * </p>
     *
     * @param srckey 需要移除的
     * @param dstkey 添加的
     * @param member set中的value
     * @return
     */
    public static Long smove(String srckey, String dstkey, String member) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.smove(srckey, dstkey, member);
        } catch (Exception e) {
            logger.error(" smove error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key获取set中value的个数
     * </p>
     *
     * @param key
     * @return
     */
    public static Long scard(String key) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.scard(key);
        } catch (Exception e) {
            logger.error(" scard error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key判断value是否是set中的元素
     * </p>
     *
     * @param key
     * @param member
     * @return
     */
    public static Boolean sismember(String key, String member) {
        Boolean res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.sismember(key, member);
        } catch (Exception e) {
            logger.error(" sismember error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key获取set中随机的value,不删除元素
     * </p>
     *
     * @param key
     * @return
     */
    public static String srandmember(String key) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.srandmember(key);
        } catch (Exception e) {
            logger.error(" srandmember error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * <p>
     * 通过key获取set中所有的value
     * </p>
     *
     * @param key
     * @return
     */
    public static Set<String> smembers(String key) {
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.smembers(key);
        } catch (Exception e) {
            logger.error(" smembers error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key向zset中添加value,score,其中score就是用来排序的 如果该value已经存在则根据score更新元素
     *
     * @param key
     * @param scoreMembers
     * @return
     */
    public static Long zadd(String key, Map<String, Double> scoreMembers) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zadd(key, scoreMembers);
        } catch (Exception e) {
            logger.error(" zadd error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key向zset中添加value,score,其中score就是用来排序的 如果该value已经存在则根据score更新元素
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    public static Long zadd(String key, double score, String member) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zadd(key, score, member);
        } catch (Exception e) {
            logger.error(" zadd error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key删除在zset中指定的value
     *
     * @param key
     * @param members 可以使一个string 也可以是一个string数组
     * @return
     */
    public static Long zrem(String key, String... members) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zrem(key, members);
        } catch (Exception e) {
            logger.error(" zrem error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key增加该zset中value的score的值
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    public static Double zincrby(String key, double score, String member) {
        Double res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zincrby(key, score, member);
        } catch (Exception e) {
            logger.error(" zincrby error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回zset中value的排名 下标从小到大排序
     *
     * @param key
     * @param member
     * @return
     */
    public static Long zrank(String key, String member) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zrank(key, member);
        } catch (Exception e) {
            logger.error(" zrank error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回zset中value的排名 下标从大到小排序
     *
     * @param key
     * @param member
     * @return
     */
    public static Long zrevrank(String key, String member) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zrevrank(key, member);
        } catch (Exception e) {
            logger.error(" zrevrank error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key将获取score从start到end中zset的value socre从大到小排序 当start为0 end为-1时返回全部
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static Set<String> zrevrange(String key, long start, long end) {
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            logger.error(" zrevrange error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static Set<String> zrevrangeByScore(String key, double start, double end) {
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zrevrangeByScore(key, start, end);
        } catch (Exception e) {
            logger.error(" zrevrangeByScore error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static Set<String> zrevrangeByScore(String key, double start, double end,int offset,int count){
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zrevrangeByScore(key, start, end,offset,count);
        } catch (Exception e) {
            logger.error(" zrevrangeByScore limit error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key将获取score从start到end中zset的value socre从大到小排序 当start为0 end为-1时返回全部
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static Set<Tuple> zrevrangeWithScore(String key, long start, long end) {
        Set<Tuple> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zrevrangeWithScores(key, start, end);
        } catch (Exception e) {
            logger.error(" zrevrange error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key将获取score从start到end中zset的value socre从大到小排序 当start为0 end为-1时返回全部
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static Set<Tuple> zrangeWithScore(String key, long start, long end) {
        Set<Tuple> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zrangeWithScores(key, start, end);
        } catch (Exception e) {
            logger.error(" zrevrange error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key将获取score从start到end中zset的value socre从大到小排序 当start为0 end为-1时返回全部
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static Set<Tuple>  zrangeByScoreWithScore(String key, long start, long end) {
        Set<Tuple> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zrangeByScoreWithScores(key, start, end);
        } catch (Exception e) {
            logger.error(" zrevrange error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }



    /**
     * 通过key返回指定score内zset中的value
     *
     * @param key
     * @param max
     * @param min
     * @return
     */
    public static Set<String> zrangeByScore(String key, Double min, Double max) {
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zrangeByScore(key, min, max);
        } catch (Exception e) {
            logger.error(" zrangeByScore error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }


    /**
     * 返回指定区间内zset中value的数量
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public static Long zcount(String key, String min, String max) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zcount(key, min, max);
        } catch (Exception e) {
            logger.error(" zcount error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回zset中的value个数
     *
     * @param key
     * @return
     */
    public static Long zcard(String key) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zcard(key);
        } catch (Exception e) {
            logger.error(" zcard error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取zset中value的score值
     *
     * @param key
     * @param member
     * @return
     */
    public static Double zscore(String key, String member) {
        Double res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zscore(key, member);
        } catch (Exception e) {
            logger.error(" zscore error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key删除给定区间内的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static Long zremrangeByRank(String key, long start, long end) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zremrangeByRank(key, start, end);
        } catch (Exception e) {
            logger.error(" zremrangeByRank error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key删除指定score内的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static Long zremrangeByScore(String key, double start, double end) {
        Long res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            logger.error(" zremrangeByScore error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 返回满足pattern表达式的所有key keys(*) 返回所有的key
     *
     * @param pattern
     * @return
     */
    public static Set<String> keys(String pattern) {
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.keys(pattern);
        } catch (Exception e) {
            logger.error(" keys error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key判断值得类型
     *
     * @param key
     * @return
     */
    public static String type(String key) {
        String res = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            res = jedis.type(key);
        } catch (Exception e) {
            logger.error(" type error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 订阅指定频道
     * @param subscriber
     * @param channels
     */
    public static void subscibeChannel(JedisPubSub subscriber, String... channels){
        try {
            Jedis jedis=getResource();
            jedis.subscribe(subscriber,channels);
        }catch (Exception e){
            logger.error("com.cn.ccc.jedis.RedisService.subscibeChannel",e);
        }
    }

    /**
     * 发布消息到指定频道
     * @param channel
     * @param message
     */
    public static void publish2Channel(String channel,String message){
        Jedis jedis = null;
        try {
            jedis = getResource();
            jedis.publish(channel,message);
        } catch (Exception e) {
            logger.error("com.cn.ccc.jedis.RedisService.publish2Channel", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    /**
     * set二进制的方式
     * @param key
     * @param field
     * @param value
     */
    public static void hset(byte[] key,byte[]field,byte[] value){
    	 Jedis jedis = null;
         try {
             jedis = getResource();
             jedis.hset(key, field, value);
         } catch (Exception e) {
             logger.error("com.cn.ccc.jedis.RedisService.hset(byte[] key,byte[]field,byte[] value)", e);
         } finally {
             if (jedis != null) {
                 jedis.close();
             }
         }
    }
    /**
     * get二进制方式
     * @param key
     * @param field
     * @return
     */
    public static byte[] hget(byte[] key,byte[]field){
     byte[] b = null;
   	 Jedis jedis = null;
        try {
            jedis = getResource();
            b = jedis.hget(key, field);
        } catch (Exception e) {
            logger.error("com.cn.ccc.jedis.RedisService.hget(byte[] key,byte[]field)", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return b;
   }
    public static Long hdel(byte[] key,byte[]...fields){
     Long l = 0l;
   	 Jedis jedis = null;
        try {
            jedis = getResource();
            l = jedis.hdel(key, fields);
        } catch (Exception e) {
            logger.error("com.cn.ccc.jedis.RedisService.hdel(byte[] key,byte[]...fields)", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return l;
   }
    public static JedisPool init() {
        JedisPool jedisPool = null;
        try {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
            jedisPoolConfig.setBlockWhenExhausted(true);
            //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间
            jedisPoolConfig.setMaxWaitMillis(10000);
            jedisPoolConfig.setMaxIdle(500);
            jedisPoolConfig.setMaxTotal(20000);
            jedisPoolConfig.setMinIdle(100);
            /*String host = redishost;
            Integer port = redisPort;
            String password = redispassword;*/

            String host = "127.0.0.1";
            Integer port = 6379;
            String password = "ly@19961836.";
            jedisPool = new JedisPool(jedisPoolConfig, host, port, 20000,password);
        } catch (Exception e) {
            logger.error("init jedis pool error.", e);
        }
        return jedisPool;
    }

}
