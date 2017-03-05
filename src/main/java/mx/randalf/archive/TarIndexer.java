/**
 * 
 */
package mx.randalf.archive;

import java.math.BigInteger;


/**
 * @author massi
 *
 */
public class TarIndexer {

	private String name;

	private long offset;

	private long size;

	private String sha1;

	private String sha256;

	private String md5;

	private String xmlType;
	
	private BigInteger imageLength;
	
	private BigInteger imageWidth;

	private BigInteger dpi;

	private String idDepositante;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getSha1() {
		return sha1;
	}

	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getXmlType() {
		return xmlType;
	}

	public void setXmlType(String xmlType) {
		this.xmlType = xmlType;
	}

	public BigInteger getImageLength() {
		return imageLength;
	}

	public void setImageLength(BigInteger imageLength) {
		this.imageLength = imageLength;
	}

	public BigInteger getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(BigInteger imageWidth) {
		this.imageWidth = imageWidth;
	}

	public BigInteger getDpi() {
		return dpi;
	}

	public void setDpi(BigInteger dpi) {
		this.dpi = dpi;
	}

	/**
	 * @return the sha256
	 */
	public String getSha256() {
		return sha256;
	}

	/**
	 * @param sha256 the sha256 to set
	 */
	public void setSha256(String sha256) {
		this.sha256 = sha256;
	}

	public void setIdDepositante(String idDepositante) {
		this.idDepositante = idDepositante;
	}

	/**
	 * @return the idDepositante
	 */
	public String getIdDepositante() {
		return idDepositante;
	}

}
