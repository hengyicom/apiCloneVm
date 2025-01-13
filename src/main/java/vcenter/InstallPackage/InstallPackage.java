package vcenter.InstallPackage;

import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.util.Arrays;

public class InstallPackage {

    SSHUtils sshUtils = new SSHUtils("10.1.132.99","root","");
    public CmdResult runCmds(String host, String... cmds) throws JSchException, IOException {
        return runCmds(host, 300, cmds);
    }

    public CmdResult runCmds(String host, int timeout, String... cmds) throws JSchException, IOException {
        CmdResult cmdResult = sshUtils.executeCommand(cmds);
        if (cmdResult.getExitCode() == 1) {
            throw new RuntimeException("xxx " + cmds + "执行失败!");
        }
        return cmdResult;
    }
    /**
     * 配置源
     */
    private void configureSource(String host) throws Exception {
        String yum = "yum";
        if (runCmds(host, 3, "apt --version").getExitCode() == 0) {
            yum = "apt";
        } else if (runCmds(host, 3, "zypper --version").getExitCode() == 0) {
            yum = "zypper";
        }
        CmdResult cmdResult = runCmds(host, "curl http://172.20.64.23/source.sh -sS | bash");
        if (!cmdResult.getOutput().contains("源配置成功")) {
            runCmds(host, yum + " –exclude=kernel* update -y");
        }
    }
}
