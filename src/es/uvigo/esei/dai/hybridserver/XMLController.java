package es.uvigo.esei.dai.hybridserver;

import java.util.Set;

public class XMLController {
	
	private XMLDAO dao;
	
	public XMLController(XMLDAO dao) {
		this.dao = dao;
	}
	
	public boolean contains(String uuid) {
		return dao.contains(uuid);
	}

	public String getContent(String uuid) {
		return dao.getContent(uuid);
	}

	public void add(String uuid, String xml) {
		dao.add(uuid, xml);
	}

	public void delete(String uuid) {
		dao.delete(uuid);
	}
	
	public Set<String> list(){
		return dao.list();
	}

}
