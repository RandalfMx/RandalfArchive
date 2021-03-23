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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.im4java.core.InfoException;

import mx.randalf.archive.exception.TarException;
import mx.randalf.archive.tools.Folder;
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

	private static Logger log = LogManager.getLogger(Tar.class);

	/**
	 * 
	 */
	public Tar() {
	}

	public List<File> decompress(File inputFile, File outputDir)
			throws FileNotFoundException, IOException, ArchiveException {

		log.info("\n"
				+ String.format("Untaring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

		List<File> untaredFiles = new LinkedList<File>();
		InputStream is = new FileInputStream(inputFile);
		TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory()
				.createArchiveInputStream("tar", is);
		TarArchiveEntry entry = null;
		while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
			File outputFile = new File(outputDir, entry.getName());
			if (entry.isDirectory()) {
				log.info(
						"\n" + String.format("Attempting to write output directory %s.", outputFile.getAbsolutePath()));
				if (!outputFile.exists()) {
					log.info("\n"
							+ String.format("Attempting to create output directory %s.", outputFile.getAbsolutePath()));
					if (!outputFile.mkdirs()) {
						throw new IllegalStateException(
								String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
					}
				}
			} else {
				log.info("\n" + String.format("Creating output file %s.", outputFile.getAbsolutePath()));
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
			debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);

			ris = new Hashtable<String, TarIndexer>();
			while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
				tarIndexer = new TarIndexer();

				tarIndexer.setName(entry.getName());
				tarIndexer.setOffset(debInputStream.getBytesRead());
				tarIndexer.setSize(entry.getSize());

				fTmp = new File(dTmp.getAbsolutePath() + File.separator + entry.getName());
				if (!entry.isDirectory()) {
					if (!fTmp.getParentFile().exists()) {
						if (!fTmp.getParentFile().mkdirs()) {
							throw new FileNotFoundException("Problemi nella creazione della cartella ["
									+ fTmp.getParentFile().getAbsolutePath() + "]");
						}
					}
					OutputStream outputFileStream = new FileOutputStream(fTmp);
					IOUtils.copy(debInputStream, outputFileStream);
					outputFileStream.flush();
					outputFileStream.close();

					tarIndexer.setSha1(SHA1Tools.readMD5File(fTmp.getAbsolutePath()));
					tarIndexer.setMd5(MD5Tools.readMD5File(fTmp.getAbsolutePath()));
					tarIndexer.setSha256(SHA256Tools.readMD5(fTmp.getAbsolutePath()));

					if (calcImg && isImg(fTmp.getName().toLowerCase())) {
						calcImg(fTmp, tarIndexer);
					}

					checkXmlType(fTmp, tarIndexer);
					fTmp.delete();
				}
				ris.put(entry.getName(), tarIndexer);
			}
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
				if (debInputStream != null) {
					debInputStream.close();
				}
				if (dTmp != null && dTmp.exists()) {
					if (!Folder.deleteFolder(dTmp)) {
						throw new IOException(
								"Problemi nella cancellazione della cartella [" + dTmp.getAbsolutePath() + "]");
					}
				}
			} catch (IOException e) {
				throw e;
			}
		}
		return ris;
	}

	protected abstract void checkXmlType(File fTmp, TarIndexer tarIndexer)
			throws FileNotFoundException, IOException, XsdException;

	private void calcImg(File fImg, TarIndexer tarIndexer) throws InfoException {
		CalcImg calcImg = null;

		try {
			calcImg = new CalcImg(fImg);
			log.debug("\n" + "fImg: " + fImg.getAbsolutePath());
			log.debug("\n" + "imageLength: " + calcImg.getImageLength());
			log.debug("\n" + "getImageWidth: " + calcImg.getImageWidth());
			log.debug("\n" + "getDpi: " + calcImg.getDpi());
			tarIndexer.setImageLength(calcImg.getImageLength());
			tarIndexer.setImageWidth(calcImg.getImageWidth());
			tarIndexer.setDpi(calcImg.getDpi());
		} catch (InfoException e) {
			throw e;
		}
	}

	private boolean isImg(String file) {
		boolean ris = false;
		if (file.endsWith(".jp2") || file.endsWith(".jpg") || file.endsWith(".tif") || file.endsWith(".png")
				|| file.endsWith(".ico") || file.endsWith(".gif")) {
			ris = true;
		}
		return ris;
	}

	public OutputStream read(File fileTar, int offset, int length) throws TarException {
		FileInputStream is = null;
		TarArchiveInputStream debInputStream = null;

		byte[] dati = null;
		ByteArrayOutputStream baos = null;

		try {
			is = new FileInputStream(fileTar);
			// is.
			// debInputStream = new TarArchiveInputStream(is);

			dati = new byte[length];
			is.skip(offset);

			is.read(dati);
			// debInputStream.skip(offset);
			// debInputStream.read(dati);
			// debInputStream.read(dati, offset, length);
			baos = new ByteArrayOutputStream();
			baos.write(dati);
			baos.flush();
		} catch (FileNotFoundException e) {
			throw new TarException(e.getMessage(), e);
			// } catch (ArchiveException e) {
			// throw new TarException(e.getMessage(), e);
		} catch (IOException e) {
			throw new TarException(e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TarException(e.getMessage(), e);
		} finally {
			try {
				if (debInputStream != null) {
					debInputStream.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				throw new TarException(e.getMessage(), e);
			}
		}
		return baos;
	}

	public static void tar(File directory, File tarFile, File fTar) throws IOException {
		FileOutputStream fos = null;
		TarArchiveOutputStream tos = null;
		Runtime run = null;
		String[] cmd = null;
		Process proc = null;
		int status = 0;

		try {
			if (fTar != null && fTar.exists()) {
				run = Runtime.getRuntime();
				cmd = new String[3];
				cmd[0] = fTar.getAbsolutePath();
				cmd[1] = directory.getAbsolutePath();
				cmd[2] = tarFile.getAbsolutePath();
				proc = run.exec(cmd, null, directory);
				status = proc.waitFor();
				if (status !=0) {
					BufferedReader buf = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String line = "";
					while ((line=buf.readLine())!=null) {
						System.out.println(line);
					}
					buf = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					line = "";
					while ((line=buf.readLine())!=null) {
						System.out.println(line);
					}
					throw new IOException("Riscontrato problema nel tar del file "+tarFile.getAbsolutePath());
				}
			} else {
				fos = new FileOutputStream(tarFile);
				tos = new TarArchiveOutputStream(fos);
	
				// Recursivly add to tar
				if (directory.isFile()) {
				doAddFileToTar(tos, "", directory);
				} else {
					for (File f : directory.listFiles()) {
						doAddFileToTar(tos, "", f);
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (InterruptedException e) {
			throw  new IOException(e.getMessage(),e);
		} finally {

			// Close
			if (tos != null) {
				tos.close();
			}
			if (fos != null) {
				fos.close();
			}

		}
	}

	/**
	 * Recursivly add a directory structure to a tar output stream
	 * 
	 * @param t Tar output stream
	 * @param r Relative path up to this point
	 * @param c Current file
	 */
	private static void doAddFileToTar(TarArchiveOutputStream t, String r, File c) throws IOException {
		ArchiveEntry ae = null;

		try {
			// Update relative path
			r += (r.isEmpty() ? "" : (r.endsWith("/") ? "" : "/")) + c.getName();

			// Is it a file?
			if (c.isFile() == true) {
				ae = t.createArchiveEntry(c, r);
				t.putArchiveEntry(ae);
				copyFileToOutputStream(t, c);
				t.closeArchiveEntry();
			} else {

				// It's a directory
				for (File f : c.listFiles())
					doAddFileToTar(t, r, f);
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Simple method for copying file bytes to output stream
	 * 
	 * @param t
	 * @param f
	 */
	public static void copyFileToOutputStream(OutputStream t, File f) throws FileNotFoundException, IOException {
		int read = 0;
		byte[] m_copyBuffer = null;
		int size = 0;
		FileInputStream fis = null;

		try {
			m_copyBuffer = new byte[4 * 1024];
			size = (int) f.length();
			fis = new FileInputStream(f);
			while (size > 0) {
				read = fis.read(m_copyBuffer, 0, Math.min(size, m_copyBuffer.length));
				t.write(m_copyBuffer, 0, read);
				size -= read;
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

}
