package net.whydah.sso.commands;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import net.whydah.sso.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;

import static com.sun.jersey.api.client.ClientResponse.Status.CONFLICT;
import static com.sun.jersey.api.client.ClientResponse.Status.FORBIDDEN;
import static com.sun.jersey.api.client.ClientResponse.Status.OK;

/**
 * Created by totto on 12/2/14.
 */
public class CommandValidateUsertokenid extends HystrixCommand<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(CommandLogonApplication.class);

    private URI tokenServiceUri ;
    private String myAppTokenId ;
    private String usertokenid;



    public CommandValidateUsertokenid(URI tokenServiceUri,String myAppTokenId,String usertokenid) {
        super(HystrixCommandGroupKey.Factory.asKey("SSOAUserAuthGroup"));
        this.tokenServiceUri = tokenServiceUri;
        this.myAppTokenId=myAppTokenId;
        this.usertokenid=usertokenid;
    }

    @Override
    protected Boolean run() {

        // logonApplication();

        Client tokenServiceClient = Client.create();

// If we get strange values...  return false
        if (usertokenid == null || usertokenid.length() < 4) {
            logger.trace("CommandValidateUsertokenid - verifyUserTokenId - Called with bogus usertokenid={}. return false",usertokenid);
            return false;
        }
        // logonApplication();
        WebResource verifyResource = tokenServiceClient.resource(tokenServiceUri).path("user/" + myAppTokenId + "/validate_usertokenid/" + usertokenid);
        ClientResponse response = verifyResource.get(ClientResponse.class);
        if (response.getStatus() == OK.getStatusCode()) {
            logger.debug("CommandValidateUsertokenid - verifyUserTokenId - usertokenid validated OK");
            return true;
        }
        if(response.getStatus() == CONFLICT.getStatusCode()) {
            logger.debug("CommandValidateUsertokenid - verifyUserTokenId - usertokenid not ok: {}", response);
            return false;
        }
        //retry
        logger.info("CommandValidateUsertokenid - verifyUserTokenId - retrying usertokenid ");
        //logonApplication();
        response = verifyResource.get(ClientResponse.class);
        boolean bolRes = response.getStatus() == OK.getStatusCode();
        logger.debug("CommandValidateUsertokenid - verifyUserTokenId - validate_usertokenid {}  result {}", "user/" + myAppTokenId + "/validate_usertokenid/" + usertokenid, response);
        return bolRes;
    }

    @Override
    protected Boolean getFallback() {
        return false;
    }


}