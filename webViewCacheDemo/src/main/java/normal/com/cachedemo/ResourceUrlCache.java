package normal.com.cachedemo;

import android.webkit.WebResourceResponse;

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

    private static final long ONE_SECOND = 1000L;
    private static final long ONE_MINUTE = 60L * ONE_SECOND;
    static final long ONE_HOUR = 60 * ONE_MINUTE;
    static final long ONE_DAY = 24 * ONE_HOUR;
    static final long ONE_MONTH = 30 * ONE_DAY;

    private static final LinkedHashMap<String, Callable<Boolean>> queueMap = new LinkedHashMap<>();


    private static class CacheEntity {
        public String url;
        public String fileName;
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

    private Map<String, CacheEntity> cacheEntries = new HashMap<>();
    private File rootDir = null;

    ResourceUrlCache() {
        //本地缓存路径，请在调试中自行修改
        this.rootDir = DiskUtil.getDiskCacheDir(BaseApplication.applicationContext);
    }

    public void register(String url, String cacheFileName, String mimeType, String encoding, long maxAgeMillis) {
        CacheEntity entity = new CacheEntity(url, cacheFileName, mimeType, encoding, maxAgeMillis);
        this.cacheEntries.put(url, entity);
    }

    // 这个方法是缓存资源文件
    public WebResourceResponse load(final String url) {
        try {
            final CacheEntity cacheEntity = this.cacheEntries.get(url);
            if (cacheEntity == null) {
                return null;
            }

            final File cachedFile = new File(this.rootDir.getAbsolutePath()+ File.separator + cacheEntity.fileName);
            Logger.d("cachedFile is " + cachedFile);
            if (cachedFile.exists() && isReadFromCache(url)) {
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
                if (!queueMap.containsKey(url)) {
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
                                Logger.e(String.valueOf(e));
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            Logger.e("Error reading file over network: " + String.valueOf(e));
        }
        return null;
    }

    //这个方法是资源下载
    private boolean downloadAndStore(final String url, final CacheEntity cacheEntity) throws IOException {
        FileOutputStream fileOutputStream = null;
        InputStream urlInput = null;
        try {
            URL urlObj = new URL(url);
            URLConnection urlConnection = urlObj.openConnection();
            urlInput = urlConnection.getInputStream();
            String tempFilePath = ResourceUrlCache.this.rootDir.getAbsolutePath() + File.separator + cacheEntity.fileName + ".temp";
            Logger.d(tempFilePath);
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
                Logger.w("rename file failed, " + tempFilePath);
            }
            return true;
        } catch (Exception e) {
            Logger.e(String.valueOf(e));
        } finally {
            if (urlInput != null) {
                urlInput.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
        return false;
    }

    private boolean isReadFromCache(String url) {
        return true;
    }

}
