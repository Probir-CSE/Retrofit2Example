# 0.Before starting ...
This project includes the following dependency projects: Please refer to the project's repository for details on each dependency project.

> ** List of dependent projects: **
> 1. square/retrofit : https://github.com/square/retrofit
> 2. square/okHttp : https://github.com/square/okhttp
> 3. google/gson : https://github.com/google/gson

1. Adding a dependency project with Gradle 10 Open the ** build.gradle (Module: app) ** file in Android Studio and add the dependency project to the ** dependencies ** block as shown below.

```
dependencies {
	...
	compile 'com.squareup.retrofit2:retrofit:2.1.0'
    compile 'com.squareup.retrofit2:converter-gson:2.1.0'
    compile 'com.squareup.okhttp3:okhttp:3.0.0-RC1'
    compile 'com.squareup.okhttp3:logging-interceptor:3.0.0-RC1'
}
```

# 2.Add network communication permission to AndroidManifest 22 The project includes network communication capabilities. Therefore, please add ** Network Communication Permissions ** to ** AndroidManifest.xml ** file as below.

```xml
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

# 3. Creating project packages and classes 43 Before organizing the project, we have organized the ** package ** and ** class ** as shown below for ease of management. (You can, of course, change it to your own situation.)

<p align="center">
	<img src="https://github.com/devetude/Retrofit2Example/blob/master/images/1.png?raw=true" width="400"/>
</p>

**core**에는 http api 통신을 위한 핵심 소스코드가 위치합니다. **APIAdapter**와 서버에서 온 **세션 데이터 관리**를 위한 **interceptor**와 **preferences**로 구성되어있습니다.

**resource**에는 http api 통신을 위한 서버의 다양한 **url**을 저장하는 **APIUrl**이 위치합니다.

**response**에는 **response 데이터의 형태**를 선언해주는 부분이며 이 프로젝트에서는 편의상 **ResData**라고 생성했습니다. (api 구성에 따라 이 부분은 변경될 수 있습니다.)

**service**에는 실제로 api 통신을 위한 **파라메터의 정의와 함수의 선언을 위한 클래스**가 위치합니다. 이 프로젝트에서는 예를 들기위해 **SignService**와 **WordService**라고 생성했습니다. (api 구성에 따라 이 부분은 변경될 수 있습니다.)

# 4. 코어 소스코드 분석
본격적으로 http api 통신을 위한 소스코드를 작성해보고 분석해보겠습니다.

## 4.1. APIAdapter.java
**APIAdapter 클래스**는 **OkHttpClient**를 이용하여 쿠키 관리를 위한 클라이언트 객체를 생성하고 이것을 **Retrofit** 객체에 적용 및 생성하는 기능을 정의하고 있습니다.

```java
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
         * Create and return a Retrofit object with the name of a service object
         *
         * ex) retrofit.create(SignService.class);
         */
        return retrofit.create(serviceName);
    }
}
```

## 4.2. CookieSharedPreferences.java
**The CookieSharedPreferences class ** defines the ability to store and retrieve ** session cookie values ​​** from the server using ** SharedPreferences ** inside the device.

```java
package net.devetude.www.retrofit2example.api.core.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;

/**
 * CookieSharedPreferences class
 *
 * @author devetude
 */
public class CookieSharedPreferences {
    /**
     * CookieSharedPreferences를 참조하기 위한 key
     *
     * 권고) 겹치지 않는 고유한 형태의 string으로 구성할 것
     */
    public static final String COOKIE_SHARED_PREFERENCES_KEY = "new.devetude.www.cookie";

    // 싱글톤 모델로 객체 초기화
    private static CookieSharedPreferences cookieSharedPreferences = null;

    public static CookieSharedPreferences getInstanceOf(Context c){
        if(cookieSharedPreferences == null){
            cookieSharedPreferences = new CookieSharedPreferences(c);
        }

        return cookieSharedPreferences;
    }

    private SharedPreferences sharedPreferences;

    /**
     * 생성자
     *
     * @param context
     */
    public CookieSharedPreferences(Context context) {
        final String COOKIE_SHARED_PREFERENCE_NAME = context.getPackageName();
        sharedPreferences = context.getSharedPreferences(COOKIE_SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
    }

    /**
     * SharedPreferences에 값을 추가하는 메소드
     *
     * @param key
     * @param hashSet
     */
    public void putHashSet(String key, HashSet<String> hashSet){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, hashSet);
        editor.commit();
    }

    /**
     * SharedPreferences에서 값을 가져오는 메소드
     *
     * @param key
     * @param cookie
     * @return
     */
    public HashSet<String> getHashSet(String key, HashSet<String> cookie){
        try {
            return (HashSet<String>) sharedPreferences.getStringSet(key, cookie);
        } catch (Exception e) {
            return cookie;
        }
    }
}
```

## 4.3. ReceivedCookiesInterceptor.java
**ReceivedCookiesInterceptor 클래스**는 서버로 부터 온 response 데이터를 가로채어 **헤더 영역에 있는 쿠키 값**을 가져와 **CookieSharedPreferences 클래스**를 이용하여 **기기 내부에 저장**하는 기능을 정의하고 있습니다.

```java
package net.devetude.www.retrofit2example.api.core.interceptor;

