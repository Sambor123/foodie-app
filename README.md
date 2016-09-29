# Android美食APP项目开源(包括后台)
------
## 项目简介
-------------------

**小食光**定位为一款集美食，社交，LBS服务于一体的美食推荐APP。为你发现周边美食的同时提供一个吃货分享的平台。

## APP截图
-----------------------
![启动页](http://a.hiphotos.bdimg.com/wisegame/pic/item/6fd7912397dda144add95bc5bab7d0a20df486f0.jpg)
![首页](http://h.hiphotos.bdimg.com/wisegame/pic/item/c0cc7cd98d1001e936d72378b00e7bec55e797d9.jpg)
![食光轴](http://b.hiphotos.bdimg.com/wisegame/pic/item/6955b319ebc4b745d23b8061c7fc1e178a82151d.jpg)
![详情页](http://d.hiphotos.bdimg.com/wisegame/pic/item/88c4b74543a982265c6e1c508282b9014a90eb1e.jpg)
## 下载地址
------------------
- [百度手机助手](http://shouji.baidu.com/software/9690734.html)
- [腾讯应用宝](http://android.myapp.com/myapp/detail.htm?apkName=com.foodie.app)


## 功能模块
-------------------
- **美食推荐** ：提供基础的美食信息查询；
- **商家推荐** : 基于用户当前位置推荐周边的人们店家；
- **百度地图API** :提供基础的周边店家检索，定位服务；
- **美食分享**：美食分享，动态发表，美食收藏等等；
- **社交网络**：提供基础的"粉丝机制"；

## 技术特性
-------------------
- **Material design设计风格**：app整体设计(除去底部tab栏)外，大量使用material design设计风格的布局和开源组件。例如FloatingActionBar，StaggeredGridView，RecycleView,CardView等等；具体可参考[Material design官方设计指南](https://material.google.com/#)
- **UIL图片加载框架**：美食，店家，用户头像等图片的加载和缓存均使用UIL框架，[UIL官方github](https://github.com/nostra13/Android-Universal-Image-Loader)
使用方法请参考[Android 开源框架Universal-Image-Loader完全解析（一）--- 基本介绍及使用](http://blog.csdn.net/xiaanming/article/details/26810303/);
- **android-asyn-http作为网络请求库**：一个android异步网络请求框架，使用方式[官方使用指南](http://loopj.com/android-async-http/)讲的很清楚；
- **Gson作为处理json和java bean**:由于APP从后台拿到的数据都是restful api提供的json数据，因此使用google的[gson](https://github.com/google/gson)来处理json数据;



## 引用的开源组件
----------------------
- [floatingsearchview](https://github.com/arimorty/floatingsearchview):开源浮动搜索框组件；
- [MultiImageSelector](https://github.com/lovetuzitong/MultiImageSelector)：开源多图片选择器；
- [Material ICON](https://design.google.com/icons/#ic_search)：google 官方material图标；
- [Kanner](https://github.com/iKrelve/Kanner)：一个开源Android轮播图组件；
- [FloatingActionButton](https://github.com/Clans/FloatingActionButton)：开源FloatingActionButton组件；
- [Android-Universal-Image-Loader](https://github.com/nostra13/Android-Universal-Image-Loader)：UIL图片加载框架；
- [android-async-http](http://loopj.com/android-async-http/):一个开源网络异步请求处理库

## APP后台地址
---------------------

后台使用Spring+Spring MVC+Mybatis集成，github地址为[https://github.com/Sambor123/foodie-webserver](https://github.com/Sambor123/foodie-webServer)

## 联系方式
---------------------
如有任何问题，可以联系我，相互学习

- weChat：xb1404195038
- Email:xiongbo010@gmail.com

## 注意事项
---------------------
由于项目是本着敏捷开发的原则做的，并且是我第一次做的完整项目，因此代码质量不敢保证，仅供学习参考。

## 打赏
---------------------
![打赏](https://github.com/Sambor123/foodie-webserver/blob/master/alipay.jpeg?raw=true)
---------------------
![打赏](https://github.com/Sambor123/foodie-webserver/blob/master/weixin.jpeg?raw=true)
# License
```
Copyright 2016 Sambor 

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

