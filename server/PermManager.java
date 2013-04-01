package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class PermManager {
	public static final String permRoot = Server.serverRoot + "perms/";
	
	public static void addPermission(String fileName, String username, int perm) {
		RandomAccessFile permRF = null;
		Map<String, Integer> permissions = new HashMap<String, Integer>();
		try {
			File permF = new File(permRoot + fileName);
			permRF = new RandomAccessFile(permF, "rw");
			String buffer;
			while((buffer = permRF.readLine()) != null) {
				String[] temp = buffer.split("\t");
				permissions.put(temp[0], Integer.parseInt(temp[1]));
			}
			permRF.close();
			permissions.put(username, perm);
			permF.delete();
			permF.createNewFile();
			permRF =new RandomAccessFile(permF, "rw");
			Iterator<Entry<String, Integer>> navi = permissions.entrySet().iterator();
			while(navi.hasNext()) {
				Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>)navi.next();
				permRF.writeBytes(pairs.getKey() + "\t" + pairs.getValue() + "\n");
			}
			permRF.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void revokePermission(String filename, String username) {
		RandomAccessFile permRF = null;
		Map<String, Integer> permissions = new HashMap<String, Integer>();
		try {
			File permF = new File(permRoot + filename);
			permRF = new RandomAccessFile(permF, "rw");
			String buffer;
			while((buffer = permRF.readLine()) != null) {
				String[] temp = buffer.split("\t");
				permissions.put(temp[0], Integer.parseInt(temp[1]));
			}
			permRF.close();
			permissions.remove(username);
			permF.delete();
			permF.createNewFile();
			permRF =new RandomAccessFile(permF, "rw");
			Iterator<Entry<String, Integer>> navi = permissions.entrySet().iterator();
			while(navi.hasNext()) {
				Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>)navi.next();
				permRF.writeBytes(pairs.getKey() + "\t" + pairs.getValue() + "\n");
			}
			permRF.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
