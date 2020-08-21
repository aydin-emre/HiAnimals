<img align="left" src="pictures/deerIcon.png" width="8%">.
# HiAnimals

## :notebook_with_decorative_cover: Introduction 
AnimalsIntroduction is a reference AR app created with HMS kits. You can use it on your phones running with the Android-based HMS service. It was developed with the Java language.
This app allows you to visualize some animals as AR.

## :iphone: Screenshots
<img src="pictures/login.jpg" width="20%">.
<img src="pictures/deer.jpg" width="20%">.
<img src="pictures/dog.jpg" width="20%">.
<img src="pictures/tiger.jpg" width="20%">.
<img src="pictures/duck.jpg" width="20%">.
<img src="pictures/deerAR.jpg" width="20%">.
<img src="pictures/dogAR.jpg" width="20%">.
<img src="pictures/duckAR.jpg" width="20%">.
<img src="pictures/duckPhoto.jpg" width="20%">.
<img src="pictures/share.jpg" width="20%">.

##  :question: Before You Start

* You need to agconnect-services.json for run this project correctly.<br/>
* If you don't have a Huawei Developer account check this document for create; <br/>
https://developer.huawei.com/consumer/en/doc/start/10104<br/>
* Open your Android project and find Debug FingerPrint (SHA256) with follow this steps;<br/>
View -> Tool Windows -> Gradle -> Tasks -> Android -> signingReport<br/>
* Login to Huawei Developer Console (https://developer.huawei.com/consumer/en/console)<br/>
* If you don't have any app check this document for create; <br/>
https://developer.huawei.com/consumer/en/doc/distribution/app/agc-create_app<br/>
* Add SHA256 FingerPrint into your app with follow this steps on Huawei Console; <br/>
My Apps -> Select App -> Project Settings<br/>
* Make enable necessary SDKs with follow this steps;<br/>
My Apps -> Select App -> Project Settings -> Manage APIs<br/>
* For this project you have to set enable Account Kit, Auth Service, Push Kit, Cloud DB<br/>
* Than go again Project Settings page and click "agconnect-services.json" button for download json file.<br/>
* Move to json file in base "app" folder that under your android project. <br/>
(https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/69407812#h1-1577692046342)<br/>
* Go to app level gradle file and change application id of your android project. It must be same with app id on AppGallery console you defined.

##  :information_desk_person: Things to Know

* Since the application is written entirely in HMS, you must have HMS Core installed on your device.<br/>
* For Android devices without HMS Core, you can download the latest version from this link; https://tik.to/9l6<br/>
* Also your phone should support Huawei AR Engine. You can reach supported device list [here] (https://developer.huawei.com/consumer/en/doc/HMSCore-Guides-V5/introduction-0000001050130900-V5#EN-US_TOPIC_0000001050130900__section260mcpsimp)
. <br/>



## :milky_way: Features

* Sign up & Sign In with Huawei Id<br/>
* Select an animal and visualize it as AR<br/>
* Take a photo with selected animal<br/>
* Save photos to the local or cloud and get it back<br/>
* Share taken photos.<br/>

## :rocket: Kits

* [Huawei Account Kit] (https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050048870)<br/>
* [Huawei AR Engine] (https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050130900)<br/>
* [Huawei Cloud DB] (https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/clouddb-quick_start_overview)<br/>
* [Huawei Ads Kit] (https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/publisher-service-introduction-0000001050064960)<br/>
* [Huawei Push Kit] (https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/service-introduction-0000001050040060)<br/>



## :link: Useful Links 
* [Huawei Developers Medium Page EN](https://medium.com/huawei-developers)
* [Huawei Developers Medium Page TR](https://medium.com/huawei-developers-tr) 
* [Huawei Developers Forum](https://forums.developer.huawei.com/forumPortal/en/home)
