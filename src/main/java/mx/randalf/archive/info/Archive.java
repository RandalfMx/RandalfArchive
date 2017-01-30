//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.26 at 08:07:43 AM CET 
//


package mx.randalf.archive.info;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="nome" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{http://www.randalf.mx/archive/info}type" minOccurs="0"/>
 *         &lt;sequence minOccurs="0">
 *           &lt;element name="xmltype" type="{http://www.randalf.mx/archive/info}xmltype"/>
 *           &lt;element name="xmlvalid" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;/sequence>
 *         &lt;element ref="{http://www.randalf.mx/archive/info}archive" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "nome",
    "type",
    "xmltype",
    "xmlvalid",
    "archive"
})
@XmlRootElement(name = "archive")
public class Archive {

    @XmlElement(required = true)
    protected String nome;
    protected Type type;
    protected Xmltype xmltype;
    protected Boolean xmlvalid;
    protected List<Archive> archive;

    /**
     * Gets the value of the nome property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNome() {
        return nome;
    }

    /**
     * Sets the value of the nome property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNome(String value) {
        this.nome = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link Type }
     *     
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link Type }
     *     
     */
    public void setType(Type value) {
        this.type = value;
    }

    /**
     * Gets the value of the xmltype property.
     * 
     * @return
     *     possible object is
     *     {@link Xmltype }
     *     
     */
    public Xmltype getXmltype() {
        return xmltype;
    }

    /**
     * Sets the value of the xmltype property.
     * 
     * @param value
     *     allowed object is
     *     {@link Xmltype }
     *     
     */
    public void setXmltype(Xmltype value) {
        this.xmltype = value;
    }

    /**
     * Gets the value of the xmlvalid property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isXmlvalid() {
        return xmlvalid;
    }

    /**
     * Sets the value of the xmlvalid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setXmlvalid(Boolean value) {
        this.xmlvalid = value;
    }

    /**
     * Gets the value of the archive property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the archive property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArchive().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Archive }
     * 
     * 
     */
    public List<Archive> getArchive() {
        if (archive == null) {
            archive = new ArrayList<Archive>();
        }
        return this.archive;
    }

}
