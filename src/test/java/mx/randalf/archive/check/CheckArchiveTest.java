/**
 * 
 */
package mx.randalf.archive.check;

import java.io.File;

import mx.randalf.archive.Tar;
import mx.randalf.archive.check.exception.CheckArchiveException;

/**
 * @author massi
 *
 */
public class CheckArchiveTest {

	/**
	 * 
	 */
	public CheckArchiveTest() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CheckArchive<ArchiveImpTest, Tar> check = null;
		ArchiveImpTest archive = null;
		
		try {
			check = new CheckArchive<ArchiveImpTest, Tar>("/Users/massi/bin/droid/droid.sh") {
				
				@Override
				public ArchiveImpTest initArchive() {
					return new ArchiveImpTest();
				}

				@Override
				public Tar initTar() {
					return null;
				}
			};
			check.setUnZip(true);
			archive = check.check(new File("/mnt/volume1/tmp/test/20140924-unimi.amonline.warc.gz"), 
					new File("/mnt/volume1/tmp/test/20140924-unimi.amonline.warc"),
					null,true, null, null);
			
			if (archive != null){
				printArchive(archive, "");
			}
		} catch (CheckArchiveException e) {
			e.printStackTrace();
		}
	}

	public static void printArchive(ArchiveImpTest archive, String prefix){
		System.out.print(prefix+"Nome: "+archive.getNome());
//		System.out.print("\tID: "+archive.getID());
		if (archive.getType() != null){
			if (archive.getType().getMimetype() != null &&
					archive.getType().getMimetype().size()>0){
				System.out.print("\tType: ");
				for (int x=0;x<archive.getType().getMimetype().size(); x++){
					System.out.print((x==0?"":",")+archive.getType().getMimetype().get(x));
				}
			}
			
			if (archive.getType().getDigest() != null &&
					archive.getType().getDigest().size()>0){
				System.out.print("\tDigest: ");
				for (int x=0; x<archive.getType().getDigest().size(); x++){
					System.out.print((x==0?"":","));
					System.out.print(archive.getType().getDigest().get(x).getType().value()+
							"-"+archive.getType().getDigest().get(x).getValue());
				}
			}
//			
//			if (archive.getType().getExt() != null){
//				System.out.print("\tExt: "+archive.getType().getExt());
//			}
//			
//			if (archive.getType().getFormat() != null){
//				System.out.print("\tFormat: "+archive.getType().getFormat().getValue());
//				if (archive.getType().getFormat().getVersion() != null){
//					System.out.print("\tFormat Version: "+archive.getType().getFormat().getVersion());
//				}
//			}
//			
//			if (archive.getType().getLastMod() != null){
//				System.out.print("\tLast Mod: "+archive.getType().getLastMod().toString());
//			}
//			
//			if (archive.getType().getPUID() != null){
//				System.out.print("\tPUID: "+archive.getType().getPUID());
//			}
//			
			if (archive.getType().getSize() != null){
				System.out.print("\tSize: "+archive.getType().getSize());
			}
//			
//			if (archive.getType().getCharset() != null){
//				System.out.print("\tCharset: "+archive.getType().getCharset().value());
//			}
			if (archive.getXmltype() != null){
				System.out.print("\tXmlType: "+archive.getXmltype().value());
			}
			if (archive.isXmlvalid() != null){
				System.out.print("\tXmlValid: "+archive.isXmlvalid().booleanValue());
			}
//			if (archive.getType().getMsgError() != null){
//				System.out.print("\tERROR: "+archive.getType().getMsgError());
//			}
			if (archive.getType().getContentLocation() != null){
				System.out.print("\tContent Location: "+archive.getType().getContentLocation());
			}

		}
		System.out.println();
		if (archive.getArchive() != null && archive.getArchive().size()>0){
			System.out.println("-------------------------------");
			prefix+="\t";
			for (int x=0; x<archive.getArchive().size(); x++){
				printArchive((ArchiveImpTest) archive.getArchive().get(x), prefix);
			}
		}
	}
}
