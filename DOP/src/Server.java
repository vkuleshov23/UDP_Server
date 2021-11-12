package src;
import java.io.IOException;
import java.util.Scanner;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
// import java.net.SocketException;
import java.nio.charset.StandardCharsets;

class Server {
	
	private static boolean clientIsConnect = false;
	// private static InetAddress clientIP;
	// private static int clientPORT = 1337;

	private static boolean quit = false;

	private static ServerSocket socket;
	private static Socket clientSocket;

	private static String name =  "ServerUser";
	private static final String quitCommand = "@quit";
	private static final String nameCommand = "@name";
	private static final int maxNameLength = 128;
	private static final int maxPacketLength = 1024;

	public static void main(String args[]){
		try {	
			try{
				init(args[0]);
				while(!quit) {
					OutputStream out = connection();
					try {
						Reciever reciever = new Reciever(clientSocket.getInputStream());
						reciever.start();
						senderLoop(out);
					} finally {
						out.close();
					}
				}
			} catch (Exception ioerr){
				ioerr.printStackTrace();
			} finally {
				socket.close();
			}
		} catch (IOException err) {

		} finally {
		}
	}

	private static void init(String strPort) throws IOException {
		socket =  new ServerSocket(Integer.parseInt(strPort));
	}

	private static OutputStream connection() throws IOException {
		clientSocket = socket.accept();
		clientIsConnect = true;
		return clientSocket.getOutputStream();
	}


	private static void senderLoop(OutputStream out) throws IOException{
		Scanner sc = new Scanner(System.in);
		while(clientIsConnect) {
			String data = readServerMsg(sc);
			// System.out.println("readed " + data);
			if(msgIsQuit(data)) {
				send(data, out);
				exit();
			} else if(msgIsName(data)) {
				setServerName(data);
			} else { 
				// System.out.println("!almost send! " + data);
				send(prepareData(data), out);
			}
		}
	}

	private static void exit() {
		quit = true;
		clientIsConnect = false;
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

	private static void send(String data, OutputStream out) throws IOException {
		System.out.println("send " + data);
		if(clientIsConnect){
		System.out.println("true send " + data);
			byte[] sendData = new byte[maxPacketLength + maxNameLength];
			sendData = data.getBytes();				
			out.write(sendData);
			out.flush();
		}
	}
	private static String prepareData(String data){
		if(data.length() > maxPacketLength){
			data = data.substring(0, maxPacketLength);
		}
		return name + ": " + data;
	}

	// private static void setConnectionWithClient(DatagramPacket packet){
	// 	clientIsConnect = true;
	// 	clientIP = packet.getAddress();
	// 	clientPORT = packet.getPort();
	// }

	static private class Reciever extends Thread {
		
		private static InputStream in;
		// private static boolean connection;

		Reciever(InputStream inStream) {
			System.out.println("init");
			in = inStream;
			// connection = con;
		}

		@Override
		public void run(){
			try {	
				while(!quit) {
					String data = getDataFromClient(in);
					if(clientSendQuit(data)){
						clientWasExited();
						break;
					} else {
						printClientData(data);
						// setConnectionWithClient(packet);		
					}
				}		
			} catch (IOException err) {

			}
		}

		private static String getDataFromClient(InputStream in) throws IOException {
			byte[] receiveData = new byte[maxPacketLength + maxNameLength];
			in.read(receiveData);
			return (new String(receiveData, StandardCharsets.UTF_8)).trim();
		}

		private static boolean clientSendQuit(String data) {
			return data.equals(quitCommand);
		}

		private static void clientWasExited() {
			System.out.println("The client exited the chat.");
			clientIsConnect = false;
		}
		
		private static void printClientData(String data) {	
			System.out.println(data);
		}		
	}
}
	