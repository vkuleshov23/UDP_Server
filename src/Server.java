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

	private static void connection(String strPort) throws IOException, SocketException{
		socket =  new DatagramSocket(Integer.parseInt(strPort));
	}
	private static void receiverLoop() throws SocketException, IOException{
		while(!quit){
			byte[] receiveData = new byte[maxPacketLength + maxNameLength];
			DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(packet);
			String data = (new String(packet.getData(), StandardCharsets.UTF_8)).trim();
			if(data.equals(quitCommand)){
				System.out.println("The client exited the chat.");
				clientIsConnect = false;
			} else {
				System.out.println(data);
				clientIsConnect = true;
				clientIP = packet.getAddress();
				clientPORT = packet.getPort();
			}
			data = "";
		}
	}
	// private static DatagramPacket send(String data){
	// 	byte[] sendData = new byte[data.length()];
	// 	sendData = data.getBytes();
	// 	DatagramPacket packet = new DatagramPacket(sendData, sendData.length(), ip, port);
	// 	socket.send(packet);
	// }
	static private class ServerSender extends Thread {
		ServerSender(){ }
		@Override
		public void run(){
			Scanner sc = new Scanner(System.in);
			String data = "";	
			while(true){
				data = sc.nextLine();
				data.trim();
				if(data.equals(quitCommand)) {
					send(data);
					break;
				}
				if(data.regionMatches(0, nameCommand, 0, nameCommand.length())){
					if(data.length() != nameCommand.length()){
						data = data.substring(nameCommand.length()+1, data.length());
						data.trim();
						if(data.length() > maxNameLength){
							data = data.substring(0, maxNameLength);
							data.trim();
						}
						name = data;
					}
					continue;
				}
				// System.out.println("sending... " + data);
				send(prepareData(data));
				// System.out.println("end sending...");
				data = "";
			}
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
	