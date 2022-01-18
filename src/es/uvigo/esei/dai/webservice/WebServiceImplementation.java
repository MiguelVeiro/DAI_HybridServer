package es.uvigo.esei.dai.webservice;

import java.util.Set;

import javax.jws.WebService;

import es.uvigo.esei.dai.hybridserver.HTMLDBDAO;
import es.uvigo.esei.dai.hybridserver.XMLDBDAO;
import es.uvigo.esei.dai.hybridserver.XSDDBDAO;
import es.uvigo.esei.dai.hybridserver.XSLTDBDAO;

@WebService(
		endpointInterface = "es.uvigo.esei.dai.webservice.WebServiceInterface",
		serviceName = "HybridServerService",
		targetNamespace = "http://hybridserver.dai.esei.uvigo.es/"
)
public class WebServiceImplementation implements WebServiceInterface {

	String DB_URL, DB_PASSWORD, DB_USER;
	
	public WebServiceImplementation (String DB_URL, String DB_PASSWORD, String DB_USER) {
		this.DB_URL = DB_URL;
		this.DB_PASSWORD = DB_PASSWORD;
		this.DB_USER = DB_USER;
	}

	@Override
	public Set<String> getHtmlUuids() {
		HTMLDBDAO dbDao = new HTMLDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.list();
	}

	@Override
	public Set<String> getXmlUuids() {
		XMLDBDAO dbDao = new XMLDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.list();
	}

	@Override
	public Set<String> getXsdUuids() {
		XSDDBDAO dbDao = new XSDDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.list();
	}

	@Override
	public Set<String> getXsltUuids() {
		XSLTDBDAO dbDao = new XSLTDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.list();
	}

	@Override
	public String getHtmlContent(String htmlUuid) {
		HTMLDBDAO dbDao = new HTMLDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.getContent(htmlUuid);
	}

	@Override
	public String getXmlContent(String xmlUuid) {
		XMLDBDAO dbDao = new XMLDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.getContent(xmlUuid);
	}

	@Override
	public String getXsdContent(String xsdUuid) {
		XSDDBDAO dbDao = new XSDDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.getContent(xsdUuid);
	}

	@Override
	public String getXsltContent(String xsltUuid) {
		XSLTDBDAO dbDao = new XSLTDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.getContent(xsltUuid);
	}

	@Override
	public String getAssociatedXsdUuid(String xsltUuid) {
		XSLTDBDAO dbDao = new XSLTDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.getXsd(xsltUuid);
	}

}
