package test;

import io.dynaload.example.models.User;
import io.dynaload.example.util.TimeUtils;

public class Exec {

    public static void main(String[] args) {
        Test test = new Test();
        test.user = new User();
        User user2 = new User();
        user2.name = "Test";
        System.out.println(user2.getGreeting());

        System.out.println(TimeUtils.getCurrentTimestamp());
        System.out.println(user2.getRamdomEmail());
    }
}
