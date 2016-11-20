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
