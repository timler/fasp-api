/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.service.impl;

import cc.altius.FASP.model.DTO.QuantimedImportDTO;
import cc.altius.FASP.model.DTO.QuantimedImportProductDTO;
import cc.altius.FASP.model.DTO.QuantimedImportRecordDTO;
import cc.altius.FASP.service.QuantimedImportService;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author altius
 */
@Service
public class QuantimedImportServiceImpl implements QuantimedImportService {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public QuantimedImportDTO importForecastData(MultipartFile file, String programId) {
        String extension = "";
        QuantimedImportDTO importDTO = new QuantimedImportDTO();
        List<QuantimedImportProductDTO> productDTOs = new LinkedList<>();
        List<QuantimedImportRecordDTO> recordDTOs = new LinkedList<>();
        QuantimedImportProductDTO importProductDTO = null;
        QuantimedImportRecordDTO importRecordDTO = null;
        int i = file.getOriginalFilename().lastIndexOf('.');
        if (i > 0) {
            extension = file.getOriginalFilename().substring(i + 1);
        }

        if (!extension.equalsIgnoreCase("xml")) {
            System.out.println("File is not an xml");
        } else {

            try {
                File file1 = new File("/home/altius/" + file.getOriginalFilename());
                file.transferTo(file1);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                FileReader fr = new FileReader(file1);
                Document doc;
                NodeList nList1, nList2;
                MapSqlParameterSource[] batchParams;
                Map<String, QuantimedImportProductDTO> map = new HashedMap<String, QuantimedImportProductDTO>();
                int[] rows1;
                int x;
                if (fr.read() == -1) {
                    //file is empty
                    logger.info("Order file is empty");
                } else {
                    doc = dBuilder.parse(file1);
                    doc.getDocumentElement().normalize();

                    importDTO.setProgramId(programId);
                    importDTO.setFileType(doc.getElementsByTagName("FileType").item(0).getTextContent());
                    importDTO.setDtmDataExported(doc.getElementsByTagName("dtmDataExported").item(0).getTextContent());
                    importDTO.setDtmStart(doc.getElementsByTagName("dtmStart").item(0).getTextContent());
                    importDTO.setDtmEnd(doc.getElementsByTagName("dtmEnd").item(0).getTextContent());
                    importDTO.setDblDataInterval(doc.getElementsByTagName("dblDataInterval").item(0).getTextContent());
                    importDTO.setSourceName(doc.getElementsByTagName("SourceName").item(0).getTextContent());

//                    System.out.println("File Type : " + doc.getElementsByTagName("FileType").item(0).getTextContent());
//                    System.out.println("DTM Data Exported : " + doc.getElementsByTagName("dtmDataExported").item(0).getTextContent());
//                    System.out.println("DTM Start : " + doc.getElementsByTagName("dtmStart").item(0).getTextContent());
//                    System.out.println("DTM End : " + doc.getElementsByTagName("dtmEnd").item(0).getTextContent());
//                    System.out.println("DBL Data Interval : " + doc.getElementsByTagName("dblDataInterval").item(0).getTextContent());
//                    System.out.println("Source : " + doc.getElementsByTagName("SourceName").item(0).getTextContent());

                    nList1 = doc.getElementsByTagName("Product");
                    System.out.println("=====================" + nList1.getLength());
                    x = 0;
                    for (int temp2 = 0; temp2 < nList1.getLength(); temp2++) {
                        Node nNode1 = nList1.item(temp2);
                        if (nNode1.getNodeType() == Node.ELEMENT_NODE) {
                            Element dataRecordElement = (Element) nNode1;

                            importProductDTO = new QuantimedImportProductDTO();
                            importProductDTO.setProductName(dataRecordElement.getElementsByTagName("strName").item(0).getTextContent().trim());
                            importProductDTO.setProductId(dataRecordElement.getElementsByTagName("strProductID").item(0).getTextContent().trim());                            
                            importProductDTO.setSource(dataRecordElement.getElementsByTagName("Source").item(0).getTextContent().trim());
                            importProductDTO.setUserDefined(dataRecordElement.getElementsByTagName("UserDefined").item(0).getTextContent().trim());
                            importProductDTO.setProductGroup(dataRecordElement.getElementsByTagName("ProductGroup").item(0).getTextContent().trim());
                            importProductDTO.setInnovatorName(dataRecordElement.getElementsByTagName("InnovatorName").item(0).getTextContent().trim());
                            importProductDTO.setLowestUnitQuantity(dataRecordElement.getElementsByTagName("LowestUnitQty").item(0).getTextContent().trim());
                            importProductDTO.setLowestUnitMeasure(dataRecordElement.getElementsByTagName("LowestUnitMeasure").item(0).getTextContent().trim());
                            importProductDTO.setQuantificationFactor(dataRecordElement.getElementsByTagName("QuantificationFactor").item(0).getTextContent().trim());
                            productDTOs.add(importProductDTO);
                            map.put(dataRecordElement.getElementsByTagName("strProductID").item(0).getTextContent().trim(), importProductDTO);

//                            System.out.println(dataRecordElement.getElementsByTagName("strName").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("strProductID").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("Source").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("UserDefined").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("ProductGroup").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("InnovatorName").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("LowestUnitQty").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("LowestUnitMeasure").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("QuantificationFactor").item(0).getTextContent()
//                            );
                        }
                    }
                    productDTOs.sort((QuantimedImportProductDTO s1, QuantimedImportProductDTO s2)->s1.getProductName().compareTo(s2.getProductName()));
                    importDTO.setProducts(productDTOs);

                    nList2 = doc.getElementsByTagName("Record");
                    System.out.println("=====================" + nList2.getLength());
                    for (int temp2 = 0; temp2 < nList2.getLength(); temp2++) {
                        Node nNode2 = nList2.item(temp2);
                        if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
                            Element dataRecordElement = (Element) nNode2;

                            importRecordDTO = new QuantimedImportRecordDTO();
                            importRecordDTO.setProductId(dataRecordElement.getElementsByTagName("strProductID").item(0).getTextContent().trim());
                            importRecordDTO.setDtmPeriod(dataRecordElement.getElementsByTagName("dtmPeriod").item(0).getTextContent().trim());
                            importRecordDTO.setIngConsumption(dataRecordElement.getElementsByTagName("lngConsumption").item(0).getTextContent().trim());
                            importRecordDTO.setIngAdjustments(dataRecordElement.getElementsByTagName("lngAdjustments").item(0).getTextContent().trim());
                            importRecordDTO.setProduct(map.get(dataRecordElement.getElementsByTagName("strProductID").item(0).getTextContent().trim()));
                            recordDTOs.add(importRecordDTO);

//                            System.out.println(dataRecordElement.getElementsByTagName("strProductID").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("dtmPeriod").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("lngConsumption").item(0).getTextContent()
//                                    + " : " + dataRecordElement.getElementsByTagName("lngAdjustments").item(0).getTextContent()
//                            );
                        }
                    }
                    recordDTOs.sort((QuantimedImportRecordDTO s1, QuantimedImportRecordDTO s2)->s1.getProduct().getProductName().compareTo(s2.getProduct().getProductName()));
                    importDTO.setRecords(recordDTOs);
                }
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(QuantimedImportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(QuantimedImportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(QuantimedImportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return importDTO;
    }
}
