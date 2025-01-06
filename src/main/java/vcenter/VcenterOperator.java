package vcenter;

import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * vcenter 操作接口
 */
public interface VcenterOperator {

    /**
     * vcenter content
     */
    ServiceConnection content() throws Exception;

    /**
     * 关闭连接
     */
    void closeConnect();
}
