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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.AutoRetryHttpClient;

public class HTTPResponse {
	
	HTTPResponseStatus status;
	public String version, content;
	private LinkedHashMap<String, String> parameters;

	public HTTPResponse() {
		parameters = new LinkedHashMap<String, String>();
		content = "";
		version = "HTTP/1.1";
	}

	public HTTPResponseStatus getStatus() {
		return this.status;
	}

	public int getCode() {
		return this.status.getCode();
	}

	public void setStatus(HTTPResponseStatus status) {
		this.status = status;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LinkedHashMap<String, String> getParameters() {
		return this.parameters;
	}

	public String putParameter(String name, String value) {
		return this.parameters.put(name, value);
	}

	public boolean containsParameter(String name) {
		boolean b = false;
		Iterator<String> it = this.getParameters().keySet().iterator();
		while (it.hasNext()) {
			if (name.equals(it))
				b = true;
		}
		return b;
	}

	public String removeParameter(String name) {
		return this.getParameters().remove(name);
	}

	public void clearParameters() {
		this.parameters.clear();
	}

	public List<String> listParameters() {
		return null;
	}

	public void print(Writer writer) throws IOException {

		StringBuilder toRet = new StringBuilder();
		
		//Primera línea
		toRet.append(this.getVersion());
		toRet.append(" ");
		toRet.append(this.getCode());
		toRet.append(" ");
		toRet.append(this.status.getStatus());
		toRet.append("\r\n");

		//Parámetros
		Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator();
		for (int i = 0; i < parameters.size(); i++) {
			Map.Entry<String, String> tempMap = iterator.next();
			toRet.append(tempMap.getKey());
			toRet.append(": ");
			toRet.append(tempMap.getValue());
			toRet.append("\r\n");
		}
		
		//Final, contenido
		if (!content.equals("")) {
			toRet.append("Content-Length: ");
			toRet.append(content.length());
			toRet.append("\r\n\r\n");
			toRet.append(content);
		} else {
			toRet.append("\r\n");			
		}		
		
		writer.append(toRet);
		
	}

	@Override
	public String toString() {
		final StringWriter writer = new StringWriter();

		try {
			this.print(writer);
		} catch (IOException e) {
		}

		return writer.toString();
	}
}
