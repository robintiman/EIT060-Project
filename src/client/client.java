package client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class client {

	public static void main(String[] args) throws Exception {
		System.out.println("Log in as: ");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String res = reader.readLine();
		String certPath = null;
		
		if (res.equals("nurse")) {
			certPath = "nurse";
		} else if (res.equals("doctor")) {
			certPath = "doctor";
		} else if (res.equals("gov") || res.equals("government")) {
			certPath = "gov";
		} else if (res.equals("patient")) {
			certPath = "patient";
		} else {
			System.out.println("Invalid role, exiting...");
			System.exit(0);
		}
		
		String host = null;
		String pwd = null;
		int port = -1;
		for (int i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "] = " + args[i]);
		}
		
		
		try { /* get input parameters */
			if (args.length < 3) {
				host = "localhost";
				port = 3000;
				pwd = "password";
				System.out.println("No args specified, using host \"" + host
						+ "\", port " + port + " and the default keystore password");
			} else {
				host = args[0];
				port = Integer.parseInt(args[1]);
				pwd = args[2];
			}
		} catch (IllegalArgumentException e) {
			System.out.println("USAGE: java client host port");
			System.exit(-1);
		}

		try { /* set up a key manager for client authentication */
			SSLSocketFactory factory = null;
			try {
				char[] password = pwd.toCharArray();
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				KeyManagerFactory kmf = KeyManagerFactory
						.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory
						.getInstance("SunX509");
				SSLContext ctx = SSLContext.getInstance("TLS");
				ks.load(new FileInputStream("certs/" + certPath + "/clientkeystore"), password); // keystore
																				// password
																				// (storepass)
				ts.load(new FileInputStream("certs/" + certPath + "/clienttruststore"), password); // truststore
																					// password
																					// (storepass);
				kmf.init(ks, password); // user password (keypass)
				tmf.init(ts); // keystore can be used as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				factory = ctx.getSocketFactory();
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
			System.out.println("\nsocket before handshake:\n" + socket + "\n");

			/*
			 * send http request
			 * 
			 * See SSLSocketClient.java for more information about why there is
			 * a forced handshake here when using PrintWriters.
			 */
			socket.startHandshake();

			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session
					.getPeerCertificateChain()[0];
			String subject = cert.getSubjectDN().getName();
			System.out
					.println("certificate name (subject DN field) on certificate received from server:\n"
							+ subject + "\n");
			System.out.println("socket after handshake:\n" + socket + "\n");
			System.out.println("secure connection established\n\n");

			BufferedReader read = new BufferedReader(new InputStreamReader(
					System.in));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String msg;
			for (;;) {
				System.out.print(">");
				msg = read.readLine();
				if (msg.equalsIgnoreCase("quit")) {
					break;
				}
//				System.out.print("sending '" + msg + "' to server...");
				out.println(msg);
				out.flush();
//				System.out.println("done");
				String recv = in.readLine();
				recv = recv.replace("\\n", "\n");
				System.out.println(recv);
			}
			in.close();
			out.close();
			read.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
