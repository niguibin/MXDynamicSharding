package com.mx.dynamic.sharding;

import com.mx.dynamic.sharding.base.CoordinatorRegistryCenter;
import com.mx.dynamic.sharding.base.InstanceManager;
import com.mx.dynamic.sharding.entity.Instance;
import com.mx.dynamic.sharding.facade.LiteFacade;
import com.mx.dynamic.sharding.facade.SetUpFacade;

/**
 * @author: niguibin
 * @date: 2022/8/9 2:48 下午
 */
public class DynamicShardBootstrap {

    private CoordinatorRegistryCenter registryCenter;

    private SetUpFacade setUpFacade;

    private LiteFacade liteFacade;


    public DynamicShardBootstrap(CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        this.setUpFacade = new SetUpFacade();
        this.liteFacade = new LiteFacade();

        InstanceManager.getINSTANCE().setRegistryCenter(registryCenter);
        InstanceManager.getINSTANCE().setInstance(new Instance());
        setUpFacade.registerStartUpInfo();
    }

}
