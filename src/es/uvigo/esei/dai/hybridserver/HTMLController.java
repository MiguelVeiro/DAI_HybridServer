package es.uvigo.esei.dai.hybridserver;

import java.util.Set;

public class HTMLController {
	
	private HTMLDAO dao;
	
	public HTMLController(HTMLDAO dao) {
		this.dao = dao;
	}
	
	public boolean contains(String uuid) {
		return dao.contains(uuid);
	}

	public String getContent(String uuid) {
		return dao.getContent(uuid);
	}

	public void add(String uuid, String html) {
		dao.add(uuid, html);
	}

	public void delete(String uuid) {
		dao.delete(uuid);
	}
	
	public Set<String> list(){
		return dao.list();
	}

}
