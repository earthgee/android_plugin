# android_plugin
android组件化，插件化，热发布学习

## plugin1
此插件参考https://github.com/singwhatiwanna/dynamic-load-apk  
原理:host项目中插桩作为代理(activity,service都是),实际的插件组件通过运行时动态生成,代理组件收到生命周期回调再回调插件组件,通过hack方式获得插件dex包的AssetManager,Resource来操作插件中的资源  
优点和不足:原理简单，没有hook任何framework层组件代码，但是通过应用层代理的方式也使得静态注册的BroadcastReceiver和ContentProvider无法运行在插件中

## plugin2  
此插件参考https://github.com/DroidPluginTeam/DroidPlugin  
无侵入式框架，实现为对系统进行hook来实现插件的运行  
1.activity插件化:对ActivtyManagerProxy startActivity进行hook，将intent target信息替换为stub占坑的activity信息，在ActiivtyThread处理启动信息时再替换回来   
2.service插件化:对startService进行hook，导致实际启动的是stub service，stub service每进程提供一个占坑，在其中进行动态分发(进程内进程间逻辑一致)  
3.broadcast receiver插件化:hook registerReceiver,欺骗系统使得插件注册的广播系统认为host注册，对于插件中静态广播,在插件进程application onCreate中将静态广播全部转变为动态广播  
4.content provider:hook getContentProvider,分两种情况:  
  (1)进程内直接启动content provider
  (2)进程间先启动stub content provider，在其中做代理分发，在新启动的进程中new target content provider

## Multidex  
google 5.0以下使用非ART运行时65536问题分dex解决方案，Multidex主要是合包时的一个阶段.  
打包时分成classesX.dex,运行时只load第一个dex,在application attachBaseContext时将apk中的其他dex包解析出来,PathClassLoader-->dexPathList-->dexElements数组中去,这样后续类都可顺利加载.  
此框架加在这里是因为一类热修复使用此类似特性实现

## nuwa  
手q空间提出的热修复方案，核心思想和multidex类似，将补丁打为一个dex包插到pathclassloader的dex数组最前面，利用findclass先拿到先用的原则，实现热修复  
但此方案引发的问题是有CLASS_ISPREVERIFIED标记的类在用另一个dex里的类时会报错，这时需要防止这些类打上CLASS_ISPREVERIFIED标记,采用gradle插件的形式，为这些类提前注入另一个dex中类的内容(字节码注入)，打patch包时采用hash来diff，实现增量patch

## virtualapk  
didi提出的插件化框架，见https://github.com/didi/VirtualAPK  
复杂度介于plugin1和plugin2之间，是一个优秀的无侵入式插件化框架。  
读取apk作为插件，解析apk并构造ContextImpl代理，补充classloader，resource（这个我没有实现宿主插件resource合并，应该需要依赖didi未开源的gradle插件）  
activity插件化:插桩替换，只hook了Instrumentation，ActivityThread中Hander  
service插件化:hook ActivityManagerProxy，用站桩service替换请求service，并加载请求service手动回调  
broadcast recevier插件化:只将静态变动态  
content provider插件化:hook ContentProviderProxy,将请求uri替换，指到站桩contentprovider上，并手动构建并回调请求contentprovider
