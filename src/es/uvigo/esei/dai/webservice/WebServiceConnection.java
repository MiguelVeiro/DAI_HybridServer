package es.uvigo.esei.dai.webservice;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import es.uvigo.dai.kata7.CalculusService;
import es.uvigo.esei.dai.hybridserver.Configuration;

public class WebServiceConnection {

	Configuration configuration;
	public WebServiceConnection(Configuration configuration) {
		this.configuration= configuration;
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		
		
        URL url= new URL(configuration.);
		
		QName name= new QName("http://kata7.dai.uvigo.es/",
				"CalculusServiceImplService");
		
		Service service = Service.create(url,name); 
		
		CalculusService cs=  service.getPort(CalculusService.class);
	}

}
