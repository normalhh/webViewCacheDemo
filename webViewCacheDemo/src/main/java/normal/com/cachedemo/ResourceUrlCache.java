package normal.com.cachedemo;

import android.webkit.WebResourceResponse;

import com.blankj.utilcode.utils.FileUtils;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by Android Studio.
 * User: Anonymous
 * Date: 2017/1/12
 * Time: 下午3:31
 * Desc: ResourceUrlCache is using for...
 */

public class ResourceUrlCache {

    private Map<String, CacheEntity> cacheEntries = new HashMap<>();
    private static final long ONE_SECOND = 1000L;
    private static final long ONE_MINUTE = 60L * ONE_SECOND;
    static final long ONE_HOUR = 60 * ONE_MINUTE;
    static final long ONE_DAY = 24 * ONE_HOUR;
    static final long ONE_MONTH = 30 * ONE_DAY;

    private static String CACHEPATH = "";

    private static final LinkedHashMap<String, Callable<Boolean>> queueMap = new LinkedHashMap<>();


    private static class CacheEntity {
        String url;
        String fileName;
        String mimeType;
        public String encoding;
        long maxAgeMillis;

        private CacheEntity(String url, String fileName, String mimeType, String encoding, long maxAgeMillis) {
            this.url = url;
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.encoding = encoding;
            this.maxAgeMillis = maxAgeMillis;
        }
    }

    ResourceUrlCache() {
        CACHEPATH = BaseApplication.properties.getProperty("CACHEPATH");
    }

    void register(String url, String cacheFileName, String mimeType, String encoding, long maxAgeMillis) {
        CacheEntity entity = new CacheEntity(url, cacheFileName, mimeType, encoding, maxAgeMillis);
        this.cacheEntries.put(url, entity);
    }

    // 这个方法是缓存资源文件
    WebResourceResponse load(final String url) {
        try {
            final CacheEntity cacheEntity = this.cacheEntries.get(url);
            if (cacheEntity == null) {
                return null;
            }

            final File cachedFile = new File(CACHEPATH + cacheEntity.fileName);
            if (cachedFile.exists() && !cachedFile.getName().contains(".jsp")) {
                //还没有下载完,在快速切换URL的时候，可能会有很多task并没有及时完成，所以这里需要一个map用于存储正在下载的URL，下载完成后需要移除相应的task
                if (queueMap.containsKey(url)) {
                    return null;
                }
                //过期后直接删除本地缓存内容
                long cacheEntryAge = System.currentTimeMillis() - cachedFile.lastModified();
                if (cacheEntryAge > cacheEntity.maxAgeMillis) {
                    cachedFile.delete();
                    if (BuildConfig.DEBUG) {
                        Logger.d("Deleting from cache: " + url);
                    }
                    return null;
                }
                //cached file exists and is not too old. Return file.
                if (BuildConfig.DEBUG) {
                    Logger.d(url + " ### cache file : " + cachedFile.getAbsolutePath());
                }
                return new WebResourceResponse(cacheEntity.mimeType, cacheEntity.encoding, new FileInputStream(cachedFile));
            } else {
                if (!queueMap.containsKey(url) && !url.startsWith("file:///") && !url.contains("iplookup.php") && !url.contains("login" +
                        ".do")) {
                    queueMap.put(url, new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return downloadAndStore(url, cacheEntity);
                        }
                    });
                    final FutureTask<Boolean> futureTask = ThreadPoolManager.getInstance().addTaskCallback(queueMap.get(url));
                    ThreadPoolManager.getInstance().addTask(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (futureTask.get()) {
                                    if (BuildConfig.DEBUG) {
                                        Logger.d("remove " + url);
                                    }
                                    queueMap.remove(url);
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                Logger.e(e.getMessage());
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            Logger.e("Error reading file over network: " + e.getMessage());
        }
        return null;
    }

    //这个方法是资源下载
    private boolean downloadAndStore(final String url, final CacheEntity cacheEntity) throws IOException {
        FileOutputStream fileOutputStream;
        InputStream urlInput;
        try {
            URL urlObj = new URL(url);
            URLConnection urlConnection = urlObj.openConnection();
            urlInput = urlConnection.getInputStream();
            String[] dirAndFile;
            String tempFilePath = BaseApplication.properties.getProperty("CACHEPATH");
            if (cacheEntity.fileName.contains("/")) {
                dirAndFile = cacheEntity.fileName.split("/");
                Logger.d(dirAndFile);
                if (dirAndFile.length > 1) {
                    for (int i = 0; i < dirAndFile.length - 1; i++) {
                        tempFilePath += dirAndFile[i] + File.separator;
                        Logger.d(tempFilePath);
                    }
                }
                FileUtils.createOrExistsDir(tempFilePath);
                tempFilePath += dirAndFile[dirAndFile.length - 1] + ".temp";
                Logger.d("有新的目录：" + tempFilePath);
            } else {
                Logger.d("没有新的目录：" + tempFilePath);
                FileUtils.createOrExistsDir(tempFilePath);
                tempFilePath += cacheEntity.fileName + ".temp";
                FileUtils.createOrExistsFile(tempFilePath);
            }

            File tempFile = new File(tempFilePath);
            fileOutputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = urlInput.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.flush();
            File lastFile = new File(tempFilePath.replace(".temp", ""));
            boolean renameResult = tempFile.renameTo(lastFile);
            if (!renameResult) {
                Logger.d("rename file failed, " + tempFilePath);
            }
            urlInput.close();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
        return false;
    }

}
