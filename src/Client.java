package src;
import java.io.IOException;
import java.util.Scanner;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

class Client {
	
	private static boolean serverIsConnet = true;

	private static DatagramSocket socket;
	private static InetAddress ip;
	private static int port = 1337;

	private static String name =  "ClientUser";
	private static final String quitCommand = "@quit";
	private static final String nameCommand = "@name";
	private static final int maxNameLength = 128;
	private static final int maxPacketLength = 1024;

	private static void send(String data) throws IOException {
		byte[] sendData = new byte[maxNameLength + maxPacketLength];
		sendData = data.getBytes();
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, ip, port);
		socket.send(packet);
	}
	private static String prepareData(String data) {
		if(data.length() > maxPacketLength) {
			data = data.substring(0, maxPacketLength);
		}
		return name + ": " + data;
	}
	private static void connection(String strIP, String strPort) throws SocketException, UnknownHostException {
		socket =  new DatagramSocket();
		ip = InetAddress.getByName(strIP);
		port = Integer.parseInt(strPort);
	}

	private static String readClientMsg(Scanner sc){
		return (sc.nextLine()).trim();
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

	private static void sendLoop() throws IOException, SocketException {
		Scanner sc = new Scanner(System.in);
		while (serverIsConnet) {
			String data = readClientMsg(sc);
			if (msgIsQuit(data)) {
				send(data);
				break;
			} else if (msgIsName(data)) {
				setClientName(data);
			} else {
				// System.out.println("sending...");
				send(prepareData(data));
				// System.out.println("end sending...");
			}
		}
		sc.close();
	}
	
	private static void breakConnection(ClientReceiver receiver) throws SocketException, InterruptedException {
		receiver.disable();
		socket.close();
		receiver.join();
	}
	
	static private class ClientReceiver extends Thread {
		private static boolean disable;
		ClientReceiver() { disable = false; }
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
			DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(packet);
			return (new String(packet.getData(), StandardCharsets.UTF_8)).trim();		
		}
		
		static public void disable() {
			disable = true;
		}
	}

	public static void main(String args[]) {
		try {	
			connection(args[0], args[1]);
			ClientReceiver receiver = new ClientReceiver();
			receiver.start();
			sendLoop();
			breakConnection(receiver);
		} catch (IOException ioerr) {
			ioerr.printStackTrace();
		// } catch (SocketException serr){
		// 	serr.printStackTrace();
		// } catch (UnknownHostException uherr){
		// 	uherr.printStackTrace();
		} catch (InterruptedException ierr) {
			ierr.printStackTrace();
		}
	}
}

// сделать не UDP а TCP.