package vcenter.InstallPackage;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class SSHUtils {
    private String host;
    private String user;
    private String password;

    public SSHUtils(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    /**
     * 远程运行cmd
     *
     * @param command 命令
     * @return 返回结果
     * @throws JSchException 连接端口22的异常
     * @throws IOException   写入写出的异常
     */
    public CmdResult executeCommand(String[] command) throws JSchException, IOException {
        StringBuilder output = new StringBuilder();
        int exitCode = -1;
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(Arrays.toString(command));

        BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        channel.connect();

        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        exitCode = channel.getExitStatus();
        channel.disconnect();
        session.disconnect();

        return new CmdResult(output.toString(), exitCode);
    }
}