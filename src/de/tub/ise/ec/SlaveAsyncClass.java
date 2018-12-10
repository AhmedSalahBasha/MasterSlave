package de.tub.ise.ec;

import de.tub.ise.hermes.AsyncCallbackRecipient;
import de.tub.ise.hermes.Response;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SlaveAsyncClass implements AsyncCallbackRecipient {

    public boolean isEchoSuccessful() {
        return echoSuccessful;
    }

    public void setEchoSuccessful(boolean echoSuccessful) {
        this.echoSuccessful = echoSuccessful;
    }

    private boolean echoSuccessful;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    private Response response;

    @Override
    public void callback(Response resp) {
        setResponse(resp);
        setEchoSuccessful(resp.responseCode());
       // System.out.println("===== This is an Asynchronous Request From Server =====");
       // System.out.println("Message: " + resp.getResponseMessage());
        //Using Date class
      //  Date date = new Date();
        //Pattern for showing milliseconds in the time "SSS"
        //DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
       // String timestamp = sdf.format(date);
        //System.out.println("END MASTER(recieve Response from Slave):" + timestamp);
        ServerHandler.preparedates(resp);
    }
}
