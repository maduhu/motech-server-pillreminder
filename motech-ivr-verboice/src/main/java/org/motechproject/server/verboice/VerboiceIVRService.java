package org.motechproject.server.verboice;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.motechproject.ivr.service.CallRequest;
import org.motechproject.ivr.service.IVRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

import static java.lang.String.format;

/**
 * Verboice specific implementation of the IVR Service interface
 */
@Component
@Qualifier("VerboiceIVRService")
public class VerboiceIVRService implements IVRService {

    private static Logger log = LoggerFactory.getLogger(VerboiceIVRService.class);

    @Qualifier("verboiceProperties")
    private Properties verboiceProperties;
    private HttpClient commonsHttpClient;

    @Autowired
    public VerboiceIVRService(Properties verboiceProperties, HttpClient commonsHttpClient) {
        this.verboiceProperties = verboiceProperties;
        this.commonsHttpClient = commonsHttpClient;
    }

    @Override
    public void initiateCall(CallRequest callRequest) {
        try {
            commonsHttpClient.executeMethod(new PostMethod(outgoingCallUri(callRequest)));
        } catch (IOException e) {
            log.error("Exception when initiating call : ", e);
        }
    }

    private String outgoingCallUri(CallRequest callRequest) {
        return format(
                "http://%s:%s/api/call?channel=%s&address=%s",
                verboiceProperties.getProperty("host"),
                verboiceProperties.getProperty("port"),
                callRequest.getCallBackUrl(),
                callRequest.getPhone()
        );
    }
}