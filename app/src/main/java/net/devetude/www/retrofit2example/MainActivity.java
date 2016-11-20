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
