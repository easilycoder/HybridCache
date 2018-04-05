[![](https://jitpack.io/v/easilycoder/HybridCache.svg)](https://jitpack.io/#easilycoder/HybridCache)

# FEATURE

#### :heavy_check_mark: 拦截webview资源请求

#### :heavy_check_mark: 自定义缓存策略

#### :heavy_check_mark: 自定义缓存key策略

#### :heavy_check_mark: 简单让webview共享fresco的图片缓存

# DEPENDENCIES

**HybridCache**已经发布到[Jitpack](https://jitpack.io/#easilycoder/HybridCache)上，你可以快速将引入EasyBridge库。

1. 在根目录的build.gradle文件中添加jitpack仓库

   ```Gradle
   allprojects {
       repositories {
          ...
          maven { url 'https://jitpack.io' }
       }
   }
   ```

2. 添加HybridCache依赖

   ```gradle
   dependencies {
   	implementation 'com.github.easilycoder.HybridCache:hybridcache:0.0.1'
   	implementation 'com.jakewharton:disklrucache:2.0.2'
   }
   ```

3. 如果你需要使用基于fresco的图片缓存共享，请添加下面的依赖
    ```
    dependencies {
    	implementation 'com.github.easilycoder.HybridCache:hybridcache-fresco:0.0.1'
    }
    ```

# HybridCache

[HybridCache](https://github.com/easilycoder/HybridCache)简而言之其实是一套native和webview共享缓存的解决方案。不过在了解HybridCache的实现细节以及能够解决的问题之前，先大概了解一下web开发中涉及到的缓存机制

## Web缓存机制

实际上，web开发当中已经具备相当完善的缓存机制，并且Android系统的WebView对这些已有的缓存机制基本上都提供了完备的支持。

web的缓存机制有以下两大类：

* 浏览器缓存机制
* web开发中的缓存机制

### 浏览器缓存机制

浏览器自身的缓存机制是基于http协议层的Header中的信息实现的

* [Cache-control](https://baike.baidu.com/item/Cache-control) && [Expires](https://baike.baidu.com/item/expires)

  这两个字段的作用是：**接收响应时，浏览器决定文件是否需要被缓存；或者需要加载文件时，浏览器决定是否需要发出请求**

  Cache-control常见的值包括：no-cache、no-store、max-age等。其中max-age=xxx表示缓存的内容将在 xxx 秒后失效, 这个选项只在HTTP 1.1可用, 并如果和Last-Modified一起使用时, 优先级较高。


* [Last-Modified](https://baike.baidu.com/item/Last-Modified) && [ETag](https://baike.baidu.com/item/ETag)

  这两个字段的作用是：**发起请求时，服务器决定文件是否需要更新**。服务端响应浏览器的请求时会添加一个Last-Modified的头部字段，字段内容表示请求的文件最后的更改时间。而浏览器会在下一次请求通过If-Modified-Since头部字段将这个值返回给服务端，以决定是否需要更新文件

这些技术都是协议层所定义的，在Android的webview当中我们可以通过配置决定是否采纳这几个协议的头部属性。设置如下：

```kotlin
webView.settings.cacheMode=WebSettings.LOAD_DEFAULT
// cacheMode的取值定义如下：
@IntDef({LOAD_DEFAULT, LOAD_NORMAL, LOAD_CACHE_ELSE_NETWORK, LOAD_NO_CACHE, LOAD_CACHE_ONLY})
@Retention(RetentionPolicy.SOURCE)
public @interface CacheMode {}
```

### web开发中的缓存机制

* Application Cache
* Dom Storage 缓存机制
* Web SQL Database 缓存机制
* IndexedDB 缓存机制
* File System

关于以上这几个web开发中的缓存机制，可以参考这篇文章[Android：手把手教你构建 全面的WebView 缓存机制 & 资源加载方案](https://www.jianshu.com/p/5e7075f4875f)

## 认识HybridCache

[HybridCache](https://github.com/easilycoder/HybridCache)旨在提供一种native和webview之间共享缓存的解决方案，尤其是共享native中的图片缓存。在native开发中，我们广泛的使用着各种图片加载库，比如：

- [fresco](https://github.com/facebook/fresco)
- [picasso](https://github.com/square/picasso)
- [glide](https://github.com/bumptech/glide)

这些存在native的图片加载框架为我们提供了非常良好的图片缓存体验。**HybridCache**的一种具体运用，就是把在webview中的图片交由我们的native的图片加载框架（或者是我们自己实现的文件缓存）进行缓存，这样的好处就是：

- 能够更持久的保存webview中的图片（而且不需要前端开发人员所关注）
- 能够更加统一app内的图片缓存
- webview和native的缓存贡献，在某些适用的场景具备节省流量和加快加载速度的优点

当然图片缓存只是一个相当具体的运用，实际上**HybridCache**提供的是更为广泛的**webview资源加载拦截**的功能，通过拦截webview中渲染网页过程中各种资源（包括图片、js文件、css样式文件、html页面文件等）的下载，根据业务的场景考虑缓存的策略，可以从app端提供webview的缓存技术方案（不需要前端人员感知的）。

## 实现原理

Android的webview在加载网页的时候，用户能够通过系统提供的API干预各个中间过程。而**HybridCache**要拦截的就是网页资源请求的环节。这个过程，`WebViewClient`当中提供了以下两个入口：

```java
public class WebViewClient {

	// android5.0以上的版本加入
   public WebResourceResponse shouldInterceptRequest(WebView view,
            WebResourceRequest request) {
        return shouldInterceptRequest(view, request.getUrl().toString());
    }

	  @Deprecated
    public WebResourceResponse shouldInterceptRequest(WebView view,
            String url) {
        return null;
    }
}
```
上面的两个API是在调用了`WebView#loadUrl()`之后，请求网页资源（包括html文件、js文件、css文件以及图片文件）的时候回调。关于这两个API有几个点需要注意：

* 回调**不是发生在主线程**，因此不能做一些处理UI的事情
* 接口的返回值是同步的
* `WebResourceResponse`这个返回值可以自行构造，其中关键的属性主要是：代表资源内容的一个输入流`InputStream`以及标记这个资源内容类型的`mMimeType`

只要在这两个入口构造正确的`WebResourceResponse`对象，就可以替换默认的请求为我们提供的资源。因此，webview和native缓存共享的方案就是通过这两个入口，在每次请求资源的时候根据请求的URL/WebResourceRequest判断是否存在本地的缓存，并在缓存存在的情况下将缓存的输入流返回，示意图如下所示：

![interceptRequest](/assets/interceptRequest.png)

## 方案设计

先放上一张方案实现的设计类图：

![webcache-artitecture](/assets/webcache-artitecture.png)

**ps:这张类图是一开始设计方案的时候画的，后续经过了多次重构和调整，部分已经不尽一致，不过基本保持了核心的概念和结构**

HybridCache的核心任务就是**拦截资源请求，下载资源并缓存资源**，因此整个库的设计就分为了下面三个核心点：

* 请求拦截
* 资源响应（下载/读取缓存）
* 缓存

### 资源请求拦截

参考okhttp拦截器的思想设计了`WebResInterceptor`和`Chain`两个接口，定义了拦截的动作以及驱动拦截器的链条。实际上，这两个接口都只是类库内部可见。具体的实现是`BaseInterceptor`和`DefaultInterceptorChain`两个对象。

`BaseInterceptor`是拦截发生和资源响应的核心对象，内部处理了包括寻找缓存资源、下载资源和写缓存的基本逻辑。同时它是一个抽象类，子类只需要实现它并根据对应的资源请求定义是否参与拦截、以及选择性的自定义配置下载和缓存的行为即可。

`DefaultInterceptorChain`仅仅只是用于用于驱动拦截器链条的流转，类库内部可见

### 资源响应

资源响应有两种情况：

* 缓存响应
* 下载响应

当对应的资源缓存不存在的时候，会直接触发资源的下载。在类库内部，会通过`HttpConnectionDownloader`直接建立一个`HttpURLConnection`进行资源的下载，获得资源的文件流。

同时参考代理模式，设计了**“边读边写”**的动作。即下载的资源流通过被封装为一个`WebResInputStreamWrapper`对象后直接返回。`WebResInputStreamWrapper`继承于`InputStream`,同时内部持有一个`TempFileWriter`的实例。在`WebResInputStreamWrapper`被浏览器读取的同时，`TempFileWriter`会把对应的资源写入到缓存当中，实现**“边读边写”**

### 缓存

`CacheProvider`定义了提供缓存的实现的规范，可以根据实际的业务场景提供任意的缓存实现方案。同时库内部通过[LruCache](https://github.com/JakeWharton/DiskLruCache)提供了简单的文件缓存的实现`SimpleCacheProvider`。同时为了拓展共享图片缓存的实现，类库还提供了一个基于fresco的图片缓存提供实例`FrescoImageProvider`

`CacheKeyProvider`使得业务可以根据实际的场景提供缓存的key的生成策略。

关于方案的实现细节，可以关注我的GitHub仓库[HybridCache](https://github.com/easilycoder/HybridCache)

## 接入使用

在图片缓存以及简单的使用文件缓存资源这两个场景上，方案已经提供了直接的实现，可以简单的一键接入使用，总的接入步骤如下：

1. 根据业务需要定义你的拦截器。你只需要继承`BaseInterceptor`,并实现仅有的一个抽象方法即可。如果你需要图片拦截器，可以直接使用类库内部提供的`ImageInterceptor`
2. 在定义拦截器的同时，你可以实现你的缓存提供器，提供你的缓存管理策略。默认的情况下会使用`SimpleCacheProvider`提供文件缓存
3. 使用`HybridCacheManager#addCacheInterceptor()`将拦截器添加都管理器中。
4. 在初始化webview的时候，设置自定义的`WebViewClient`对象，并在其拦截资源请求的入口方法中调用`HybridCacheManager#interceptWebResRequest()`方法

以上简单的几步即可拥有native和webview共享缓存的功能。具体的实例可以参考GitHub仓库中的demo。

## 你可能会遇到的坑

在使用webview的时候，你可能会遇到一些坑

* 页面当中资源包含http和https两种请求

  如果你加载的页面以及页面请求的资源包好了http和https两种请求，那么你有可能会出现部分资源无法加载的情况。这是因为在Android5.0之后，webview默认禁止在一个页面当中包含两种协议请求。这时候你需要添加这样的设置：`setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)`

* 加载的页面部分的资源请求出错（比如404）没有回调

  正如前面提及的，在Android6.0以后，你可以通过`WebViewClient#onReceivedHttpError()`接收到资源请求出错的回调，但是在此之前是没有这个api的，同时另外一个API`WebViewClient#onReceivedError()`的回调是在整个页面不可达等情况才会回调，而不会因为资源请求问题而响应，具体可以参考文档备注。

  **在使用HybridCache资源拦截之后，你可以通过设置`BaseInterceptor#setOnErrorListener(onErrorListener)`感知到资源加载出错的情况**

* 缓存key不同导致缓存不能共享
