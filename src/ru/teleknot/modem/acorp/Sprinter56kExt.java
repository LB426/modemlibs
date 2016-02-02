package ru.teleknot.modem.acorp;

import com.fazecast.jSerialComm.*;

import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lb426 on 31.12.2015.
 */
public class Sprinter56kExt {

    public Sprinter56kExt(String aPortName, String channame, String url) throws IOException
    {
        portName = aPortName;
        chanName = channame;
        serverURL = url;
        comPort = SerialPort.getCommPort(portName);
        comPort.setComPortParameters(115200,8,SerialPort.ONE_STOP_BIT,SerialPort.NO_PARITY);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
        if(comPort.openPort()) {
            System.out.println("CHECKPOINT: open RS232 port " + portName);

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
                if (numRead != 0) {
                    String str = getStrFromBuf(readBuffer, numRead);
                    //System.out.println("str='"+str+"'");
                    Matcher m = p.matcher(str);
                    if (m.matches()) {
                        comportinitstep = comportinitstep + 1;
                    }
                }
            }
            System.out.println("CHECKPOINT: init RS232 port " + portName);
        } else {
            System.out.println("ERROR: open COM port " + portName);
            throw new IOException("class: Sprinter56kExt, method: constructor, port no open: " + portName);
        }
    }

    public String getStrFromBuf(byte[] buffer, int length)
    {
        if(length > 0) {
            byte[] buf = new byte[length];
            for (int i = 0; i < length; i++) {
                buf[i] = buffer[i];
            }
            String bytesAsString = new String(buf, StandardCharsets.US_ASCII);
            return bytesAsString;
        } else {
            return "";
        }
    }

    public void Close() throws IOException
    {
        if(comPort.isOpen()) {
            comPort.closePort();
        } else {
            System.out.println("ERROR: port alredy close " + comPort.getDescriptivePortName());
            throw new IOException("class: Sprinter56kExt, method: Close, port no open: " + portName);
        }
    }

    public void StartWaitNMBR()
    {
        System.out.println("CHECKPOINT: wait incoming call on RS232 port " + portName);
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
                    String callerid =  m.group(6).toString();
                    try {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println(dateFormat.format(date) +
                                           " INCOMING CALL FROM " + chanName + " : \t" + callerid);
                        System.out.println();
                        SendCallerIdToServer(callerid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //System.out.println("READ ZERO byte");
            }
        }
    }

    public void SendCallerIdToServer(String callerid) throws Exception
    {
        if(serverURL.equals("undefined"))
            return;
        URL url = new URL(serverURL);
        URLConnection conn = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)conn;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        Map<String,String> arguments = new HashMap<>();
        arguments.put("phonenum", callerid);
        arguments.put("channame", chanName); // This is a fake password obviously
        StringJoiner sj = new StringJoiner("&");
        for(Map.Entry<String,String> entry : arguments.entrySet()) {
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                    + URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        String acc = "" ;
        try {
            http.connect();
            OutputStream os = http.getOutputStream();
            os.write(out);
            os.flush();
            // Обработка ответа
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                acc += line;
            }
            os.close();
            reader.close();
            //System.out.println(acc);
            JSONObject obj = new JSONObject(acc);
            if(!obj.getString("error").equals("none")){
                System.out.println("SERVER RETURN ERROR: " + obj.getString("error"));
                //System.out.println("SERVER RETURN ERROR RESULT: " + obj.getString("result"));
                System.out.println("SERVER STRING: " + acc);
            }else{
                System.out.println("CHECKPOINT: send CallerID to server success " + portName);
                //System.out.println("SERVER RETURN ERROR: " + obj.getString("error"));
                //System.out.println("SERVER RETURN RESULT: " + obj.keys());
            }
        }catch (SocketException e) {
            //e.printStackTrace();
            System.out.println(" ATTENTION: send CallerID to server ERROR!!!");
        }
    }

    private SerialPort comPort;
    private String portName;
    private String chanName;
    private String serverURL;
    public static final byte[] AT     = "AT\r\n".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] ATE0   = "ATE=0\r\n".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] ATVCID = "AT+VCID=1\r\n".getBytes(StandardCharsets.US_ASCII);
}