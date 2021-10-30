package src;
import java.io.IOException;
import java.util.Scanner;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
// import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

class Server {
	
	private static boolean clientIsConnect = false;
	private static InetAddress clientIP;
	private static int clientPORT = 1337;

	private static boolean quit = false;

	private static DatagramSocket socket;

	private static String name =  "ServerUser";
	private static final String quitCommand = "@quit";
	private static final String nameCommand = "@name";
	private static final int maxNameLength = 128;
	private static final int maxPacketLength = 1024;

	private static void connection(String strPort) throws IOException, SocketException {
		socket =  new DatagramSocket(Integer.parseInt(strPort));
	}

	private static DatagramPacket getClientPacket() throws IOException{
		byte[] receiveData = new byte[maxPacketLength + maxNameLength];
		DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
		socket.receive(packet);
		return packet;
	}

	private static String getDataFromPacket(DatagramPacket packet) {
		return (new String(packet.getData(), StandardCharsets.UTF_8)).trim();		
	}

	private static void clientWasExited() {
		System.out.println("The client exited the chat.");
		clientIsConnect = false;
	}
	
	private static void printClientData(String data) {	
		System.out.println(data);
	}

	private static void setConnectionWithClient(DatagramPacket packet){
		clientIsConnect = true;
		clientIP = packet.getAddress();
		clientPORT = packet.getPort();
	}

	private static boolean clientSendQuit(String data) {
		return data.equals(quitCommand);
	}
	
	private static void receiverLoop() throws SocketException, IOException{
		while(!quit){
			DatagramPacket packet = getClientPacket();
			String data = getDataFromPacket(packet);
			if(clientSendQuit(data)){
				clientWasExited();
			} else {
				printClientData(data);
				setConnectionWithClient(packet);			
			}
		}
	}

	// private static DatagramPacket send(String data){
	// 	byte[] sendData = new byte[data.length()];
	// 	sendData = data.getBytes();
	// 	DatagramPacket packet = new DatagramPacket(sendData, sendData.length(), ip, port);
	// 	socket.send(packet);
	// }
	static private class ServerSender extends Thread {
		
		ServerSender() { }


		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);
			while(true) {
				String data = readServerMsg(sc);						
				if(msgIsQuit(data)) {
					send(data);
					break;
				} else if(msgIsName(data)) {
					setServerName(data);
					// continue;
				} else { 
					// System.out.println("sending... " + data);
					send(prepareData(data));
					// System.out.println("end sending...");
				}
			}
			closeScannerAndExit(sc);
		}

		private static String readServerMsg(Scanner sc){
			return (sc.nextLine()).trim();
		}

		private static boolean msgIsName(String data) {
			return data.regionMatches(0, nameCommand, 0, nameCommand.length());
		}

		private static void setServerName(String data){
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

		private static boolean msgIsQuit(String data) {
			return data.equals(quitCommand);	
		}

		private static void closeScannerAndExit( Scanner sc) {
			sc.close();
			System.exit(0);
		}

		private static void send(String data){
			if(clientIsConnect){
				byte[] sendData = new byte[maxPacketLength + maxNameLength];
				sendData = data.getBytes();
				DatagramPacket packet = new DatagramPacket(sendData, sendData.length, clientIP, clientPORT);
				try{
					socket.send(packet);
				} catch(IOException ioerr){
					ioerr.printStackTrace();
				}
			}
		}
		private static String prepareData(String data){
			if(data.length() > maxPacketLength){
				data = data.substring(0, maxPacketLength);
			}
			return name + ": " + data;
		}

	}
	public static void main(String args[]) throws IOException, SocketException{
		try{
			connection(args[0]);
			ServerSender sender = new ServerSender();
			sender.start();
			receiverLoop();
			// breakConnection();
		} catch (Exception ioerr){
			ioerr.printStackTrace();
		// } catch (SocketException serr){
		// 	serr.printStackTrace();
		}
	}
}
	