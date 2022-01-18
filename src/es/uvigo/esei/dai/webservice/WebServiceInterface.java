package es.uvigo.esei.dai.webservice;

import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface WebServiceInterface {

	@WebMethod
	public Set<String> getHtmlUuids();
	@WebMethod
	public Set<String> getXmlUuids();
	@WebMethod
	public Set<String> getXsdUuids();
	@WebMethod
	public Set<String> getXsltUuids();
	
	@WebMethod
	public String getHtmlContent(String htmlUuid);
	@WebMethod
	public String getXmlContent(String xmlUuid);
	@WebMethod
	public String getXsdContent(String xsdUuid);
	@WebMethod
	public String getXsltContent(String xsltUuid);

	@WebMethod
	public String getAssociatedXsdUuid(String xsltUuid);

}
