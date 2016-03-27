/**
 * 
 */
package mx.randalf.archive.check;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * @author massi
 *
 */
public class ArchiveNpm extends NamespacePrefixMapper {

	/**
	 * 
	 */
	public ArchiveNpm() {
	}

	/**
	 * @see com.sun.xml.bind.marshaller.NamespacePrefixMapper#getPreferredPrefix(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		if (namespaceUri.equals("http://www.randalf.mx/archive/info")){
			return "mxArchive";
		} else {
			System.out.println("namespaceUri: "+namespaceUri+"\tsuggestion: "+suggestion+"\trequirePrefix: "+requirePrefix);
			return null;
		}
	}

}
