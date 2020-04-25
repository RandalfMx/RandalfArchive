/**
 * 
 */
package mx.randalf.archive.check.mrc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.List;

import org.marc4j.MarcError;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Record;

import mx.randalf.archive.TarIndexer;
import mx.randalf.archive.check.exception.CheckArchiveException;
import mx.randalf.archive.tools.Folder;
import mx.randalf.tools.MD5Tools;
import mx.randalf.tools.SHA1Tools;
import mx.randalf.tools.SHA256Tools;

/**
 * @author massi
 *
 */
public class CheckMrc {

	/**
	 * 
	 */
	public CheckMrc() {
	}

	public static Hashtable<String, TarIndexer> indexer(File fileTar)
			throws FileNotFoundException, NoSuchAlgorithmException, IOException, CheckArchiveException {
		Hashtable<String, TarIndexer> ris = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		MarcStreamReader msr = null;
		MarcStreamWriter msw = null;
		Record record = null;
		File dTmp = null;
		File fTmp = null;
		TarIndexer tarIndexer = null;
		int offSet = 0;
		String msg = "";
		MarcError marcError = null;

		try {
			ris = new Hashtable<String, TarIndexer>();

			dTmp = Files.createTempDirectory("MrcIndexer-").toFile();
			dTmp.mkdirs();

			fis = new FileInputStream(fileTar);
			msr = new MarcStreamReader(fis);
			while (msr.hasNext()) {
				record = msr.next();
				if (record.getErrors() != null && record.getErrors().size() > 0) {
					for (int x = 0; x < record.getErrors().size(); x++) {
						marcError = record.getErrors().get(x);
						if (marcError != null) {
							if (marcError.severity != MarcError.INFO) {
								if (!msg.trim().equals("")) {
									msg += "\n";
								}
								if (!marcError.message.trim().equals("")) {
									if (marcError.severity == MarcError.ERROR_TYPO) {
										msg += "Error Typo: ";
									} else if (marcError.severity == MarcError.MINOR_ERROR) {
										msg += "Minor Error: ";
									} else if (marcError.severity == MarcError.MAJOR_ERROR) {
										msg += "Major Error: ";
									} else if (marcError.severity == MarcError.FATAL) {
										msg += "Fatal: ";
									}

									if (marcError.curField != null && !marcError.curField.trim().equals("")) {
										msg += "[" + marcError.curField;
										if (marcError.curSubfield != null && !marcError.curSubfield.trim().equals("")) {
											msg += " - " + marcError.curSubfield;
										}
										msg += "] ";
									} else if (marcError.curSubfield != null
											&& !marcError.curSubfield.trim().equals("")) {
										msg += "[" + marcError.curSubfield;
										msg += "] ";
									}
									msg += marcError.message;
								}
							}
						}
					}
					if (!msg.trim().equals("")) {
						throw new CheckArchiveException(msg);
					}
				}
				tarIndexer = new TarIndexer();

				tarIndexer.setName(read(record.getControlFields(), "001"));
				tarIndexer.setOffset(offSet);
				tarIndexer.setSize(record.getLeader().getRecordLength());
				offSet += record.getLeader().getRecordLength();
				fTmp = new File(dTmp.getAbsolutePath() + File.separator + "tempInfo");
				if (!fTmp.getParentFile().exists()) {
					if (!fTmp.getParentFile().mkdirs()) {
						throw new FileNotFoundException("Problemi nella creazione della cartella ["
								+ fTmp.getParentFile().getAbsolutePath() + "]");
					}
				}

				fos = new FileOutputStream(fTmp);

				msw = new MarcStreamWriter(fos);
				msw.write(record);
				fos.flush();
				fos.close();

				tarIndexer.setSha1(SHA1Tools.readMD5File(fTmp.getAbsolutePath()));
				tarIndexer.setMd5(MD5Tools.readMD5File(fTmp.getAbsolutePath()));
				tarIndexer.setSha256(SHA256Tools.readMD5(fTmp.getAbsolutePath()));
				fTmp.delete();
				ris.put(tarIndexer.getName(), tarIndexer);
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (NoSuchAlgorithmException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (CheckArchiveException e) {
			throw e;
		} finally {
			try {
				if (fis != null) {
					fis.close();
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

	private static String read(List<ControlField> controlFields, String key) {
		String result = null;
		for (int x = 0; x < controlFields.size(); x++) {
			if (controlFields.get(x).getTag().equals(key)) {
				result = controlFields.get(x).getData();
				break;
			}
		}
		return result;
	}

}
