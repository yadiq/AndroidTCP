package com.hqumath.tcp.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.hqumath.tcp.utils.CommonUtil;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.onAdaptListener;
import me.jessyan.autosize.utils.ScreenUtils;

/**
 * ****************************************************************
 * 文件名称: App
 * 作    者: Created by gyd
 * 创建时间: 2019/1/22 15:31
 * 文件描述:
 * 注意事项:
 * 版权声明:
 * ****************************************************************
 */
public class App extends Application {
    //获取全局上下文  CommonUtil.getContext();
    //private static Application sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化工具类
        CommonUtil.init(this);
        //生命周期监听回调
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        //屏幕适配方案，根据ui图修改,屏幕最小宽度360dp
        AutoSizeConfig.getInstance()
                //全局调节 APP 字体大小 1sp=1dp
                .setPrivateFontScale(1.0f)
                //屏幕适配监听器
                .setOnAdaptListener(new onAdaptListener() {
                    @Override
                    public void onAdaptBefore(Object target, Activity activity) {
                        //使用以下代码, 可以解决横竖屏切换时的屏幕适配问题
                        //使用以下代码, 可支持 Android 的分屏或缩放模式, 但前提是在分屏或缩放模式下当用户改变您 App 的窗口大小时
                        //系统会重绘当前的页面, 经测试在某些机型, 某些情况下系统不会重绘当前页面, ScreenUtils.getScreenSize(activity) 的参数一定要不要传 Application!!!
                        int widthPixels = ScreenUtils.getScreenSize(activity)[0];
                        int heightPixels = ScreenUtils.getScreenSize(activity)[1];
                        AutoSizeConfig.getInstance().setScreenWidth(Math.min(widthPixels, heightPixels));//使用宽高中的最小值计算最小宽度
                        AutoSizeConfig.getInstance().setScreenHeight(Math.max(widthPixels, heightPixels));
                        //AutoSizeLog.d(String.format(Locale.ENGLISH, "%s onAdaptBefore!", target.getClass().getName()));
                    }

                    @Override
                    public void onAdaptAfter(Object target, Activity activity) {
                        //AutoSizeLog.d(String.format(Locale.ENGLISH, "%s onAdaptAfter!", target.getClass().getName()));
                    }
                });
    }

    /*@Override
    protected void attachBaseContext(Context base) {
        //多语言设置
        Context context = MultiLanguageUtil.updateConfiguration(base);
        super.attachBaseContext(context);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //当资源配置发生更改时，例如横竖屏切换，需要重新设置多语言
        MultiLanguageUtil.updateConfiguration(this);
    }*/

    private ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            //注册监听每个activity的生命周期,便于堆栈式管理
            AppManager.getInstance().addActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            AppManager.getInstance().removeActivity(activity);
        }
    };
}
