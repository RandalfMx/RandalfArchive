/**
 * 
 */
package mx.randalf.archive.check;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.compress.archivers.ArchiveException;
import org.im4java.core.InfoException;

import mx.randalf.archive.Tar;
import mx.randalf.archive.TarIndexer;
import mx.randalf.xsd.exception.XsdException;

/**
 * @author massi
 *
 */
public class TarTest {

	/**
	 * 
	 */
	public TarTest() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Hashtable<String, TarIndexer> ris = null;
		TarIndexer tarIndexer = null;
		Enumeration<String> keys = null;
		String key = null;
		String file = "/Users/massi/bin/droid/pippo/CF000265884.tar";
		Tar tar = null;
		
		try {
			tar = new Tar() {
				
				@Override
				protected void validateXsd(File fXml, String ris) throws XsdException {
					// TODO Auto-generated method stub
					
				}
			};
			ris = tar.indexer(new File(file), false);
			keys = ris.keys();
			while (keys.hasMoreElements()){
				key = keys.nextElement();
				tarIndexer = ris.get(key);
				System.out.print("Name: "+tarIndexer.getName());
				System.out.print("\tOffset: "+tarIndexer.getOffset());
				System.out.print("\tSize: "+tarIndexer.getSize());
				System.out.print("\tSHA-1: "+tarIndexer.getSha1());
				System.out.print("\tMD5: "+tarIndexer.getMd5());
				if (tarIndexer.getXmlType() != null){
					System.out.print("\tXmlType: "+tarIndexer.getXmlType());
				}
				System.out.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ArchiveException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InfoException e) {
			e.printStackTrace();
		} catch (XsdException e) {
			e.printStackTrace();
		}
	}
}
