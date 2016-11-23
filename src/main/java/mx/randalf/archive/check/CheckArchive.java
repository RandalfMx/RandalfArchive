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
import mx.randalf.archive.check.exception.CheckArchiveException;
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
	public A check(File fInput, File fileTar, Boolean deCompEsito) throws CheckArchiveException {
		return check(fInput, fileTar, false, deCompEsito);
	}

	/**
	 * Metodo utilizzato per iniziare la verifica del File
	 * 
	 * @param fInput File da Veridicare
	 * @return Risultato della ricerca
	 * @throws CheckArchiveException
	 */
	public A check(File fInput, File fileTar, boolean calcImg, Boolean deCompEsito) throws CheckArchiveException {
		A archive = null;
		boolean fileGz = false; 

		try {
			if (deCompEsito == null || !deCompEsito.booleanValue()){
				if (unZip){
					if (fInput.getName().endsWith(".tgz")
							|| fInput.getName().endsWith(".tar.gz")) {
						fileGz = true;
						if (fileTar== null){
							throw new CheckArchiveException("Nome del File Tar non indicato");
	//						fileTar = new File(fInput.getParentFile().getAbsolutePath()
	//								+ File.separator
	//								+ fInput.getName().replace(".tgz", ".tar")
	//										.replace(".tar.gz", ".tar"));
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
				}
			} else {
				fileGz = true;
			}


			if (fileGz){
				this.fileOutput = fileTar;
				archive = scan(fileTar);
				archive.setNome(fileTar.getParentFile().getName()+File.separator+fileTar.getName());
				addDigest(archive, fileTar);

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
				if (fInput.getName().endsWith(".tar")){
					checkTar(fInput, archive.getArchive(), archive, calcImg);
				}
			}
		} catch (FileNotFoundException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (IOException e) {
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
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
	
	@SuppressWarnings("unchecked")
	private void checkTar(File fileTar, List<Archive> list, A archiveTar, boolean calcImg) throws CheckArchiveException{
		Hashtable<String, TarIndexer> indexer;
		TarIndexer tarIndexer;
		Xmltype xmlType = null;
		Image image = null;
		A archive;

		try {
			indexer = Tar.indexer(fileTar, calcImg);
			for (int x=0; x<list.size(); x++){
				archive = (A) list.get(x);
				tarIndexer = indexer.get(archive.getNome());

				if (tarIndexer != null){
					addDigest(archive, tarIndexer.getMd5(), tarIndexer.getSha1(), null);
	
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
					id = new Integer(nextLine[0]);
					idPadre = new Integer(nextLine[1]);
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
//		String[] value = null;
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
			
			if (csvPadri.get(new Integer(first[0]))!= null){
				figli = csvPadri.get(new Integer(first[0]));
				for (int x=0; x<figli.size(); x++){
					archive.getArchive().add(scan(csvResult, csvPadri, figli.get(x)));
				}
			}
//			for(Integer key:keys){
//				value = csvResult.get(key);
//				if (value[1].equals(parent.toString())){
//					archive.getArchive().add(scan(csvResult, new Integer(value[0])));
//				}
//			}
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
			archive.setNome(csv[4]);
			type = new Type();
//		type.setCharset(value);
			if (csv[9] != null &&
					!csv[9].equals("")){
				type.setExt(csv[9]);
			}

			if (csv[16] != null &&
					!csv[16].equals("")){
				format = new Format();
				format.setValue(csv[16]);
				if (csv[17] != null &&
						!csv[17].equals("")){
					format.setVersion(csv[17]);
				}
				type.setFormat(format);
			}

			df = DatatypeFactory.newInstance();
			type.setLastMod(df.newXMLGregorianCalendar(csv[10]));

			type.setPUID(csv[14]);

			if (csv[7]!= null && !csv[7].equals("")){
				type.setSize(new Long(csv[7]));
			} else {
				type.setSize(new Long("0"));
			}

			if (csv[15] != null &&
					!csv[15].equals("")){
				st = csv[15].split(",");
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

//	private A check_old(File fInput, String fileName)
//			throws CheckArchiveException {
//		A archive = null;
//		Type type = null;
//		Path dTmp = null;
//		File fTmp = null;
//		List<File> files = null;
//
//		try {
//			if (fInput.exists()) {
//				archive = initArchive();
//				archive.setNome(fileName);
//				type = checkArchive(fInput);
//				if (type != null) {
//					archive.setType(type);
//					if (type.getMimetype() != null) {
//						if (type.getMimetype().equals(Mimetype.APPLICATION_X_GZIP)) {
//							if (fileName.endsWith(".gz")) {
//								fileName = fileName.replace(".gz", "");
//							} else if (fileName.endsWith(".tgz")) {
//								fileName = fileName.replace(".tgz", ".tar");
//							}
//							fTmp = File.createTempFile("CheckArchive-", ".tmp");
//							Gzip.decompress(fInput, fTmp);
//							archive.getArchive().add(check(fTmp, fileName));
//							fTmp.delete();
//						} else if (type.getMimetype().equals(
//								Mimetype.APPLICATION_X_TAR)) {
//							dTmp = Files.createTempDirectory("CheckArchive-");
//							files = Tar.decompress(fInput, dTmp.toFile());
//							for (int x = 0; x < files.size(); x++) {
//								archive.getArchive()
//										.add(check(
//												files.get(x),
//												fileName
//														+ "#"
//														+ files.get(x)
//																.getAbsolutePath()
//																.replace(
//																		dTmp.toFile()
//																				.getAbsolutePath(),
//																		"")));
//								files.get(x).delete();
//								if (files.get(x).getParentFile().listFiles().length == 0) {
//									files.get(x).getParentFile().delete();
//								}
//							}
//							dTmp.toFile().delete();
//						} else if (type.getMimetype().equals(
//								Mimetype.APPLICATION_XML)) {
//							checkXml(fInput, type);
//						}
//					}
//				}
//			}
//		} catch (CheckArchiveException e) {
//			throw e;
//		} catch (IOException e) {
//			throw new CheckArchiveException(e.getMessage(), e);
//		} catch (ArchiveException e) {
//			throw new CheckArchiveException(e.getMessage(), e);
//		}
//		return archive;
//	}

//	private void checkXml(File fXml, Type type) throws CheckArchiveException{
//		FileReader fr = null;
//		BufferedReader br = null;
//		FileInputStream fis = null;
//		String line = null;
//		Parser parser = null;
//		ParserException pException = null;
//
//		try {
//			fr = new FileReader(fXml);
//			br = new BufferedReader(fr);
//			br.readLine();
//
//			line = br.readLine();
//			if (line.trim().toLowerCase().startsWith("<mets:mets")){
//				type.setXmltype(Xmltype.METS);
//			} else if(line.trim().toLowerCase().startsWith("<metadigit")){
//				type.setXmltype(Xmltype.MAG);
//			}
//
//			fis = new FileInputStream(fXml);
//			
//			pException = new ParserException();
//			parser = new Parser(fis, pException, false);
//			if (parser != null){
//				type.setXmlvalid(pException.getNumErr()==0);
//			}
//		} catch (FileNotFoundException e) {
//			log.error(e.getMessage(), e);
//			throw new CheckArchiveException(e.getMessage(), e);
//		} catch (IOException e) {
//			log.error(e.getMessage(), e);
//			throw new CheckArchiveException(e.getMessage(), e);
//		} catch (PubblicaException e) {
//			log.error(e.getMessage(), e);
//			throw new CheckArchiveException(e.getMessage(), e);
//		} finally {
//			try {
//				if (br != null){
//					br.close();
//				}
//				if (fr != null){
//					fr.close();
//				}
//				if (fis != null){
//					fis.close();
//				}
//			} catch (IOException e) {
//				log.error(e.getMessage(), e);
//				throw new CheckArchiveException(e.getMessage(), e);
//			}
//		}
//	}

//	private Type checkArchive(File fInput) throws CheckArchiveException {
//		Type result = null;
//		Runtime rt = null;
//		Process process = null;
//		InputStreamReader isrInput = null;
//		InputStreamReader isrError = null;
//		BufferedReader brInput = null;
//		BufferedReader brError = null;
//		String line = null;
//		String msgErr = "";
////		String[] st = null;
//		int pos = 0;
//		String fileName = null;
//		String mimeType = null;
//		String charSet = null;
//		Mimetype mimetype = null;
//		Charset charset = null;
//
//		try {
//			rt = Runtime.getRuntime();
//			process = rt.exec("file --mime-encoding --mime-type " + fInput.getAbsolutePath());
//			process.waitFor();
//
//			isrError = new InputStreamReader(process.getErrorStream());
//			brError = new BufferedReader(isrError);
//			while ((line = brError.readLine()) != null) {
//				msgErr += (msgErr.equals("") ? "" : "\n") + line;
//			}
//			if (!msgErr.endsWith("")) {
//				throw new CheckArchiveException(msgErr);
//			}
//
//			isrInput = new InputStreamReader(process.getInputStream());
//			brInput = new BufferedReader(isrInput);
//			line = brInput.readLine();
//
//			pos = line.indexOf(":");
//			fileName = line.substring(0,pos).trim();
//			line = line.substring(pos+1);
//
//			pos = line.lastIndexOf("charset");
//			if (pos >-1){
//				mimeType = line.substring(0,pos).trim();
//				charSet = line.substring(pos).trim();
//			} else{
//				mimeType = line.trim();
//			}
//
//			if (mimeType.endsWith(";")){
//				mimeType = mimeType.substring(0, mimeType.length()-1);
//			}
//			result = new Type();
//
//			if (mimeType.startsWith("ERROR")){
//				result.setMsgError(mimeType);
//			} else {
//				try {
//					mimetype = Mimetype.fromValue(mimeType);
//					if (mimetype != null) {
//						result.getMimetype().add(mimetype);
//						if (charSet != null){
//							try {
//								charset = Charset.fromValue(charSet.split("=")[1].trim());
//								if (charset != null) {
//									result.setCharset(charset);
//								} else {
//									log.error("Il charset [" + charSet
//											+ "] non \u00E3 gestita ");
//								}
//							} catch (IllegalArgumentException e) {
//								log.error("Il charset [" + charSet
//										+ "] non \u00E3 gestita ");
//							}
//						}
//					} else {
//						result.setMsgError("La tipologia [" + mimeType
//								+ "] non \u00E8 gestita ");
//						log.error("La tipologia [" + mimeType
//								+ "] non \u00E8 gestita ");
//					}
//				} catch (IllegalArgumentException e) {
//					result.setMsgError("La tipologia [" + mimeType
//							+ "] non \u00E8 gestita ");
//					log.error("La tipologia [" + mimeType
//							+ "] non \u00E8 gestita ");
//				}
//	
//			}
//		} catch (IOException e) {
//			throw new CheckArchiveException(e.getMessage(), e);
//		} catch (InterruptedException e) {
//			throw new CheckArchiveException(e.getMessage(), e);
//		} catch (CheckArchiveException e) {
//			throw e;
//		} finally {
//			try {
//				if (brError != null) {
//					brError.close();
//				}
//				if (isrError != null) {
//					isrError.close();
//				}
//				if (brInput != null) {
//					brInput.close();
//				}
//				if (isrInput != null) {
//					isrInput.close();
//				}
//			} catch (IOException e) {
//				throw new CheckArchiveException(e.getMessage(), e);
//			}
//		}
//		return result;
//	}

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
