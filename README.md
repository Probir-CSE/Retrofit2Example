Retrofit2Example
===================


**Retrofit2** + **OkHttp3** + **Gson2**를 이용한 **http api 통신** 예제

----------


* 시작하기 전에...
-------------

이 프로젝트에서는 아래 같은 **의존성 프로젝트**가 포함되어있습니다. 의존성 프로젝트별 자세한 설명은 해당 프로젝트의 레포지토리에서 직접확인해주세요.

> **의존성 프로젝트 목록:**

> 1. square/retrofit : https://github.com/square/retrofit
> 2. square/okHttp : https://github.com/square/okhttp
> 3. google/gson : https://github.com/google/gson

#### Gradle을 이용한 의존성 프로젝트 추가

아래와 같이 **build.gradle (Module: app)** 파일을 안드로이드 스튜디오에서 열어 **dependencies** 블록에 의존성 프로젝트를 추가해주세요.

```
dependencies {
	...
	compile 'com.squareup.retrofit2:retrofit:2.1.0'
    compile 'com.squareup.retrofit2:converter-gson:2.1.0'
    compile 'com.squareup.okhttp3:okhttp:3.0.0-RC1'
    compile 'com.squareup.okhttp3:logging-interceptor:3.0.0-RC1'
}
```

#### AndroidManifest에 네트워크 통신 퍼미션 추가

이 프로젝트는 네트워크 통신 기능을 포함하고 있습니다. 따라서 아래와 같이 **AndroidManifest.xml** 파일에 **네트워크 통신 퍼미션**을 추가해주세요.

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="net.devetude.www.retrofitexample">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        ...
    </application>

	...
    <uses-permission android:name="android.permission.INTERNET" />
</manifest>
```

#### 프로젝트 패키지 및 클래스 생성

프로젝트를 구성하기 전에 관리의 용의성을 고려하여 아래와 같이 **패키지** 및 **클래스**를 구성했습니다. (물론 자신의 상황에 맞게 변경하셔도 무관합니다.)

![](https://github.com/devetude/Retrofit2Example/blob/master/images/1.png?raw=true)

**core**에는 http api 통신을 위한 핵심 소스코드가 위치합니다. **APIAdapter**와 서버에서 온 **세션 데이터 관리**를 위한 **interceptor**와 **preferences**로 구성되어있습니다.

**resource**에는 http api 통신을 위한 서버의 다양한 **url**을 저장하는 **APIUrl**이 위치합니다.

**response**에는 **response 데이터의 형태**를 선언해주는 부분이며 이 프로젝트에서는 편의상 **ResData**라고 생성했습니다. (api 구성에 따라 이 부분은 변경될 수 있습니다.)

**service**에는 실제로 api 통신을 위한 **파라메터의 정의와 함수의 선언을 위한 클래스**가 위치합니다. 이 프로젝트에서는 예를 들기위해 **SignService(로그인 서비스)**와 **WordService(단어 서비스)**라고 생성했습니다. (api 구성에 따라 이 부분은 변경될 수 있습니다.)

----------

* 소스코드 분석
-------------

본격적으로 http api 통신을 위한 소스코드를 작성해보고 분석해보겠습니다.

#### APIAdapter.java

```
package net.devetude.www.retrofit2example.api.core;

import android.content.Context;

import net.devetude.www.retrofit2example.api.core.interceptor.AddCookiesInterceptor;
import net.devetude.www.retrofit2example.api.core.interceptor.ReceivedCookiesInterceptor;
import net.devetude.www.retrofit2example.api.resource.APIUrl;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * APIAdapter 클래스
 *
 * @autor devetude
 */
public class APIAdapter {
    /**
     * Retrofit 객체를 초기화하는 메소드
     *
     * @param context
     * @param serviceName
     * @return
     */
    protected static Object retrofit(Context context, Class<?> serviceName) {
        /**
         * OkHttpClient 객체 생성 과정
         *
         * 1. OkHttpClient 객체 생성
         * 2. 세션 데이터의 획득을 위해 response 데이터 중 헤더 영역의 쿠키 값을 가로채기 위한 RecivedCookiesInterceptor 추가
         * 3. 서버로 데이터를 보내기 전 세션 데이터 삽입을 위해 AddCookiesInterceptor 추가
         * 4. OkHttpClient 빌드
         *
         * 주의) 가로채기 위한 메소드는 addInterceptor이고 삽입하기 위한 메소드는 addNetworkInterceptor
         */
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new ReceivedCookiesInterceptor(context))
                .addNetworkInterceptor(new AddCookiesInterceptor(context))
                .build();

        /**
         * Retrofit 객체 생성 과정
         *
         * 1. Retrofit 객체 생성
         * 2. base(api 서버) url 설정
         * 3. json 형식의 reponse 데이터의 파싱을 위해 Gson 추가
         * 3. 위에서 만든 OkHttpClient 객체를 추가
         * 4. Retrofit 빌드
         *
         * 주의) addConverterFactory를 추가하지 않을 경우 어플리케이션이 종료됨
         */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIUrl.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();


        /**
         * 서비스객체의 이름으로 Retrofit 객체 생성 및 반환
         *
         * ex) retrofit.create(SignService.class);
         */
        return retrofit.create(serviceName);
    }
}
```

----------
