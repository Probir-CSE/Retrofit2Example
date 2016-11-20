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
 * WordService 클래스
 *
 * @author devetude
 */
public class WordService extends APIAdapter {
    /**
     * Retrofit 객체를 가져오는 메소드
     *
     * @param context
     * @return
     */
    public static WordAPI getRetrofit(Context context) {
        // 현재 서비스객체의 이름으로 Retrofit 객체를 초기화 하고 반환
        return (WordAPI) retrofit(context, WordAPI.class);
    }

    // WordAPI 인터페이스
    public interface WordAPI {
        /**
         * 단어 유형 목록을 가져오는 메소드
         */
        @FormUrlEncoded
        @POST(APIUrl.GET_WORD_TYPE_LIST)
        Call<ResData> getWordTypeList(
                @Field("root_idx") String rootIdx,
                @Field("maximum_depth") String maximumDepth
        );
    }
}
