package vcenter.InstallPackage;

public class CmdResult {
    private String output;  // 命令输出
    private int exitCode;   // 命令退出状态码

    public CmdResult(String output, int exitCode) {
        this.output = output;
        this.exitCode = exitCode;
    }

    public String getOutput() {
        return output;
    }

    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return "CmdResult{" +
                "output='" + output + '\'' +
                ", exitCode=" + exitCode +
                '}';
    }
}