import android.content.Context;

import net.devetude.www.retrofit2example.api.core.preferences.CookieSharedPreferences;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * ReceivedCookiesInterceptor 클래스
 *
 * @author devetude
 */
public class ReceivedCookiesInterceptor implements Interceptor {
    // CookieSharedReferences 객체
    private CookieSharedPreferences cookieSharedPreferences;

    /**
     * 생성자
     *
     * @param context
     */
    public ReceivedCookiesInterceptor(Context context){
        // CookieSharedReferences 객체 초기화
        cookieSharedPreferences = CookieSharedPreferences.getInstanceOf(context);
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        // 가져온 chain으로 부터 리스폰스 객체를 생성
        Response response = chain.proceed(chain.request());

        // 리스폰스의 헤더 영역에 Set-Cookie가 설정되어있는 경우
        if (!response.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();

            // 쿠키 값을 읽어옴
            for (String header : response.headers("Set-Cookie")) {
                cookies.add(header);
            }

            // 쿠키 값을 CookieSharedPreferences에 저장
            cookieSharedPreferences.putHashSet(CookieSharedPreferences.COOKIE_SHARED_PREFERENCES_KEY, cookies);
        }

        // 리스폰스 객체 반환
        return response;
    }
}
```

## 4.4. AddCookiesInterceptor.java
**AddCookiesInterceptor 클래스**는 서버로 보내는 데이터를 가로채어 **CookieSharedPreferences 클래스**를 이용하여 기기 내부에 저장되어있는 **쿠키 값**을 서버로 보낼 데이터의 **헤더 영역에 추가**하는 기능을 정의하고 있습니다.

```java
package net.devetude.www.retrofit2example.api.core.interceptor;

import android.content.Context;

import net.devetude.www.retrofit2example.api.core.preferences.CookieSharedPreferences;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * AddCookiesInterceptor 클래스
 *
 * @author devetude
 */
public class AddCookiesInterceptor implements Interceptor {
    // CookieSharedReferences 객체
    private CookieSharedPreferences cookieSharedPreferences;

    /**
     * 생성자
     *
     * @param context
     */
    public AddCookiesInterceptor(Context context){
        // CookieSharedReferences 객체 초기화
        cookieSharedPreferences = CookieSharedPreferences.getInstanceOf(context);
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        // 가져온 chain으로 부터 빌더 객체를 생성
        Request.Builder builder = chain.request().newBuilder();

        // CookieSharedPreferences에 저장되어있는 쿠키 값을 가져옴
        HashSet<String> cookies = (HashSet) cookieSharedPreferences.getHashSet(
                CookieSharedPreferences.COOKIE_SHARED_PREFERENCES_KEY,
                new HashSet<String>()
        );

        // 빌더 헤더 영역에 쿠키 값 추가
        for (String cookie : cookies) {
            builder.addHeader("Cookie", cookie);
        }

        // 체인에 빌더를 적용 및 반환
        return chain.proceed(builder.build());
    }
}
```

# 5. 사용자 정의 소스코드 분석
서버의 api 구성 환경에 따라서 아래의 클래스들은 알맞게 변경하여 사용하시길 바랍니다.

## 5.1. APIUrl.java
**APIUrl 클래스**는 api 서버의 다양한 **url**을 선언하는 기능을 정의하고 있습니다.

```java
package net.devetude.www.retrofit2example.api.resource;

/**
 * APIUrl 클래스
 *
 * @author devetude
 */
public class APIUrl {
    /**
     * api 서버 url
     *
     * 주의) Retrofit2 부터 base url의 끝에 /(루트)를 꼭 기입해줘야 함
     */
    public static final String API_BASE_URL = "http://api.devetude.net/";

    /**
     * 실제 api 경로
     *
     * 주의) /sign/in.json (x), sign/in.json (o)
     */
    public static final String SIGN_IN_URL = "sign/in.json";
    public static final String SIGN_UP_URL = "sign/up.json";
    public static final String GET_WORD_TYPE_LIST = "word/type/list.json";
}
```

## 5.2. ResData.java
**ResData 클래스**는 api 서버로 부터 오는 **response 데이터의 형식**을 선언하는 기능을 정의하고 있습니다.

```java
package net.devetude.www.retrofit2example.api.response;

/**
 * ResData 클래스
 *
 * @author devetude
 */
public class ResData {
    /**
     * json 형식의 response 데이터를 받기 위한 멤버 변수
     *
     * 주의) 실제 api 서버에서 돌아오는 json 형식의 response 데이터의 필드명과 동일해야 함
     */
    public boolean res;
    public String msg;
    public Object data;
}
```

## 5.3. SignService.java
**SignService 클래스**는 로그인 및 회원가입을 위한 다양한 **api 메소드** 및 **파라메터**를 정의하고 있습니다.

```java
package net.devetude.www.retrofit2example.api.service;

