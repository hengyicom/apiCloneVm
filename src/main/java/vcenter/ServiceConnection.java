package vcenter;

import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;

public class ServiceConnection {
    private VimPortType service;
    private ServiceContent content;
    private UserSession usrSes;
    public ServiceConnection(VimPortType service, ServiceContent content, UserSession usrSes) {
        this.service = service;
        this.content = content;
        this.usrSes = usrSes;
    }
    /**
     * Returns the service interface of the connection.
     *
     * @return the service interface
     */
    public VimPortType getService() {
        return service;
    }

    /**
     * Returns the service content of the connection.
     *
     * @return the service content
     */
    public ServiceContent getServiceContent() {
        return content;
    }

    public UserSession getUserSession() {
        return usrSes;
    }

    public void setUserSession(UserSession usrSes) {
        this.usrSes = usrSes;
    }

    /**
     * Internally disconnect the client.
     *
     * @throws RuntimeFaultFaultMsg
     */
    protected void disconnect() throws RuntimeFaultFaultMsg {
        service.logout(content.getSessionManager());
    }
}
