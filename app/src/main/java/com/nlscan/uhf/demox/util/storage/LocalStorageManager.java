package com.nlscan.uhf.demox.util.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.nlscan.uhf.demox.AppApplication;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalStorageManager {
    /**
     * 全局范围的SharePreference表名
     **/
    public static final String GLOBAL_SHARED_FILE = "global_shared_file";
    public static final String ACHIEVE_SHARED_FILE = "achieve_shared_file";

    private static Map<Integer, SharedPreferences> sSharedPreferences = new ConcurrentHashMap<>();
    private static final int DEFAULT_SHARED_PREFERENCE_CAPACITY = 4;

    private static SharedPreferences getSharedPreference(String key) {
        return getHashSharedPreference(key);
    }

    /**
     * 根据hash算法拆分的SharedPreference
     *
     * @param key
     * @return
     */
    private static SharedPreferences getHashSharedPreference(String key) {
        int index = getKeyHash(key);
        SharedPreferences sharedPreferences = sSharedPreferences.get(index);
        if (sharedPreferences == null) {
            sharedPreferences = getHashSharedPreference(index);
            sSharedPreferences.put(index, sharedPreferences);
        }
        return sharedPreferences;
    }

    public static SharedPreferences getHashSharedPreference(int hashCodeIndex) {
        return AppApplication.getInstance().getSharedPreferences(GLOBAL_SHARED_FILE + "_" + hashCodeIndex, Context.MODE_PRIVATE);
    }

    public static int getKeyHash(String key) {
        return Math.abs(key.hashCode()) % DEFAULT_SHARED_PREFERENCE_CAPACITY;
    }

    private static boolean checkMainProcess(String key) {
//        if (BuildConfig.TEST_MODE) {
//            if (AppUtils.isMainProcess(AppApplication.getInstance())) {
//                return true;
//            } else {
//                ToastUtils.showLong("set <" + key + "> in deamon process", Toast.LENGTH_LONG);
//                return false;
//            }
//        }
        return true;
    }

    public static boolean hasKey(String key) {
        if (checkMainProcess(key)) {
            return getSharedPreference(key).contains(key);
        }
        return false;
    }

    public static void setBoolean(String key, boolean value) {
        if (checkMainProcess(key)) {
            getSharedPreference(key).edit().putBoolean(key, value).apply();
        }
    }

    public static void setInt(String key, int value) {
        if (checkMainProcess(key)) {
            getSharedPreference(key).edit().putInt(key, value).apply();
        }
    }

    public static int getAndIncrease(String key) {
        int result = getInt(key, 0);
        int temp = result;
        setInt(key, ++temp);
        return result;
    }

    public static void setLong(String key, Long value) {
        if (checkMainProcess(key)) {
            getSharedPreference(key).edit().putLong(key, value).apply();
        }
    }

    public static void setFloat(String key, float value) {
        if (checkMainProcess(key)) {
            getSharedPreference(key).edit().putFloat(key, value).apply();
        }
    }

    public static void setString(String key, String value) {
        if (checkMainProcess(key)) {
            getSharedPreference(key).edit().putString(key, value).apply();
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        if (checkMainProcess(key)) {
            return getSharedPreference(key).getBoolean(key, defaultValue);
        }
        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        if (checkMainProcess(key)) {
            return getSharedPreference(key).getInt(key, defaultValue);
        }
        return defaultValue;
    }

    public static long getLong(String key, long defaultValue) {
        if (checkMainProcess(key)) {
            return getSharedPreference(key).getLong(key, defaultValue);
        }
        return defaultValue;
    }

    public static float getFloat(String key, float defaultValue) {
        if (checkMainProcess(key)) {
            return getSharedPreference(key).getFloat(key, defaultValue);
        }
        return defaultValue;
    }

    public static String getString(String key, String defaultValue) {
        if (checkMainProcess(key)) {
            return getSharedPreference(key).getString(key, defaultValue);
        }
        return defaultValue;
    }

    public static Set<String> getStringSet(String key, Set<String> defaultValues) {
        return getSharedPreference(key).getStringSet(key, defaultValues);
    }

    public static boolean contains(String key) {
        return getSharedPreference(key).contains(key);
    }

    public static void remove(String key) {
        getSharedPreference(key).edit().remove(key).apply();
    }
}
