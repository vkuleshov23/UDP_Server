package src;

import java.io.IOException;
import java.util.Scanner;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;
// import java.net.DatagramPacket;
// import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

class Client {
	
	private static boolean serverIsConnet = true;

	private static Socket socket;
	private static int port = 1337;

	private static String name =  "ClientUser";
	private static final String quitCommand = "@quit";
	private static final String nameCommand = "@name";
	private static final int maxNameLength = 128;
	private static final int maxPacketLength = 1024;


	public static void main(String args[]) {
		try {	
			OutputStream out = connection(args[0], args[1]);
			ClientReceiver receiver = new ClientReceiver(socket.getInputStream());
			receiver.start();
			sendLoop(out);
			breakConnection(receiver, out);
		} catch (IOException ioerr) {
			ioerr.printStackTrace();
		} catch (InterruptedException ierr) {
			ierr.printStackTrace();
		}
	}


	private static OutputStream connection(String strIP, String strPort) throws UnknownHostException, IOException {
		socket =  new Socket(strIP, Integer.parseInt(strPort));
		return socket.getOutputStream();
		// ip = InetAddress.getByName(strIP);
		// port = ;
	}

	private static void sendLoop(OutputStream out) throws IOException, SocketException {
		Scanner sc = new Scanner(System.in);
		while (serverIsConnet) {
			String data = readClientMsg(sc);
			if (msgIsQuit(data)) {
				send(data, out);
				break;
			} else if (msgIsName(data)) {
				setClientName(data);
			} else {
				send(prepareData(data), out);
			}
		}
		sc.close();
	}

	private static void send(String data, OutputStream out) throws IOException {
		byte[] sendData = new byte[maxNameLength + maxPacketLength];
		sendData = data.getBytes();
		out.write(sendData);
		out.flush();
	}

	private static String readClientMsg(Scanner sc){
		return (sc.nextLine()).trim();
	}

	private static String prepareData(String data) {
		if(data.length() > maxPacketLength) {
			data = data.substring(0, maxPacketLength);
		}
		return name + ": " + data;
		// return data;
	}

	private static boolean msgIsQuit(String data) {
		return data.equals(quitCommand);	
	}

	private static boolean msgIsName(String data) {
		return data.regionMatches(0, nameCommand, 0, nameCommand.length());
	}

	private static void setClientName(String data){
		if(data.length() != nameCommand.length()){
			data = data.substring(nameCommand.length()+1, data.length());
			data.trim();
			if(data.length() > maxNameLength){
				data = data.substring(0, maxNameLength);
				data.trim();
			}
			name = data;
		}
	}
	
	private static void breakConnection(ClientReceiver receiver, OutputStream out) throws SocketException, InterruptedException, IOException {
		receiver.disable();
		socket.close();
		receiver.join();
		out.close();
	}
	
	static private class ClientReceiver extends Thread {
		private static boolean disable;
		private static InputStream in;
		ClientReceiver(InputStream ins) {
			in = ins;
			disable = false;
		}
		@Override
		public void run() {
			while(!disable) {
				try {
					String data = receiveData();
					if(serverSendQuit(data)) {
						stopRecieve();
						break;
					} else {
						printServerMsg(data);
					}
				} catch(IOException ioerr) {
					serverIsNotConnected();
					break;
				}
			}
		}

		static private void stopRecieve() {
			serverIsNotConnected();
			System.out.println("The server closed the connection.");
		}

		static private void serverIsNotConnected() {
			serverIsConnet = false;
		}

		static private void printServerMsg(String data) {
			System.out.println(data);
		}

		static private boolean serverSendQuit(String data) {
			return data.equals(quitCommand);
		}
		
		static private String receiveData() throws IOException {
			byte[] receiveData = new byte[maxPacketLength + maxNameLength];
			in.read(receiveData);
			return (new String(receiveData, StandardCharsets.UTF_8)).trim();		
		}
		
		static public void disable() {
			disable = true;
		}
	}
}

// сделать не UDP а TCP.