package DTAC.Helper;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import com.healthmarketscience.jackcess.*;

public class Dtac {

	public void repair(StyledDocument doc, JButton btnDtac) throws IOException, ZipException, BadLocationException {

		String logMessage = "";
		PrintWriter writer = new PrintWriter("dtac.log", "UTF-8");

		logMessage = "trying to get export folder\n";
		log(logMessage);

		String exportFolder = (getExportFolder());

		logMessage = "the patch is: " + exportFolder + "\n"; 	
		log(logMessage);
		
		File cruFiles = new File(exportFolder);

		logMessage = "got cru files\n"; 	
		log(logMessage);
		
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".cwu");
			}
		};

		String[] cruFilesNames = cruFiles.list(filter);

		logMessage = "got cru names\n"; 	
		log(logMessage);
		
		checkCwuFiles(exportFolder, cruFilesNames, doc, btnDtac);
		logMessage = "did check\n"; 	
		log(logMessage);
		writer.close();
	}

	private void log(String message) throws IOException {
		try {
			Files.write(Paths.get("dtac.log"), message.getBytes(), StandardOpenOption.APPEND);
		} catch (Exception e) {
			String tmp = e.getMessage();
			Files.write(Paths.get("dtac.log"), tmp.getBytes(), StandardOpenOption.APPEND);
		}
	}
	
	private static void checkCwuFiles(String exportFolder, String[] cruFilesNames, StyledDocument doc, JButton btnDtac)
			throws ZipException, IOException, BadLocationException {

		JTextPane jtp = new JTextPane();
		Style style = jtp.addStyle("Red coloured text", null);
		StyleConstants.setForeground(style, Color.green);
		StyleConstants.setAlignment(style, 1);
		doc.insertString(doc.getLength(), "Start\n", null);
		
		btnDtac.setEnabled(false);
		for (String fileName : cruFilesNames) {

			doc.insertString(doc.getLength(), "found: " + cruFilesNames.length + " files\n", null);
			String databasePatch = unzipCruFile(new File(exportFolder + "\\" + fileName));
			doc.insertString(doc.getLength(), "\n" + fileName, null);

			boolean check = checkDatabase(databasePatch,doc);
			// System.out.println(check);

			if (!check) {
				doc.insertString(doc.getLength(), "\nrepering..........", null);
				repairDatabase(exportFolder, databasePatch);
				doc.insertString(doc.getLength(), "done", null);
				doc.insertString(doc.getLength(), "          OK", style);
			} else {
				doc.insertString(doc.getLength(), "          OK", style);
			}
			FileUtils.deleteDirectory(new File(databasePatch));
		}
		doc.insertString(doc.getLength(), "\nFINISHED", null);
		Files.write(Paths.get("the-file-name.txt"), "koniec\\n".getBytes(), StandardOpenOption.APPEND);
		btnDtac.setEnabled(true);
	}

	private static void repairDatabase(String exportFolder, String databasePatch) throws ZipException, IOException {

		String newCwuFileName = zipFile(databasePatch);

		changeExtension(new File(newCwuFileName), "cwu");

		FileUtils.copyFileToDirectory(new File(newCwuFileName.replace(".zip", ".cwu")), new File(exportFolder));

	}

	public static String zipFile(String databasePatch) throws ZipException {

		ArrayList<File> fileList = new ArrayList<File>();

		File[] filesToPack = (new File(databasePatch)).listFiles();
		for (File file : filesToPack) {
			fileList.add(file);
		}

		String fileName = databasePatch.substring(databasePatch.lastIndexOf("\\"));
		String newCwuFileName = databasePatch + "\\" + fileName + ".zip";
		ZipFile zipFile = new ZipFile(newCwuFileName);
		ZipParameters zip4jZipParameters = new ZipParameters();
		zip4jZipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		zip4jZipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

		zipFile.addFiles(fileList, zip4jZipParameters);

		return newCwuFileName;
	}

	public static String unzipCruFile(File file) throws ZipException, IOException {
		ZipFile zipFile = new ZipFile(file.getCanonicalPath());
		String databasePatch = FilenameUtils.getFullPath(file.getAbsolutePath())
				+ FilenameUtils.getBaseName(file.getName());
		zipFile.extractAll(databasePatch);
		return databasePatch;
	}

	public static File changeExtension(File file, String extension) {
		String filename = file.getName();

		if (filename.contains(".")) {
			filename = filename.substring(0, filename.lastIndexOf('.'));
		}
		filename += "." + extension;

		file.renameTo(new File(file.getParentFile(), filename));
		return file;
	}

	private static String getExportFolder() throws IOException {

		String tmp = FileUtils.readFileToString(new File("conf.txt"), StandardCharsets.UTF_8);
		tmp = tmp.substring(tmp.indexOf("=") + 1).toString().replace("\\\\", "\\");

		return tmp.replace("\r\n", "");
	}

	public static boolean checkDatabase(String databasePatch, StyledDocument doc) throws IOException, BadLocationException {

		// true - baza nie zawiera bledow
		// false - baza z bledami, naprawiono

		boolean result = true;
		doc.insertString(doc.getLength(), "opening database\n", null);
		Database db = DatabaseBuilder.open(new File(databasePatch + "\\unit.mdb"));
		doc.insertString(doc.getLength(), "database opened\n", null);
		Table table = db.getTable("Projects");
		doc.insertString(doc.getLength(), "table opened\n", null);
		int i = 0;
		for (Row row : table) {
			doc.insertString(doc.getLength(), "checking row: " + i + "\n" , null);	
			if (row.containsValue("Mariusz Ziolkowski test prep")) {
				doc.insertString(doc.getLength(), "got error\n" , null);
				try {
					doc.insertString(doc.getLength(), row.toString()  + "\n" , null);
					table.deleteRow(row);	
				} catch (Exception e) {
					doc.insertString(doc.getLength(), "error: " + e.getMessage() + "\n", null);
				}
				doc.insertString(doc.getLength(), "id wiersza: " + row.getId() + "\n", null);
				
				try {
					doc.insertString(doc.getLength(), row.toString()  + "\n" , null);
					table.deleteRow(row);	
				} catch (Exception e) {
					doc.insertString(doc.getLength(), "error2: " + e.getMessage() + "\n", null);
				}
				doc.insertString(doc.getLength(), "error fixed\n" , null);
				result = false;
			}
			i++;
		}

		db.close();

		return result;
	}

}
