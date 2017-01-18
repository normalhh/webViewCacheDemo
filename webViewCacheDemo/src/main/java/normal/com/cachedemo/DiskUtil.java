package normal.com.cachedemo;

import android.content.Context;

import com.blankj.utilcode.utils.FileUtils;
import com.blankj.utilcode.utils.SDCardUtils;
import com.orhanobut.logger.Logger;

import java.io.File;

/**
 * Created by Android Studio.
 * User: Anonymous
 * Date: 2017/1/12
 * Time: 下午2:44
 * Desc: DiskUtil is using for...
 */
public class DiskUtil {

    public static File getDiskCacheDir(Context context) {
        File file = new File(SDCardUtils.getSDCardPath() + "/cacheDemo");

        if (FileUtils.createOrExistsDir(file)) {
            Logger.d(file.getAbsolutePath());
            return file;
        } else {
            boolean b = file.mkdirs();
        }

        return file;
    }
}
