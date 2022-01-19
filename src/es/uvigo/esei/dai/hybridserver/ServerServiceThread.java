package es.uvigo.esei.dai.hybridserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.ws.WebServiceException;

import org.xml.sax.SAXException;

import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.sax.SAXParserImplementation;
import es.uvigo.esei.dai.sax.SAXTransformation;
import es.uvigo.esei.dai.webservice.WebServiceConnection;
import es.uvigo.esei.dai.webservice.ControllerService;
import es.uvigo.esei.dai.webservice.WebServiceInterface;

public class ServerServiceThread implements Runnable {

	private Socket socket;

	private HTMLController htmlProvider;
	private XMLController xmlProvider;
	private XSDController xsdProvider;
	private XSLTController xsltProvider;
	private List<ServerConfiguration> listServers;
	List<WebServiceInterface> availableConnections;
	List<ServerConfiguration> infoServers;

//	public ServerServiceThread(Socket socket, HTMLController htmlProvider, XMLController xmlProvider,
//			XSDController xsdProvider, XSLTController xsltProvider) {
//		this.socket = socket;
//		this.htmlProvider = htmlProvider;
//		this.xmlProvider = xmlProvider;
//		this.xsdProvider = xsdProvider;
//		this.xsltProvider = xsltProvider;
//		
//	}
	public ServerServiceThread(Socket socket, HTMLController htmlProvider, XMLController xmlProvider,
			XSDController xsdProvider, XSLTController xsltProvider, List<ServerConfiguration> listServers) {
		this.socket = socket;
		this.htmlProvider = htmlProvider;
		this.xmlProvider = xmlProvider;
		this.xsdProvider = xsdProvider;
		this.xsltProvider = xsltProvider;
		this.listServers = listServers;
	}

