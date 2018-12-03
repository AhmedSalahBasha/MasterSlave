package de.tub.ise.ec;

import de.tub.ise.hermes.AsyncCallbackRecipient;
import de.tub.ise.hermes.Response;

public class AsyncClass implements AsyncCallbackRecipient {

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
        System.out.println("===== This is an Asynchronous Request =====");
        System.out.println("Hello from response callback...");
        System.out.println("Message: " + resp.getResponseMessage());
        setResponse(resp);
        setEchoSuccessful(resp.responseCode());
    }
}