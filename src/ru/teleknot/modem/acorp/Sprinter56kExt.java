package ru.teleknot.modem.acorp;

import com.fazecast.jSerialComm.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lb426 on 31.12.2015.
 */
public class Sprinter56kExt {

    public Sprinter56kExt(String portName){
        comPort = SerialPort.getCommPort(portName);
        comPort.setComPortParameters(115200,8,SerialPort.ONE_STOP_BIT,SerialPort.NO_PARITY);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
        if(comPort.openPort()) {
            System.out.println("SUCCESS: open COM port " + comPort.getDescriptivePortName());
        } else {
            System.out.println("ERROR: open COM port " + comPort.getDescriptivePortName());
        }
    }

    public String getStrFromBuf(byte[] buffer, int length){
        byte[] buf = new byte[length];
        for (int i=0; i<length; i++){
            buf[i] = buffer[i];
        }
        String bytesAsString = new String(buf, StandardCharsets.US_ASCII);
        return bytesAsString;
    }

    public void Init(){
        try {
            int comportinitstep = 1;
            //Pattern p = Pattern.compile(".*OK.*", Pattern.DOTALL);
            Pattern p = Pattern.compile("\r\nOK\r\n");
            while (comportinitstep != 4){
                if(comportinitstep == 1)
                    comPort.writeBytes(ATE0,ATE0.length);
                if(comportinitstep == 2)
                    comPort.writeBytes(AT,AT.length);
                if(comportinitstep == 3)
                    comPort.writeBytes(ATVCID,ATVCID.length);
                byte[] readBuffer = new byte[1024];
                int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                if(numRead != 0) {
                    String str = getStrFromBuf(readBuffer,numRead);
                    //System.out.println("str='"+str+"'");
                    Matcher m = p.matcher(str);
                    if (m.matches()) {
                        comportinitstep = comportinitstep + 1;
                    }
                }
            }
            System.out.println("SUCCESS: init");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Close(){
        if(comPort.isOpen()) {
            comPort.closePort();
        } else {
            System.out.println("ERROR: port alredy close " + comPort.getDescriptivePortName());
        }
    }

    public void StartWaitNMBR(){
        try {
            Pattern p = Pattern.compile(
                    "^(\r\nDATE\\s=\\s)([0-9]{0,})(\r\nTIME\\s=\\s)([0-9]{0,})(\r\nNMBR\\s=\\s)([0-9]{0,})(\r\n)$"
            );
            while (true){
                byte[] readBuffer = new byte[1024];
                int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                if(numRead != 0) {
                    String str = getStrFromBuf(readBuffer,numRead);
                    //System.out.println("str='"+str+"'");
                    Matcher m = p.matcher(str);
                    if (m.matches()) {
                        System.out.println("INCOMING CALL FROM PHONE: " + m.group(6));
                    }
                } else {
                    //System.out.println("READ ZERO byte");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("test ACORP Sprinter@56k Ext");
    }

    private SerialPort comPort;
    public static final byte[] AT     = "AT\r\n".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] ATE0   = "ATE=0\r\n".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] ATVCID = "AT+VCID=1\r\n".getBytes(StandardCharsets.US_ASCII);
}
