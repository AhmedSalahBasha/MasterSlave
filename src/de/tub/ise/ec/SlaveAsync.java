package de.tub.ise.ec;

import de.tub.ise.hermes.AsyncCallbackRecipient;
import de.tub.ise.hermes.Response;

public class SlaveAsync implements AsyncCallbackRecipient {

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

    /**
     * this is the callback function which has the response object coming from Slave when the requestType is Async
     * @param resp is the response object which will be passed to the prepareDates function to prepare the timestamps CSV file
     */
    @Override
    public void callback(Response resp) {
        setResponse(resp);
        setEchoSuccessful(resp.responseCode());
        MasterHandler.prepareDates(resp);
    }
}
