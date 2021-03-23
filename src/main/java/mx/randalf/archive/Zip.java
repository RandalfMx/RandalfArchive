/**
 * 
 */
package mx.randalf.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mx.randalf.archive.tools.Folder;

/**
 * @author massi
 *
 */
public class Zip {

	/**
	 * 
	 */
	public Zip() {
	}

	public static void convertTar(File input, File output, File fTar) throws FileNotFoundException, IOException {
		Path folderTmp = null;
		File fTmp = null;

		try {
			folderTmp = Files.createTempDirectory(Paths.get(output.getParentFile().getAbsolutePath()), "MD-");
			fTmp = folderTmp.toFile();
			unZip(input, fTmp);
			
			Tar.tar(fTmp, output, fTar);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (!Folder.deleteFolder(fTmp)) {
				throw new IOException("Problemi nella cacellazione della cartella " + fTmp.getAbsolutePath());
			}
		}
	}

	public static void unZip(File input, File fTmp) throws FileNotFoundException, IOException {
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry ze = null;
		String fileName = null;
		File newFile = null;
		FileOutputStream fos = null;
		byte[] buffer = new byte[1024];
		int len = 0;

		try {
			if (!fTmp.exists()) {
				if (!fTmp.mkdirs()) {
					throw new IOException("Problemi nella creazione della cartella " + fTmp.getAbsolutePath());
				}
			}

			fis = new FileInputStream(input);
			zis = new ZipInputStream(fis);
			ze = zis.getNextEntry();
			while (ze != null) {
				fileName = ze.getName();
				newFile = new File(fTmp.getAbsolutePath() + File.separator + fileName);
//				System.out.println("Unzipping to " + newFile.getAbsolutePath());
				// create directories for sub directories in zip
				new File(newFile.getParent()).mkdirs();
				if (!ze.isDirectory()) {
					fos = null;
					try {
						fos = new FileOutputStream(newFile);
						len = 0;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					} catch (FileNotFoundException e) {
						throw e;
					} catch (IOException e) {
						throw e;
					} finally {
						if (fos != null) {
							fos.close();
						}
					}
				}
				// close this ZipEntry
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (zis != null) {
				// close last ZipEntry
				zis.closeEntry();
				zis.close();
			}
			if (fis != null) {
				fis.close();
			}
		}

	}
}
