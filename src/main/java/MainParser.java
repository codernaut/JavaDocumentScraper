import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import com.google.gson.stream.JsonWriter;
import com.snowtide.PDF;
import com.snowtide.pdf.Document;
import com.snowtide.pdf.OutputTarget;
/**
 * 
 * This class will be the main parsing launching point
 * 
 * @author GulZaib
 *
 */
public class MainParser {
	
	enum Type {table,paragraph};
	
	/**
	 * 
	 * @param args
	 * 1st Argument input file path
	 * 2nd Output file path
	 * 3rd Type
	 * 4th Find string
	 * 5th lenght
	 */
	public static void main(String args[])
	{
		String inputFilePath=args[0];
		String outputFilePath=args[1];
		Type parseType=Type.table;
		if (args[2].equals("1"))
			parseType=Type.table;
		else if (args[2].equals("2"))
			parseType=Type.paragraph;
				
		int dotPosition=inputFilePath.indexOf(".");
		String extension=inputFilePath.substring(dotPosition);
		try {
		if(extension.equals("xlsx"))
			writeJsonData(outputFilePath,parseExcel(inputFilePath,parseType,args[4],Integer.parseInt(args[5])));
		else if(extension.equals("docx"))
			writeJsonData(outputFilePath,parseDocx(inputFilePath,parseType,args[4],Integer.parseInt(args[5])));
		else if(extension.equals("pdf"))
			writeJsonData(outputFilePath,parsePDF(inputFilePath,parseType,args[4],Integer.parseInt(args[5])));
		writeSampleJsonData();
		}
		catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	
	}

	private static void writeSampleJsonData() throws IOException {
		ArrayList<JsonData> sampleData=new 	ArrayList<JsonData>();
		JsonData jsonData=new JsonData(null, null, null);
		HashMap<String,String> values= new HashMap<String, String>();
		values.put("key1", "value1");
		values.put("key2", "value2");
		jsonData.setTitle("parent");
		jsonData.setKeyValues(values);
		ArrayList<JsonData> sampleData1=new 	ArrayList<JsonData>();
		JsonData jsonData1=new JsonData(null, null, null);
		HashMap<String,String> values1= new HashMap<String, String>();
		values.put("key1", "value1");
		values.put("key2", "value2");
		jsonData1.setTitle("child");
		jsonData1.setKeyValues(values1);
		sampleData1.add(jsonData1);
		jsonData.setChilderen(sampleData1);
		sampleData.add(jsonData);
		writeJsonData("d:\\test.json", sampleData);
		
		
	}

	private static ArrayList<JsonData> parsePDF(String inputFilePath, Type parseType, String args, int i) throws IOException {
		Document pdf = PDF.open(inputFilePath);
	    StringBuilder text = new StringBuilder(1024);
	    pdf.pipe(new OutputTarget(text));
	    pdf.close();
	    System.out.println(text);
		return null;
		
	}

	private static ArrayList<JsonData> parseDocx(String inputFilePath, Type parseType, String args, int i) throws IOException {
		 File file = new File(inputFilePath);
         FileInputStream fis = new FileInputStream(file.getAbsolutePath());

         XWPFDocument document = new XWPFDocument(fis);

         List<XWPFParagraph> paragraphs = document.getParagraphs();


         for (XWPFParagraph para : paragraphs) {
             System.out.println(para.getText());
         }
         fis.close();
         document.close();
		return null;
		
	}

	private static ArrayList<JsonData> parseExcel(String inputFilePath, Type parseType, String args, int i) throws IOException {
		 FileInputStream excelFile = new FileInputStream(new File(inputFilePath));
         Workbook workbook = new XSSFWorkbook(excelFile);
         Sheet datatypeSheet = workbook.getSheetAt(0);
         Iterator<Row> iterator = datatypeSheet.iterator();

         while (iterator.hasNext()) {

             Row currentRow = iterator.next();
             Iterator<Cell> cellIterator = currentRow.iterator();

             while (cellIterator.hasNext()) {

                 Cell currentCell = cellIterator.next();
                 //getCellTypeEnum shown as deprecated for version 3.15
                 //getCellTypeEnum ill be renamed to getCellType starting from version 4.0
                 if (currentCell.getCellTypeEnum() == CellType.STRING) {
                     System.out.print(currentCell.getStringCellValue() + "--");
                 } else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                     System.out.print(currentCell.getNumericCellValue() + "--");
                 }

             }
             System.out.println();
         }
		return null;
		
	}
	
	private static void writeJsonData(String outputFileName, ArrayList<JsonData> dataSets) throws IOException {
		JsonWriter  writer = new JsonWriter(new FileWriter(outputFileName));
		writer.beginObject();
		for(JsonData dataSet:dataSets) {
			 writeIndiviualDataSet(writer,dataSet);
		        
		}
		writer.endObject();
        writer.close();
	}
	
	private static void writeIndiviualDataSet(JsonWriter writer, JsonData dataSet) throws IOException {
		if(dataSet!=null&&!dataSet.getTitle().isEmpty())		        
			writer.name(dataSet.getTitle());
	        writer.beginArray();
	        writer.beginObject();
	        Iterator<String> keyIterator = dataSet.getKeyValues().keySet().iterator();
	        while(keyIterator.hasNext()) {
	        	String key=keyIterator.next();
	        	writer.name(key).value(dataSet.getKeyValues().get(key));		        
	        }
	       /** for(JsonData childerenDataSet:dataSet.getChilderen()) {
	        	//writer.beginObject();
	        	//writer.beginArray();	        	
	        	writeIndiviualDataSet(writer,childerenDataSet);	        	
	        	//writer.endArray();
	        	//writer.endObject();
	        }*/	       	
	        writer.endObject();
	        writer.endArray();
		
	}

	private static class JsonData {
		private HashMap<String,String> keyValues=new HashMap<String, String>();
		private String title;
		private ArrayList<JsonData> childeren=new ArrayList<MainParser.JsonData>();
		
		public JsonData(HashMap<String, String> keyValues, String title, ArrayList<JsonData> childeren) {
			super();
			this.keyValues = keyValues;
			this.title = title;
			this.childeren = childeren;
		}

		public HashMap<String, String> getKeyValues() {
			return keyValues;
		}

		public void setKeyValues(HashMap<String, String> keyValues) {
			this.keyValues = keyValues;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public ArrayList<JsonData> getChilderen() {
			return childeren;
		}

		public void setChilderen(ArrayList<JsonData> childeren) {
			this.childeren = childeren;
		}

		@Override
		public String toString() {
			return "JsonData [keyValues=" + keyValues + ", title=" + title + ", childeren=" + childeren + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((childeren == null) ? 0 : childeren.hashCode());
			result = prime * result + ((keyValues == null) ? 0 : keyValues.hashCode());
			result = prime * result + ((title == null) ? 0 : title.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JsonData other = (JsonData) obj;
			if (childeren == null) {
				if (other.childeren != null)
					return false;
			} else if (!childeren.equals(other.childeren))
				return false;
			if (keyValues == null) {
				if (other.keyValues != null)
					return false;
			} else if (!keyValues.equals(other.keyValues))
				return false;
			if (title == null) {
				if (other.title != null)
					return false;
			} else if (!title.equals(other.title))
				return false;
			return true;
		}
		
		
		
		
	
	}
	
}
