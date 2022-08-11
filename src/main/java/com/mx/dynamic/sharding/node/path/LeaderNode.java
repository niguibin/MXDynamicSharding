package com.mx.dynamic.sharding.node.path;

/**
 * @author: niguibin
 * @date: 2022/8/11 11:17 上午
 */
public class LeaderNode {

    public static final String ROOT = "/leader";

    public static final String ELECTION = ROOT + "/election";

    public static final String ELECTION_LATCH = ELECTION + "/latch";

    public static final String ELECTION_INSTANCE = ELECTION + "/instance";

    public static final String SHARDING_NECESSARY = ROOT + "/sharding/necessary";

    public static final String SHARDING_PROCESS = ROOT + "/sharding/processing";

    public static boolean isLeaderInstancePath(String path) {
        return ELECTION_INSTANCE.equals(path);
    }

    public static boolean isShardingNecessaryPath(String path) {
        return SHARDING_NECESSARY.equals(path);
    }

    public static boolean isShardingProcessPath(String path) {
        return SHARDING_PROCESS.equals(path);
    }

}
