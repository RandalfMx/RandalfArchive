/**
 * 
 */
package mx.randalf.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author massi
 *
 */
public class ZipTest {

	/**
	 * 
	 */
	public ZipTest() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if (args.length==2) {
				Zip.convertTar(new File(args[0]), new File(args[1]), new File("/usr/bin/tar"));
			} else {
				System.out.println("Indicare il nome cel file Zip e il nome del file Tar");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
