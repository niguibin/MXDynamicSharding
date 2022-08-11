package com.mx.dynamic.sharding.node.storage;

import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.TransactionOp;

import java.util.List;

/**
 * @author: niguibin
 * @date: 2022/8/9 3:41 下午
 */
public interface TransactionExecutionCallback {
    List<CuratorOp> createCuratorOperators(TransactionOp transactionOp) throws Exception;
}
