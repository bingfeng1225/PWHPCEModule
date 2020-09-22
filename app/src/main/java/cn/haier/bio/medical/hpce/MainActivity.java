package cn.haier.bio.medical.hpce;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import cn.haier.bio.medical.pce.IPCEListener;
import cn.haier.bio.medical.pce.PCEManager;
import cn.qd.peiwen.logger.PWLogger;
import cn.qd.peiwen.pwtools.EmptyUtils;
import cn.qd.peiwen.serialport.PWSerialPort;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MainActivity extends AppCompatActivity implements IPCEListener {
    //Control
    private int lockControl = 0;
    private int powerControl = 0;
    private int dryingControl = 0;
    private int movetoControl = 0;
    private int runningControl = 0;

    //Parameters
    private int correctType = 0;
    private int cavityCorrect = 100;
    private int sampleCorrect = 105;
    private int cavityN2Correct = 130;
    private int sampleN2Correct = 140;
    private int cavityMixingCorrect = 125;
    private int sampleMixingCorrect = 135;
    private int ambientCorrect = 110;
    private int highAlarmOffset = 115;
    private int lowerAlarmOffset = 120;
    private int doorOpenDelay = 5;


    //Testing
    private int testing = 0;

    private int testFan = 0;
    private int testLock = 0;
    private int testSolenoidA = 0;
    private int testSolenoidB = 0;
    private int testHeatingTube = 0;

    private int iotFunction = 0;
    private int lockFunction = 0;
    private int rfidFunction = 0;
    private int fingerFunction = 0;

    private int productModel = 0x10;

    //Program running
    private int programType = 0;
    private int programIndex = 0;
    private ProgramEntity program = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String path = "/dev/ttyS4";
        if ("magton".equals(Build.MODEL)) {
            path = "/dev/ttyS2";
        }
        PCEManager.getInstance().init(path);
        PCEManager.getInstance().changeListener(this);
        PCEManager.getInstance().enable();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PCEManager.getInstance().disable();
        PCEManager.getInstance().release();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.finish:
                this.finishProgram();
                break;
            case R.id.custom:
                this.executeCustomProgram();
                break;
            case R.id.standard:
                this.executeStandardProgram();
                break;
        }
    }

    @Override
    public void onPCEConnected() {
        PWLogger.debug("onPCEConnected");
    }

    @Override
    public void onPCESwitchWriteModel() {
        PWLogger.debug("onPCESwitchWriteModel");
        if (!"magton".equals(Build.MODEL)) {
            PWSerialPort.writeFile("/sys/class/gpio/gpio24/value", "0");
        } else {
            PWSerialPort.writeFile("/sys/class/misc/sunxi-acc/acc/sochip_acc", "1");
        }
    }

    @Override
    public void onPCESwitchReadModel() {
        PWLogger.debug("onPCESwitchReadModel");
        if (!"magton".equals(Build.MODEL)) {
            PWSerialPort.writeFile("/sys/class/gpio/gpio24/value", "1");
        } else {
            PWSerialPort.writeFile("/sys/class/misc/sunxi-acc/acc/sochip_acc", "0");
        }
    }

    @Override
    public void onPCEPrint(String message) {
        PWLogger.debug("" + message);
    }

    @Override
    public void onPCEException(Throwable throwable) {
        PWLogger.debug("onPCEException");
        PWLogger.error(throwable);
    }

    @Override
    public byte[] onPCEStatusPackageReceived(byte[] data) {
        PWLogger.debug("onPCEStatusPackageReceived");


        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(this.makeControl());
        buf.writeByte(0x00);

        buf.writeByte(this.movetoControl);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);

        byte[] response = new byte[buf.readableBytes()];
        buf.readBytes(response, 0, response.length);
        buf.release();
        return response;
    }

    @Override
    public byte[] onPCEProgramPackageReceived(byte[] data) {
        PWLogger.debug("onPCEProgramPackageReceived");
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(data);

        buf.skipBytes(4);
        int index = 0xFF & buf.readByte();
        buf.release();
        return this.packageProgramMessage(index);
    }

    @Override
    public byte[] onPCETestingPackageReceived(byte[] data) {
        PWLogger.debug("onPCETestingPackageReceived");

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(this.testing);
        buf.writeByte(this.makeTesting());

        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(this.makeFunction());

        buf.writeByte(0x00);
        buf.writeByte(this.productModel);


        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);

        byte[] response = new byte[buf.readableBytes()];
        buf.readBytes(response, 0, response.length);
        buf.release();
        return response;
    }


    @Override
    public byte[] onPCEParameterPackageReceived(byte[] data) {
        PWLogger.debug("onPCEParameterPackageReceived");

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(this.correctType);

        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(this.cavityCorrect);
        buf.writeByte(this.sampleCorrect);
        buf.writeByte(this.ambientCorrect);

        buf.writeByte(0x00);
        buf.writeByte(this.highAlarmOffset);
        buf.writeByte(this.lowerAlarmOffset);
        buf.writeByte(this.doorOpenDelay - 1);

        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(this.cavityMixingCorrect);
        buf.writeByte(this.cavityN2Correct);
        buf.writeByte(this.sampleMixingCorrect);
        buf.writeByte(this.sampleN2Correct);

        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);

        byte[] response = new byte[buf.readableBytes()];
        buf.readBytes(response, 0, response.length);
        buf.release();
        return response;
    }


    private int makeControl() {
        int lock = (this.lockControl == 0) ? 0x00 : 0x02;
        int power = (this.powerControl == 0) ? 0x00 : 0x01;
        int drying = (this.dryingControl == 0) ? 0x00 : 0x08;
        int running = (this.runningControl == 0) ? 0x00 : 0x04;
        return (lock | power | drying | running);
    }

    private int makeTesting() {
        int fan = (this.testFan == 0) ? 0x00 : 0x01;
        int lock = (this.testLock == 0) ? 0x00 : 0x016;
        int solenoidA = (this.testSolenoidA == 0) ? 0x00 : 0x02;
        int solenoidB = (this.testSolenoidB == 0) ? 0x00 : 0x04;
        int heatingTube = (this.testHeatingTube == 0) ? 0x00 : 0x08;
        return (fan | lock | solenoidA | solenoidB | heatingTube);
    }


    private int makeFunction() {
        int iot = (this.iotFunction == 0) ? 0x00 : 0x04;
        int lock = (this.lockFunction == 0) ? 0x00 : 0x01;
        int rfid = (this.rfidFunction == 0) ? 0x00 : 0x08;
        int finger = (this.fingerFunction == 0) ? 0x00 : 0x02;
        return (iot | lock | rfid | finger);
    }

    private ProgramEntity makeProgram() {
        ProgramEntity program = new ProgramEntity();
        program.setId(7);

        ProgramTaskEntity wait = new ProgramTaskEntity();
        wait.setId(1);
        wait.setPid(7);
        wait.setIndex(1);
        wait.setHoldTime(200);
        wait.setCalibrationType(CalibrationType.CALIBRATION_CAVITY);
        wait.setCavityTemperature(TemperatureTools.uc2SC(-20.0f));
        wait.setTaskType(ProgramTaskType.PROGRAM_TASK_WAIT);
        program.addTask(wait);

        ProgramTaskEntity waitfor = new ProgramTaskEntity();
        waitfor.setId(2);
        waitfor.setPid(7);
        waitfor.setIndex(2);
        waitfor.setHoldTime(300);
        waitfor.setCavityTemperature(TemperatureTools.uc2SC(-20.0f));
        waitfor.setSampleTemperature(TemperatureTools.uc2SC(-15.0f));
        waitfor.setTaskType(ProgramTaskType.PROGRAM_TASK_WAIT_FOR);
        program.addTask(waitfor);

        ProgramTaskEntity ramp = new ProgramTaskEntity();
        ramp.setId(3);
        ramp.setPid(7);
        ramp.setIndex(3);
        ramp.setFrequency(TemperatureTools.uc2SC(1.0f));
        ramp.setCalibrationType(CalibrationType.CALIBRATION_CAVITY);
        ramp.setFinishTemperature(TemperatureTools.uc2SC(-50.0f));
        ramp.setTaskType(ProgramTaskType.PROGRAM_TASK_RAMP);
        program.addTask(ramp);


        ProgramTaskEntity ramp1 = new ProgramTaskEntity();
        ramp1.setId(4);
        ramp1.setPid(7);
        ramp1.setIndex(4);
        ramp1.setFrequency(TemperatureTools.uc2SC(1.0f));
        ramp1.setCalibrationType(CalibrationType.CALIBRATION_SAMPLE);
        ramp1.setFinishTemperature(TemperatureTools.uc2SC(10.0f));
        ramp1.setTaskType(ProgramTaskType.PROGRAM_TASK_RAMP);
        program.addTask(ramp1);


        ProgramTaskEntity hold = new ProgramTaskEntity();
        hold.setId(5);
        hold.setPid(7);
        hold.setIndex(5);
        hold.setHoldTime(400);
        hold.setCalibrationType(CalibrationType.CALIBRATION_CAVITY);
        hold.setFinishTemperature(TemperatureTools.uc2SC(-10.0f));
        hold.setTaskType(ProgramTaskType.PROGRAM_TASK_HOLD);
        program.addTask(hold);


        ProgramTaskEntity hold1 = new ProgramTaskEntity();
        hold1.setId(6);
        hold1.setPid(7);
        hold1.setIndex(6);
        hold1.setHoldTime(500);
        hold1.setCalibrationType(CalibrationType.CALIBRATION_SAMPLE);
        hold1.setFinishTemperature(TemperatureTools.uc2SC(-15.0f));
        hold1.setTaskType(ProgramTaskType.PROGRAM_TASK_HOLD);
        program.addTask(hold1);


        ProgramTaskEntity jump = new ProgramTaskEntity();
        jump.setId(7);
        jump.setPid(7);
        jump.setIndex(7);
        jump.setTargetIndex(3);
        jump.setTaskType(ProgramTaskType.PROGRAM_TASK_JUMP);
        program.addTask(jump);


        ProgramTaskEntity loop = new ProgramTaskEntity();
        loop.setId(8);
        loop.setPid(7);
        loop.setIndex(8);
        loop.setLoopCount(2);
        loop.setTargetIndex(3);
        program.addTask(loop);
        loop.setTaskType(ProgramTaskType.PROGRAM_TASK_LOOP);
        return program;
    }

    private synchronized void finishProgram() {
        if(this.programType != 0) {
            this.program = null;
            this.programType = 0;
            this.programIndex = 0;
            this.powerControl = 0;
        }
    }

    private synchronized void executeCustomProgram() {
        if(this.programType == 0) {
            this.programType = 2;
            this.powerControl = 1;
            this.programIndex = 0;
            this.program = this.makeProgram();
        }
    }

    private synchronized void executeStandardProgram() {
        if(this.programType == 0) {
            this.program = null;
            this.programType = 1;
            this.programIndex = 2;
            this.powerControl = 1;
        }
    }

    private synchronized byte[] packageProgramMessage(int index) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(this.programType);
        if (this.programType == 0) {
            for (int i = 0; i < 19; i++) {
                buf.writeByte(0x00);
            }
        } else if (this.programType == 1) {
            buf.writeByte(0x00);
            buf.writeByte(0x00);
            buf.writeByte(this.programIndex);
            for (int i = 0; i < 16; i++) {
                buf.writeByte(0x00);
            }
        } else {
            PWLogger.debug("正在获取步骤 ("+(index + 1)+"/" + this.program.getTasksCount());
            buf.writeByte(this.program.getTasksCount());
            buf.writeByte(0x00);
            buf.writeByte(this.program.getId());
            buf.writeByte(0x00);
            ProgramTaskEntity task = this.program.findTaskByIndex(index  + 1);
            if(EmptyUtils.isEmpty(task)) {
                task = new ProgramTaskEntity();
                task.setIndex(index + 1);
                task.setTaskType(ProgramTaskType.PROGRAM_TASK_OVER);
            }
            buf.writeBytes(task.packageMessage());
        }

        byte[] response = new byte[buf.readableBytes()];
        buf.readBytes(response, 0, response.length);
        buf.release();
        return response;
    }
}
