

## 一、ZK 节点

1. 键空间：dynamic-sharding
   - instances		持久化节点
     - 192.168.1.3@-@14609：instanceId: 192.168.1.3@-@14609 serverIp: 192.168.1.3		临时节点
     - 192.168.1.3@-@19512：instanceId: 192.168.1.3@-@19512 serverIp: 192.168.1.3        临时节点
     - 192.168.1.3@-@19516：instanceId: 192.168.1.3@-@19516 serverIp: 192.168.1.3        临时节点
     - 192.168.1.3@-@19520：instanceId: 192.168.1.3@-@19520 serverIp: 192.168.1.3        临时节点
   - leader
     - sharding：“”        持久化节点
       - necessary		持久化节点，由 leader 创建，创建后触发分片，分片结束立即删除
       - processing       临时节点，由 leader 创建，只有在分片时才会创建，分片结束立即删除
     - election        临时节点
       - instance：192.168.1.3@-@14609		临时节点
       - latch        临时节点，LeaderLatch 选举时使用，选完就删除了
   - sharding
     - 0		持久化节点
       - instance：192.168.1.3@-@14609		持久化节点
     - 1        持久化节点
       - instance：192.168.1.3@-@19512        持久化节点
     - 2        持久化节点
       - instance：192.168.1.3@-@19516        持久化节点
     - 3        持久化节点
       - instance：192.168.1.3@-@19520        持久化节点

## 二、原理

1. 当启动时，执行了三个方法

   ```java
   // 添加四个数据监听器和一个连接状态监听器
   listenerManager.addAllListeners();
   // 进行选举
   leaderService.electLeader(); // 该方法和下面的 persistOnline 不冲突，当数据监听的时候，会和下面的方法有关系，会先判断下面方法创建的节点是否存在
   // 创建临时节点 /instances/192.168.1.3@-@xxx
   instancesService.persistOnline();
   ```

2. 数据监听器，实现 CuratorCacheListener 接口

   - ShardingProcessListener：监听 /leader/sharding/necessary 节点的创建和删除
     - 创建：说明需要分片，调用 ShardingNotice#startSharding 方法，由用户自己实现
     - 删除：说明分片完成，调用 ShardingNotice#shardingCompleted，由用户自己实现
   - ShardingNecessaryListener：监听 /leader/sharding/necessary 节点的创建，进行分片，分片过程如下：
     - 获取所有的在线实例，即获取 /instances 下的所有子节点的值通过 YmalEngine 反序列化的 ServerInstance 对象列表
     - 对实例列表进行排序
     - 如果不是 leader，并且存在 /leader/sharding/necessary 节点或存在 /leader/sharding/processing 节点就一直阻塞（每 100ms 检查一次），存在 /leader/sharding/necessary 节点或存在 /leader/sharding/processing 节点说明分片还没有完成
     - 如果是 leader，则先创建临时节点 /leader/sharding/processing，表明分片正在执行中
     - 然后删除 /sharding/%s/instance 节点，然后再根据上面的实例总数创建 /sharding/%s
     - 然后在事务中创建持久化节点 /sharding/%s/instance，value 是上面在线实例列表中对应位置 ServerInstance 的 instanceId
     - 在事务中创建完成后上一步后，紧接着删除（仍然在事务中） /leader/sharding/necessary 节点和 /leader/sharding/processing 节点，说明分片完成，取消非 leader 的阻塞流程，并触发 ShardingProcessListener 监听器
   - InstancesChangedListener：监听 /instances 的子节点的创建和删除，即新增实例或减少实例时触发，触发后，判断本机是不是 leader，如果是，则去创建持久化节点 /leader/sharding/necessary，触发 ShardingNecessaryListener 监听器和 ShardingProcessListener 监听器
   - LeaderElectionListener：每次当有节点变化时，都检查
     - 判断是否有 leader：看有没有 /leader/election/instance 节点
     - 判断本机实例是否在线：获取 /instances 节点下的子节点，判断是否包含本机实例节点 192.168.1.3@-@14609
     - 如果没有 leader 并且本机在线，则参加选举

3. 连接状态监听器：RegistryCenterConnectionStateListener，实现 ConnectionStateListener 接口

   - 如果状态为 RECONNECTED，即断开重新连接了之后，则执行 `InstancesService#persistOnline` 方法创建临时节点 /instances/192.168.1.3@-@14609
   - 在启动时就已经执行过 `InstancesService#persistOnline` 方法了，所以不用监听 CONNECTED 状态
   - 因为创建的是临时节点，所以当实例下线后，/instances 下对应的节点会自己删除，会触发 LeaderElectionListener 和 InstancesChangedListener 监听器。当新加一个实例或旧的实例下线且重新上线之后，会再创建 /instances/xxx 节点，也会触发 LeaderElectionListener 和 InstancesChangedListener 监听器

