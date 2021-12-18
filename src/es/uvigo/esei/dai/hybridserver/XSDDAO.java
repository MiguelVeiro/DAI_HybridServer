package es.uvigo.esei.dai.hybridserver;

import java.util.Set;

public interface XSDDAO {

	public void add(String uuid, String xsd);
	public boolean contains(String uuid);
	public void delete(String uuid);
	public Set<String> list();
 	public String getContent(String uuid);

}
