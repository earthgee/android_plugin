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