import android.content.Context;

import net.devetude.www.retrofit2example.api.core.APIAdapter;
import net.devetude.www.retrofit2example.api.resource.APIUrl;
import net.devetude.www.retrofit2example.api.response.ResData;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * SignService 클래스
 *
 * @author devetude
 */
public class SignService extends APIAdapter {
    /**
     * Retrofit 객체를 가져오는 메소드
     *
     * @param context
     * @return
     */
    public static SignAPI getRetrofit(Context context) {
        // 현재 서비스객체의 이름으로 Retrofit 객체를 초기화 하고 반환
        return (SignAPI) retrofit(context, SignAPI.class);
    }

    // SignAPI 인터페이스
    public interface SignAPI {
        /**
         * 회원가입 메소드
         *
         * @param email
         * @param pw
         * @param name
         * @return
         */
        @FormUrlEncoded
        @POST(APIUrl.SIGN_UP_URL)
        Call<ResData> up(
                @Field("email") String email,
                @Field("pw") String pw,
                @Field("name") String name
        );

        /**
         * 로그인 메소드
         *
         * @param email
         * @param pw
         * @return
         */
        @FormUrlEncoded
        @POST(APIUrl.SIGN_IN_URL)
        Call<ResData> in(
                @Field("email") String email,
                @Field("pw") String pw
        );
    }
}
```

# 6. api 클래스의 적용
위에서 만든 다양한 api 클래스를 **실제로 적용하는 방법**에 대해서 알아보겠습니다.

## 6.1. MainActivity.java
로그인 api를 호출하여 로그인 과정을 마친 이후, 단어 api를 호출하여 단어 유형을 가져오는 간단한 세션 베이스를 아래와 같은 코드로 구현할 수 있습니다.

```java
package net.devetude.www.retrofit2example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import net.devetude.www.retrofit2example.api.response.ResData;
import net.devetude.www.retrofit2example.api.service.SignService;
import net.devetude.www.retrofit2example.api.service.WordService;

import org.json.JSONArray;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MainActivity 클래스
 *
 * @author devetude
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 로그인 api 호출
        SignService.getRetrofit(getApplicationContext()).in("[이메일]", "[비밀번호]").enqueue(new Callback<ResData>() {
            @Override
            public void onResponse(Call<ResData> call, Response<ResData> response) {
                if(response.body().res) {
                    Log.d("devetude", "로그인 성공");

                    // 단어 api 호출 (이 부분부터 로그인 세션이 필요)
                    WordService.getRetrofit(getApplicationContext()).getWordTypeList("-1", "3").enqueue(new Callback<ResData>() {
                        @Override
                        public void onResponse(Call<ResData> call, Response<ResData> response) {
                            if(response.body().res) {
                                Log.d("devetude", "단어 유형 리스트 가져오기 성공");

                                try {
                                    JSONArray jsonArray = new JSONArray(new Gson().toJson(response.body().data));
                                    Log.d("devetude", "첫번째 단어 유형의 idx : " + jsonArray.getJSONObject(0).getString("idx"));
                                    Log.d("devetude", "첫번쨰 단어 유형의 name : " + jsonArray.getJSONObject(0).getString("name"));
                                } catch(Exception e) {
                                    Log.d("devetude", "json 데이터 파싱 실패");
                                    Log.d("devetude", "메세지 : " + e.getMessage());
                                }
                            }

                            else {
                                Log.d("devetude", "단어 유형 리스트 가져오기 실패");
                                Log.d("devetude", "메세지 : " + response.body().msg);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResData> call, Throwable t) {
                            Log.d("devetude", "서버 통신 실패");
                            Log.d("devetude", "메세지 : " + t.getMessage());
                        }
                    });
                }

                else {
                    Log.d("devetude", "로그인 실패");
                    Log.d("devetude", "메세지 : " + response.body().msg);
                }
            }

            @Override
            public void onFailure(Call<ResData> call, Throwable t) {
                Log.d("devetude", "서버 통신 실패");
                Log.d("devetude", "메세지 : " + t.getMessage());
            }
        });
    }
}
```

## 6.2. Logcat 결과
Logcat으로 출력한 결과는 아래와 같습니다.

<p align="center">
	<img src="https://github.com/devetude/Retrofit2Example/blob/master/images/2.png?raw=true" width="700"/>
</p>

# 7. 라이센스
본 프로젝트는 Apache 2.0 License를 따릅니다. http://www.apache.org/licenses/LICENSE-2.0

# 8. 문의사항
기타 문의사항이 있으실 경우 아래의 **문의 수단**으로 연락해주세요.
> **문의 수단:**
> - 메일 : **devetude@naver.com** 또는 **devetude@gmail.com**
> - github : **https://github.com/devetude/Retrofit2Example/issues**
