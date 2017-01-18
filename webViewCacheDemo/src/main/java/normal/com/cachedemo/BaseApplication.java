package normal.com.cachedemo;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.utils.Utils;
import com.orhanobut.logger.Logger;

import java.util.Properties;

/**
 * Created by Android Studio.
 * User: Anonymous
 * Date: 2017/1/12
 * Time: 下午2:27
 * Desc: BaseApplication is using for...
 */

public class BaseApplication extends Application {
    public Context applicationContext;
    public static Properties properties;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        Logger.init("Anonymous");
        Utils.init(applicationContext);
        properties = MyPropertiesUtil.getProperties(applicationContext);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
