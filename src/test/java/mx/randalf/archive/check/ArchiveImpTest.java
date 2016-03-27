/**
 * 
 */
package mx.randalf.archive.check;

import java.util.UUID;

/**
 * @author massi
 *
 */
public class ArchiveImpTest extends ArchiveImp {

	/**
	 * 
	 */
	public ArchiveImpTest() {
	}

	/**
	 * 
	 * @see mx.randalf.archive.check.ArchiveImp#getID()
	 */
	@Override
	public String getID() {
		return UUID.randomUUID().toString();
	}

}
