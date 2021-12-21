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
package es.uvigo.esei.dai.hybridserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import es.uvigo.esei.dai.sax.SAXParserImplementation;

public class HybridServer {

	public int SERVICE_PORT;
	public int NUM_CLIENTS;
	private Thread serverThread;
	private boolean stop;
	private HTMLController htmlController;
	private XMLController xmlController;
	private XSDController xsdController;
	private XSLTController xsltController;
	private ExecutorService threadPool;
	
	public HybridServer() {
		
		String DB_USER = "hsdb";
		String DB_PASSWORD = "hsdbpass";
		this.NUM_CLIENTS = 50;
		String DB_URL = "jdbc:mysql://localhost:3306/hstestdb";
		this.SERVICE_PORT = 8888;

		htmlController = new HTMLController(new HTMLDBDAO(DB_URL,DB_PASSWORD,DB_USER));
		System.err.println("1111111111111111");
		
	}

	public HybridServer(Map<String, String> pages) {
		htmlController = new HTMLController(new HTMLMapDAO(pages));
		this.SERVICE_PORT = 8888;
	    this.NUM_CLIENTS = 50;
	}

	public HybridServer(Properties properties) {
		
		System.out.println("333333333333333");
	
		String DB_USER = properties.getProperty("db.user");
		String DB_PASSWORD = properties.getProperty("db.password");
		String DB_URL = properties.getProperty("db.url");
		SERVICE_PORT = Integer.parseInt(properties.getProperty("port"));
		NUM_CLIENTS = Integer.parseInt(properties.getProperty("numClients"));

		htmlController = new HTMLController(new HTMLDBDAO(DB_URL,DB_PASSWORD,DB_USER));
		xmlController = new XMLController(new XMLDBDAO(DB_URL,DB_PASSWORD,DB_USER));
		xsdController = new XSDController(new XSDDBDAO(DB_URL,DB_PASSWORD,DB_USER));
		xsltController = new XSLTController(new XSLTDBDAO(DB_URL,DB_PASSWORD,DB_USER));
		
	}

	public HybridServer(Configuration configuration) {
		
		System.out.println("444444444444444");

		String DB_USER = configuration.getDbUser();
		String DB_PASSWORD = configuration.getDbPassword();
		String DB_URL = configuration.getDbURL();
		SERVICE_PORT = configuration.getHttpPort();
		NUM_CLIENTS = configuration.getNumClients();

		htmlController = new HTMLController(new HTMLDBDAO(DB_URL,DB_PASSWORD,DB_USER));
		xmlController = new XMLController(new XMLDBDAO(DB_URL,DB_PASSWORD,DB_USER));
		xsdController = new XSDController(new XSDDBDAO(DB_URL,DB_PASSWORD,DB_USER));
		xsltController = new XSLTController(new XSLTDBDAO(DB_URL,DB_PASSWORD,DB_USER));

	}

	public int getPort() {
		return SERVICE_PORT;
	}

	public void start() {
		this.serverThread = new Thread() {
			@Override
			public void run() {
				try (final ServerSocket serverSocket = new ServerSocket(SERVICE_PORT)) {
					threadPool = Executors.newFixedThreadPool(NUM_CLIENTS);	
					while (true) {
						Socket socket = serverSocket.accept();
						if (stop)
							break;
						ServerServiceThread serviceTask = 
								new ServerServiceThread(socket, htmlController, xmlController, xsdController, xsltController);
						threadPool.execute(serviceTask);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		this.stop = false;
		this.serverThread.start();
	}

	public void stop() {
		
		this.stop = true;

		try (Socket socket = new Socket("localhost", SERVICE_PORT)) {
			// Esta conexión se hace, simplemente, para "despertar" el hilo servidor
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			this.serverThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		this.serverThread = null;
		
		threadPool.shutdownNow();
		 
		try {
		  threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		  e.printStackTrace();
		}
		
	}
}
