package normal.com.cachedemo;

import android.content.Context;

import com.orhanobut.logger.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Android Studio.
 * User: Anonymous
 * Date: 2017/1/18
 * Time: 下午3:37
 * Desc: MyPropertiesUtil is using for...
 */

class MyPropertiesUtil {

    static Properties getProperties(Context context) {
        Properties props = new Properties();
        try {
            //方法一：通过activity中的context攻取setting.properties的FileInputStream
            InputStream in = context.getAssets().open("CacheFileType.properties");
            //方法二：通过class获取setting.properties的FileInputStream
            //InputStream in = PropertiesUtill.class.getResourceAsStream("/assets/  setting.properties "));
            props.load(in);
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
        return props;
    }

}
