/**
 * 
 */
package mx.randalf.archive.check;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import mx.randalf.archive.Gzip;
import mx.randalf.archive.Tar;
import mx.randalf.archive.TarIndexer;
import mx.randalf.archive.check.droid.CheckDroid;
import mx.randalf.archive.check.droid.DroidKey;
import mx.randalf.archive.check.exception.CheckArchiveException;
import mx.randalf.archive.check.warc.CheckWarc;
import mx.randalf.archive.info.Archive;
import mx.randalf.archive.info.DigestType;
import mx.randalf.archive.info.Type;
import mx.randalf.archive.info.Type.Digest;
import mx.randalf.archive.info.Type.Format;
import mx.randalf.archive.info.Type.Image;
import mx.randalf.archive.info.Xmltype;
import mx.randalf.tools.MD5Tools;
import mx.randalf.tools.SHA1Tools;
import mx.randalf.tools.SHA256Tools;
import mx.randalf.tools.Utils;
import mx.randalf.tools.exception.UtilException;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.log4j.Logger;
import org.im4java.core.InfoException;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Classe utilizzata per analizzare un file e la ricostruzione dei tipi di
 * archivio di cui è composto
 * 
 * @author massi
 * 
 */
public abstract class CheckArchive<A extends ArchiveImp> {

	/**
	 * Variabile utilizzata per loggare l'applicazione
	 */
	private Logger log = Logger.getLogger(getClass());

	/**
	 * Variabile utilizzata per indicare se decomprimere un file Zippato
	 */
	private boolean unZip = false;

	/**
	 * Variabile utilizzata per indicare se cancellare il file originale che è stato decompresso
	 */
	private boolean removeOrgin = false;

	/**
	 * Variabile utilizzata per indicare la posizione del file Droid
	 */
	private String fileDroid = null;

	/**
	 * Variabile utilizzata per indicare la data in cui inizia la procedura di unzip dei File
	 */
	private GregorianCalendar gcUnzipStart = null;

	/**
	 * Variabile utilizzata per indicare la data in cui inizia la procedura di unzip dei File
	 */
	private String[] unzipError = null;

	/**
	 * Variabile utilizzata per indicare la data in cui fine la procedura di unzip dei File
	 */
	private GregorianCalendar gcUnzipStop = null;

	private File fileOutput = null;

	/**
	 * Costruttore
	 */
	public CheckArchive(String fileDroid) {
		this.fileDroid = fileDroid;
	}

	/**
	 * Metodo da implementare per l'inizializzare l'oggetto Archive
	 * @return
	 */
	public abstract A initArchive();

	/**
	 * Metodo utilizzato per iniziare la verifica del File
	 * 
	 * @param fInput File da Veridicare
	 * @return Risultato della ricerca
	 * @throws CheckArchiveException
	 */
	public A check(File fInput, File fileTar, Boolean deCompEsito, boolean decompressRequired) throws CheckArchiveException {
		return check(fInput, fileTar, false, deCompEsito, decompressRequired);
	}

