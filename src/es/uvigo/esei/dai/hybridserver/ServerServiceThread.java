package es.uvigo.esei.dai.hybridserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.sax.SAXParserImplementation;

public class ServerServiceThread implements Runnable {

	private Socket socket;
	
	private HTMLController htmlProvider;
	private XMLController xmlProvider;
	private XSDController xsdProvider;
	private XSLTController xsltProvider;

	public ServerServiceThread(
			Socket socket, 
			HTMLController htmlProvider,
			XMLController xmlProvider, 
			XSDController xsdProvider, 
			XSLTController xsltProvider) {
		this.socket = socket;
		this.htmlProvider = htmlProvider;
		this.xmlProvider = xmlProvider;
		this.xsdProvider = xsdProvider;
		this.xsltProvider = xsltProvider;
	}

	public void run() {
		// Atender al cliente
		try (Socket socket = this.socket) {
			// Responder al cliente
			int port = socket.getLocalPort();
			
			HTTPRequest request;
			HTTPResponse response = new HTTPResponse();

			StringBuilder stringBuilder = new StringBuilder();

			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			OutputStream output = socket.getOutputStream();

			try {
				request = new HTTPRequest(reader);
				System.out.println(request.toString());

				switch (request.getMethod()) {
				case GET:

					if (request.getResourceName().equals("html")) {

						if (request.getResourceChain().equals("/html")) {
							stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
									+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
									+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
									+ "<h1>Hybrid Server</h1>" + "<ol>");
							for (String set : htmlProvider.list()) {
								stringBuilder.append("<li><a href=\"http://localhost:" + port + "/html?uuid=" + set + "\">"
										+ set + "</a></li>");
							}
							stringBuilder.append("</ol>\n" + "</body>" + "</html>");

							response.setStatus(HTTPResponseStatus.S200);
							response.setContent(stringBuilder.toString());

						} else {
							String uuid = request.getResourceParameters().get("uuid");
							// Search for valid UUID
							if (htmlProvider.contains(uuid)) {
								response.setStatus(HTTPResponseStatus.S200);
								response.putParameter("Content-Type", "text/html");
								response.setContent(htmlProvider.getContent(uuid));
							} else {
								throw new HTTPParseException("Not Found");
							}

						}

					} else if (request.getResourceChain().equals("/")) {
						
						stringBuilder.append("<!DOCTYPE html>" 
								+ "<html lang=\"es\">" 
								+ "<head>"
								+ "  <meta charset=\"utf-8\"/>" 
								+ "  <title>Hybrid Server</title>"
								+ "</head>" 
								+ "<body>"
								+ "<h1>Hybrid Server</h1>"
								+ "Autores: Miguel Veiro Romero, Martín Pereira González."
								+ "</body>" 
								+ "</html>");

						response.setStatus(HTTPResponseStatus.S200);
						response.setContent(stringBuilder.toString());

					} else if (request.getResourceName().equals("xml")){
						String uuidXslt = request.getResourceParameters().get("xslt");
						String uuidXml = request.getResourceParameters().get("uuid");
						
						if(uuidXslt!= null && uuidXml!= null) {
							
							
							if(!xsltProvider.contains(uuidXslt)) {
							
							throw new HTTPParseException("Not Found");
							}else {
								String uuidXsd = xsltProvider.getXsd(uuidXslt);
								try {
								SAXParserImplementation.parseAndValidateXSD(xmlProvider.getContent(uuidXml),
							    		xsdProvider.getContent(uuidXsd));
								}catch(ParserConfigurationException | SAXException | IOException e){
									throw new HTTPParseException("Bad Request");
								}
								response.setStatus(HTTPResponseStatus.S200);
								response.putParameter("Content-Type", "application/xml");
								response.setContent(xmlProvider.getContent(uuidXml));
							}
							//coger excepcion si da fallo al validar
						    
							
							
							
						}else if (request.getResourceChain().equals("/xml")) {
							stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
									+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
									+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
									+ "<h1>Hybrid Server</h1>" + "<ol>");
							for (String set : xmlProvider.list()) {
								stringBuilder.append("<li><a href=\"http://localhost:" + port + "/html?uuid=" + set + "\">"
										+ set + "</a></li>");
							}
							stringBuilder.append("</ol>\n" + "</body>" + "</html>");

							response.setStatus(HTTPResponseStatus.S200);
							response.setContent(stringBuilder.toString());

						} else {
							String uuid = request.getResourceParameters().get("uuid");
							// Search for valid UUID
							if (xmlProvider.contains(uuid)) {
								response.setStatus(HTTPResponseStatus.S200);
								response.putParameter("Content-Type", "application/xml");
								response.setContent(xmlProvider.getContent(uuid));
							} else {
								throw new HTTPParseException("Not Found");
							}

						}					
						
						
						
					} else if (request.getResourceName().equals("xslt")) {

						if (request.getResourceChain().equals("/xslt")) {
							stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
									+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
									+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
									+ "<h1>Hybrid Server</h1>" + "<ol>");
							for (String set : xsltProvider.list()) {
								stringBuilder.append("<li><a href=\"http://localhost:" + port + "/xslt?uuid=" + set + "\">"
										+ set + "</a></li>");
							}
							stringBuilder.append("</ol>\n" + "</body>" + "</html>");

							response.setStatus(HTTPResponseStatus.S200);
							response.setContent(stringBuilder.toString());

						} else {
							String uuid = request.getResourceParameters().get("uuid");
							// Search for valid UUID
							if (xsltProvider.contains(uuid)) {
								response.setStatus(HTTPResponseStatus.S200);
								response.putParameter("Content-Type", "application/xml");
								response.setContent(xsltProvider.getContent(uuid));
							} else {
								throw new HTTPParseException("Not Found");
							}

						}					
						
					} else if (request.getResourceName().equals("xsd")) {

						if (request.getResourceChain().equals("/xsd")) {
							stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
									+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
									+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
									+ "<h1>Hybrid Server</h1>" + "<ol>");
							for (String set : xsdProvider.list()) {
								stringBuilder.append("<li><a href=\"http://localhost:" + port + "/xsdt?uuid=" + set + "\">"
										+ set + "</a></li>");
							}
							stringBuilder.append("</ol>\n" + "</body>" + "</html>");

							response.setStatus(HTTPResponseStatus.S200);
							response.setContent(stringBuilder.toString());

						} else {
							String uuid = request.getResourceParameters().get("uuid");
							// Search for valid UUID
							if (xsdProvider.contains(uuid)) {
								response.setStatus(HTTPResponseStatus.S200);
								response.putParameter("Content-Type", "application/xml");
								response.setContent(xsdProvider.getContent(uuid));
							} else {
								throw new HTTPParseException("Not Found");
							}

						}					
						
					} else {						
						throw new HTTPParseException("Bad Request");						
					}

