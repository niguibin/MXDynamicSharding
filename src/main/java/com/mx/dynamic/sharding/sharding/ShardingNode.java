package com.mx.dynamic.sharding.sharding;

import com.mx.dynamic.sharding.election.LeaderNode;

/**
 * @author: niguibin
 * @date: 2022/8/10 5:04 下午
 */
public class ShardingNode {

    public static final String ROOT = "sharding";

    private static final String INSTANCE_APPENDIX = "instance";

    private static final String INSTANCE = ROOT + "/%s/" + INSTANCE_APPENDIX;

    private static final String LEADER_ROOT = LeaderNode.ROOT + "/" + ROOT;

    public static final String NECESSARY = LEADER_ROOT + "/necessary";

    public static final String PROCESSING = LEADER_ROOT + "/processing";

    public static String getInstanceNode(int item) {
        return String.format(INSTANCE, item);
    }


}
