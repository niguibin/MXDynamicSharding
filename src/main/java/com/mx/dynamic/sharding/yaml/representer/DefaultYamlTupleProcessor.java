package com.mx.dynamic.sharding.yaml.representer;

import org.yaml.snakeyaml.nodes.*;

/**
 * @author: niguibin
 * @date: 2022/8/10 4:11 下午
 */
public final class DefaultYamlTupleProcessor {

    public NodeTuple process(final NodeTuple nodeTuple) {
        return isUnsetNodeTuple(nodeTuple.getValueNode()) ? null : nodeTuple;
    }

    private boolean isUnsetNodeTuple(final Node valueNode) {
        return isNullNode(valueNode) || isEmptyCollectionNode(valueNode);
    }

    private boolean isNullNode(final Node valueNode) {
        return Tag.NULL.equals(valueNode.getTag());
    }

    private boolean isEmptyCollectionNode(final Node valueNode) {
        return valueNode instanceof CollectionNode && (isEmptySequenceNode(valueNode) || isEmptyMappingNode(valueNode));
    }

    private boolean isEmptySequenceNode(final Node valueNode) {
        return Tag.SEQ.equals(valueNode.getTag()) && ((SequenceNode) valueNode).getValue().isEmpty();
    }

    private boolean isEmptyMappingNode(final Node valueNode) {
        return Tag.MAP.equals(valueNode.getTag()) && ((MappingNode) valueNode).getValue().isEmpty();
    }
}
