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
import org.apache.log4j.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import com.healthmarketscience.jackcess.*;

public class Dtac {

	static Logger log = Logger.getLogger(Dtac.class.getName());
	
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

		logMessage = "got cru dsanames\n";
		log(logMessage);

		checkCwuFiles(exportFolder, cruFilesNames, doc, btnDtac);
		logMessage = "checked\n";
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
		doc.insertString(doc.getLength(), "found: " + cruFilesNames.length + " files\n", null);
		btnDtac.setEnabled(false);
		for (String fileName : cruFilesNames) {
			
			String databasePatch = unzipCruFile(new File(exportFolder + "\\" + fileName));
			doc.insertString(doc.getLength(), "\n" + fileName, null);

			boolean check = checkDatabase(databasePatch, doc);
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

		String tmp = FileUtils.readFileToString(new File("conf.cfg"), StandardCharsets.UTF_8);
		tmp = tmp.substring(tmp.indexOf("=") + 1).toString().replace("\\\\", "\\");

		return tmp.replace("\r\n", "");
	}

	public static boolean checkDatabase(String databasePatch, StyledDocument doc)
			throws IOException, BadLocationException {

		// true - baza nie zawiera bledow
		// false - baza z bledami, naprawiono

		boolean result = true;

		Database db = DatabaseBuilder.open(new File(databasePatch + "\\unit.mdb"));

		Table table = db.getTable("Projects");

		int i = 0;
		for (Row row : table) {
			//log.info(row.get("PName"));
			//String tmp = row.get("PName").toString();
			if (!row.get("PName").toString().contains("dtac")) {
				doc.insertString(doc.getLength(), " found error", null);
				
				try {
					table.deleteRow(row);
				} catch (Exception e) {
					doc.insertString(doc.getLength(), "\n error: " + e.getMessage() + "\n", null);
				}				
				result = false;
			}
			i++;
		}

		db.close();

		return result;
	}

}
