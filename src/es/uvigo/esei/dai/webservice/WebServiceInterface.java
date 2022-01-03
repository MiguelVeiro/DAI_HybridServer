package es.uvigo.esei.dai.webservice;

import java.util.Set;

public interface WebServiceInterface {

	public Set<String> getHtmlUuids();
	public Set<String> getXmlUuids();
	public Set<String> getXsdUuids();
	public Set<String> getXsltUuids();

	public String getHtmlContent(String htmlUuid);
	public String getXmlContent(String xmlUuid);
	public String getXsdContent(String xsdUuid);
	public String getXsltContent(String xsltUuid);
	
	public String getAssociatedXsdUuid(String xsltUuid);

}
