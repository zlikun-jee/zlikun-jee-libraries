package com.zlikun.learing;

import lombok.extern.slf4j.Slf4j;
import org.hashids.Hashids;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author zlikun <zlikun-dev@hotmail.com>
 * @date 2017-12-21 09:11
 */
@Slf4j
public class HashidsTest {

    @Test
    public void test() {

        String salt = "zlikun";
        Hashids hashids = new Hashids(salt);

        // 编码
        String stringHash = hashids.encodeHex("123456");
        assertThat(stringHash, is("b6pM4n"));

        // 解码
        assertThat(hashids.decodeHex(stringHash), is("123456"));

        // 数组编码
        String numberHash = hashids.encode(1, 1, 2, 3, 5, 8);
        assertThat(numberHash, is("67uQuVsPU7fb"));

        // 数组解码
        long [] numbers = hashids.decode(numberHash);
        assertEquals(1L, numbers[0]);
        assertEquals(2L, numbers[2]);
        assertEquals(8L, numbers[5]);

    }

    @Test
    public void performance() {

        final String salt = "zlikun";
        final Hashids hashids = new Hashids(salt);

        ExecutorService exec = Executors.newFixedThreadPool(20);

        long time = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            exec.execute(() -> {
                String stringHash = hashids.encodeHex("123456");
                hashids.decodeHex(stringHash);
            });
        }

        exec.shutdown();
        while (!exec.isTerminated());

        // 单个线程：5,036 | 5,124 | 4,986
        // 20个线程：4,123 | 4,150 | 3,934
        log.info("程序执行耗时：{} 毫秒", System.currentTimeMillis() - time);

    }

}
