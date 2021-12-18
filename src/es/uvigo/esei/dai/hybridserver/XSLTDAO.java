package es.uvigo.esei.dai.hybridserver;

import java.util.Set;

public interface XSLTDAO {

	public void add(String uuid, String xslt, String xsd);
	public boolean contains(String uuid);
	public void delete(String uuid);
	public Set<String> list();
 	public String getContent(String uuid);

}
