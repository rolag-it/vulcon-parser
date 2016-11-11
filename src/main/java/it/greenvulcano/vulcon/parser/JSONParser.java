package it.greenvulcano.vulcon.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JSONParser {

	public static void main(String[] args) {
		File inputFile =  new File(args[0]);
		File outputFile =  new File(inputFile.getName().replace(".json", "").concat(".xml"));
		JSONParser parser = new JSONParser();
		
		try {
			Document document = parser.parse(Files.newInputStream(inputFile.toPath(), StandardOpenOption.READ));
			parser.writeXml(outputFile, document);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		

	}
	
	
	Document parse(InputStream inputStream) throws UnsupportedEncodingException, ParserConfigurationException {
		
		InputStreamReader contentReader = new InputStreamReader(inputStream, "UTF-8");	       	   
 	   	BufferedReader bufferedReader = new BufferedReader(contentReader);
  
    	String inputData = bufferedReader.lines().collect(Collectors.joining("\n"));
		
		
		JSONObject jsonFlow = new JSONObject(inputData);
		
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();		
		Document document = documentBuilder.newDocument();	
		
		Element flowTree = parseNode(document, jsonFlow);
		document.appendChild(flowTree);
		
		return document;
		
	}
	
	Element parseNode(Document document, JSONObject jsonNode) {
		Element node = document.createElement(jsonNode.getString("node"));
		
		JSONObject attributes = jsonNode.optJSONObject("attr");
		if(Objects.nonNull(attributes)) {
			attributes.keySet().stream().forEach(k -> node.setAttribute(k, attributes.getString(k)));
		}
		
		JSONArray childrens = jsonNode.optJSONArray("children");
		if(Objects.nonNull(childrens)) {		
			IntStream.range(0, childrens.length())
						.mapToObj(childrens::getJSONObject)
						.forEach(n-> node.appendChild(parseNode(document, n)));
		}
		return node;
	}
	
	 void writeXml(File outputXml, Document document) throws IOException, TransformerException, TransformerFactoryConfigurationError {
		 if(!outputXml.exists()) {
			 	outputXml.createNewFile();
			}
			
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
	    DOMSource source = new DOMSource(document);
	    StreamResult file = new StreamResult(Files.newOutputStream(outputXml.toPath(), StandardOpenOption.TRUNCATE_EXISTING));
	    transformer.transform(source, file);
	}
	

}
