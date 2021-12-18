package es.uvigo.esei.dai.hybridserver;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HTMLMapDAO implements HTMLDAO {
	
	private Map<String, String> contentMap;
	
	public HTMLMapDAO() {
		contentMap = new LinkedHashMap<String, String>();
		for (int i = 0; i < 10; i++) {
			UUID uuid = UUID.randomUUID();
			contentMap.put(uuid.toString(), "<html><body>Pagina " + i + "</body></html>");			
		}
	}
	
	public HTMLMapDAO(Map<String, String> contentMap) {
		this.contentMap = contentMap;
	}
	
	public boolean contains(String uuid) {
		if(contentMap.containsKey(uuid))
			return true;
		else
			return false;
	}
	
	public String getContent(String uuid) {
		return contentMap.get(uuid);
	}
	
	public String toString() {
		return contentMap.toString();
	}

	public void add(String uuid, String html) {
		contentMap.put(uuid, html);
	}
	
	public void delete(String uuid) {
		contentMap.remove(uuid);		
	}

	@Override
	public Set<String> list() {
		return contentMap.keySet();
	}

}
