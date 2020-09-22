package cn.haier.bio.medical.pce;

public interface IPCEListener {
    void onPCEConnected();
    void onPCESwitchWriteModel();
    void onPCESwitchReadModel();
    void onPCEPrint(String message);
    void onPCEException(Throwable throwable);
    byte[] onPCEStatusPackageReceived(byte[] data);
    byte[] onPCEProgramPackageReceived(byte[] data);
    byte[] onPCETestingPackageReceived(byte[] data);
    byte[] onPCEParameterPackageReceived(byte[] data);
}
