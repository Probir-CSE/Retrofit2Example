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