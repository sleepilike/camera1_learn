package com.example.testone.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolUtil {

    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * 在线程池执行一个任务
     * @param runnable 任务
     */
    public static void execute(Runnable runnable){
        threadPool.execute(runnable);
    }

}
