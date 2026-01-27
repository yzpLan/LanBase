package com.yzplan.lanbase.app.arouter;

import android.content.Context;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Interceptor;
import com.alibaba.android.arouter.facade.callback.InterceptorCallback;
import com.alibaba.android.arouter.facade.template.IInterceptor;
import com.alibaba.android.arouter.launcher.ARouter;
import com.yzplan.lanbase.app.manager.LoginManager;
import com.yzplan.lanbase.utils.log.L;


/**
 * 登录状态检查拦截器
 * 优先级越低，越早执行。我们给它一个较高的优先级（例如 8），让基础路由检查先执行。
 */
@Interceptor(priority = 8, name = "登录状态检查")
public class LoginInterceptor implements IInterceptor {

    @Override
    public void init(Context context) {
    }

    @Override
    public void process(Postcard postcard, InterceptorCallback callback) {
        String path = postcard.getPath();
        if (!ARouterPath.LoginActivity.equals(path) && !LoginManager.getInstance().isLogin()) {
            L.e("用户未登录");
            callback.onInterrupt(new RuntimeException("用户未登录，访问受限"));
            ARouter.getInstance().build(ARouterPath.LoginActivity).navigation();
        } else {
            callback.onContinue(postcard);
        }
    }
}
