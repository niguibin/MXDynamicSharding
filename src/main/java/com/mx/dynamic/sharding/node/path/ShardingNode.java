package com.mx.dynamic.sharding.node.path;

/**
 * @author: niguibin
 * @date: 2022/8/11 11:16 上午
 */
public class ShardingNode {

    public static final String ROOT = "/sharding";

    public static final String ITEM = ROOT + "/%s";

    public static final String INSTANCE = ITEM + "/instance";

    public static String getInstancePath(int item) {
        return String.format(INSTANCE, item);
    }

    public static String getItemPath(int item) {
        return String.format(ITEM, item);
    }
}
