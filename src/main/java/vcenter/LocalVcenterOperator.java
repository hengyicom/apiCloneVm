package vcenter;

import com.vmware.vim25.*;

import javax.net.ssl.*;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.BindingProvider;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

public class LocalVcenterOperator implements VcenterOperator {
    private ServiceConnection connection;
    private final  String user;
    private final  String password;
    private final String  VcenterIp;
    public LocalVcenterOperator(String user, String password, String vcenterIp){
        this.user = user;
        this.password = password;
        VcenterIp = vcenterIp;
    }
    @Override
    public ServiceConnection content() throws Exception {
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };
        trustAllHttpsCertificates();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);


        if (StringUtil.isEmpty(user) || StringUtil.isEmpty(password) || StringUtil.isEmpty(VcenterIp)) {
            throw new RuntimeException("Couldn't connect to vSphere due to null user or password or vcenter ip");
        }

        String url =  VcenterIp;
        boolean repeatLogin = true;
        int numFailedLogins = 0;
        while (repeatLogin) {  // 控制重试次数
            try {
                HttpsURLConnection.setDefaultHostnameVerifier(hv);
                VimService vimService = new VimService();
                VimPortType vimPort = vimService.getVimPort();
                Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();
                QName xsdQName = new QName("http://www.w3.org/2001/XMLSchema", "xsd");
                ctxt.put("javax.xml.namespace.NamespaceContext", new NamespaceSContext(xsdQName));
                ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
                ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);

                ManagedObjectReference morSvcInstance = new ManagedObjectReference();
                morSvcInstance.setType("ServiceInstance");
                morSvcInstance.setValue("ServiceInstance");
                ServiceContent serviceContent = vimPort.retrieveServiceContent(morSvcInstance);

                connection = new ServiceConnection(vimPort, serviceContent,
                        vimPort.login(serviceContent.getSessionManager(), user, password, null));

                repeatLogin = false;
                return  connection;
            } catch (InvalidLocaleFaultMsg | RuntimeFaultFaultMsg | InvalidLoginFaultMsg e) {
                if (numFailedLogins > 2) {
                    throw e;
                }
                numFailedLogins++;
                repeatLogin = true;
                try {
                    Thread.sleep(3000);
                } catch (@SuppressWarnings("unused") InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null;
    }

    @Override
    public void closeConnect() {
        if (connection != null) {
            log.println("Current connection is disconnected");
            try {
                connection.disconnect();
            } catch (Throwable e) {
                log.printf("failed to disconnection ", e);
            }
            connection = null;
        }
    }
    private static void trustAllHttpsCertificates() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new TrustAllTrustManager();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
    private static class TrustAllTrustManager implements TrustManager, X509TrustManager {
        private TrustAllTrustManager() {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }
    }
}
