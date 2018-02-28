### anyRTC-RTMPC-Android SDK for Android
### 简介
基于RTMP和RTC混合引擎的在线视频连麦互动直播

Android 直播（网络自适应码率RTMP publisher）、点播播放器（播放器经过专业优化，可实现秒开RTMP Player）、基于RTMP和RTC混合引擎的视频连麦互动（最多支持4人同时互动）

### 优势
- 商业级开源代码，高效稳定 超小内存占有率，移动直播针对性极致优化，代码冗余率极低 
- iOS,Web,PC全平台适配，硬件编解码可保证99%的可用性
- 接口极简，推流：2个 拉流：2个
- 底层库C++核心库代码风格采用：Google code style
- 极简内核，无需再去深扒复杂的FFMpeg代码
- OpenH264软件编码，FFMpeg软件解码，FAAC/FAAD软件编解码，适配不同系统的硬件编解码统统包含
- 支持SRS、Nginx-RTMP等标准RTMP服务;同时支持各大CDN厂商的接入

### 项目展示
![image](https://github.com/AnyRTC/anyRTC-RTMPC-Android/blob/master/images/1.jpg)
![image](https://github.com/AnyRTC/anyRTC-RTMPC-Android/blob/master/images/2.jpg)
![image](https://github.com/AnyRTC/anyRTC-RTMPC-Android/blob/master/images/3.jpg)
![image](https://github.com/AnyRTC/anyRTC-RTMPC-Android/blob/master/images/4.jpg)
![image](https://github.com/AnyRTC/anyRTC-RTMPC-Android/blob/master/images/5.jpg)
![image](https://github.com/AnyRTC/anyRTC-RTMPC-Android/blob/master/images/6.jpg)
![image](https://github.com/AnyRTC/anyRTC-RTMPC-Android/blob/master/images/7.jpg)
![image](https://github.com/AnyRTC/anyRTC-RTMPC-Android/blob/master/images/8.jpg)
![image](https://github.com/AnyRTC/anyRTC-RTMPC-Android/blob/master/images/9.jpg)


### app体验

##### 扫码下载
![image](https://github.com/AnyRTC/anyRTC-RTMPC-Android/blob/master/images/demo_qrcode.png)
##### [点击下载](https://www.pgyer.com/anyrtc_rtmpc_android)
##### [WEB在线体验](https://www.anyrtc.cc/demo/lianmai)

### SDK集成
# > 方式一（推荐）[ ![Download](https://api.bintray.com/packages/dyncanyrtc/anyrtc_dev/anyRTC-RTMPC-Android/images/download.svg) ](https://bintray.com/dyncanyrtc/anyrtc_dev/anyRTC-RTMPC-Android/_latestVersion)

添加Jcenter仓库 Gradle依赖：

```
dependencies {
  compile 'org.anyrtc:rtmpc_hybrid:2.4'//最新版本见上面Download
}
```

或者 Maven
```
<dependency>
  <groupId>org.anyrtc</groupId>
  <artifactId>rtmpc_hybrid</artifactId>
  <version>2.4</version>
  <type>pom</type>
</dependency>
```

>方式二

 [下载aar SDK](https://www.anyrtc.io/resoure)

>1. 将下载好的rtmpc_hybrid-release.aar文件放入项目的libs目录中
>2. 在Model下的build.gradle文件添加如下代码依赖RTMPC SDK

```
android
{

 repositories {
        flatDir {dirs 'libs'}
    }
    
 }
    
```
```
dependencies {
    compile(name: 'rtmpc_hybrid-release', ext: 'aar')
}
```

### 安装

##### 编译环境

AndroidStudio

##### 运行环境

Android API 15+
真机运行

### 如何使用

##### 注册开发者信息

>如果您还未注册anyRTC开发者账号，请登录[anyRTC官网](http://www.anyrtc.io)注册及获取更多的帮助。

##### 替换开发者账号
在[anyRTC官网](http://www.anyrtc.io)获取了开发者账号，AppID等信息后，替换DEMO中
**Constans**类中的app信息即可（AppVToken需开通视频加速功能）

### 操作步骤

1. 演示需要两部以及两部以上的手机，装上该demo.
2. 一部手机创建直播间，另外两部手机在主页，下拉刷新当前直播列表，点击列表进入直播间。
3. 游客端点击链接按钮，进行连麦。

### 完整文档
SDK集成，API介绍，详见官方完整文档：[点击查看](https://www.anyrtc.io/resoure)

### Ios版anyRTC-Meeting视频会议

[anyRTC-RTMPC-Ios](https://github.com/AnyRTC/anyRTC-RTMPC-iOS)

### Web版anyRTC-Meeting视频会议在线体验

[anyRTC-RTMPC-Web](https://www.anyrtc.cc/demo/lianmai)


### 支持的系统平台
**Android** 4.0及以上

### 支持的CPU架构
**Android** arm64-v8a  armeabi armeabi-v7a


### 注意事项
1. RTMPC SDK所有回调均在子线程中，所以在回调中操作UI等，应切换主线程。
2. 注意安卓6.0+动态权限处理。
3. 常见错误代码请参考[错误码查询](https://www.anyrtc.io/resoure)

### 商业授权
程序发布需商用授权，业务咨询请联系 QQ:580477436 

QQ交流群:554714720

联系电话:021-65650071-839

Email:hi@dync.cc

### 技术支持 
- anyRTC官方网址：[https://www.anyrtc.io](https://www.anyrtc.io/resoure)
- QQ技术咨询群：580477436
- 

### 关于直播

本公司有一整套完整直播解决方案。本公司开发者平台www.anyrtc.io。除了基于RTMP协议的直播系统外，我公司还有基于WebRTC的时时交互直播系统、P2P呼叫系统、会议系统等。快捷集成SDK，便可让你的应用拥有时时通话功能。欢迎您的来电~

### License

- RTMPCEngine is available under the MIT license. See the LICENSE file for more info.





   



 
