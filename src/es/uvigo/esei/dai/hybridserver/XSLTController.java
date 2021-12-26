package es.uvigo.esei.dai.hybridserver;

import java.util.Set;

public class XSLTController {
	
	private XSLTDAO dao;
	
	public XSLTController(XSLTDAO dao) {
		this.dao = dao;
	}
	
	public boolean contains(String uuid) {
		return dao.contains(uuid);
	}

	public String getContent(String uuid) {
		return dao.getContent(uuid);
	}
	
	public String getXsd(String uuid) {
		return dao.getXsd(uuid);
	}

	public void add(String uuid, String xslt, String xsd) {
		dao.add(uuid, xslt, xsd);
	}

	public void delete(String uuid) {
		dao.delete(uuid);
	}
	
	public Set<String> list(){
		return dao.list();
	}

}
