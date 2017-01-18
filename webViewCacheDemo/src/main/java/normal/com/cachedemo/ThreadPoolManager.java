package normal.com.cachedemo;

import com.orhanobut.logger.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by Android Studio.
 * User: Anonymous
 * Date: 2017/1/12
 * Time: 下午3:33
 * Desc: ThreadPoolManager is using for...
 */

public class ThreadPoolManager {

    private static final ThreadPoolManager instance = new ThreadPoolManager();

    private ExecutorService threadPool = Executors.newFixedThreadPool(100);

    public static ThreadPoolManager getInstance() {
        return instance;
    }

    /**
     * @param runnable 不返回执行结果的异步任务
     */
    public void addTask(Runnable runnable) {
        try {
            if (runnable != null) {
                threadPool.execute(runnable);
            }
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
    }

    /**
     * @param callback 异步任务
     * @return 你可以获取相应的执行结果
     */
    FutureTask<Boolean> addTaskCallback(Callable<Boolean> callback) {
        if (callback == null) {
            return null;
        } else {
            FutureTask<Boolean> futureTask = new FutureTask<>(callback);
            threadPool.submit(futureTask);
            return futureTask;
        }

    }
// 这是一个demo，如果你看不懂，可以打开跑一下
//    public static void main(String args[]) {
//        FutureTask ft = ThreadPoolManager.getInstance().addTaskCallback(new Callable<Object>() {
//            @Override
//            public Object call() throws Exception {
//                int sum = 0;
//                for (int i = 0; i < 1000; i++) {
//                    sum++;
//                }
//                return sum;
//            }
//        });
//        try {
//            System.out.println("执行结果是:" + ft.get());
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
//
//    }
}
