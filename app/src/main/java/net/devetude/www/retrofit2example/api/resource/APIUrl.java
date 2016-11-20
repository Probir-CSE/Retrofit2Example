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
