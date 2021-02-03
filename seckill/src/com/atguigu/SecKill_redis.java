package com.atguigu;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.Transaction;


public class SecKill_redis {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecKill_redis.class);

    public static void main(String[] args) {

        Jedis jedis = new Jedis("192.168.22.128", 6379);

        for (int i = 100; i > 0; i--) {
            jedis.rpush("sk:0101:qt", String.valueOf(i));
        }


        jedis.close();
    }

    public static boolean doSecKill(String uid, String prodid) throws IOException {
        Jedis jedis = new Jedis("192.168.22.128", 6379);
        System.out.println(jedis.ping());
        String qtKey = "sk:" + prodid + ":qt";
        String usrKey = "sk:" + prodid + ":usr";
        jedis.watch(qtKey);
        if (jedis.sismember(usrKey, uid)) {
            System.out.println("不能重复秒杀");
            jedis.close();
            return false;
        }
        String qtStr = jedis.get(qtKey);
        if (qtStr == null) {
            System.out.println("莫着急，活动还没有开始");
            jedis.close();
            return false;
        }
        int qt = Integer.parseInt(qtStr);
        if (qt <= 0) {
            System.out.println("活动结束");
            jedis.close();
            return false;
        }
        Transaction multi = jedis.multi();
        jedis.decr(qtKey);
        jedis.sadd(usrKey, uid);
        List<Object> exec = multi.exec();
        if (exec == null || exec.size() == 0) {
            System.out.println("秒杀失败");
            jedis.close();
            return false;
        }
        System.out.println("秒杀成功");
        return true;



        /*  ab -n 1000 -c 200 -p /postfile -T "application/x-www-form-urlencoded" http://192.168.22.1:8080/seckill/doseckill*/

    }

}
















