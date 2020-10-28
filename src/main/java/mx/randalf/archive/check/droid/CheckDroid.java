package mx.randalf.archive.check.droid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mx.randalf.archive.check.exception.CheckArchiveException;

public class CheckDroid {

	/**
	 * Variabile utilizzata per loggare l'applicazione
	 */
	private Logger log = LogManager.getLogger(getClass());

	/**
	 * Variabile utilizzata per indicare la posizione del file Droid
	 */
	private String fileDroid = null;

	/**
	 * Costruttore
	 * 
	 * @param fileDroid
	 */
	public CheckDroid(String fileDroid) {
		this.fileDroid = fileDroid;
	}

	/**
	 * Metodo per eseguire la verifica del file con Droid
	 * 
	 * @param fileAnalize
	 *            File da analizzare
	 * @return File Csv con il risultato dell'analisi
	 * @throws CheckArchiveException 
	 */
	public File check(File fileAnalize, File pathTmp) throws CheckArchiveException {
		File fileCsv;

		try {
			try{
				checkSignature();
			} catch (CheckArchiveException e) {
			}
			fileCsv = checkFile(fileAnalize, pathTmp);
		} catch (CheckArchiveException e) {
			throw e;
		}
		return fileCsv;
	}

	/**
	 * Metodo utilizzato per analizzare un file con Droid
	 * 
	 * @param fileAnalize File da analizzare
	 * @return File Csv con il risultato dell'analisi
	 * @throws CheckArchiveException
	 */
	private File checkFile(File fileAnalize, File pathTmp) throws CheckArchiveException {
		String[] cmd = null;
		Vector<String> ris = null;
		File fileDroid = null;
		File fileCsv = null;

		try {
			if (fileAnalize.exists()) {

				fileDroid = File.createTempFile("CheckArchive-", ".droid", 
						pathTmp);

				cmd = new String[] { this.fileDroid, "-a",
						fileAnalize.getAbsolutePath(), "-p",
						fileDroid.getAbsolutePath() };

				ris = execute(cmd);

				if (ris != null) {
					fileCsv = File.createTempFile("CheckArchive-", ".csv", pathTmp);
					cmd = new String[] { this.fileDroid, "-e",
							fileCsv.getAbsolutePath(), "-p",
							fileDroid.getAbsolutePath() };

					ris = execute(cmd);
				}

			} else {
				throw new FileNotFoundException("Il file ["
						+ fileAnalize.getAbsolutePath() + "] non esiste");
			}
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (CheckArchiveException e) {
			throw e;
		} finally {
			if (fileDroid != null) {
				if (!fileDroid.delete()) {
					throw new CheckArchiveException(
							"Problema nella cancellazione del file ["
									+ fileDroid.getAbsolutePath() + "]");
				}
			}
		}
		return fileCsv;
	}

	/**
	 * Metodo utilizzato per verificare se ' necessario eseguire l'aggiornare le
	 * informazioni per Droid
	 * 
	 * @throws CheckArchiveException
	 */
	private void checkSignature() throws CheckArchiveException {
		String[] cmd = null;
		Vector<String> ris = null;

		try {
			cmd = new String[] { fileDroid, "-c" };

			ris = execute(cmd);
			if (ris != null && ris.size() > 0) {
				if (!ris.get(0)
						.startsWith("No signature updates are available")) {
					cmd = new String[] { fileDroid, "-d" };

					ris = execute(cmd);
				}
			}
		} catch (CheckArchiveException e) {
			throw e;
		}

	}

	/**
	 * Metodo utilizzato per l'esecuzione Runtime di una applicazione
	 * 
	 * @param cmd
	 *            Comandi da eseguire relative all'applicazione
	 * @return Risultato dell'applicazione
	 * @throws CheckArchiveException
	 */
	private Vector<String> execute(String[] cmd) throws CheckArchiveException {
		Runtime rt = null;
		Process proc = null;
		InputStreamReader isrError = null;
		InputStreamReader isrInput = null;
		BufferedReader brError = null;
		BufferedReader brInput = null;
		String msgErr = null;
		String line = null;
		Vector<String> ris = null;
		String firstLine = null;

		try {
			rt = Runtime.getRuntime();
			proc = rt.exec(cmd);
			proc.waitFor();

			isrInput = new InputStreamReader(proc.getInputStream());
			brInput = new BufferedReader(isrInput);
			ris = new Vector<String>();
			while ((line = brInput.readLine()) != null) {
				ris.add(line);
			}

			isrError = new InputStreamReader(proc.getErrorStream());
			brError = new BufferedReader(isrError);
			msgErr = "";
			while ((line = brError.readLine()) != null) {
				if (firstLine== null) {
					firstLine = line;
				} else {
					if (line.startsWith("INFO")) {
						firstLine = null;
					} else {
						msgErr += (firstLine==null?"":
							(
							  (firstLine.trim().equals("") ? "" : "\n") + 
							  firstLine.trim()
							 ));
						msgErr += (msgErr.equals("") ? "" : "\n") + line;
						firstLine = null;
					}
				}
			}
			if (!msgErr.equals("")) {
				throw new IOException(msgErr);
			}

		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new CheckArchiveException(e.getMessage(), e);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			throw new CheckArchiveException(e.getMessage(), e);
		} finally {
			try {
				if (brInput != null) {
					brInput.close();
				}
				if (isrInput != null) {
					isrInput.close();
				}
				if (brError != null) {
					brError.close();
				}
				if (isrError != null) {
					isrError.close();
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				throw new CheckArchiveException(e.getMessage(), e);
			}
		}
		return ris;
	}
}
