/**
 * 
 */
package mx.randalf.archive.check;

import java.io.File;

import mx.randalf.archive.info.Archive;
import mx.randalf.xsd.ReadXsd;
import mx.randalf.xsd.exception.XsdException;

/**
 * @author massi
 *
 */
public class ArchiveXsd extends ReadXsd<Archive> {

	/**
	 * 
	 */
	public ArchiveXsd() {
	}

	public void write(Archive archive, File fArchive) throws XsdException{
		this.write(archive, fArchive, new ArchiveNpm(), null, null, null);
	}
}