	/**
	 * Metodo utilizzato per iniziare la verifica del File
	 * 
	 * @param fInput File da Veridicare
	 * @return Risultato della ricerca
	 * @throws CheckArchiveException
	 */
	public A check(File fInput, File fileTar, boolean calcImg, Boolean deCompEsito, boolean decompressRequired) 
			throws CheckArchiveException {
		A archive = null;
		boolean fileGz = false; 

		try {
			if (deCompEsito == null || !deCompEsito.booleanValue()){
				if (unZip){
					if (decompressRequired){
						if (fInput.getName().endsWith(".tgz")
								|| fInput.getName().endsWith(".tar.gz")
								|| fInput.getName().endsWith(".gz")) {
							fileGz = true;
							if (fileTar== null){
								throw new CheckArchiveException("Nome del File Tar non indicato");
							}
							gcUnzipStart = new GregorianCalendar();
							try {
								Gzip.decompress(fInput, fileTar);
							} catch (FileNotFoundException e) {
								unzipError = new String[] {
										e.getMessage()
								};
								throw e;
							} catch (IOException e) {
								unzipError = new String[] {
										e.getMessage()
								};
								throw e;
							} finally {
								gcUnzipStop = new GregorianCalendar();
							}
						}
					} else if (fInput.getName().endsWith(".tar")
							&& fileTar != null) {
						if (!fInput.getAbsolutePath().equals(fileTar.getAbsolutePath())){
							if (!Utils.copyFileValidate(fInput.getAbsolutePath(), fileTar.getAbsolutePath(), false)){
								throw new CheckArchiveException("Problema nella copia del file ["+fInput.getAbsolutePath()+
										"] in ["+fileTar.getAbsolutePath()+"]");
							}
						}
						fileGz = true;
					}
				}
			} else {
				fileGz = true;
			}


			if (fileGz){
				this.fileOutput = fileTar;
				archive = scan(fileTar);
				archive.setNome(fileTar.getParentFile().getName()+File.separator+fileTar.getName());
				addDigest(archive, fileTar);
				if (fileTar.getName().endsWith(".warc")){
					archive.setXmltype(Xmltype.WARC);
					archive.setXmlvalid(Boolean.TRUE);
				}

				checkTar(fileTar, archive.getArchive(), archive, calcImg);
				if (removeOrgin){
					if (!fInput.delete()){
						throw new CheckArchiveException("Riscontrato un problema nella cancellazione del file ["+fInput.getAbsolutePath()+"]");
					}
				}
			}else {
				this.fileOutput = fInput;
				archive = scan(fInput);
				archive.setNome((fileTar==null?fInput.getAbsolutePath():fileTar.getParentFile().getName()+File.separator+fInput.getName()));
				addDigest(archive, fInput);
				if (fileTar != null &&
						fileTar.getName() != null &&
						fileTar.getName().endsWith(".warc")){
					archive.setXmltype(Xmltype.WARC);
					archive.setXmlvalid(Boolean.TRUE);
				}

				if (fInput.getName().endsWith(".tar") || fInput.getName().endsWith(".warc")){
					checkTar(fInput, archive.getArchive(), archive, calcImg);
				}
			}
		} catch (FileNotFoundException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (IOException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (UtilException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		}
		return archive;
	}

	private void addDigest(A archive, File file) throws FileNotFoundException, NoSuchAlgorithmException, IOException{
		String md5 = null;
		String sha1 = null;
		String sha256 = null;
		try{
			md5 = MD5Tools.readMD5File(file.getAbsolutePath());
			sha1 = SHA1Tools.readMD5File(file.getAbsolutePath());
			sha256 = SHA256Tools.readMD5(file.getAbsolutePath());
			addDigest(archive, md5, sha1, sha256);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (NoSuchAlgorithmException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}

	private void addDigest(A archive, String md5, String sha1, String sha256) {
		Digest digest;

			if (md5 != null){
				digest = new Digest();
				digest.setType(DigestType.MD_5);
				digest.setValue(md5);
				archive.getType().getDigest().add(digest);
			}

			if (sha1 != null){
				digest = new Digest();
				digest.setType(DigestType.SHA_1);
				digest.setValue(sha1);
				archive.getType().getDigest().add(digest);
			}

			if (sha256 != null){
				digest = new Digest();
				digest.setType(DigestType.SHA_256);
				digest.setValue(sha256);
				archive.getType().getDigest().add(digest);
			}

	}
	
	private void checkTar(File fileTar, List<Archive> list, A archiveTar, boolean calcImg) throws CheckArchiveException{
		Hashtable<String, TarIndexer> indexer = null;

		try {
			if (fileTar.getName().endsWith(".tar")){
				indexer = Tar.indexer(fileTar, calcImg);
			} else if (fileTar.getName().endsWith(".warc")){
				indexer = CheckWarc.indexer(fileTar, calcImg);
			}
			if (indexer != null){
				checkTar(null, list, indexer, archiveTar, fileTar.getName().endsWith(".warc"));
			}
		} catch (FileNotFoundException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (ArchiveException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (IOException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (InfoException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private void checkTar(String folder, List<Archive> list, Hashtable<String, TarIndexer> indexer, A archiveTar, boolean isWarc){
		TarIndexer tarIndexer;
		A archive;
		String search = null;
		Xmltype xmlType = null;
		Image image = null;

		for (int x=0; x<list.size(); x++){
			archive = (A) list.get(x);
			if (folder ==null){
				search = archive.getNome();
			} else {
				search = folder+File.separator+archive.getNome();
			}
			if (isWarc){
				tarIndexer = indexer.get("http://"+search);
				if (tarIndexer == null){
					tarIndexer = indexer.get("https://"+search);
					if (tarIndexer != null){
						archive.setProtocol("https://");
					}
				} else {
					archive.setProtocol("http://");
				}
			} else {
				tarIndexer = indexer.get(search);
			}

			if (tarIndexer != null){
				addDigest(archive, tarIndexer.getMd5(), tarIndexer.getSha1(), tarIndexer.getSha256());

				if (tarIndexer.getXmlType()!=null){
					xmlType = Xmltype.fromValue(tarIndexer.getXmlType());
					archiveTar.setXmltype(xmlType);
					archiveTar.setXmlvalid(Boolean.TRUE);
					archive.setXmltype(xmlType);
					archive.setXmlvalid(Boolean.TRUE);
				}
				if (tarIndexer.getImageLength()!= null &&
						tarIndexer.getImageWidth() != null){
					image = new Image();
					image.setHeight(tarIndexer.getImageLength().intValue());
					image.setWidth(tarIndexer.getImageWidth().intValue());
					if (tarIndexer.getDpi() != null){
						image.setPpi(tarIndexer.getDpi().intValue());
					}
					archive.getType().setImage(image);
				}
				archive.getType().setContentLocation(Long.toString(tarIndexer.getOffset()));
			}
			
			if (archive.getArchive() != null && 
					archive.getArchive().size()>0){
				checkTar(search, archive.getArchive(), indexer, archiveTar, isWarc);
			}
		}
	}
	
	private A scan(File fInput)
			throws CheckArchiveException {
		A archive = null;
		File fileCsv = null;
		CheckDroid checkDroid = null;
		CSVReader csvReader = null;
		FileReader fr = null;
		String[] nextLine;
		Hashtable<Integer, String[]> csvResult = null;
		Hashtable<Integer, Vector<Integer>> csvPadri = null;
		int idPadre = 0;
		int id = 0;

		try {
			checkDroid = new CheckDroid(fileDroid);
			fileCsv = checkDroid.check(fInput);

//			System.out.println("FileCsv: "+fileCsv);
			fr = new FileReader(fileCsv);
			csvReader = new CSVReader(fr);
			csvResult = new Hashtable<Integer, String[]>();
			csvPadri = new Hashtable<Integer, Vector<Integer>>();
			while ((nextLine = csvReader.readNext()) != null) {
				if (!nextLine[0].equals("ID")){
					id = new Integer(nextLine[DroidKey.ID.value()]);
					idPadre = new Integer(nextLine[DroidKey.PARENT_ID.value()]);
					csvResult.put(id, nextLine);
					if (csvPadri.get(idPadre)== null){
						csvPadri.put(idPadre, new Vector<Integer>());
					}
					csvPadri.get(idPadre).add(id);
				}
			}
			
			archive = scan(csvResult, csvPadri, new Integer("1"));
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			throw new CheckArchiveException(e.getMessage(),e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new CheckArchiveException(e.getMessage(),e);
		} catch (CheckArchiveException e) {
			throw e;
		} finally {
			try {
				if (csvReader != null){
					csvReader.close();
				}
				if (fileCsv != null){
					if (!fileCsv.delete()){
						throw new CheckArchiveException("Riscontrato un problema nella cancellazione del file ["+fileCsv.getAbsolutePath()+"]");
					}
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				throw new CheckArchiveException(e.getMessage(),e);
			}
		}
		return archive;
	}

	private A scan(Hashtable<Integer, String[]> csvResult, Hashtable<Integer, Vector<Integer>> csvPadri, Integer parent) throws CheckArchiveException{
		A archive = null;
		List<Integer> keys = null;
		String[] first = null;
		Vector<Integer> figli = null;

		try {
			keys = Collections.list(csvResult.keys());
			Collections.sort(keys);

			first = csvResult.get(parent);
			if (first == null){
				parent =keys.get(0);
				first = csvResult.get(keys.get(0));
			}
			archive = initArchive(first);
			
			
			if (csvPadri.get(new Integer(first[DroidKey.ID.value()]))!= null){
				figli = csvPadri.get(new Integer(first[DroidKey.ID.value()]));
				for (int x=0; x<figli.size(); x++){
					archive.getArchive().add(scan(csvResult, csvPadri, figli.get(x)));
				}
			}
		} catch (NumberFormatException e) {
			log.error(e.getMessage(), e);
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (CheckArchiveException e) {
			throw e;
		}
		return archive;
	}

	private A initArchive(String[] csv) throws CheckArchiveException{
		A archive = null;
		Type type = null;
		Format format = null;
		DatatypeFactory df = null;
		String[] st = null;

		try {
			archive = initArchive();
			archive.setNome(csv[DroidKey.NAME.value()]);
			if (csv[DroidKey.TYPE.value()] != null &&
					!csv[DroidKey.TYPE.value()].equals("")){
				archive.setTypeObject(csv[DroidKey.TYPE.value()]);
			}
			type = new Type();
//		type.setCharset(value);
			if (csv[DroidKey.EXT.value()] != null &&
					!csv[DroidKey.EXT.value()].equals("")){
				type.setExt(csv[DroidKey.EXT.value()]);
			}

			if (csv[DroidKey.FORMAT_NAME.value()] != null &&
					!csv[DroidKey.FORMAT_NAME.value()].equals("")){
				format = new Format();
				format.setValue(csv[DroidKey.FORMAT_NAME.value()]);
				if (csv[DroidKey.FORMAT_VERSION.value()] != null &&
						!csv[DroidKey.FORMAT_VERSION.value()].equals("")){
					format.setVersion(csv[DroidKey.FORMAT_VERSION.value()]);
				}
				type.setFormat(format);
			}

			if (csv[DroidKey.LAST_MODIFIED.value()] != null &&
					!csv[DroidKey.LAST_MODIFIED.value()].trim().equals("")){
				df = DatatypeFactory.newInstance();
				type.setLastMod(df.newXMLGregorianCalendar(csv[DroidKey.LAST_MODIFIED.value()]));
			}

			type.setPUID(csv[DroidKey.PUID.value()]);

			if (csv[DroidKey.SIZE.value()]!= null && !csv[DroidKey.SIZE.value()].equals("")){
				type.setSize(new Long(csv[DroidKey.SIZE.value()]));
			} else {
				type.setSize(new Long("0"));
			}

			if (csv[DroidKey.MIME_TYPE.value()] != null &&
					!csv[DroidKey.MIME_TYPE.value()].equals("")){
				st = csv[DroidKey.MIME_TYPE.value()].split(",");
				for (int x=0; x<st.length; x++){
					type.getMimetype().add(st[x].trim());
				}
			}
			archive.setType(type);
		} catch (NumberFormatException e) {
			log.error(e.getMessage(),e);
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (DatatypeConfigurationException e) {
			log.error(e.getMessage(),e);
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new CheckArchiveException(e.getMessage(), e);
		}

		return archive;
	}

	public boolean isUnZip() {
		return unZip;
	}

	public void setUnZip(boolean unZip) {
		this.unZip = unZip;
	}

	public boolean isRemoveOrgin() {
		return removeOrgin;
	}

	public void setRemoveOrgin(boolean removeOrgin) {
		this.removeOrgin = removeOrgin;
	}

	/**
	 * @return the gcUnzipStart
	 */
	public GregorianCalendar getGcUnzipStart() {
		return gcUnzipStart;
	}

	/**
	 * @return the gcUnzipStop
	 */
	public GregorianCalendar getGcUnzipStop() {
		return gcUnzipStop;
	}

	/**
	 * @return the gcUnzipError
	 */
	public String[] getUnzipError() {
		return unzipError;
	}

	public File getFileOutput() {
		return fileOutput;
	}
}
