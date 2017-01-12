package normal.com.cachedemo;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.utils.Utils;
import com.orhanobut.logger.Logger;

/**
 * Created by Android Studio.
 * User: Anonymous
 * Date: 2017/1/12
 * Time: 下午2:27
 * Desc: BaseApplication is using for...
 */

public class BaseApplication extends Application {
    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        Logger.init("Anonymous");
        Utils.init(applicationContext);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
