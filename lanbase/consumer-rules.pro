# ===== Gson / Retrofit 核心属性保留 =====
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod

# ===== 保留源码文件名和行号（方便线上崩溃排查）=====
-keepattributes SourceFile,LineNumberTable

# ===== lanbase 全量保护=====
# 保护你自己库里所有的代码不被混淆，确保反射和调用正常
-keep class com.yzplan.lanbase.** { *; }

# ===== BaseResponse 及其子类（重点保护数据实体）=====
# 只要是继承了 BaseResponse 的模型类都会被保护
-keep class com.yzplan.lanbase.base.BaseResponse { *; }
-keep class * extends com.yzplan.lanbase.base.BaseResponse { *; }

# ===== Serializable 实体成员保留 =====
-keepclassmembers class * implements java.io.Serializable {
    <fields>;
}

# ===== 核心三方库保护 (Retrofit + RxJava + OkHttp) =====
-keep class retrofit2.** { *; }
-keep interface retrofit2.http.** { *; }
-keep class io.reactivex.** { *; }
-keep class okhttp3.** { *; }

# ===== 忽略警告（防止打包时因为三方库引用缺失报错）=====
-dontwarn retrofit2.**
-dontwarn io.reactivex.**
-dontwarn okhttp3.**
-dontwarn com.yzplan.lanbase.**
-dontwarn com.contrarywind.**