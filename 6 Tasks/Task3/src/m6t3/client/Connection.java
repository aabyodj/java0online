package m6t3.client;

import static m6t3.common.Const.INVALID_ID;
import static m6t3.common.Tranceiver.AUTH_ACKNOWLEDGEMENT;
import static m6t3.common.Tranceiver.AUTH_CHALLENGE;
import static m6t3.common.Tranceiver.receiveInt;
import static m6t3.server.ServerMain.DEFAULT_IP_PORT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import m6t3.common.AuthAcknowledgement;
import m6t3.common.AuthChallenge;
import m6t3.common.ChangePassRequest;
import m6t3.common.LoginRequest;

class Connection {

	static final String DEFAULT_SERVER_HOST = "localhost";
	static final int DEFAULT_TIMEOUT = 3000;
	static final int RETRIES_COUNT = 3;
	
	final ClientMain client;
//	final Display display;
	private String serverHost;
	private int serverPort;
	private Socket socket;
	final ClientReceiver receiver;
	final ClientTransmitter transmitter;
	final Synchronizer synchronizer;
	
	private String login = "";
	private char[] password = null;
	private AuthChallenge lastChallenge = null;
	
//	final Queue<Student> inQueue = new LinkedList<>();
	final BlockingQueue<Object> outQueue = new LinkedBlockingQueue<>();
	private boolean connected = false;
	private boolean terminated = false;

	public Connection(ClientMain client) {
		this.serverHost = DEFAULT_SERVER_HOST;
		this.serverPort = DEFAULT_IP_PORT;
		this.client = client;
		receiver = new ClientReceiver(this);
		receiver.start();
		transmitter = new ClientTransmitter(this);
		transmitter.start();
		synchronizer = new Synchronizer(client);
		synchronizer.start();
//		outQueue.add(SYNC_REQUEST);
	}

	void reconnect() {
		while (!terminated) {
//			System.out.print("Reconnection ");
			int retries = RETRIES_COUNT;
			while (retries-- > 0) {
				connected = false;
//				System.out.print(retries + " ");
				try {
					socket.close();
				} catch (Exception e) {
					//Nothing to do here
				}
				socket = new Socket();
				try {
					socket.connect(new InetSocketAddress(serverHost, serverPort), DEFAULT_TIMEOUT);
					InputStream in = socket.getInputStream();
					OutputStream out = socket.getOutputStream();
//					System.out.print("success. Now authenticating...");
					if (authenticate(in, out)) {
//						System.out.println("Ok.");
						receiver.in = in;
						transmitter.out = out;
						connected = true;
						return;						
					}
				} catch (IOException e) {
					//Nothing to do here
				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					// TODO Auto-generated catch block
//					System.err.println("Cryptographic error");
					e.printStackTrace();
				}
				if (terminated) return;
			}
//			System.out.println("failed.");
			if (terminated) return;
			client.showReconnDlg();
		}
//		System.out.println("No more reconnects");
		terminate();
	}
	
	private boolean authenticate(InputStream in, OutputStream out) 
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (null == lastChallenge) {
			client.showLoginDialog();
		}
		new LoginRequest(login).transmit(out);
		if (receiveInt(in) != AUTH_CHALLENGE) return false;
		AuthChallenge challenge = AuthChallenge.receive(in);
		if (null != password) {
			challenge.createResponse(password).transmit(out);
			Arrays.fill(password, (char) 0);
			password = null;			
		} else {
			challenge.createResponse(lastChallenge).transmit(out);
		}
		if (receiveInt(in) != AUTH_ACKNOWLEDGEMENT) return false;
		AuthAcknowledgement authAck = AuthAcknowledgement.receive(in);
		if (INVALID_ID == authAck.userId) {
			lastChallenge = null;
			return false;
		}
		client.setLogin(login);
		client.setAdmin(authAck.admin);
		lastChallenge = challenge;
		return true;
	}

//	private void showLoginDialog() {
//		if (!(boolean) new LoginDialog(client).open()) {
//			client.close();
//		}
//	}
//
//	private void showReconnDlg() {
//		if (!(boolean) new ConnectionDialog(this).open()) {
//			client.close();
//		}
//	}

	public void terminate() {
		if (terminated) return;
		terminated = true;
//		System.out.println("Terminating...");
		connected = false;
		synchronizer.interrupt();
		if (outQueue.isEmpty()) {
//			System.out.println("Interrupting transmitter...");
			transmitter.interrupt();
		}
		try {
			transmitter.join();
		} catch (InterruptedException e) {
			//Nothing to do here
		}
		try {
			socket.close();
		} catch (Exception e) {
			//Nothing to do here
		}
//		System.out.println("Terminated");
	}

	public String getServerHost() {
		return serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerHost(String newHost) {
		if (newHost.isBlank()) return;
		newHost = newHost.trim();
		if (newHost.equalsIgnoreCase(serverHost)) return;
		serverHost = newHost;
		if (connected) reconnect();
	}

	public void setServerPort(int newPort) {
		if (newPort == serverPort) return;
		serverPort = newPort;
		if (connected) reconnect();
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String newLogin) {
		if (newLogin.isBlank()) return;
		newLogin = newLogin.trim();
		if (newLogin.equalsIgnoreCase(login)) return;
		login = newLogin;
		if (connected) reconnect();
	}

	public void setPassword(char[] newPass) {
		if (newPass.length == 0) return;
		password = newPass;
		if (connected) reconnect();
	}

	public boolean changePass(char[] oldPass, char[] newPass) {
		try {
			ChangePassRequest request = lastChallenge.createChangePassRequest(oldPass, newPass);
			if (request != null) {
				outQueue.add(request);
				return true;
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
