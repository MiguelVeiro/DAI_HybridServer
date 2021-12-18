/**
 *  HybridServer
 *  Copyright (C) 2021 Miguel Reboiro-Jato
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.esei.dai.hybridserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.net.URLDecoder;

public class HTTPRequest {

	private HTTPRequestMethod method;
	private String resourceChain, resourceName, version, content;
	private int contentLength;
	private String[] resourcePath;
	private LinkedHashMap<String, String> resourceParameters;
	private LinkedHashMap<String, String> headerParameters;
	private String cadena;
	private BufferedReader reading;
	private LinkedList<String[]> structure;

	public HTTPRequest(Reader reader) throws IOException, HTTPParseException {

		this.resourceParameters = new LinkedHashMap<String, String>();
		this.headerParameters = new LinkedHashMap<String, String>();
		reading = new BufferedReader(reader);
		structure = new LinkedList<String[]>();

		cadena = reading.readLine();
		structure.add(cadena.split(" "));

		// Method

		if (structure.get(0).length != 3) {
			if (!structure.get(0)[0].equals(HTTPRequestMethod.values()))
				throw new HTTPParseException("Missing method");
			else if (structure.get(0)[1].contains("HTTP/"))
				throw new HTTPParseException("Missing resource");
			else if (cadena.contains(": "))
				throw new HTTPParseException("Missing first line");
			else
				throw new HTTPParseException("Missing version");
		}

		switch (structure.get(0)[0]) {
		case "GET":
			this.method = HTTPRequestMethod.GET;
			break;
		case "POST":
			this.method = HTTPRequestMethod.POST;
			break;
		case "HEAD":
			this.method = HTTPRequestMethod.HEAD;
			break;
		case "PUT":
			this.method = HTTPRequestMethod.PUT;
			break;
		case "DELETE":
			this.method = HTTPRequestMethod.DELETE;
			break;
		case "TRACE":
			this.method = HTTPRequestMethod.TRACE;
			break;
		case "OPTIONS":
			this.method = HTTPRequestMethod.OPTIONS;
			break;
		case "CONNECT":
			this.method = HTTPRequestMethod.CONNECT;
			break;
		}

		// Resource - resourceName, resourceChain, resourceParameters, resourcePath

		this.resourceChain = structure.get(0)[1];
		
		if (this.resourceChain.contains("?")) {

			String[] resourceChainSplit = this.resourceChain.split("\\?");
			this.resourceName = resourceChainSplit[0].substring(1);
			this.resourcePath = resourceChainSplit[0].substring(1).split("/");

			if (resourceChainSplit.length > 1) {
				String[] parameters = resourceChainSplit[1].split("&");

				for (int i = 0; i < parameters.length; i++) {
					if (!parameters[i].contains("="))
						throw new HTTPParseException("Invalid parameters");
					
					int posIgual = parameters[i].indexOf("=");
					String tempClave = parameters[i].substring(0, posIgual);
					String tempValor = parameters[i].substring(posIgual + 1, parameters[i].length());
					this.resourceParameters.put(tempClave, tempValor);
				}
			}
			
		} else {
			
			if(this.getResourceChain().equals("/")) {
				this.resourceName = "";
				this.resourcePath = new String[0];
			} else {
				this.resourceName = this.resourceChain.substring(1); //substring 1 para omitir la barra de ruta
				this.resourcePath = this.resourceChain.substring(1).split("/");				
			}
			
		}
		
		//Version
		
		this.version = structure.get(0)[2];
		
		//Headers y POST parameters
		
		cadena = reading.readLine();
		while (cadena != null &&cadena.contains(": ")) {
			structure.add(cadena.split(": "));
			String[] headerSplit = cadena.split(": ");
			String headerName = headerSplit[0];
			String headerValue = headerSplit[1];
			this.headerParameters.put(headerName, headerValue);
			cadena = reading.readLine();
		}
		if (cadena != null && !cadena.isEmpty()) {
			System.err.println("Invalid header: " + cadena);
			throw new HTTPParseException("Invalid header");
		}
		
		//Content
		
		if (this.headerParameters.containsKey("Content-Length")) {
			contentLength = Integer.parseInt(this.headerParameters.get("Content-Length"));
			StringBuilder contentBuilder = new StringBuilder();
			for (int i = 0; i < contentLength; i++) {
				char nextChar = (char) reading.read();
				contentBuilder.append(nextChar);
			}
			content = contentBuilder.toString();
		}	   
		
		String type = headerParameters.get("Content-Type");
		if (type != null && type.startsWith("application/x-www-form-urlencoded")) {
			content = URLDecoder.decode(content, "UTF-8");
		}
		
		if(this.method == HTTPRequestMethod.POST) {
			String[] parameters = content.split("&");

			for (int i = 0; i < parameters.length; i++) {
				if (!parameters[i].contains("="))
					throw new HTTPParseException("Invalid parameters");
				
				int posIgual = parameters[i].indexOf("=");
				String tempClave = parameters[i].substring(0, posIgual);
				String tempValor = parameters[i].substring(posIgual + 1, parameters[i].length());
				this.resourceParameters.put(tempClave, tempValor);
			}
		}
		
		//reading.close();

	}

	public HTTPRequestMethod getMethod() {
		// TODO Auto-generated method stub
		return method;
	}

	public String getResourceChain() {
		// TODO Auto-generated method stub
		return resourceChain;
	}

	public String[] getResourcePath() {
		// TODO Auto-generated method stub
		return resourcePath;
	}

	public String getResourceName() {
		// TODO Auto-generated method stub
		return resourceName;
	}

	public Map<String, String> getResourceParameters() {
		// TODO Auto-generated method stub
		return resourceParameters;
	}

	public String getHttpVersion() {
		// TODO Auto-generated method stub
		return version;
	}

	public Map<String, String> getHeaderParameters() {
		// TODO Auto-generated method stub
		return headerParameters;
	}

	public String getContent() {
		// TODO Auto-generated method stub
		return content;
	}

	public int getContentLength() {
		// TODO Auto-generated method stub
		return contentLength;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.getMethod().name()).append(' ').append(this.getResourceChain())
				.append(' ').append(this.getHttpVersion()).append("\r\n");

		for (Map.Entry<String, String> param : this.getHeaderParameters().entrySet()) {
			sb.append(param.getKey()).append(": ").append(param.getValue()).append("\r\n");
		}

		if (this.getContentLength() > 0) {
			sb.append("\r\n").append(this.getContent());
		}

		return sb.toString();
	}
}
