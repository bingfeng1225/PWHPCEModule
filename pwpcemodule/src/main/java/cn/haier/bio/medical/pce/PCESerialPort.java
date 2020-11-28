package cn.haier.bio.medical.pce;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cn.qd.peiwen.serialport.PWSerialPortHelper;
import cn.qd.peiwen.serialport.PWSerialPortListener;
import cn.qd.peiwen.serialport.PWSerialPortState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class PCESerialPort implements PWSerialPortListener {
    private ByteBuf buffer;
    private PCEHandler handler;
    private HandlerThread thread;
    private PWSerialPortHelper helper;

    private boolean ready = false;
    private boolean enabled = false;
    private WeakReference<IPCEListener> listener;

    public PCESerialPort() {
    }

    public void init(String path) {
        this.createHandler();
        this.createHelper(path);
        this.createBuffer();
    }

    public void enable() {
        if (this.isInitialized() && !this.enabled) {
            this.enabled = true;
            this.helper.open();
        }
    }

    public void disable() {
        if (this.isInitialized() && this.enabled) {
            this.enabled = false;
            this.helper.close();
        }
    }

    public void release() {
        this.listener = null;
        this.destoryHandler();
        this.destoryHelper();
        this.destoryBuffer();
    }

    public void changeListener(IPCEListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    private boolean isInitialized() {
        if (this.handler == null) {
            return false;
        }
        if (this.helper == null) {
            return false;
        }
        return this.buffer != null;
    }

    private void createHelper(String path) {
        if (this.helper == null) {
            this.helper = new PWSerialPortHelper("PCESerialPort");
            this.helper.setTimeout(10);
            this.helper.setPath(path);
            this.helper.setBaudrate(9600);
            this.helper.init(this);
        }
    }

    private void destoryHelper() {
        if (null != this.helper) {
            this.helper.release();
            this.helper = null;
        }
    }

    private void createHandler() {
        if (this.thread == null && this.handler == null) {
            this.thread = new HandlerThread("PCESerialPort");
            this.thread.start();
            this.handler = new PCEHandler(this.thread.getLooper());
        }
    }

    private void destoryHandler() {
        if (null != this.thread) {
            this.thread.quitSafely();
            this.thread = null;
            this.handler = null;
        }
    }

    private void createBuffer() {
        if (this.buffer == null) {
            this.buffer = Unpooled.buffer(4);
        }
    }

    private void destoryBuffer() {
        if (null != this.buffer) {
            this.buffer.release();
            this.buffer = null;
        }
    }

    private void write(byte[] data) {
        if (!this.isInitialized() || !this.enabled) {
            return;
        }
        this.helper.writeAndFlush(data);
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onPCEPrint("PCESerialPort Send:" + PCETools.bytes2HexString(data, true, ", "));
        }
    }

    private void switchReadModel() {
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onPCESwitchReadModel();
        }
    }

    private void switchWriteModel() {
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onPCESwitchWriteModel();
        }
    }

    private boolean ignorePackage() {
        int index = PCETools.indexOf(this.buffer, PCETools.HEADER);
        if (index != -1) {
            byte[] data = new byte[index];
            this.buffer.readBytes(data, 0, data.length);
            this.buffer.discardReadBytes();
            if (null != this.listener && null != this.listener.get()) {
                this.listener.get().onPCEPrint("PCESerialPort 指令丢弃:" + PCETools.bytes2HexString(data, true, ", "));
            }
            return this.processBytesBuffer();
        }
        return false;
    }

    private boolean processBytesBuffer() {
        if (this.buffer.readableBytes() < 4) {
            return true;
        }
        byte[] header = new byte[PCETools.HEADER.length];
        this.buffer.getBytes(0, header);
        byte command = this.buffer.getByte(3);
        if (!PCETools.checkHeader(header)) {
            return this.ignorePackage();
        }
        if (!PCETools.checkCommand(command)) {
            //当前指令不合法 丢掉正常的包头以免重复判断
            this.buffer.resetReaderIndex();
            this.buffer.skipBytes(2);
            this.buffer.discardReadBytes();
            return this.ignorePackage();
        }
        int frameLength = 0xFF & this.buffer.getByte(2) + 3;
        if (this.buffer.readableBytes() < frameLength) {
            return true;
        }
        this.buffer.markReaderIndex();
        byte[] data = new byte[frameLength];
        this.buffer.readBytes(data, 0, data.length);

        if (!PCETools.checkFrame(data)) {
            this.buffer.resetReaderIndex();
            //当前包不合法 丢掉正常的包头以免重复判断
            this.buffer.skipBytes(2);
            this.buffer.discardReadBytes();
            return this.ignorePackage();
        }
        this.buffer.discardReadBytes();
        if (!this.ready) {
            this.ready = true;
            if (null != this.listener && null != this.listener.get()) {
                this.listener.get().onPCEReady();
            }
        }
        this.switchWriteModel();
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onPCEPrint("PCESerialPort Recv:" + PCETools.bytes2HexString(data, true, ", "));
        }
        Message msg = Message.obtain();
        msg.obj = data;
        msg.what = 0xFF & command;
        this.handler.sendMessage(msg);
        return true;
    }

    private void processPackageReceived(int command, byte[] data) {
        byte[] response = null;
        if (command == 0x81) {
            if (null != this.listener && null != this.listener.get()) {
                response = this.listener.get().onPCEStatusPackageReceived(data);
            }
        } else if (command == 0x82) {
            if (null != this.listener && null != this.listener.get()) {
                response = this.listener.get().onPCEParameterPackageReceived(data);
            }
        } else if (command == 0x83) {
            if (null != this.listener && null != this.listener.get()) {
                response = this.listener.get().onPCETestingPackageReceived(data);
            }
        } else {
            if (null != this.listener && null != this.listener.get()) {
                response = this.listener.get().onPCEProgramPackageReceived(data);
            }
        }

        if (null != response && response.length > 0) {
            byte[] buffer = PCETools.packageResponse(command, response);
            this.write(buffer);
        }
        this.switchReadModel();
    }

    @Override
    public void onConnected(PWSerialPortHelper helper) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        this.ready = false;
        this.buffer.clear();
        this.switchReadModel();
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onPCEConnected();
        }
    }

    @Override
    public void onReadThreadReleased(PWSerialPortHelper helper) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onPCEPrint("PCESerialPort read thread released");
        }
    }

    @Override
    public void onException(PWSerialPortHelper helper, Throwable throwable) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        this.ready = false;
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onPCEException(throwable);
        }
    }

    @Override
    public void onStateChanged(PWSerialPortHelper helper, PWSerialPortState state) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onPCEPrint("PCESerialPort state changed: " + state.name());
        }
    }

    @Override
    public boolean onByteReceived(PWSerialPortHelper helper, byte[] buffer, int length) throws IOException {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return false;
        }
        this.buffer.writeBytes(buffer, 0, length);
        return this.processBytesBuffer();
    }

    private class PCEHandler extends Handler {
        public PCEHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PCESerialPort.this.processPackageReceived(msg.what, (byte[]) msg.obj);
        }
    }
}
