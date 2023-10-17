package test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.logic.Function;
import io.apibrew.faas.instance.AllowGuestAccess;
import lombok.Data;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.Map;

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
