package de.tub.ise.ec;

import de.tub.ise.hermes.IRequestHandler;
import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.Response;

public class SampleMessageHandler implements IRequestHandler {

    /*
     * (non-Javadoc)
     *
     * @see
     * IRequestHandler#handleRequest(edu.kit.aifb.dbe
     * .hermes.Request)
     */
    @Override
    public Response handleRequest(Request req) {
        return new Response("Echo okay for target: "
                + req.getTarget(), true, req,req.getItems());

    }

    /*
     * (non-Javadoc)
     *
     * @see IRequestHandler#requiresResponse()
     */
    @Override
    public boolean requiresResponse() {
        return true;
    }


}