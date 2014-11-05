package attendance4j;

import java.util.Random;

/**
 * Created by TEJNEK on 2014/11/05.
 */
//TODO change lib name...
public class Attendance {
    public static String hash;
    public static Integer notice;
    public static Long timestamp;
    public static Long lectureId;
    public Attendance(){

    }
    public Integer getNotice(){
        Random random = new Random();
        notice = random.nextInt(10000000);
        return notice;
    }
}
