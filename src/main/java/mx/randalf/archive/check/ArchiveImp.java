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
	String protocol = null;

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

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
}
