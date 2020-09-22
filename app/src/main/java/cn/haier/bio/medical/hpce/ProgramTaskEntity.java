package cn.haier.bio.medical.hpce;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ProgramTaskEntity {
    private int id;                 //任务id
    private int pid;                //任务所属程序id
    private int index;              //当前步骤编号

    private int holdTime;           //维持时间,单位S
    private int frequency;          //升降温速率，单位摄氏度每分钟，精度1位小数，值为设定摄氏度值 * 100

    private int sampleTemperature;  //腔体温度，单位摄氏度精度1位小数，值为设定摄氏度值 * 100
    private int cavityTemperature;  //样本温度，单位摄氏度精度1位小数，值为设定摄氏度值 * 100
    private int finishTemperature;  //结束温度，单位摄氏度精度1位小数，值为设定摄氏度值 * 100

    private int loopCount;          //循环次数
    private int targetIndex;        //需要跳转或循环的步骤编号

    private int taskType;  //任务类型
    private int calibrationType;  //标定类型：样本/腔体

    public ProgramTaskEntity() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getHoldTime() {
        return holdTime;
    }

    public void setHoldTime(int holdTime) {
        this.holdTime = holdTime;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getSampleTemperature() {
        return sampleTemperature;
    }

    public void setSampleTemperature(int sampleTemperature) {
        this.sampleTemperature = sampleTemperature;
    }

    public int getCavityTemperature() {
        return cavityTemperature;
    }

    public void setCavityTemperature(int cavityTemperature) {
        this.cavityTemperature = cavityTemperature;
    }

    public int getFinishTemperature() {
        return finishTemperature;
    }

    public void setFinishTemperature(int finishTemperature) {
        this.finishTemperature = finishTemperature;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public void setLoopCount(int loopCount) {
        this.loopCount = loopCount;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public int getCalibrationType() {
        return calibrationType;
    }

    public void setCalibrationType(int calibrationType) {
        this.calibrationType = calibrationType;
    }


    public byte[] packageMessage() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(this.index);
        buf.writeByte(this.taskType);
        buf.writeByte(0x00);
        switch (this.taskType) {
            case ProgramTaskType.PROGRAM_TASK_WAIT:{
                buf.writeByte(this.index);
                buf.writeByte(this.calibrationType);
                buf.writeShort(this.cavityTemperature);
                buf.writeShort(this.holdTime);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                break;
            }
            case ProgramTaskType.PROGRAM_TASK_WAIT_FOR:{
                buf.writeByte(this.index);
                buf.writeByte(0x00);
                buf.writeShort(this.cavityTemperature);
                buf.writeShort(this.sampleTemperature);
                buf.writeShort(this.holdTime);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                break;
            }
            case ProgramTaskType.PROGRAM_TASK_RAMP:{
                buf.writeByte(this.index);
                buf.writeByte(this.calibrationType);
                buf.writeShort(this.frequency);
                buf.writeShort(this.finishTemperature);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                break;
            }
            case ProgramTaskType.PROGRAM_TASK_HOLD:{
                buf.writeByte(this.index);
                buf.writeByte(this.calibrationType);
                buf.writeShort(this.finishTemperature);
                buf.writeShort(this.holdTime);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                break;
            }
            case ProgramTaskType.PROGRAM_TASK_JUMP:{
                buf.writeByte(this.index);
                buf.writeByte(0x00);
                buf.writeByte(this.targetIndex);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                break;
            }
            case ProgramTaskType.PROGRAM_TASK_LOOP:{
                buf.writeByte(this.index);
                buf.writeByte(0x00);
                buf.writeByte(this.targetIndex);
                buf.writeByte(this.loopCount);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                break;
            }
            case ProgramTaskType.PROGRAM_TASK_OVER:{
                buf.writeByte(this.index);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                buf.writeByte(0x00);
                break;
            }
        }
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data, 0, data.length);
        buf.release();
        return data;
    }
}
