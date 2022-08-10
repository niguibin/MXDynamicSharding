package com.mx.dynamic.sharding.election;

import com.mx.dynamic.sharding.storage.NodePath;

/**
 * @author: niguibin
 * @date: 2022/8/10 3:27 下午
 */
public class LeaderNode {

    public static final String ROOT = "leader";

    private static final String ELECTION_ROOT = ROOT + "/election";

    static final String INSTANCE = ELECTION_ROOT + "/instance";

    static final String LATCH = ELECTION_ROOT + "/latch";

    public boolean isLeaderInstancePath(String path) {
        return NodePath.getFullPath(INSTANCE).equals(path);
    }
}
