package cwh.utils.process;

import cwh.utils.concurrent.ThreadUtils;
import cwh.utils.log.VSLog;
import cwh.web.model.CommonDefine;

import java.io.*;

/**
 * Created by cwh on 16-1-2
 */
public class CmdExecutor {
    public static String TAG = "CmdExecutor";
    public static boolean LOG_TOGGLE = false;

    public static String exec(String cmd) {
        try {
            String[] cmdA = {"/bin/sh", "-c", cmd};
            Process process = Runtime.getRuntime().exec(cmdA);
            LineNumberReader br = new LineNumberReader(
                    new InputStreamReader(process
                            .getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                VSLog.d(TAG, line);
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void wait(String command) {
        Process proc = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            VSLog.d(TAG, command);
            long curTime = System.currentTimeMillis();
            proc = runtime.exec(command);
            InputStream err = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(err);
            BufferedReader br = new BufferedReader(isr);
            String line = null;

//            VSLog.d(TAG, proc.toString());
            while ((line = br.readLine()) != null && LOG_TOGGLE) {
                VSLog.d(TAG, line);
            }
            int exitVal = proc.waitFor();
            VSLog.d(TAG, "Exit value: " + exitVal + ", time: " + (System.currentTimeMillis() - curTime)
                    + "ms, command: " + command + " complete");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void waitForPrint(String command, OnLine onLine) {
        Process proc = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            proc = runtime.exec(command);
            InputStream err = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(err);
            BufferedReader br = new BufferedReader(isr);
            String line = null;

//            VSLog.d(TAG, proc.toString());
            while ((line = br.readLine()) != null) {
                if (LOG_TOGGLE) {
                    VSLog.d(TAG, line);
                }
                onLine.onLine(line);
            }
            int exitVal = proc.waitFor();
            VSLog.d(TAG, "Exit value: " + exitVal + "; command: " + command + " complete");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Process run(String command) {
        VSLog.d(TAG, command);
        Process proc = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            VSLog.d(TAG, command);
            proc = runtime.exec(command);
            VSLog.d(TAG, "call: " + command + " complete");
            if (LOG_TOGGLE) {
                InputStream err = proc.getErrorStream();
                InputStreamReader isr = new InputStreamReader(err);
                final BufferedReader br = new BufferedReader(isr);
                // 开一个新线程，否则会卡进程
                ThreadUtils.runInBackGround(new Runnable() {
                    @Override
                    public void run() {
                        String line = null;
                        try {
                            while ((line = br.readLine()) != null) {
                                if (line.startsWith("frame=")) {
                                    continue;
                                }
                                VSLog.d(TAG, line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // 这里的stream由外层proc控制关闭
                    }
                });
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return proc;
    }

    public static void main(String[] args) {
        String rst = exec("ls");
        VSLog.d(TAG, rst);
    }
}
