/**
 * 
 */
package mx.randalf.archive.check;

import mx.randalf.archive.info.Archive;

/**
 * @author massi
 *
 */
public abstract class ArchiveImp extends Archive {
	String typeObject = null;

	/**
	 * 
	 */
	public ArchiveImp() {
	}

	public abstract String getID();

	/**
	 * @return the typeObject
	 */
	public String getTypeObject() {
		return typeObject;
	}

	/**
	 * @param typeObject the typeObject to set
	 */
	public void setTypeObject(String typeObject) {
		this.typeObject = typeObject;
	}
}
