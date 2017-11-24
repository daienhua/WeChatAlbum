# Album
Android自定义相册，为了方便公司或者自己的开发，抽空准备整理出一个比较完善的库，方便以后开发可以随时拉取代码。如果你对这部分的代码感兴趣，欢迎引入使用，如果引用过程中发现遇到什么闪退，麻烦在Github上给我提个issue，我会尽快定位修复。<p>

![截屏1](https://github.com/daienhua/WeChatAlbum/blob/master/screenshots/1.jpg)

![截屏2](https://github.com/daienhua/WeChatAlbum/blob/master/screenshots/2.jpg)

![截屏3](https://github.com/daienhua/WeChatAlbum/blob/master/screenshots/3.jpg)

![截屏4](https://github.com/daienhua/WeChatAlbum/blob/master/screenshots/4.jpg)

![截屏4](https://github.com/daienhua/WeChatAlbum/blob/master/screenshots/5.jpg)


# How to<p>
## Step 1<p>
Add it in your root build.gradle at the end of repositories:<p>

    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
## Step 2<p>
Add the dependency<p>

    dependencies {
	        compile 'com.github.daienhua:Album:1.0'
	}
  
## Use<p>
查看demo
