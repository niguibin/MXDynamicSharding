package com.mx.dynamic.sharding.yaml;

import com.mx.dynamic.sharding.yaml.representer.CustomYamlRepresenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

/**
 * @author: niguibin
 * @date: 2022/8/10 4:07 下午
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YamlEngine {

    public static String marshal(final Object value) {
        return new Yaml(new CustomYamlRepresenter()).dumpAsMap(value);
    }

    public static <T> T unmarshal(final String yamlContent, final Class<T> classType) {
        return new Yaml().loadAs(yamlContent, classType);
    }
}
