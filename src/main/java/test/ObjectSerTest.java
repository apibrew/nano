package test;

import io.apibrew.nano.instance.AllowGuestAccess;
import lombok.Data;
import org.graalvm.polyglot.Context;

public class ObjectSerTest {

    @AllowGuestAccess
    @Data
    public static class Person {
        @AllowGuestAccess
        private String name;

        @AllowGuestAccess
        public String getName2() {
            return name;
        }
    }

    public static void main(String[] args) {
        try (Context context = Context.newBuilder("js")
                .allowExperimentalOptions(true)
                .build()) {

            Person person = new Person();
            person.name = "John";

            context.getBindings("js").putMember("input", new SimpleProxy());

            context.eval("js", "" +
                    "console.log(input.a.b(input.a.b.c()));" +
                    "");


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
