package com.mx.dynamic.sharding.yaml.representer;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * @author: niguibin
 * @date: 2022/8/10 4:10 下午
 */
public final class CustomYamlRepresenter extends Representer {

    public CustomYamlRepresenter() {
        super(new DumperOptions());
    }

    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
        return new DefaultYamlTupleProcessor().process(super.representJavaBeanProperty(javaBean, property, propertyValue, customTag));
    }
}