	public void connectToAvailableServers() {

		availableConnections = new ArrayList<WebServiceInterface>();
		infoServers = new ArrayList<ServerConfiguration>();

		String currentServer = "null";
		Iterator i = listServers.iterator();
		while (i.hasNext()) {
			try {
				ServerConfiguration serverConfiguration = (ServerConfiguration) i.next();
				currentServer = serverConfiguration.getName();

				WebServiceConnection wsc = new WebServiceConnection(serverConfiguration.getName(),
						serverConfiguration.getWsdl(), serverConfiguration.getNamespace(),
						serverConfiguration.getService(), serverConfiguration.getHttpAddress());

				WebServiceInterface connection = wsc.setConnection();
				availableConnections.add(connection);
				infoServers.add(serverConfiguration);
			} catch (WebServiceException e) {
				System.out.println("No se pudo conectar al servidor " + currentServer);

			}

		}

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

			if (listServers != null)
				connectToAvailableServers();

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
							// archivos html locales
							for (String set : htmlProvider.list()) {
								stringBuilder.append("<li><a href=\"http://localhost:" + port + "/html?uuid=" + set
										+ "\">" + set + "</a></li>");
							}
							// archivos html remotos
							stringBuilder.append("</ol>\n");
							if (listServers != null) {
								Iterator iConnections = availableConnections.iterator();
								Iterator iInfoServers = infoServers.iterator();
								while (iConnections.hasNext()) {
									ServerConfiguration info = (ServerConfiguration) iInfoServers.next();
									stringBuilder.append("<h1>" + info.getName() + "</h1><ol>");
									WebServiceInterface connection = (WebServiceInterface) iConnections.next();
									Set<String> uuidsHtml = connection.getHtmlUuids();
									for (String uuidHtml : uuidsHtml) {
										stringBuilder.append("<li><a href=\"" + info.getHttpAddress() + "html?uuid="
												+ uuidHtml + "\">" + uuidHtml + "</a></li>");
									}
									stringBuilder.append("</ol>");
								}
							}
							stringBuilder.append("</body>" + "</html>");

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
								String content = null;
								if (listServers != null) {
									Iterator i = listServers.iterator();
									while (i.hasNext() && content == null) {
										try {
											ServerConfiguration serverConfiguration = (ServerConfiguration) i.next();
											WebServiceConnection wsc = new WebServiceConnection(
													serverConfiguration.getName(), serverConfiguration.getWsdl(),
													serverConfiguration.getNamespace(),
													serverConfiguration.getService(),
													serverConfiguration.getHttpAddress());

											WebServiceInterface ws = wsc.setConnection();
											Set<String> uuidsHtml = ws.getHtmlUuids();
											System.err.println(serverConfiguration.getName());
											System.err.println(uuid);
											if (uuidsHtml.contains(uuid)) {
												content = ws.getHtmlContent(uuid);
												response.setStatus(HTTPResponseStatus.S200);
												response.putParameter("Content-Type", "text/html");
												response.setContent(content);
											}
										} catch (WebServiceException e) {
											System.out.println("No se pudo conectar al servidor");
										}

									}
								}

								if (content == null)
									throw new HTTPParseException("Not Found");

							}

						}

					} else if (request.getResourceChain().equals("/")) {

						stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
								+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>" + "</head>"
								+ "<body>" + "<h1>Hybrid Server</h1>"
								+ "Autores: Miguel Veiro Romero, Martín Pereira González." + "</body>" + "</html>");

						response.setStatus(HTTPResponseStatus.S200);
						response.setContent(stringBuilder.toString());

					} else if (request.getResourceName().equals("xml")) {
						String uuidXslt = request.getResourceParameters().get("xslt");
						String uuidXml = request.getResourceParameters().get("uuid");
						// uuidXslt->uuidXsd->validar con content XSD y content Xml

						String foundXsdUuid = null;
						String foundXsltContent = null;

						// Si viene el parametro xslt, buscamos uuidXslt para conseguir uuidXsd
						String foundXsdContent = null;
						if (uuidXslt != null) {
							// Busqueda local de uuid de xslt
							if (xsltProvider.contains(uuidXslt)) {
								foundXsdUuid = xsltProvider.getXsd(uuidXslt);
								foundXsltContent = xsltProvider.getContent(uuidXslt);
							} else {
								// Busqueda remota de uuid de xslt
								Iterator i = availableConnections.iterator();
								WebServiceInterface connection = null;
								while (foundXsdUuid == null && i.hasNext()) {
									connection = (WebServiceInterface) i.next();
									Set<String> currentServerXsltUuids = connection.getXsltUuids();
									if (currentServerXsltUuids.contains(uuidXslt)) {
										foundXsdUuid = connection.getAssociatedXsdUuid(uuidXslt);
										foundXsltContent = connection.getXsltContent(uuidXslt);
									}
								}
							}

							// Si se encontró un uuidXslt en algún lado

							if (foundXsdUuid != null) {

								// Busqueda local de uuid de xsd
								if (xsdProvider.contains(foundXsdUuid)) {
									foundXsdContent = xsdProvider.getContent(foundXsdUuid);

								} else {
									// Busqueda remota de uuid de xsd
									Iterator i = availableConnections.iterator();
									WebServiceInterface connection = null;
									while (foundXsdContent == null && i.hasNext()) {
										connection = (WebServiceInterface) i.next();
										Set<String> currentServerXsdUuids = connection.getXsdUuids();
										if (currentServerXsdUuids.contains(foundXsdUuid)) {
											foundXsdContent = connection.getXsdContent(foundXsdUuid);
										}
									}
								}

								if (foundXsdContent == null) {
									throw new HTTPParseException("Not Found");
								}

							} else {
								// Si no se encontró un uuidXslt en algún lado
								throw new HTTPParseException("Not Found");

							}

						}
						String foundXmlContent = null;
						if (uuidXml != null) {

							// Busqueda local de uuid de xsd
							if (xmlProvider.contains(uuidXml)) {
								foundXmlContent = xmlProvider.getContent(uuidXml);

							} else {
								// Busqueda remota de uuid de xsd
								if (listServers != null) {
									Iterator i = availableConnections.iterator();
									WebServiceInterface connection = null;
									while (foundXmlContent == null && i.hasNext()) {
										connection = (WebServiceInterface) i.next();
										Set<String> currentServerXmlUuids = connection.getXmlUuids();
										if (currentServerXmlUuids.contains(uuidXml)) {
											foundXmlContent = connection.getXmlContent(uuidXml);
										}
									}
								}

							}

							if (foundXmlContent == null) {
								throw new HTTPParseException("Not Found");
							}
						}

						if (foundXsdContent != null && foundXmlContent != null) {

							try {
								SAXParserImplementation.parseAndValidateXSD(foundXmlContent, foundXsdContent);
							} catch (ParserConfigurationException | SAXException | IOException e) {
								throw new HTTPParseException("Bad Request");
							}
							response.setStatus(HTTPResponseStatus.S200);
							response.putParameter("Content-Type", "text/html");
							String transform = SAXTransformation.transformWithXSLT(foundXmlContent, foundXsltContent);
							response.setContent(transform);

						} else if (request.getResourceChain().equals("/xml")) {
							stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
									+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
									+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
									+ "<h1>Hybrid Server</h1>" + "<ol>");

							// archivos locales
							for (String set : xmlProvider.list()) {
								stringBuilder.append("<li><a href=\"http://localhost:" + port + "/html?uuid=" + set
										+ "\">" + set + "</a></li>");
							}

							// archivos remotos

							stringBuilder.append("</ol>\n");
							if (listServers != null) {
								Iterator iConnections = availableConnections.iterator();
								Iterator iInfoServers = infoServers.iterator();
								while (iConnections.hasNext()) {
									ServerConfiguration info = (ServerConfiguration) iInfoServers.next();
									stringBuilder.append("<h1>" + info.getName() + "</h1><ol>");
									WebServiceInterface connection = (WebServiceInterface) iConnections.next();
									Set<String> uuidsXml = connection.getXmlUuids();
									for (String currentuuidXml : uuidsXml) {
										stringBuilder.append("<li><a href=\"" + info.getHttpAddress() + "xml?uuid="
												+ currentuuidXml + "\">" + currentuuidXml + "</a></li>");
									}
									stringBuilder.append("</ol>");
								}
							}
							stringBuilder.append("</body>" + "</html>");

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
								String content = null;
								Iterator i = listServers.iterator();
								while (i.hasNext() && content == null) {
									try {
										ServerConfiguration serverConfiguration = (ServerConfiguration) i.next();
										WebServiceConnection wsc = new WebServiceConnection(
												serverConfiguration.getName(), serverConfiguration.getWsdl(),
												serverConfiguration.getNamespace(), serverConfiguration.getService(),
												serverConfiguration.getHttpAddress());

										WebServiceInterface ws = wsc.setConnection();
										Set<String> uuidsXml = ws.getXmlUuids();
										System.err.println(serverConfiguration.getName());
										System.err.println(uuid);
										if (uuidsXml.contains(uuid)) {
											content = ws.getXmlContent(uuid);
											response.setStatus(HTTPResponseStatus.S200);
											response.putParameter("Content-Type", "application/xml");
											response.setContent(content);
										}
									} catch (WebServiceException e) {
										System.out.println("No se pudo conectar al servidor");
									}

								}

								if (content == null)
									throw new HTTPParseException("Not Found");

							}

						}

					} else if (request.getResourceName().equals("xslt")) {

						if (request.getResourceChain().equals("/xslt")) {
							stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
									+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
									+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
									+ "<h1>Hybrid Server</h1>" + "<ol>");
							// archivos locales
							for (String set : xsltProvider.list()) {
								stringBuilder.append("<li><a href=\"http://localhost:" + port + "/xslt?uuid=" + set
										+ "\">" + set + "</a></li>");
							}
							// archivos remotos
							stringBuilder.append("</ol>\n");
							if (listServers != null) {
								Iterator iConnections = availableConnections.iterator();
								Iterator iInfoServers = infoServers.iterator();
								while (iConnections.hasNext()) {
									ServerConfiguration info = (ServerConfiguration) iInfoServers.next();
									stringBuilder.append("<h1>" + info.getName() + "</h1><ol>");
									WebServiceInterface connection = (WebServiceInterface) iConnections.next();
									Set<String> uuidsXslt = connection.getXsltUuids();
									for (String uuidXslt : uuidsXslt) {
										stringBuilder.append("<li><a href=\"" + info.getHttpAddress() + "xml?uuid="
												+ uuidXslt + "\">" + uuidXslt + "</a></li>");
									}
									stringBuilder.append("</ol>");
								}
							}
							stringBuilder.append("</body>" + "</html>");

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

								String content = null;
								if (listServers != null) {
									Iterator i = listServers.iterator();
									while (i.hasNext() && content == null) {
										try {
											ServerConfiguration serverConfiguration = (ServerConfiguration) i.next();
											WebServiceConnection wsc = new WebServiceConnection(
													serverConfiguration.getName(), serverConfiguration.getWsdl(),
													serverConfiguration.getNamespace(),
													serverConfiguration.getService(),
													serverConfiguration.getHttpAddress());

											WebServiceInterface ws = wsc.setConnection();
											Set<String> uuidsXslt = ws.getXsltUuids();
											System.err.println(serverConfiguration.getName());
											System.err.println(uuid);
											if (uuidsXslt.contains(uuid)) {
												content = ws.getXsltContent(uuid);
												response.setStatus(HTTPResponseStatus.S200);
												response.putParameter("Content-Type", "application/xml");
												response.setContent(content);
											}
										} catch (WebServiceException e) {
											System.out.println("No se pudo conectar al servidor");
										}

									}
								}
								if (content == null)
									throw new HTTPParseException("Not Found");

							}

						}

					} else if (request.getResourceName().equals("xsd")) {

						if (request.getResourceChain().equals("/xsd")) {
							stringBuilder.append("<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>"
									+ "  <meta charset=\"utf-8\"/>" + "  <title>Hybrid Server</title>"
									+ "  <link href=\"style.css\" rel=\"stylesheet\" />" + "</head>" + "<body>"
									+ "<h1>Hybrid Server</h1>" + "<ol>");
							// archivos locales
							for (String set : xsdProvider.list()) {
								stringBuilder.append("<li><a href=\"http://localhost:" + port + "/xsdt?uuid=" + set
										+ "\">" + set + "</a></li>");
							}
							// archivos remotos
							stringBuilder.append("</ol>\n");
							if (listServers != null) {
								Iterator iConnections = availableConnections.iterator();
								Iterator iInfoServers = infoServers.iterator();
								while (iConnections.hasNext()) {
									ServerConfiguration info = (ServerConfiguration) iInfoServers.next();
									stringBuilder.append("<h1>" + info.getName() + "</h1><ol>");
									WebServiceInterface connection = (WebServiceInterface) iConnections.next();
									Set<String> uuidsXsd = connection.getXsdUuids();
									for (String uuidXsd : uuidsXsd) {
										stringBuilder.append("<li><a href=\"" + info.getHttpAddress() + "xml?uuid="
												+ uuidXsd + "\">" + uuidXsd + "</a></li>");
									}
									stringBuilder.append("</ol>");
								}
							}
							stringBuilder.append("</body>" + "</html>");

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
								String content = null;
								if (listServers != null) {
									Iterator i = listServers.iterator();

									while (i.hasNext() && content == null) {
										try {
											ServerConfiguration serverConfiguration = (ServerConfiguration) i.next();
											WebServiceConnection wsc = new WebServiceConnection(
													serverConfiguration.getName(), serverConfiguration.getWsdl(),
													serverConfiguration.getNamespace(),
													serverConfiguration.getService(),
													serverConfiguration.getHttpAddress());

											WebServiceInterface ws = wsc.setConnection();
											Set<String> uuidsXsd = ws.getXsdUuids();
											System.err.println(serverConfiguration.getName());
											System.err.println(uuid);
											if (uuidsXsd.contains(uuid)) {
												content = ws.getXsdContent(uuid);
												response.setStatus(HTTPResponseStatus.S200);
												response.putParameter("Content-Type", "application/xml");
												response.setContent(content);
											}
										} catch (WebServiceException e) {
											System.out.println("No se pudo conectar al servidor");
										}

									}
								}
								if (content == null)
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

							xsltProvider.add(uuid.toString(), request.getResourceParameters().get("xslt"),
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
									auxStringBuilder
											.append("<li><a href=\"xslt?uuid=" + set + "\">" + set + "</a></li>");
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
