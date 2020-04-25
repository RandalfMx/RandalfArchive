/**
 * 
 */
package mx.randalf.archive.check.warc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.utils.IOUtils;
import org.im4java.core.InfoException;
import org.jwat.common.ContentType;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.HeaderLine;
import org.jwat.common.NewlineParser;
import org.jwat.common.Uri;
import org.jwat.warc.WarcConcurrentTo;
import org.jwat.warc.WarcDigest;
import org.jwat.warc.WarcHeader;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import mx.randalf.archive.TarIndexer;
import mx.randalf.archive.tools.Folder;
import mx.randalf.tools.MD5Tools;
import mx.randalf.tools.SHA1Tools;
import mx.randalf.tools.SHA256Tools;

/**
 * @author massi
 *
 */
public class CheckWarc {

	/**
	 * 
	 */
	public CheckWarc() {
	}

	public static void main(String[] args) {
		CheckWarc checkWarc = null;
		if (args.length == 1) {
			checkWarc = new CheckWarc();
			checkWarc.read(args[0]);
		}
	}

	public static Hashtable<String, TarIndexer> indexer(File fileTar, boolean calcImg) throws FileNotFoundException,
			ArchiveException, IOException, NoSuchAlgorithmException, InfoException {
		InputStream is = null;
		WarcReader reader = null;
		WarcRecord record = null;
		File dTmp = null;
		File fTmp = null;
		Hashtable<String, TarIndexer> ris = null;
		TarIndexer tarIndexer = null;

		try {
			dTmp = Files.createTempDirectory("TarIndexer-").toFile();
			dTmp.mkdirs();
			
			is = new FileInputStream(fileTar);

			reader = WarcReaderFactory.getReader(is);

			ris = new Hashtable<String, TarIndexer>();
			while ((record = reader.getNextRecord()) != null) {
				if (record.header.contentLength>0){
					tarIndexer = new TarIndexer();
	
					if (record.header.warcFilename != null &&
							!record.header.warcFilename.trim().equals("")){
						tarIndexer.setName(record.header.warcFilename);
					} else {
						tarIndexer.setName(record.header.warcTargetUriStr);
					}
					tarIndexer.setOffset(record.getStartOffset());
					tarIndexer.setSize(record.header.contentLength);
	
					fTmp = new File(dTmp.getAbsolutePath() + File.separator
							+ "tempInfo");
					if (!fTmp.getParentFile().exists()){
						if (!fTmp.getParentFile().mkdirs()){
							throw new FileNotFoundException("Problemi nella creazione della cartella ["+fTmp.getParentFile().getAbsolutePath()+"]");
						}
					}
					OutputStream outputFileStream = new FileOutputStream(fTmp);
					
					IOUtils.copy(record.getPayloadContent(), outputFileStream);
					outputFileStream.flush();
					outputFileStream.close();
	
					tarIndexer.setSha1(SHA1Tools.readMD5File(fTmp.getAbsolutePath()));
					tarIndexer.setMd5(MD5Tools.readMD5File(fTmp.getAbsolutePath()));
					tarIndexer.setSha256(SHA256Tools.readMD5(fTmp.getAbsolutePath()));
					fTmp.delete();
					ris.put(tarIndexer.getName(), tarIndexer);
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (NoSuchAlgorithmException e) {
			throw e;
		} finally {
			try {
				if (reader != null){
					reader.close();
				}
				if (is != null){
					is.close();
				}
				if (dTmp != null && dTmp.exists()){
					if (!Folder.deleteFolder(dTmp)){
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

	public void read(String warcFile) {
		File file = null;
		InputStream in = null;
		int records = 0;
		int errors = 0;
		WarcReader reader = null;
		WarcRecord record = null;

		try {
			file = new File(warcFile);
			in = new FileInputStream(file);

			PrintStream ps = null;
			ps = new PrintStream(new File(warcFile+".out"));
			System.setOut(ps);
			reader = WarcReaderFactory.getReader(in);

			while ((record = reader.getNextRecord()) != null) {
				// printRecordErrors(record);

				++records;
				System.out.println("Records: " + records);
				printRecord(record);

				// if (record.hasErrors()) {
				// errors += record.getValidationErrors().size();
				// }
			}

			System.out.println("--------------");
			System.out.println("       Records: " + records);
			System.out.println("        Errors: " + errors);
			reader.close();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printRecord(WarcRecord record) {
		System.out.println("--------------");
		if (record.computedBlockDigest != null) {
			System.out.println("computedBlockDigest");
			print(record.computedBlockDigest);
		}
		if (record.computedPayloadDigest != null) {
			System.out.println("computedPayloadDigest");
			print(record.computedPayloadDigest);
		}
		if (record.diagnostics != null) {
			System.out.println("diagnostics");
			print(record.diagnostics);
		}
		if (record.header != null) {
			System.out.println("header");
			print(record.header);
		}
		print("", "isValidBlockDigest",record.isValidBlockDigest);
		print("", "isValidPayloadDigest",record.isValidPayloadDigest);
		if (record.nlp != null) {
			System.out.println("nlp");
			print(record.nlp);
		}
		print("", "trailingNewlines",record.trailingNewlines);
		print("", "getConsumed",record.getConsumed());
//		record.getHeader(field);
		if (record.getHeaderList() != null){
			System.out.println("getHeaderList");
			for (HeaderLine headerLine: record.getHeaderList()){
				System.out.println("\t------------");
				print(headerLine);
			}
		}
//		if (record.getPayload() != null){
//			System.out.println("getPayload");
//			print(record.getPayload());
//		}
		print("", "getStartOffset", record.getStartOffset());
		print("", "hasPayload", record.hasPayload());
		print("", "isClosed", record.isClosed());
		print("", "isCompliant", record.isCompliant());
		try {
			record.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void print(HeaderLine headerLine) {
		print("\t","bfErrors", headerLine.bfErrors);
		print("\t","line", headerLine.line);
		printH("\t","lines", headerLine.lines);
		print("\t","name", headerLine.name);
		print("\t","raw", headerLine.raw);
		print("\t","type", headerLine.type);
		print("\t","value", headerLine.value);
		
	}

	private void print(String prefix, String key, byte type) {
		print(prefix, key, new Byte(type).toString());
	}

	private void printH(String prefix, String key, List<HeaderLine> lines) {
		for (HeaderLine line: lines){
			print(line);
		}
	}

	private void print(NewlineParser nlp) {
		print("\t","hashCode", nlp.hashCode());
		print("\t","bMisplacedCr", nlp.bMisplacedCr);
		print("\t","bMisplacedLf", nlp.bMisplacedLf);
		print("\t","bMissingCr", nlp.bMissingCr);
		print("\t","bMissingLf", nlp.bMissingLf);
	}

	private void print(WarcHeader header) {
		print("\t","contentLengthStr",header.contentLengthStr);
		print("\t","contentTypeStr",header.contentTypeStr);
		print("\t","warcBlockDigestStr",header.warcBlockDigestStr);
		print("\t","warcDateStr",header.warcDateStr);
		print("\t","warcFilename",header.warcFilename);
		print("\t","warcIdentifiedPayloadTypeStr",header.warcIdentifiedPayloadTypeStr);
		print("\t","warcIpAddress",header.warcIpAddress);
		print("\t","warcPayloadDigestStr",header.warcPayloadDigestStr);
		print("\t","warcProfileStr",header.warcProfileStr);
		print("\t","warcRecordIdStr",header.warcRecordIdStr);
		print("\t","warcRefersToStr",header.warcRefersToStr);
		print("\t","warcSegmentNumberStr",header.warcSegmentNumberStr);
		print("\t","warcSegmentOriginIdStr",header.warcSegmentOriginIdStr);
		print("\t","warcSegmentTotalLengthStr",header.warcSegmentTotalLengthStr);
		print("\t","warcTargetUriStr",header.warcTargetUriStr);
		print("\t","warcTruncatedStr",header.warcTruncatedStr);
		print("\t","warcTypeStr",header.warcTypeStr);
		print("\t","warcWarcinfoIdStr",header.warcWarcinfoIdStr);
		print("\t","hashCode",header.hashCode());
		print("\t","contentLength",header.contentLength);
		print("\t","warcProfileIdx",header.warcProfileIdx);
		print("\t","warcSegmentNumber",header.warcSegmentNumber);
		print("\t","warcSegmentTotalLength",header.warcSegmentTotalLength);
		print("\t","warcTruncatedIdx",header.warcTruncatedIdx);
		print("\t","warcTypeIdx",header.warcTypeIdx);
		print("\t","contentType",header.contentType);
		print("\t","headerBytes",header.headerBytes);
		print("\t","warcBlockDigest",header.warcBlockDigest);
		printC("\t","warcConcurrentToList",header.warcConcurrentToList);
		printC("\t","warcDate",header.warcDate);
		print("\t","warcIdentifiedPayloadType",header.warcIdentifiedPayloadType);
		print("\t","warcInetAddress",header.warcInetAddress);
		print("\t","warcPayloadDigest",header.warcPayloadDigest);
		print("\t","warcRecordIdStr",header.warcRecordIdStr);
		print("\t","warcRefersToUri",header.warcRefersToUri);
		print("\t","warcSegmentOriginIdUrl",header.warcSegmentOriginIdUrl);
		print("\t","warcTargetUriUri",header.warcTargetUriUri);
		print("\t","warcWarcInfoIdUri",header.warcWarcinfoIdUri);
	}

	private void print(Diagnostics<Diagnosis> diagnostics) {
		print("\t","hashCode", diagnostics.hashCode());
		print("\t","getErrors", diagnostics.getErrors());
		print("\t","getWarnings", diagnostics.getWarnings());
		print("\t","hasErrors", diagnostics.hasErrors());
		print("\t","hasWarnings", diagnostics.hasWarnings());
	}

	private void print(WarcDigest warcDigest) {
		print("\t", "algorithm", warcDigest.algorithm);
		print("\t", "digestString", warcDigest.digestString);
		print("\t", "encoding", warcDigest.encoding);
		print("\t", "toString", warcDigest.toString());
		print("\t", "toStringFull", warcDigest.toStringFull());
	}

	private void print(String prefix, String key, String value) {
		if (value != null) {
			System.out.println(prefix + key + ": " + value);
		}
	}

	private void print(String prefix, String key, byte[] value) {
		if (value != null){
			print(prefix, key, new String(value));
		}
	}

	private void print(String prefix, String key, Integer value) {
		if (value != null) {
			System.out.println(prefix + key + ": " + value);
		}
	}

	private void print(String prefix, String key, Long value) {
		if (value != null) {
			System.out.println(prefix + key + ": " + value);
		}
	}

	private void print(String prefix, String key, List<Diagnosis> values) {
		if (values != null && values.size()>0){
			print(prefix+"\t",key, values.size());
			prefix+="\t";
			for(int x=0; x<values.size(); x++){
				System.out.println(prefix+"--------------");
				print(prefix,"entity", values.get(x).entity);
				print(prefix,"hashCode", values.get(x).hashCode());
				print(prefix,"information", values.get(x).information);
				print(prefix,"type", values.get(x).type);
			}
		}
	}

	private void print(String prefix, String key, String[] values) {
		if (values != null && values.length>0){
			System.out.println(prefix + key + ": "+values.length);
			prefix+="\t";
			for(int x=0; x<values.length; x++){
				print(prefix,"["+x+"]", values[x]);
			}
		}
	}

	private void print(String prefix, String key, DiagnosisType value) {
		if (value != null) {
			print(prefix,key, value.name()+" - "+value.ordinal());
		}
	}

	private void print(String prefix, String key, Boolean value) {
		if (value != null) {
			System.out.println(prefix + key + ": " + value);
		}
	}

	private void print(String prefix, String key, ContentType value) {
		if (value != null) {
			System.out.println(prefix + key + ": ");
			print(prefix+"\t","contentType", value.contentType);
			print(prefix+"\t","mediaType", value.mediaType);
			if (value.parameters != null){
				System.out.println(prefix +"\t" + "parameters" );
				for (String vKey:value.parameters.keySet()){
					print(prefix+"\t\t",vKey, value.parameters.get(vKey));
				}
			}
		}
	}

	private void print(String prefix, String key, WarcDigest value) {
		if (value != null){
			System.out.println(prefix + key);
			print(prefix+"\t", "algorithm", value.algorithm);
			print(prefix+"\t", "digestBytes", value.digestBytes);
			print(prefix+"\t", "digestString", value.digestString);
			print(prefix+"\t", "encoding", value.encoding);
			print(prefix+"\t", "toString", value.toString());
			print(prefix+"\t", "toStringFull", value.toStringFull());
			print(prefix+"\t", "hashCode", value.hashCode());
		}
	}

	private void printC(String prefix, String key, List<WarcConcurrentTo> value) {
		if (value != null && value.size() > 0) {
			print(prefix,"warcConcurrentToList",value.size());
			prefix+="\t";
			for (int x = 0; x < value.size(); x++) {
				System.out.println(prefix+"--------------");
				print(prefix,"warcConcurrentToStr",value.get(x).warcConcurrentToStr);
				print(prefix,"warcConcurrentToUri",value.get(x).warcConcurrentToUri);
			}
		}
	}

	private void printC(String prefix, String key, Date value) {
		print(prefix,key, value.toString());
	}

	private void print(String prefix, String key, InetAddress value) {
		if (value != null){
			System.out.println(prefix + key);
			print(prefix+"\t", "getHostAddress", value.getHostAddress());
			print(prefix+"\t", "getHostName", value.getHostName());
			print(prefix+"\t", "isLoopbackAddress", value.isLoopbackAddress());
			print(prefix+"\t", "getAddress", value.getAddress());
			print(prefix+"\t", "getCanonicalHostName", value.getCanonicalHostName());
			print(prefix+"\t", "isLinkLocalAddress", value.isLinkLocalAddress());
			print(prefix+"\t", "isSiteLocalAddress", value.isSiteLocalAddress());
			print(prefix+"\t", "toString", value.toString());
			print(prefix+"\t", "hashCode", value.hashCode());
			print(prefix+"\t", "isAnyLocalAddress", value.isAnyLocalAddress());
			print(prefix+"\t", "isMCGlobal", value.isMCGlobal());
			print(prefix+"\t", "isMCLinkLocal", value.isMCLinkLocal());
			print(prefix+"\t", "isMCNodeLocal", value.isMCNodeLocal());
			print(prefix+"\t", "isMCOrgLocal", value.isMCOrgLocal());
			print(prefix+"\t", "isMCSiteLocal", value.isMCSiteLocal());
			print(prefix+"\t", "isMulticastAddress", value.isMulticastAddress());
		}
	}

	private void print(String prefix, String key, Uri value) {
		if (value != null){
			print(prefix,key, value.toString());
		}
	}
}
