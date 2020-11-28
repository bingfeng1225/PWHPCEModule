package cn.haier.bio.medical.pce;

/***
 * 超低温变频、T系列、双系统主控板通讯
 *
 */
public class PCEManager {
    private PCESerialPort serialPort;
    private static PCEManager manager;

    public static PCEManager getInstance() {
        if (manager == null) {
            synchronized (PCEManager.class) {
                if (manager == null)
                    manager = new PCEManager();
            }
        }
        return manager;
    }

    private PCEManager() {

    }

    public void init(String path) {
        if (this.serialPort == null) {
            this.serialPort = new PCESerialPort();
            this.serialPort.init(path);
        }
    }

    public void enable() {
        if (null != this.serialPort) {
            this.serialPort.enable();
        }
    }

    public void disable() {
        if (null != this.serialPort) {
            this.serialPort.disable();
        }
    }

    public void release() {
        if (null != this.serialPort) {
            this.serialPort.release();
            this.serialPort = null;
        }
    }

    public void changeListener(IPCEListener listener) {
        if (null != this.serialPort) {
            this.serialPort.changeListener(listener);
        }
    }
}

