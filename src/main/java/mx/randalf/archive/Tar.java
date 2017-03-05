/**
 * 
 */
package mx.randalf.archive;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.im4java.core.InfoException;

import mx.randalf.archive.exception.TarException;
import mx.randalf.archive.info.Xmltype;
import mx.randalf.digital.img.reader.CalcImg;
import mx.randalf.tools.MD5Tools;
import mx.randalf.tools.SHA1Tools;
import mx.randalf.tools.SHA256Tools;
import mx.randalf.xsd.exception.XsdException;

/**
 * @author massi
 * 
 */
public abstract class Tar {

	private static Logger log = Logger.getLogger(Tar.class);

	/**
	 * 
	 */
	public Tar() {
	}

	public List<File> decompress(File inputFile, File outputDir)
			throws FileNotFoundException, IOException, ArchiveException {

		log.info(String.format("Untaring %s to dir %s.",
				inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

		List<File> untaredFiles = new LinkedList<File>();
		InputStream is = new FileInputStream(inputFile);
		TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory()
				.createArchiveInputStream("tar", is);
		TarArchiveEntry entry = null;
		while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
			File outputFile = new File(outputDir, entry.getName());
			if (entry.isDirectory()) {
				log.info(String.format(
						"Attempting to write output directory %s.",
						outputFile.getAbsolutePath()));
				if (!outputFile.exists()) {
					log.info(String.format(
							"Attempting to create output directory %s.",
							outputFile.getAbsolutePath()));
					if (!outputFile.mkdirs()) {
						throw new IllegalStateException(String.format(
								"Couldn't create directory %s.",
								outputFile.getAbsolutePath()));
					}
				}
			} else {
				log.info(String.format("Creating output file %s.",
						outputFile.getAbsolutePath()));
				OutputStream outputFileStream = new FileOutputStream(outputFile);
				IOUtils.copy(debInputStream, outputFileStream);
				outputFileStream.close();
			}
			untaredFiles.add(outputFile);
		}
		debInputStream.close();

		return untaredFiles;
	}

	public Hashtable<String, TarIndexer> indexer(File fileTar, boolean calcImg) throws FileNotFoundException,
			ArchiveException, IOException, NoSuchAlgorithmException, InfoException, XsdException {
		InputStream is = null;
		TarArchiveInputStream debInputStream = null;
		TarArchiveEntry entry = null;
		File dTmp = null;
		File fTmp = null;
		Hashtable<String, TarIndexer> ris = null;
		TarIndexer tarIndexer = null;

		try {
			dTmp = Files.createTempDirectory("TarIndexer-").toFile();
			dTmp.mkdirs();
			is = new FileInputStream(fileTar);
			debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory()
					.createArchiveInputStream("tar", is);

			ris = new Hashtable<String, TarIndexer>();
			while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
				tarIndexer = new TarIndexer();

				tarIndexer.setName(entry.getName());
				tarIndexer.setOffset(debInputStream.getBytesRead());
				tarIndexer.setSize(entry.getSize());

				fTmp = new File(dTmp.getAbsolutePath() + File.separator
						+ entry.getName());
				if (!entry.isDirectory()){
					if (!fTmp.getParentFile().exists()){
						if (!fTmp.getParentFile().mkdirs()){
							throw new FileNotFoundException("Problemi nella creazione della cartella ["+fTmp.getParentFile().getAbsolutePath()+"]");
						}
					}
					OutputStream outputFileStream = new FileOutputStream(fTmp);
					IOUtils.copy(debInputStream, outputFileStream);
					outputFileStream.flush();
					outputFileStream.close();

					tarIndexer.setSha1(SHA1Tools.readMD5File(fTmp.getAbsolutePath()));
					tarIndexer.setMd5(MD5Tools.readMD5File(fTmp.getAbsolutePath()));
					tarIndexer.setSha256(SHA256Tools.readMD5(fTmp.getAbsolutePath()));

					if (calcImg && isImg(fTmp.getName().toLowerCase())){
						calcImg(fTmp, tarIndexer);
					}
					if (fTmp.getName().toLowerCase().endsWith(".xml") ||
							fTmp.getName().toLowerCase().endsWith(".premis")) {
//						System.out.print("File: "+fTmp.getName());
						tarIndexer.setXmlType(checkXml(fTmp));
//						System.out.println(" XmlType: "+tarIndexer.getXmlType());
					}
					if (fTmp.getName().toLowerCase().equals("bag-info.txt")){
						tarIndexer.setXmlType(Xmltype.BAGIT.value());
						tarIndexer.setIdDepositante(checkBagInfo(fTmp));
					}
					fTmp.delete();
				}
				ris.put(entry.getName(), tarIndexer);
			}
			dTmp.delete();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (ArchiveException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (NoSuchAlgorithmException e) {
			throw e;
		} catch (InfoException e) {
			throw e;
		} catch (XsdException e) {
			throw e;
		} finally {
			try {
				if (debInputStream != null){
					debInputStream.close();
				}
			} catch (IOException e) {
				throw e;
			}
		}
		return ris;
	}

	private String checkBagInfo(File fTmp) throws FileNotFoundException, IOException { 
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		String idDepositante = null;

		try {
			fr = new FileReader(fTmp);
			br = new BufferedReader(fr);
			while ((line =br.readLine())!= null){
				if (line.trim().startsWith("BNCF-user-id:")){
					idDepositante = line.replace("BNCF-user-id:", "").trim();
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}finally {
			try {
				if (br != null){
					br.close();
				}
				if(fr != null){
					fr.close();
				}
			} catch (IOException e) {
				throw e;
			}
		}
		return idDepositante;
	}

	private void calcImg(File fImg, TarIndexer tarIndexer) throws InfoException{
		CalcImg calcImg = null;

		try {
			calcImg = new CalcImg(fImg);
			log.debug("fImg: "+fImg.getAbsolutePath());
			log.debug("imageLength: "+calcImg.getImageLength());
			log.debug("getImageWidth: "+calcImg.getImageWidth());
			log.debug("getDpi: "+calcImg.getDpi());
			tarIndexer.setImageLength(calcImg.getImageLength());
			tarIndexer.setImageWidth(calcImg.getImageWidth());
			tarIndexer.setDpi(calcImg.getDpi());
		} catch (InfoException e) {
			throw e;
		}
	}

	private boolean isImg(String file){
		boolean ris = false;
		if (file.endsWith(".jp2") ||
				file.endsWith(".jpg") ||
				file.endsWith(".tif") ||
				file.endsWith(".png") ||
				file.endsWith(".ico") ||
				file.endsWith(".gif")){
			ris  = true;
		}
		return ris;
	}

	private String checkXml(File fXml) throws FileNotFoundException, IOException, XsdException {
		FileReader fr = null;
		BufferedReader br = null;
		String ris = null;
		String line = null;
		String firstLine = null;
		int pos = 0;

		try {
			fr = new FileReader(fXml);
			br = new BufferedReader(fr);
			firstLine = br.readLine();

			line = br.readLine();
			if (line ==null){
				firstLine = firstLine.substring(1);
				pos = firstLine.indexOf("<");
				if (pos >-1){
					line = firstLine.substring(pos);
				}
			} 
			if (line.trim().toLowerCase().startsWith("<mets")) {
				ris = Xmltype.METS.value();
			} else if (line.trim().toLowerCase().startsWith("<metadigit")) {
				ris = Xmltype.MAG.value();
			} else if (line.trim().toLowerCase().startsWith("<premis")) {
				ris = Xmltype.PREMIS.value();
			} else if (line.trim().toLowerCase().startsWith("<agent")) {
				ris = Xmltype.AGENT.value();
			} else if (line.trim().toLowerCase().startsWith("<rights")) {
				ris = Xmltype.RIGHTS.value();
			} else if (line.trim().toLowerCase().startsWith("<event")) {
				ris = Xmltype.EVENT.value();
			} else if (line.trim().toLowerCase().startsWith("<mdregistroingressi")) {
				ris = Xmltype.REGISTRO.value();
			}
			if (ris != null){
				validateXsd(fXml, ris);
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (XsdException e) {
			throw e;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (fr != null) {
					fr.close();
				}
			} catch (IOException e) {
				throw e;
			}
		}
		return ris;
	}

	protected abstract void validateXsd(File fXml, String ris) throws XsdException;

	public OutputStream read(File fileTar, int offset, int length) throws TarException{
		FileInputStream is = null;
		TarArchiveInputStream debInputStream = null;
		
		byte[] dati = null;
		ByteArrayOutputStream baos = null;

		try {
			is = new FileInputStream(fileTar);
//			is.
//			debInputStream = new TarArchiveInputStream(is);
			
			dati = new byte[length];
			is.skip(offset);
			
			is.read(dati);
//			debInputStream.skip(offset);
//			debInputStream.read(dati);
//			debInputStream.read(dati, offset, length);
			baos = new ByteArrayOutputStream();
			baos.write(dati);
			baos.flush();
		} catch (FileNotFoundException e) {
			throw new TarException(e.getMessage(), e);
//		} catch (ArchiveException e) {
//			throw new TarException(e.getMessage(), e);
		} catch (IOException e) {
			throw new TarException(e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TarException(e.getMessage(), e);
		} finally {
			try {
				if (debInputStream != null){
					debInputStream.close();
				}
				if (is != null){
					is.close();
				}
			} catch (IOException e) {
				throw new TarException(e.getMessage(), e);
			}
		}
		return baos;
	}
}
