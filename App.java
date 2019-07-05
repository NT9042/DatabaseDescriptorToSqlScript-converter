package com.convert.convertDescToSql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		String directory = System.getProperty("user.home");
		String fileName = "Documents" + File.separator + "doc_rptObjects_final_word.docx";
		String sourceAbsolutePath = directory + File.separator + fileName;
		XWPFDocument docx;
		Map<String, List<TableColumn>> tableData = new HashMap<String, List<TableColumn>>();
		try {
			docx = new XWPFDocument(new FileInputStream(sourceAbsolutePath));
			// using XWPFWordExtractor Class
			List<IBodyElement> bodyElements = docx.getBodyElements();
			for (Iterator iterator = bodyElements.iterator(); iterator.hasNext();) {
				IBodyElement iBodyElement = (IBodyElement) iterator.next();
				List<XWPFParagraph> Paragraphs = iBodyElement.getBody().getParagraphs();

				String text = "";
				String tableName = "";
				List<TableColumn> tableColumns = new ArrayList<TableColumn>();

				for (Iterator iterator2 = Paragraphs.iterator(); iterator2.hasNext();) {
					XWPFParagraph para = (XWPFParagraph) iterator2.next();
					text = para.getText();

					if (text.contains("Table:")) {
						// new table
						// clear columns
						tableName = text.split(":")[1].replace("Page", "").trim();
						//System.out.println("Table: " +tableName);
						tableColumns = new ArrayList<TableColumn>();
					} else if (text.contains("Long") || text.contains("Text") || text.contains("Attachment")
							|| text.contains("Yes")) {
						// prev table
						// keep on adding cols
						TableColumn col = new TableColumn();
						String[] line = text.split("\\s+");
						col.setColumnName(line[1].trim());
						//System.out.println("Col: " +col.getColumnName());
						col.setColumnType(getDataType(line[2]));
						tableColumns.add(col);
					}
					
					tableData.put(tableName, tableColumns);

				}

				

			}

			// writing sqls
			for (Entry<String, List<TableColumn>> tableEntry : tableData.entrySet()) {
				StringBuilder str = new StringBuilder();
				
				str.append("create table ").append(tableEntry.getKey()).append("(");
				
				for (Iterator iterator2 = tableEntry.getValue().iterator(); iterator2.hasNext();) {
					TableColumn tableColumn = (TableColumn) iterator2.next();
					str.append(tableColumn.getColumnName())
					.append(" ")
					.append(tableColumn.getColumnType())
					.append(",");
				}
				
				
				str.append(" CONSTRAINT ").append("PK_" + tableEntry.getKey())
					.append(" PRIMARY KEY (" + tableEntry.getKey() + ")");
				str.append(")");
				
				System.out.println(str.toString());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Hello World!");
	}

	public static String getDataType(String inputType) {
		if (inputType.contains("Long")) {
			return "BIGINT";
		} else if (inputType.contains("Text")) {
			return "varchar2(20)";
		} else if (inputType.contains("Attachment ")) {
			return "blob";
		} else if (inputType.contains("Yes/No")) {
			return "Bit(1)";
		} else if (inputType.contains("Date/Time")) {
			return "DATETIME";
		} else if (inputType.contains("Currency")) {
			return "INT";
		} else if (inputType.contains("Double")) {
			return "Double";
		}
		return "";
	}
}
