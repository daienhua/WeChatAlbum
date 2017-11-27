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
### 选择一张图片
    private static final int REQUEST_CODE_SELECT_SINGLE = 100;
    /**
     * 选择单张图片
     */
    private void selectSinglePhoto() {
        Intent intent = new Intent(TestSelectPhotoActivity.this, PhotoSelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(PhotoSelectActivity.EXTRA_SHOW_CAMERA, true);
        bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_MODE, PhotoSelectActivity.MODE_SINGLE);
        bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_COUNT, 1);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_SELECT_SINGLE);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_SELECT_SINGLE:
                if (data != null) {
                    Image image = (Image) data.getExtras().getSerializable(PhotoSelectActivity.EXTRA_RESULT);
                    if (image != null) {
                        //TODO
                    }
                }
                break;
            }
        }
    }
    
### 选择单张图片并裁剪
    private static final int REQUEST_CODE_SELECT_CROP = 101;
    /**
     * 选择单张图片并裁剪
     */
    private void selectSinglePhotoCrop() {
        Intent intent = new Intent(TestSelectPhotoActivity.this, PhotoSelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(PhotoSelectActivity.EXTRA_SHOW_CAMERA, true);
        bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_MODE, PhotoSelectActivity.MODE_CROP);
        bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_COUNT, 1);
        //设置裁剪宽高比
        //bundle.putInt(PhotoSelectActivity.EXTRA_CROP_ASPECTX, 1);
        //bundle.putInt(PhotoSelectActivity.EXTRA_CROP_ASPECTY, 1);
        bundle.putInt(PhotoSelectActivity.EXTRA_CROP_OUTPUTX, 400);
        bundle.putInt(PhotoSelectActivity.EXTRA_CROP_OUTPUTY, 400);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_SELECT_CROP);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_SELECT_CROP:
                if (data != null) {
                    Image image = (Image) data.getExtras().getSerializable(PhotoSelectActivity.EXTRA_RESULT);
                    if (image != null) {
                        //TODO
                    }
                }
                break;
            }
        }
    }
        
### 选择多张图片
    private static final int REQUEST_CODE_SELECT_MULTI = 102;
    private ArrayList<Image> mPictures = new ArrayList<>();
    /**
    * 选择多张图片
    */
    private void selectMultiPhoto() {
       Intent intent = new Intent(TestSelectPhotoActivity.this, PhotoSelectActivity.class);
       Bundle bundle = new Bundle();
       bundle.putBoolean(PhotoSelectActivity.EXTRA_SHOW_CAMERA, true);
       bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_MODE, PhotoSelectActivity.MODE_MULTI);
       bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_COUNT, 9);
       bundle.putSerializable(PhotoSelectActivity.EXTRA_DEFAULT_SELECTED_LIST, mPictures);
       intent.putExtras(bundle);
       startActivityForResult(intent, REQUEST_CODE_SELECT_MULTI);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (resultCode != Activity.RESULT_OK) {
           return;
       }
       switch (requestCode) {
           case REQUEST_CODE_SELECT_MULTI:
               if (data != null) {
                   ArrayList<Image> images = (ArrayList<Image>) data.getExtras().getSerializable(PhotoSelectActivity.EXTRA_RESULT);
                   if (images != null && images.size() != 0) {
                       mPictures.clear();
                       mPictures.addAll(images);
                       //TODO
                   }
               }
           }
       }
    }
    
    
### 注意
    图片加载库使用的是Glide4.3.1，如果项目中使用到了Glide，最好使用和此相册加载使用的Glide版本一样，不然可能会编译报错
