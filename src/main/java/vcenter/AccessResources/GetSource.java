package vcenter.AccessResources;

import com.vmware.vim25.*;
import vcenter.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class GetSource {
    private final ServiceConnection serviceConnection;
    private static final AtomicReference<GetSource> instance = new AtomicReference<>();

    private GetSource(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
    }

    public static GetSource getInstance(ServiceConnection serviceConnection) {
        GetSource currentInstance = instance.get();
        if (currentInstance == null) {
            synchronized (GetSource.class) {
                currentInstance = instance.get();
                if (currentInstance == null) {
                    currentInstance = new GetSource(serviceConnection);
                    instance.set(currentInstance);
                }
            }
        }
        return currentInstance;
    }

    //获得克隆虚机的源机名
    public Map<String, ManagedObjectReference> inContainerByType(ManagedObjectReference container, String morefType) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        return inContainerByType(container, morefType, new RetrieveOptions());
    }

    public Map<String, ManagedObjectReference> inContainerByType(ManagedObjectReference folder, String morefType, RetrieveOptions retrieveOptions) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        RetrieveResult results = containerViewByType(folder, morefType, retrieveOptions);
        return toMap(results);
    }

    public Map<String, ManagedObjectReference> toMap(RetrieveResult results) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        Map<String, ManagedObjectReference> targetMap = new HashMap<>();
        String token = null;

        // 使用 continueRetrievePropertiesEx 进行分页获取
        token = populate(results, targetMap);
        while (token != null && !token.isEmpty()) {
            results = serviceConnection.getService().continueRetrievePropertiesEx(serviceConnection.getServiceContent().getPropertyCollector(), token);
            token = populate(results, targetMap);
        }
        return targetMap;
    }

    private String populate(RetrieveResult results, Map<String, ManagedObjectReference> targetMap) {
        String token = null;
        if (results != null) {
            token = results.getToken();
            for (ObjectContent oc : results.getObjects()) {
                ManagedObjectReference mr = oc.getObj();
                String entityName = null;
                List<DynamicProperty> dynamicProperties = oc.getPropSet();
                if (dynamicProperties != null) {
                    for (DynamicProperty dp : dynamicProperties) {
                        entityName = (String) dp.getVal();
                    }
                }
                targetMap.put(entityName, mr);
            }
        }
        return token;
    }

    public RetrieveResult containerViewByType(ManagedObjectReference container, String morefType, RetrieveOptions retrieveOptions) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        return containerViewByType(container, morefType, retrieveOptions, "name");
    }

    public RetrieveResult containerViewByType(ManagedObjectReference container, String morefType, RetrieveOptions retrieveOptions, String... morefProperties) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        PropertyFilterSpec[] propertyFilterSpecs = createPropertyFilterSpecs(container, morefType, morefProperties);
        return containerViewByType(container, morefType, retrieveOptions, propertyFilterSpecs);
    }

    private RetrieveResult containerViewByType(ManagedObjectReference container, String morefType, RetrieveOptions retrieveOptions, PropertyFilterSpec[] propertyFilterSpecs) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        return serviceConnection.getService().retrievePropertiesEx(
                serviceConnection.getServiceContent().getPropertyCollector(),
                Arrays.asList(propertyFilterSpecs),
                retrieveOptions);
    }

    private PropertyFilterSpec[] createPropertyFilterSpecs(ManagedObjectReference container, String morefType, String... morefProperties) throws RuntimeFaultFaultMsg {
        ManagedObjectReference viewManager = serviceConnection.getServiceContent().getViewManager();
        ManagedObjectReference containerView = serviceConnection.getService().createContainerView(
                viewManager, container, Arrays.asList(morefType), true);

        PropertySpecBuilder propSpecBuilder = new PropertySpecBuilder();
        propSpecBuilder.all(false).type(morefType).pathSet(morefProperties);

        ObjectSpecBuilder objSpecBuilder = new ObjectSpecBuilder();
        objSpecBuilder.obj(containerView).skip(true)
                .selectSet(new TraversalSpecBuilder().name("view").path("view").skip(false).type("ContainerView"));

        return new PropertyFilterSpec[]{
                new PropertyFilterSpecBuilder().propSet(propSpecBuilder).objectSet(objSpecBuilder)
        };
    }

    //获取数据存储
    public ConfigTarget getConfigTargetForHost(ManagedObjectReference computeResMor, ManagedObjectReference hostMor) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference envBrowseMor = (ManagedObjectReference) entityProps(computeResMor, new String[]{"environmentBrowser"}).get("environmentBrowser");
        ConfigTarget configTarget = serviceConnection.getService().queryConfigTarget(envBrowseMor, hostMor);

        if (configTarget == null) {
            throw new RuntimeException("No ConfigTarget found in ComputeResource");
        }
        return configTarget;
    }

    public Map<String, Object> entityProps(ManagedObjectReference entityMor, String[] props) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        Map<String, Object> result = new HashMap<>();
        //构建属性过滤规范
        PropertyFilterSpec[] propertyFilterSpecs = new PropertyFilterSpec[]{
                new PropertyFilterSpecBuilder().propSet(new PropertySpecBuilder().all(false).type(entityMor.getType()).pathSet(props))
                        .objectSet(new ObjectSpecBuilder().obj(entityMor))
        };
        //调用远程服务获取属性信息
        List<ObjectContent> objectContents = serviceConnection.getService().retrievePropertiesEx(
                serviceConnection.getServiceContent().getPropertyCollector(),
                Arrays.asList(propertyFilterSpecs),
                new RetrieveOptions()).getObjects();

        if (objectContents != null) {
            for (ObjectContent objectContent : objectContents) {
                List<DynamicProperty> dynamicProperties = objectContent.getPropSet();
                for (DynamicProperty dynamicProperty : dynamicProperties) {
                    result.put(dynamicProperty.getName(), dynamicProperty.getVal());
                }
            }
        }
        return result;
    }
}
