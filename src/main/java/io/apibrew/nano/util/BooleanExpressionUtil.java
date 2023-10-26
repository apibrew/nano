package io.apibrew.nano.util;

import io.apibrew.client.model.Extension;
import io.apibrew.client.model.Resource;
import lombok.experimental.UtilityClass;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.apibrew.client.BooleanExpressionBuilder.*;

@UtilityClass
public class BooleanExpressionUtil {
    public Extension.BooleanExpression booleanExpressionFrom(Value expHolder) {
        if (expHolder.hasMembers()) {
            if (expHolder.getMemberKeys().size() != 1) {
                throw new IllegalArgumentException("query expected 1 member, got " + expHolder.getMemberKeys().size());
            }
            for (String member : expHolder.getMemberKeys()) {
                Value exp = expHolder.getMember(member);
                switch (member) {
                    case "and":
                        return and(asList(exp).stream().map(BooleanExpressionUtil::booleanExpressionFrom).collect(Collectors.toList()));
                    case "or":
                        return or(asList(exp).stream().map(BooleanExpressionUtil::booleanExpressionFrom).collect(Collectors.toList()));
                    case "not":
                        if (exp.getArraySize() != 1) {
                            throw new IllegalArgumentException("not expected 1 argument, got " + exp.getArraySize());
                        }
                        return not(booleanExpressionFrom(exp.getArrayElement(0)));
                    case "eq":
                    case "equal":
                        if (exp.hasArrayElements()) {
                            if (exp.getArraySize() != 2) {
                                throw new IllegalArgumentException("equal(eq) expected 2 arguments, got " + exp.getArraySize());
                            }
                            return eq(exp.getArrayElement(0).asString(), exp.getArrayElement(1).asString());
                        } else {
                            Extension.BooleanExpression expression = null;
                            if (exp.getMemberKeys().isEmpty()) {
                                throw new IllegalArgumentException("equal(eq) expected at least 1 member, got " + exp.getMemberKeys().size());
                            }
                            for (String property : exp.getMemberKeys()) {
                                if (expression == null) {
                                    expression = eq(property, exp.getMember(property).as(Object.class));
                                } else {
                                    expression = and(expression, eq(property, exp.getMember(property).as(Object.class)));
                                }
                            }
                            return expression;
                        }
                    case "ne":
                    case "notEqual":
                        if (exp.getArraySize() != 2) {
                            throw new IllegalArgumentException("notEqual(ne) expected 2 arguments, got " + exp.getArraySize());
                        }
                        return notEqual(exp.getArrayElement(0).asString(), exp.getArrayElement(1).as(Object.class));
                    default:
                        throw new IllegalArgumentException("Unsupported expression: " + exp);
                }
            }
        }

        throw new IllegalArgumentException("Unsupported expression: " + expHolder);
    }


    private List<Value> asList(Value exp) {
        List<Value> values = new ArrayList<>();

        for (int i = 0; i < exp.getArraySize(); i++) {
            values.add(exp.getArrayElement(i));
        }

        return values;
    }
}
