package test;

import io.apibrew.client.model.Extension;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.Proxy;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

public class SimpleProxy implements ProxyObject, ProxyExecutable {
    @Override
    public Object getMember(String key) {
        return this;
    }

    @Override
    public Object getMemberKeys() {
        return null;
    }

    @Override
    public boolean hasMember(String key) {
        return true;
    }

    @Override
    public void putMember(String key, Value value) {

    }

    public Object a() {
        return this;
    }

    @Override
    public Object execute(Value... arguments) {
        if (arguments.length > 0) {
            return arguments[0].isProxyObject();
        }
        return this;
    }
}
