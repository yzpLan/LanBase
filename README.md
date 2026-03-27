# LanBase 核心基础组件库

## 📍 快速索引
- [📖简介](#简介)
- [🚀快速集成](#快速集成)
    - [1.Gradle配置](#1Gradle配置)
    - [2.全局初始化(必须)](#2全局初始化)
- [功能模块使用指南](#功能模块使用指南)
    - [📱一、MVP架构与页面开发](#一mvp架构与页面开发)
    - [🌐二、网络请求(Retrofit+RxJava)](#二网络请求retrofitrxjava)
    - [🎨三、通用UI组件](#三通用ui组件)
    - [📚四、通用管理类](#四通用管理类)
    - [🔧五、工具类速查表](#五工具类速查表)
- [⚠注意事项](#注意事项)
---
## 📖简介

`LanBase`是一个 Android 基础架构库。它高度封装了 **MVP 架构**、**网络请求**、**常用 UI 组件** 及 *
*工具类**，旨在统一开发规范，减少重复代码，实现“拿来即用”。

* **最低兼容**: Android 5.0 (API 21)
* **编译版本**: Android 12 (API 31) / Java 8
* **核心依赖**: Retrofit2, RxJava2, ARouter, ViewBinding, SmartRefreshLayout

---

## 🚀快速集成

### 1.Gradle配置

在主工程（App 模块）的 `build.gradle` 中进行以下配置：

```groovy
android {
    // ⚠️ 开启ViewBinding (因为 BaseActivity 依赖自动绑定)
    buildFeatures {
        viewBinding true
        buildConfig = true
    }
}

dependencies {
    // ⚠️引入基础库
//    implementation project(':LanBase') //(module方式)
    implementation 'com.github.yzpLan:LanBase:1.1.0' //依赖方式
}
```
因为项目使用`PickerView`,下载依赖需要在项目根目录gradle中添加阿里镜像:
```groovy
maven { url 'https://maven.aliyun.com/repository/public' }
maven { url 'https://jitpack.io' }
```
由于本库引用了部分较老的第三方 UI 库，为了确保依赖树能够正确转换并避免 Duplicate class 冲突，
请务必在项目根目录的 gradle.properties 文件中添加以下配置：
```groovy
# 开启 AndroidX 支持
android.useAndroidX=true
# 开启 Jetifier 自动转换工具 (解决旧版 Support 库冲突的关键)
android.enableJetifier=true
```

### 2.全局初始化

在您的 `Application` 类中进行初始化：

```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // ⚠️ 初始化基础库（参数:Application 实例）
        BaseApp.init(this);
        // 按需配置 如不设置则不打印日志！（参数:日志自定义TAG）
        BaseApp.initLog("tag");
        // 按需配置,需要权限 如不设置则不保存日志文件！（参数1:日志写入文件地址; 参数2:日志保留天数）
        BaseApp.openLogFileSave(path, 15);
    }
}
```

## 功能模块使用指南

### 📱一、MVP架构与页面开发

`LanBase` 自动处理了 **ViewBinding** 的初始化以及 **Presenter** 的生命周期绑定与解绑。

#### 1.定义接口 (Contract)

```java
public interface LoginContact {
    // 定义 View 接口，继承 IBaseView 以获得通用弹窗能力
    interface View extends IBaseView {
        void loginSuccess(LoginRespDTO data);

        void loginFailure(String errorMsg);
    }

    // 定义 presenter 接口
    interface IPresenter {
        void login(String name, String pwd);
    }
}
```

#### 2.创建 Presenter

继承 `BasePresenter`，可通过 `isViewAttached()` 安全地操作 View。

```java
public class LoginPresenter extends BasePresenter<LoginContact.View> {
    private static final String TAG = "LoginPresenter";
    private ApiService.Service mApi;

    @Override
    protected void createApi() {
        // 初始化接口类
        mApi = PisApi.getInstance().get();
    }

    @Override
    protected boolean interceptError(String code, String msg) {
        //拦截错误码，特殊处理
        if ("99".equals(code)) {
            L.e(TAG, "特殊错误特殊处理");
            return true;
        } else {
            return super.interceptError(code, msg);
        }
    }

    @Override
    public void login(String name, String pwd) {
        // 父类实现网络层封装，getView安全校验
        send(mApi.login(new LoginRequest(name, pwd)), new ApiCall<>() {
            @Override
            public void onSubscribe(Disposable d) {
                getView().showLoading("登录中...");
            }

            @Override
            public void onSuccess(LoginRespDTO data) {
                getView().loginSuccess(data);
            }

            @Override
            public void onError(Throwable e) {
                getView().loginFailure(parseErrorMsg(e));
            }
        });
    }
}
```

#### 3.创建 Activity

继承 `BaseActivity`，实现布局与 Presenter 的自动关联。

```java
import android.view.LayoutInflater;

public class LoginActivity extends BaseActivity<ActivityLoginBinding, LoginPresenter> {

    @Override
    protected ActivityLoginBinding getViewBinding(LayoutInflater inflater) {
        return ActivityLoginBinding.inflate(inflater); // 自动绑定 ViewBinding
    }

    @Override
    protected LoginPresenter createPresenter() {
        return new LoginPresenter(); // 自动关联并绑定 Presenter 生命周期
    }

    @Override
    protected String initTitle() {
        // baseBinding.titleBar获取控件可以做一些自定义设置
        // baseBinding.titleBar.setBackVisible(false);
        return "用户登录"; // 自动设置标题栏，返回 null 则隐藏标题栏
    }

    @Override
    protected void initData() {
        // 业务初始化逻辑
        binding.btnLogin.setOnClickListener(v -> {
            presenter.login("admin", "123456");
        });
    }

    @Override
    public void loginSuccess(LoginRespDTO loginRespDTO) {
        // 成功回调
        showSuccess("登录成功", () -> {
            toast("登录成功");
            LoginManager.getInstance().login(loginRespDTO);
        });
    }

    @Override
    public void loginFailure(String errorMsg) {
        // 失败回调
        showError("登录失败" + errorMsg);
    }
}
```

#### 4.BaseActivity介绍

`BaseActivity/IBaseView` 内置方法
继承 `BaseActivity` 后，你可以在 Activity 或 Presenter (通过 `getView()`) 中直接调用以下方法：

###### 1.状态加载 (Loading)

控制页面耗时操作时的等待状态。

* **`showLoading(String msg)`**
* **`showLoading(String msg, Runnable onRun)`**
  > 显示模态加载弹窗，阻止用户操作。
    * `msg`: (可选) 自定义显示的文字，如 "正在登录..."。传 `null` 或 `""` 则显示默认文案。
    * `onRun`: (可选) 弹框超时120S自动消失的回调。如果不需要回调，可传 `null`。

* **`hideLoading()`**
  > 关闭当前显示的加载弹窗。

###### 2.结果反馈 (Feedback)

针对业务逻辑执行结果的弹窗反馈。

* **`showSuccess(String msg, Runnable onRun)`**
  > ✅ **操作成功**
    * 显示带有“成功”图标的弹窗。
    * `onRun`: (可选) 间隔1S弹框消失的回调。如果不需要回调，可传 `null`。

* **`showError(String msg)`**
  > ❌ **操作失败**
    * 显示带有“失败/警告”图标的弹窗。通常用于 API 请求失败或校验不通过。

###### 3.轻量提示 (Tips)

* **`toast(String msg)`**
  > 底部浮层提示，几秒后自动消失，不打断用户操作。

---

### 🌐二、网络请求(Retrofit+RxJava)

#### 1.定义统一响应体(ApiBaseResponse)

`ApiBaseResponse` 用于接收服务器返回的标准 JSON 结构。你需要根据后端接口文档定义的字段名称（如 `code`,`msg`, `data`）进行对应。

> **关键点：** 使用泛型 `<T>` 来动态解析 `data` 字段中的具体业务对象（如 `LoginRespDTO`）。

```java
public class LiBaseResponse<T> implements BaseResponse<T>, Serializable {
    private String respCode;
    private String respDesc;
    private T respData;

    @Override
    public boolean isSuccess() {
        // 根据业务逻辑判断是否成功
        return "00".equals(respCode);
    }

    @Override
    public T getData() {
        // 返回业务对象
        return respData;
    }

    @Override
    public String getMessage() {
        // 返回错误信息
        return respDesc;
    }

    @Override
    public String getCode() {
        // 返回错误码
        return respCode;
    }
}
```

#### 2.封装 Service(继承 BaseApi)

使用双重锁校验单例模式创建请求服务。

```java
import java.util.List;

public class ApiService extends BaseApi<Service> {
    private static volatile ApiService instance;

    public static ApiService getInstance() {
        if (instance == null) {
            synchronized (ApiService.class) {
                if (instance == null) instance = new ApiService();
            }
        }
        return instance;
    }

    @Override
    protected String getBaseUrl() {
        // 1. 配置域名
        return "https://www.example.com/api/";
    }

    @Override
    protected Class<Service> getServiceClass() {
        // 2. 绑定 Retrofit 接口
        return Service.class;
    }

    @Override
    protected void registerInterceptors(List<Interceptor> interceptors) {
        // 3. 配置拦截器
        // libBase中封装了以下拦截器供继承：
        // BaseResponseCodeInterceptor错误码拦截器
        // BaseSignInterceptor签名拦截器
        // 示例：interceptors.add(new MyInterceptor());
    }

    //  接口定义区
    public interface Service {
        @POST("login")
        Single<LibBaseResponse<LoginRespDTO>> login(@Body LoginRequest request);
    }
}
```

#### 3.发起请求

使用 BasePresenter中的send方法直接快捷发起请求。

```java
public void login(String name, String pwd) {
    // 父类实现网络层封装，getView安全校验
    send(mApi.login(new LoginRequest(name, pwd)), new ApiCall<>() {
        @Override
        public void onSubscribe(Disposable d) {
            getView().showLoading("登录中...");
        }

        @Override
        public void onSuccess(LoginRespDTO data) {
            getView().loginSuccess(data);
        }

        @Override
        public void onError(Throwable e) {
            getView().loginFailure(parseErrorMsg(e));
        }
    });
}
```

#### 4.BasePresenter介绍

`BasePresenter` 是所有 Presenter 的基类，它封装了 **RxJava 生命周期管理** 与 **网络请求流程**，确保请求回调在
View 销毁后不会执行，防止内存泄漏或 Crash。

##### 核心特性

1. **自动生命周期绑定**：请求自动加入 `CompositeDisposable`，页面销毁时自动取消订阅。
2. **安全回调**：内部使用 `SafeObserver`，在回调前自动检查 `isViewAttached()`。
3. **统一线程调度**：内部调用 `RxUtils.handleRequest()`，自动切换 IO/Main 线程。
4. **全局错误拦截**：提供 `interceptError()` 钩子方法，可拦截特定错误码（如 Token 失效）。

### 🎨三、通用UI组件

#### 1.通用标题栏 (CommonTitleBar)

- **无需自己在布局写 Toolbar，直接在 XML 中使用：**

  ```xml
  <com.yzplan.lanbase.view.CommonTitleBar
    android:id="@+id/title_bar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:ctb_title="页面标题"
    app:ctb_showBack="true"
    app:ctb_showLine="true"
    app:ctb_rightText="保存" />
  ```

- **代码中操作：**
  ```java
  baseBinding.titleBar.setRightText("更多", v -> {toast("点击了更多");});
  ```

#### 2.通用弹窗(DialogHelper)

```java
// 双按钮确认框
DialogHelper.showConfirm(context, "温馨提示","确定要退出吗？",v ->{// 点击确定的回调});

// 倒计时自动关闭框
DialogHelper.showSingleCountDown(context, "提示","操作成功，3秒后关闭","确定",3,v ->{finish();});
```

#### 3.列表助手(RecycleViewHelper)

封装了 `SmartRefreshLayout` 与 `RecyclerView` 的联动逻辑，支持分页管理和缺省页自动切换。

> 导入`RecycleView`控件

```xml
<include
    android:id="@+id/listView"
    layout="@layout/lib_layout_common_list"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_weight="1" />
```

> 使用`RecycleView`控件
1. **绑定**：关联布局与数据实体。
2. **加载**：在 `loadFrom` 中编写接口请求（Helper 自动维护页码）。
3. **反馈**：请求成功调用 `notifyData(list)`，失败调用 `notifyError()`。

```java
private RecycleViewHelper<OrderRespDTO> recycleViewHelper;

/**
 * OrderRespDTO: 订单列表实体类
 * binding.listView: 列表控件
 * R.layout.lib_item_order:列表item 
 */
private void initRecycle() {
    recycleViewHelper = new RecycleViewHelper<OrderRespDTO>(LibLayoutCommonListBinding.bind(binding.listView))
            .setLayout(R.layout.lib_item_order, (holder, item, position) -> {
                holder.getView(R.id.tv_title).setText(item.getTitle());
            })
            .loadFrom((page, pageSize) -> {
                // 执行分页加载逻辑
                presenter.getOrderList(page, pageSize);
            })
            .setOnItemClickListener((view, item, pos) -> {
                // 点击处理
            })
            .start();
}

// 在 MVP 的 View 回调中更新数据
@Override
public void getOrderListSuccess(List<OrderBean> data) {
    // ⚠️ 关键步骤：将网络请求的数据喂给 Helper，它会自动处理刷新或加载更多
    listHelper.notifyData(data);
}

@Override
public void getOrderListFailure(String msg) {
    // ⚠️ 关键步骤：请求失败时调用，Helper 会自动处理页码回退和结束刷新动画
    listHelper.notifyError();
}
```
#### 4. 范围日期选择器 (DatePickerView)

- **核心属性说明：**
    - `app:picker_mode`: 精度控制。`day` (仅日期), `time` (日期+时间)。
    - `app:is_single`: 模式控制。`true` (单选), `false` (范围选择)。
    - `app:limit_days`: 范围限制。设置可选的最早日期（距离今天的偏移天数，如 180）。

  ```xml
  <com.yzplan.lanbase.view.DatePickerView
    android:id="@+id/datePicker"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:is_single="false"
    app:picker_mode="time"
    app:picker_divider_text=" 至 "
    app:picker_divider_color="#FF0000" 
    app:picker_item_background="@drawable/lib_selector_main_red_outline" />
  ```
- **代码中操作（支持链式调用）：**
    ```xml
        // 1. 基本配置与样式修改
        binding.datePicker
                .setSingle(false)                           // 设置为范围选择
                .setDateMode(DatePickerView.DateMode.TIME)  // 设置为时间精度
                .setLimitDays(90)                           // 限制只能选择最近 90 天
                .setDividerText(" ～ ")                      // 修改中间的分隔符文字
                .setDividerColor(Color.RED)                 // 修改中间分隔符颜色 (此处加上)
                .setItemBackgroundResource(R.drawable.custom_bg); // 动态修改背景

        // 2. 设置初始默认时间
        binding.datePicker.setDefaultDate(new Date(), new Date());

        // 3. 监听日期变更
        binding.datePicker.setOnDateChangeListener((start, end) -> {
            String startTime = TimeUtils.dateToString(start, TimeUtils.FORMAT_YMDHMS);
            String endTime = TimeUtils.dateToString(end, TimeUtils.FORMAT_YMDHMS);
            L.i("选中时间范围: " + startTime + " 到 " + endTime);
        });
  ```
- **常用 API 说明：**

| 方法名                                  | 说明                                      |
|:-------------------------------------|:----------------------------------------|
| `setDateMode(DateMode)`              | 切换显示精度，支持 `DAY` (年月日) 和 `TIME` (年月日时分秒) |
| `setDefaultDate(Date, Date)`         | 动态设置选择器的当前显示数值                          |
| `setSingle(boolean)`                 | 设置单选模                                   |
| `setLimitDays(int)`                  | 限制最小可选日期（距离今天的偏移天数）                     |
| `getStartDate() / getEndDate()`      | 直接获取当前选中的 Date 对象                       |

#### 5. Loading框 (CommonLoadingDialog)
- **如上BaseActivity介绍中提到的使用方法**
- 
#### 5. 设置条 (SettingItemView)
SettingItemView 是一个高度可定制的列表项控件，专为个人中心、设置页面设计。支持图标、标题、右侧文字、箭头的全方位属性控制。
* ##### 📘 SettingItemView 属性全量说明

`SettingItemView` 支持通过 XML 属性高度定制化，涵盖了图标、标题、右侧文字、箭头及其间距的所有控制。

| 属性名                          | 格式        | 描述          | 默认值     |
|:-----------------------------|:----------|:------------|:--------|
| `app:siv_icon`               | reference | 左侧图标资源      | 无       |
| `app:siv_icon_size`          | dimension | 图标宽高尺寸      | 20dp    |
| `app:siv_title`              | string    | 标题内容        | 无       |
| `app:siv_title_size`         | dimension | 标题文字大小      | 16sp    |
| `app:siv_title_color`        | color     | 标题文字颜色      | #333333 |
| `app:siv_right_text`         | string    | 右侧描述文字内容    | 无       |
| `app:siv_right_text_size`    | dimension | 右侧文字大小      | 14sp    |
| `app:siv_right_text_color`   | color     | 右侧文字颜色      | #999999 |
| `app:siv_show_arrow`         | boolean   | 右侧箭头是否展示    | true    |
| `app:siv_arrow_size`         | dimension | 右侧箭头宽高尺寸    | 16dp    |
| `app:siv_arrow_color`        | color     | 箭头着色 (Tint) | #CCCCCC |
| `app:siv_arrow_margin_start` | dimension | 箭头与右侧文字的间距  | 8dp     |

* ##### 💻 XML 使用示例

```xml
//全量属性示例
<com.yzplan.lanbase.view.SettingItemView
    android:id="@+id/siv_profile"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:siv_icon="@drawable/ic_avatar"
    app:siv_icon_size="45dp"
    app:siv_title="修改头像"
    app:siv_title_size="18sp"
    app:siv_title_color="#222222"
    app:siv_right_text="点击更换"
    app:siv_right_text_size="14sp"
    app:siv_right_text_color="#007AFF"
    app:siv_arrow_size="20dp"
    app:siv_arrow_color="#007AFF"
    app:siv_arrow_margin_start="15dp" />
    
//简洁属性示例
<com.yzplan.lanbase.view.SettingItemView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:siv_icon="@drawable/ic_score"
    app:siv_title="我的积分"
    app:siv_right_text="1784" />
```
* ##### 💻 Java 代码动态调用
```java
    binding.siv.setTitle("我的勋章")
       .setTitleColor(Color.BLACK)
       .setRightText("已获得3枚")
       .setRightTextColor(Color.RED)
       .setIcon(R.drawable.ic_medal)
       .setIconSize(24)
       .setArrowColor(Color.GRAY)
       .setOnItemClickListener(v -> {
        // 处理点击逻辑
        });
```

### 📚四、通用管理类

#### 💡模块简介

* **🔐 ILoginManager**：采用接口化设计，在 App 模块实现具体存储逻辑，实现业务解耦。
* **🏗️ ActivityManager**：配合 `BaseActivity` 使用，在 `onCreate` 时自动入栈，`onDestroy` 时自动出栈。
* **⏳ LoadingHelper**：负责 Loading 弹窗的显示、复用和状态更新，提供超时保护以防止长久转圈。
* **💬 AlertDialogHelper**：基于 `CommonAlertDialog` 封装，支持双按钮询问、单按钮提示以及倒计时自动确认/取消等多种模式。
* **📃 RecycleViewHelper**：支持 ViewBinding 快速绑定，内部自动维护分页页码 `pageNo`，并提供智能的空页面（EmptyView）切换逻辑。
* **🖱️ SingleClickListener**：继承自 `View.OnClickListener`，内部调用 `ClickUtils` 校验点击间隔，默认拦截 800ms 内的重复点击。
* **📢 LiveEventBus (消息总线)**： 用于跨模块/跨页面且无直接关联的消息通知。利用`LiveData` 的特性，确保事件只在 View 处于活跃状态时接收，避免由于 Activity 销毁导致的回调异常“。(子线程/主线程发 ->主线程收。支持粘性和单次非粘性消息)
* **⚠️ CrashHandler** ：全局 Java 异常捕获器。崩溃时自动拼接设备快照信息（型号、SDK、线程名），并利用同步写入机制（Sync）确保“临终遗言”完整落盘，实现线上问题精准复现。
---

| 分类  | 管理类名                    | 功能描述                                  | 核心方法示例                                                                    |
|:----|:------------------------|:--------------------------------------|:--------------------------------------------------------------------------|
| 🔐  | **`ILoginManager`**     | 登录业务标准化接口，用于规范用户信息存储与状态维护。            | `isLogin()`, `logout(boolean isForce)`                                    |
| 🏗️ | **`ActivityManager`**   | 统一管理 Activity 堆栈，支持一键退出及结束指定页面。       | `addActivity()`, `finishAllActivity()`                                    |
| 💬  | **`DialogHelper`**      | 快捷构建标准询问、单按钮提示及带倒计时的业务弹窗。             | `showConfirm()`, `showSingleCountDown()`                                  |
| ⏳   | **`LoadingHelper`**     | 统筹管理 Loading 状态，支持自动处理超时保护。           | `showLoading()`, `showSuccess()`, `dismiss()`                             |
| 📃  | **`RecycleViewHelper`** | 极简列表助手，封装了分页、刷新、点击及缺省页逻辑。             | `setLayout()`, `loadFrom()`, `notifyData()`                               |
| 🖱️ | **`SingleClick`**       | 统一的点击监听包装类，基于 `ClickUtils` 实现全局防抖。    | `onSingleClick(View v)`                                                   |
| 📢  | 	**`LiveEventBus`**     | 跨组件通信总线，用于替代 EventBus。具备生命周期感知，无须解绑。	 | `with(key, type).set(data)`,`.asSingleEvent()`, `observe(this, observer)` |

---

### 🔧五、工具类速查表

#### 💡模块简介
`LanBase` 提供了丰富的工具类，统一位于 `com.yzplan.lanbase.utils` 包下。

| 工具类名                | 功能描述       | 常用方法示例                                                        |
|:--------------------|:-----------|:--------------------------------------------------------------|
| **L**               | 日志打印       | `L.i("msg")`, `L.e("tag", "msg")`, `L.json(str)`              |
| **SpUtils**         | SP 存储      | `putString()`, `getString()`, `getBoolean()`                  |
| **TimeUtils**       | 时间处理       | `getNowString()`, `millisToString()`, `stringToMillis()`      |
| **MoneyUtils**      | 金额转换       | `fenToYuan("100")`  , `yuanToFen("1")`                        |
| **JsonUtil**        | JSON 解析    | `toJson(obj)`, `fromJson(str, Class)`                         |
| **StringUtils**     | 字符串操作      | `isNullOrEmpty()`, `hideMobile("138...")`                     |
| **ClickUtils**      | 防抖点击       | `isFastClick(view)`                                           |
| **PermissionUtils** | 权限申请       | `request(activity, callback, permissions...)`                 |
| **AppUtils**        | App 信息     | `getVersionName()`, `getVersionCode()`                        |
| **NetUtils**        | 网络状态       | `isConnected()`, `isWifiConnected()`                          |
| **DensityUtils**    | 屏幕单位       | `dp2px()`, `px2dp()`, `getScreenWidth()`                      |
| **KeyboardUtils**   | 软键盘控制      | `autoHideKeyboard()`                                          |
| **StatusBarUtils**  | 状态栏设置      | `setStatusBar()`, `setTransparentStatusBar()`                 |
| **QRCodeUtils**     | 二维码生成      | `createQRCode("content", 400)`                                |
| **OrderIdUtils**    | 订单号生成      | `getLocalTermOdrId()` (生成唯一流水号)                               |
| **DatePickUtils**   | 日期选择       | `showDayPicker()`, `showTimePicker()`                         |
| **GlideUtils**      | 图片加载       | `load()`, `loadCircle()` ,`loadRounded()`                     |
| **LogFileUtils**    | 业务日志管理     | `init(path)`, `writeLogAsync(tag, msg)` ,`getMergedZipFile()` |
| **FileUtils**       | 文件原子操作     | `delete(file)`, `copyFile(src, dest)` ,`formatSize(long)`     |
| **ZipUtils**        | 文件压缩工具     | `zip(resFile, zipFile)`, `zipFiles(list, zipFile)`            |
| **ToastUtils**      | 线程安全 Toast | `showShort("msg")`                                            |

## ⚠注意事项

1. **资源命名规范**  
   Lib 内部的所有资源（Layout、Drawable、String、Color）必须以 `lib_` 开头。  
   禁止在主工程中定义与 Lib 同名的资源，以防合并冲突。
2. **混淆配置**  
   Lib 已内置 `consumer-rules.pro`，主工程打 Release 包时会自动应用混淆规则，无需额外配置(尽量不要开启混淆)。
3. **如使用ARouter，其适配器可自行实现。**
4. **如有依赖down不下来，建议使用国内镜像**
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 【核心】把阿里云镜像放在最前面！
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        // 这是一个 Github 库，有时阿里云也没同步到，保留 Jitpack
        maven { url 'https://jitpack.io' }
        // 官方源放最后兜底
        mavenCentral()
        google()
        jcenter()
    }
}
```
5. **ARouter依赖如下：**
```groovy
 defaultConfig {
    // ... 其他配置

    // 配置 ARouter 路由表生成参数
    javaCompileOptions {
        annotationProcessorOptions {
            arguments = [AROUTER_MODULE_NAME: project.getName()]
        }
    }
}

// 路由 API
    api 'com.alibaba:arouter-api:1.5.2'
    // 路由编译器 (每个模块都要独立配置)
    annotationProcessor 'com.alibaba:arouter-compiler:1.5.2'

    if (BuildConfig.DEBUG) {
        ARouter.openLog();     // 打印日志
        ARouter.openDebug();   // 开启调试模式
    }
    ARouter.init(this);

```

6. **上送日志相关写法**
```java
@Override
    public void upLoadFile(String startTime, String endTime) {
        Disposable d = RxUtils.doTask(() -> LogFileUtils.getMergedZipFile(startTime, endTime), file -> {
            if (file != null && file.exists()) {
                uploadToServer(file, startTime, endTime);
            } else {
                getView().upLoadFileFailure("未找到相关日志或打包失败");
            }
        }, throwable -> getView().upLoadFileFailure("未找到相关日志或打包失败"));
        addDisposable(d);
        getView().showLoading("获取日志中...");
    }


    private void uploadToServer(File file, String startTime, String endTime) {
        // 1. 准备核心业务参数
        Map<String, RequestBody> partMap = new HashMap<>();
        String startFull = startTime + "000000";
        String endFull = endTime + "235959";
        long startTimeMill = TimeUtils.stringToMillis(startFull, TimeUtils.FORMAT_FULL_SN);
        long endTimeMill = TimeUtils.stringToMillis(endFull, TimeUtils.FORMAT_FULL_SN);
        partMap.put("startDate", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(startTimeMill)));
        partMap.put("endDate", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(endTimeMill)));
        // 2. 准备文件 Part
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
        // 3. 直接发送
        send(mApi.reportLog(partMap, filePart), new ApiCall<>() {
            @Override
            public void onSubscribe(Disposable d) {
                getView().showLoading("上送日志中...");
            }

            @Override
            public void onSuccess(EmptyResponse data) {
                LogFileUtils.clearTempFiles(); // 只有成功才清理
                getView().upLoadFileSuccess();
            }

            @Override
            public void onError(Throwable e) {
                getView().upLoadFileFailure("上传失败: " + e.getMessage());
            }
        });
    }

     @Multipart
     @POST("terminal/reportLog")
     Single<LibBaseResponse<EmptyResponse>> reportLog(@PartMap Map<String, RequestBody> params, @Part MultipartBody.Part file);
```
---

## 感谢您的支持！

&nbsp;
