package mx.randalf.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class Gzip {

	public Gzip() {
	}

	public static void decompress(File input, File output) throws FileNotFoundException,IOException {
		FileInputStream fileStream = null;
		GZIPInputStream zip = null;
		FileOutputStream s = null;
		int lunghezza;
		long freeMem;
		byte[] buffer;

		try {
			if (input.exists()){
				fileStream = new FileInputStream(input);
				zip = new GZIPInputStream(fileStream);

				if (!output.getParentFile().exists()){
					if (!output.getParentFile().mkdirs()){
						throw new FileNotFoundException("Riscontrato un problema nella creazione della cartella ["+output.getParentFile().getAbsolutePath()+"]");
					}
				}
				s = new FileOutputStream(output);
	
				freeMem = Runtime.getRuntime().freeMemory();
				freeMem = freeMem/2;
				buffer = new byte[(int) freeMem];
				while ((lunghezza = zip.read(buffer)) > 0) {
					s.write(buffer, 0, lunghezza);
				}
			} else {
				throw new FileNotFoundException("Il file ["+input.getAbsolutePath()+"] non esiste");
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (zip != null){
					zip.close();
				}
				if (s != null){
					s.flush();
					s.close();
				}
				if (fileStream != null){
					fileStream.close();
				}
			} catch (IOException e) {
				throw e;
			}
		}
	}
}
