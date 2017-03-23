# android_plugin
android组件化，插件化，热发布学习

## plugin1
此插件参考https://github.com/singwhatiwanna/dynamic-load-apk  
原理:host项目中插桩作为代理(activity,service都是),实际的插件组件通过运行时动态生成,代理组件收到生命周期回调再回调插件组件,通过hack方式获得插件dex包的AssetManager,Resource来操作插件中的资源  
优点和不足:原理简单，没有hook任何framework层组件代码，但是通过应用层代理的方式也使得静态注册的BroadcastReceiver和ContentProvider无法运行在插件中