					break;

				case POST:

					if (request.getResourceParameters().containsKey("html")) {
						
						UUID uuid = UUID.randomUUID();
						htmlProvider.add(uuid.toString(), request.getResourceParameters().get("html"));

						stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
								+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
								+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
								+ "<h1>Hybrid Server</h1>" + "<ol>");
						StringBuilder auxStringBuilder = new StringBuilder();
						for (String set : htmlProvider.list()) {
							if (set.equals(uuid.toString())) {
								stringBuilder.append("<li><a href=\"html?uuid=" + set + "\">" + set + "</a></li>");
							} else {
								auxStringBuilder.append("<li><a href=\"html?uuid=" + set + "\">" + set + "</a></li>");
							}
						}
						stringBuilder.append(auxStringBuilder);
						stringBuilder.append("</ol>\n" + "</body>" + "</html>");
						
					} else if (request.getResourceParameters().containsKey("xml")) {
						
						UUID uuid = UUID.randomUUID();
						xmlProvider.add(uuid.toString(), request.getResourceParameters().get("xml"));

						stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
								+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
								+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
								+ "<h1>Hybrid Server</h1>" + "<ol>");
						StringBuilder auxStringBuilder = new StringBuilder();
						for (String set : xmlProvider.list()) {
							if (set.equals(uuid.toString())) {
								stringBuilder.append("<li><a href=\"xml?uuid=" + set + "\">" + set + "</a></li>");
							} else {
								auxStringBuilder.append("<li><a href=\"xml?uuid=" + set + "\">" + set + "</a></li>");
							}
						}
						stringBuilder.append(auxStringBuilder);
						stringBuilder.append("</ol>\n" + "</body>" + "</html>");
						
					} else if (request.getResourceParameters().containsKey("xslt")) {
						
						UUID uuid = UUID.randomUUID();
						String xsdUuid = request.getResourceParameters().get("xsd");
						
						if (xsdUuid == null) {
							throw new HTTPParseException("Bad Request");
						}
							
						if (xsdProvider.contains(xsdUuid)) {
							
							xsltProvider.add(
									uuid.toString(),
									request.getResourceParameters().get("xslt"),
									request.getResourceParameters().get("xsd"));

							stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
									+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
									+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
									+ "<h1>Hybrid Server</h1>" + "<ol>");
							StringBuilder auxStringBuilder = new StringBuilder();
							for (String set : xsltProvider.list()) {
								if (set.equals(uuid.toString())) {
									stringBuilder.append("<li><a href=\"xslt?uuid=" + set + "\">" + set + "</a></li>");
								} else {
									auxStringBuilder.append("<li><a href=\"xslt?uuid=" + set + "\">" + set + "</a></li>");
								}
							}
							stringBuilder.append(auxStringBuilder);
							stringBuilder.append("</ol>\n" + "</body>" + "</html>");
							
						} else {
							throw new HTTPParseException("Not Found");
						}
						
					} else if (request.getResourceParameters().containsKey("xsd")) {
						
						UUID uuid = UUID.randomUUID();
						xsdProvider.add(uuid.toString(), request.getResourceParameters().get("xsd"));

						stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
								+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
								+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
								+ "<h1>Hybrid Server</h1>" + "<ol>");
						StringBuilder auxStringBuilder = new StringBuilder();
						for (String set : xsdProvider.list()) {
							if (set.equals(uuid.toString())) {
								stringBuilder.append("<li><a href=\"xsd?uuid=" + set + "\">" + set + "</a></li>");
							} else {
								auxStringBuilder.append("<li><a href=\"xsd?uuid=" + set + "\">" + set + "</a></li>");
							}
						}
						stringBuilder.append(auxStringBuilder);
						stringBuilder.append("</ol>\n" + "</body>" + "</html>");
						
					} else {
						throw new HTTPParseException("Bad Request");
					}

					response.setStatus(HTTPResponseStatus.S200);
					response.setContent(stringBuilder.toString());

					break;

				case DELETE:
					
					if (htmlProvider.contains(request.getResourceParameters().get("uuid"))) {

						htmlProvider.delete(request.getResourceParameters().get("uuid"));

						stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
								+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
								+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
								+ "<h1>Hybrid Server</h1>" + "<ol>");
						for (String set : htmlProvider.list()) {
							stringBuilder.append("<li><a href=\"html?uuid=" + set + "\">" + set + "</a></li>");
						}
						stringBuilder.append("</ol>\n" + "</body>" + "</html>");
						
					} else if (xmlProvider.contains(request.getResourceParameters().get("uuid"))) {

						xmlProvider.delete(request.getResourceParameters().get("uuid"));

						stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
								+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
								+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
								+ "<h1>Hybrid Server</h1>" + "<ol>");
						for (String set : xmlProvider.list()) {
							stringBuilder.append("<li><a href=\"xml?uuid=" + set + "\">" + set + "</a></li>");
						}
						stringBuilder.append("</ol>\n" + "</body>" + "</html>");

					} else if (xsltProvider.contains(request.getResourceParameters().get("uuid"))) {

						xsltProvider.delete(request.getResourceParameters().get("uuid"));

						stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
								+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
								+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
								+ "<h1>Hybrid Server</h1>" + "<ol>");
						for (String set : xsltProvider.list()) {
							stringBuilder.append("<li><a href=\"xslt?uuid=" + set + "\">" + set + "</a></li>");
						}
						stringBuilder.append("</ol>\n" + "</body>" + "</html>");

					} else if (xsdProvider.contains(request.getResourceParameters().get("uuid"))) {

						xsdProvider.delete(request.getResourceParameters().get("uuid"));

						stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
								+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
								+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
								+ "<h1>Hybrid Server</h1>" + "<ol>");
						for (String set : xsdProvider.list()) {
							stringBuilder.append("<li><a href=\"xsd?uuid=" + set + "\">" + set + "</a></li>");
						}
						stringBuilder.append("</ol>\n" + "</body>" + "</html>");

					} else {
						throw new HTTPParseException("Not Found");
					}

					response.setStatus(HTTPResponseStatus.S200);
					response.setContent(stringBuilder.toString());

					break;

				default:
					break;
				}

			} catch (HTTPParseException e) {
				if (e.getLocalizedMessage().contains("Not Found")) {
					response.setStatus(HTTPResponseStatus.S404);
					response.setContent("Not Found");
					
				} else if (e.getLocalizedMessage().contains("Bad Request")) {
					response.setStatus(HTTPResponseStatus.S400);
					response.setContent("Bad Request");
					
				}
			} catch (Exception e) {
				response.setStatus(HTTPResponseStatus.S500);
				response.setContent("Internal Server Error");
				e.printStackTrace();
			}

			System.out.println("------------");
			System.out.println(response.toString());
			System.out.println("------------");

			output.write(response.toString().getBytes());
			// output.write(stringBuilder.toString().getBytes());
			
//			reader.close();
//			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
