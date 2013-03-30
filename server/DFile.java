package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class DFile extends RandomAccessFile{
	public static final String filesRoot = Server.serverRoot + "Files/";
	
	public DFile(String name, String mode) throws FileNotFoundException {
		super(Server.serverRoot + "/Files/" + name, mode);
	}

	public DFile(File testingF, String mode) throws FileNotFoundException {
		super(testingF, mode);
	}


}